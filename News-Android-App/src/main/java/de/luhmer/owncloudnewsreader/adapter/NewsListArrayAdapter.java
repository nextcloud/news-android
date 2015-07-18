package de.luhmer.owncloudnewsreader.adapter;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import de.greenrobot.dao.query.LazyList;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.NewsDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.async_tasks.IGetTextForTextViewAsyncTask;
import de.luhmer.owncloudnewsreader.cursor.IOnStayUnread;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.FillTextForTextViewHelper;
import de.luhmer.owncloudnewsreader.helper.FontHelper;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.IPlayPausePodcastClicked;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;

/**
 * Created by David on 18.07.2014.
 */
public class NewsListArrayAdapter extends GreenDaoListAdapter<RssItem> {

    private static final String TAG = "NewsListArrayAdapter";

    public static SparseArray<Integer> downloadProgressList = new SparseArray<Integer>();
    private long idOfCurrentlyPlayedPodcast = -1;

    private int titleLineCount;
    private DatabaseConnectionOrm dbConn;
    private final int LengthBody = 400;
    private ForegroundColorSpan bodyForegroundColor;
    private IOnStayUnread onStayUnread;
    private PostDelayHandler pDelayHandler;
    private int selectedDesign = 0;
    private FragmentActivity mActivity;
    private IPlayPausePodcastClicked playPausePodcastClicked;
    private FavIconHandler favIconHandler;

    public NewsListArrayAdapter(FragmentActivity activity, LazyList<RssItem> lazyList, IOnStayUnread onStayUnread, IPlayPausePodcastClicked playPausePodcastClicked) {
        super(activity, lazyList);

        mActivity = activity;
        this.onStayUnread = onStayUnread;
        this.playPausePodcastClicked = playPausePodcastClicked;

        favIconHandler = new FavIconHandler(mActivity);
        pDelayHandler = new PostDelayHandler(context);

        //simpleDateFormat = new SimpleDateFormat("EEE, d. MMM HH:mm:ss");
        bodyForegroundColor = new ForegroundColorSpan(context.getResources().getColor(android.R.color.secondary_text_dark));

        dbConn = new DatabaseConnectionOrm(context);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        selectedDesign = Integer.valueOf(mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0"));
        titleLineCount = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_TITLE_LINES_COUNT, "2"));

        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(UpdatePodcastStatusEvent podcast) {
        if (podcast.isPlaying()) {
            if (podcast.getRssItemId() != idOfCurrentlyPlayedPodcast) {
                idOfCurrentlyPlayedPodcast = podcast.getRssItemId();
                notifyDataSetChanged();

                Log.v(TAG, "Updating Listview - Podcast started");
            }
        } else if (idOfCurrentlyPlayedPodcast != -1) {
            idOfCurrentlyPlayedPodcast = -1;
            notifyDataSetChanged();

            Log.v(TAG, "Updating Listview - Podcast paused");
        }
    }

    @Override
    public void bindView(final View view, final Context context, final RssItem item) {


        switch (selectedDesign) {
            case 0:
                setSimpleLayout(view, item);
                break;
            case 1:
                setExtendedLayout(view, item);
                break;
            case 2:
                setExtendedLayoutWebView(view, item);
                break;
            default:
                break;
        }


        CheckBox cbStarred = (CheckBox) view.findViewById(R.id.cb_lv_item_starred);

        FontHelper fHelper = new FontHelper(context);
        fHelper.setFontForAllChildren(view, fHelper.getFont());

        if (ThemeChooser.isDarkTheme(mActivity))
            cbStarred.setBackgroundResource(R.drawable.checkbox_background_holo_dark);

        cbStarred.setOnCheckedChangeListener(null);

        cbStarred.setChecked(item.getStarred_temp());
        cbStarred.setClickable(true);

        cbStarred.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setStarred_temp(isChecked);
                dbConn.updateRssItem(item);

                if (isChecked)
                    UpdateIsReadCheckBox(buttonView, item);

                pDelayHandler.DelayTimer();
            }
        });


        LinearLayout ll_cb_starred_wrapper = (LinearLayout) view.findViewById(R.id.ll_cb_starred_wrapper);
        if (ll_cb_starred_wrapper != null) {
            ll_cb_starred_wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox cbStarred = (CheckBox) view.findViewById(R.id.cb_lv_item_starred);
                    cbStarred.setChecked(!cbStarred.isChecked());
                }
            });
        }

        CheckBox cbRead = (CheckBox) view.findViewById(R.id.cb_lv_item_read);
        cbRead.setTag(item.getId());
        cbRead.setOnCheckedChangeListener(null);

        Boolean isRead = item.getRead_temp();
        //Log.d("ISREAD", "" + isChecked + " - Cursor: " + cursor.getString(0));
        cbRead.setChecked(isRead);
        if (!isRead) {
            TextView textView = (TextView) view.findViewById(R.id.summary);
            fHelper.setFontStyleForSingleView(textView, fHelper.getFontUnreadStyle());
        }


        cbRead.setClickable(true);
        cbRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ChangeReadStateOfItem((CheckBox) buttonView, view, isChecked, mActivity);
            }
        });


        if(item.getFeed() != null) {
            String colorString = item.getFeed().getAvgColour();
            View viewColor = view.findViewById(R.id.color_line_feed);
            if (colorString != null)
                viewColor.setBackgroundColor(Integer.parseInt(colorString));
            else
                Log.v(TAG, "NO COLOR SET! - " + item.getFeed().getFeedTitle());
        } else {
            //This this only happen while sync is running
            Log.v(TAG, "No feed found");
        }
    }

    public void ChangeReadStateOfItem(CheckBox checkBox, View parentView, boolean isChecked, Context context) {
        Long rssItemId = (Long) checkBox.getTag();
        RssItem rssItem = dbConn.getRssItemById(rssItemId);
        rssItem.setRead_temp(isChecked);
        dbConn.updateRssItem(rssItem);

        UpdateListCursor(mActivity);

        pDelayHandler.DelayTimer();

        TextView textView = (TextView) parentView.findViewById(R.id.summary);
        if (textView != null && parentView.getTop() >= 0) {
            FontHelper fHelper = new FontHelper(context);
            if (isChecked)
                fHelper.setFontStyleForSingleView(textView, fHelper.getFont());
            else {
                fHelper.setFontStyleForSingleView(textView, fHelper.getFontUnreadStyle());
                onStayUnread.stayUnread((Long) checkBox.getTag());
            }

            textView.invalidate();
        }
    }

    static class SimpleLayout {
        @InjectView(R.id.divider)
        View viewDivider;
        @InjectView(R.id.summary)
        TextView textViewSummary;
        @InjectView(R.id.tv_item_date)
        TextView textViewItemDate;
        @InjectView(R.id.tv_subscription)
        TextView textViewTitle;
        @Optional
        @InjectView(R.id.btn_playPausePodcast)
        ImageView btnPlayPausePodcast;
        @Optional
        @InjectView(R.id.fl_playPausePodcastWrapper)
        FrameLayout flPlayPausePodcastWrapper;
        @Optional
        @InjectView(R.id.imgViewFavIcon)
        ImageView imgViewFavIcon;

        SimpleLayout(View view) {
            ButterKnife.inject(this, view);
        }
    }

    static class ExtendedLayout extends SimpleLayout {
        @InjectView(R.id.body)
        TextView textViewItemBody;

        ExtendedLayout(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }


    public void setSimpleLayout(View view, final RssItem rssItem) {
        SimpleLayout simpleLayout = new SimpleLayout(view);

        simpleLayout.textViewSummary.setText(Html.fromHtml(rssItem.getTitle()).toString());
        simpleLayout.textViewSummary.setLines(titleLineCount);

        long pubDate = rssItem.getPubDate().getTime();
        String dateString = (String) DateUtils.getRelativeTimeSpanString(pubDate);
        simpleLayout.textViewItemDate.setText(dateString);

        if(rssItem.getFeed() != null) {
            simpleLayout.textViewTitle.setText(rssItem.getFeed().getFeedTitle());
            favIconHandler.loadFavIconForFeed(rssItem.getFeed().getFaviconUrl(), simpleLayout.imgViewFavIcon);
        } else {
            //This this only happen while sync is running
            Log.v(TAG, "Feed not found!!!");
            showNotifyMessage("Problem detected", "Inconsistency in database detected. Please clear the cache and try to sync again.");
        }

        if (!ThemeChooser.isDarkTheme(mActivity)) {
            simpleLayout.viewDivider.setBackgroundColor(mActivity.getResources().getColor(R.color.divider_row_color_light_theme));
        }




        //Podcast stuff
        if (DatabaseConnectionOrm.ALLOWED_PODCASTS_TYPES.contains(rssItem.getEnclosureMime())) {
            final boolean isPlaying = idOfCurrentlyPlayedPodcast == rssItem.getId();
            int drawableResource;
            //Enable podcast buttons in view
            simpleLayout.flPlayPausePodcastWrapper.setVisibility(View.VISIBLE);

            if (ThemeChooser.isDarkTheme(mActivity)) {
                drawableResource = isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
            } else {
                drawableResource = isPlaying ? R.drawable.ic_action_pause : R.drawable.ic_action_play_arrow;
            }
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) simpleLayout.btnPlayPausePodcast.getLayoutParams();
            params.setMargins(DpToPx(mActivity, isPlaying ? 0 : 2), 0, 0, 0);
            simpleLayout.btnPlayPausePodcast.setLayoutParams(params);
            simpleLayout.btnPlayPausePodcast.setBackgroundResource(drawableResource);


            simpleLayout.flPlayPausePodcastWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPlaying) {
                        playPausePodcastClicked.pausePodcast();
                    } else {
                        playPausePodcastClicked.openPodcast(rssItem);
                    }
                }
            });

            setDownloadPodcastProgressbar(view, rssItem);
        } else {
            simpleLayout.flPlayPausePodcastWrapper.setVisibility(View.GONE);
        }
    }

    private void showNotifyMessage(String title, String text) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mActivity)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(text));

        // Creates an Intent for the Activity
        Intent notifyIntent =  new Intent(mActivity, SettingsActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        mActivity,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Puts the PendingIntent into the notification builder
        mBuilder.setContentIntent(notifyPendingIntent);


        NotificationManager mNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(45613, mBuilder.build());
    }

    public static int DpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics());
    }

    public void setDownloadPodcastProgressbar(View view, RssItem rssItem) {
        HoloCircularProgressBar pbPodcastDownloadProgress = (HoloCircularProgressBar) view.findViewById(R.id.podcastDownloadProgress);

        if(!ThemeChooser.isDarkTheme(mActivity)) {
            pbPodcastDownloadProgress.setProgressBackgroundColor(context.getResources().getColor(R.color.slide_up_panel_header_background_color));
        }

        if(PodcastAlreadyCached(context, rssItem.getEnclosureLink()))
            pbPodcastDownloadProgress.setProgress(1);
        else {
            if(downloadProgressList.get(rssItem.getId().intValue()) != null) {
                float progressInPercent = downloadProgressList.get(rssItem.getId().intValue()) / 100f;
                pbPodcastDownloadProgress.setProgress(progressInPercent);
            } else {
                pbPodcastDownloadProgress.setProgress(0);
            }
        }
    }

    public static boolean PodcastAlreadyCached(Context context, String podcastUrl) {
        File file = new File(PodcastDownloadService.getUrlToPodcastFile(context, podcastUrl, false));
        return file.exists();
    }


    public void setExtendedLayout(View view, RssItem rssItem)
    {
        ExtendedLayout extendedLayout = new ExtendedLayout(view);
        setSimpleLayout(view, rssItem);

        extendedLayout.textViewItemBody.setVisibility(View.INVISIBLE);

        IGetTextForTextViewAsyncTask iGetter = new DescriptionTextGetter(rssItem.getId());
        FillTextForTextViewHelper.FillTextForTextView(extendedLayout.textViewItemBody, iGetter, true);//TODO is this really needed any longer? direct insert text is also possible

        if(!ThemeChooser.isDarkTheme(mActivity)) {
            extendedLayout.textViewItemBody.setTextColor(mActivity.getResources().getColor(R.color.extended_listview_item_body_text_color_light_theme));
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setExtendedLayoutWebView(View view, RssItem rssItem)
    {
        WebView webViewContent = (WebView) view.findViewById(R.id.webView_body);
        webViewContent.setClickable(false);
        webViewContent.setFocusable(false);

        String htmlPage = NewsDetailFragment.getHtmlPage(mActivity, rssItem);

        NewsDetailFragment.SetSoftwareRenderModeForWebView(htmlPage, webViewContent);

        webViewContent.loadDataWithBaseURL("", htmlPage, "text/html", "UTF-8", "");
    }



    private void UpdateIsReadCheckBox(View view, RssItem item)
    {
        LinearLayout lLayout = (LinearLayout) view.getParent();
        Boolean read = item.getRead_temp();
        CheckBox cbRead = (CheckBox) lLayout.findViewById(R.id.cb_lv_item_read);
        if(cbRead == null) {//In the default layout the star checkbox is nested two times.
            lLayout = (LinearLayout) lLayout.getParent();
            cbRead = (CheckBox) lLayout.findViewById(R.id.cb_lv_item_read);
        }
        cbRead.setChecked(read);
    }

    public static void ChangeCheckBoxState(CheckBox cb, boolean state)
    {
        if(cb != null && cb.isChecked() != state)
            cb.setChecked(state);
    }

    public static void UpdateListCursor(Context context)
    {
        FragmentActivity sfa = (FragmentActivity) context;

        if(sfa instanceof NewsReaderListActivity && ((NewsReaderListActivity) sfa).isSlidingPaneOpen())
            ((NewsReaderListActivity) sfa).reloadCountNumbersOfSlidingPaneAdapter();
    }


    private String getBodyText(String body)
    {
        body = body.replaceAll("<img[^>]*>", "");
        body = body.replaceAll("<video[^>]*>", "");

        SpannableString bodyStringSpannable = new SpannableString(Html.fromHtml(body));
        bodyStringSpannable.setSpan(bodyForegroundColor, 0, bodyStringSpannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        String bodyString = bodyStringSpannable.toString().trim();

        if(bodyString.length() > LengthBody)
            bodyString = bodyString.substring(0, LengthBody);

        return bodyString;
    }


    @Override
    public View newView(Context context, RssItem item, ViewGroup parent) {
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = null;

        switch (selectedDesign) {
            case 0:
                retView = inflater.inflate(R.layout.subscription_detail_list_item_simple, parent, false);
                break;
            case 1:
                retView = inflater.inflate(R.layout.subscription_detail_list_item_extended, parent, false);
                break;
            case 2:
                retView = inflater.inflate(R.layout.subscription_detail_list_item_extended_webview, parent, false);
                break;
        }


        if(retView != null)
            retView.setTag(item.getId());

        return retView;
    }



    class DescriptionTextGetter implements IGetTextForTextViewAsyncTask {

        private Long idItemDb;

        public DescriptionTextGetter(Long idItemDb) {
            this.idItemDb = idItemDb;
        }

        @Override
        public String getText() {

            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(mActivity);

            RssItem rssItem = dbConn.getRssItemById(idItemDb);

            String result = "Rss item not found.";
            if(rssItem != null) {
                String body = rssItem.getBody();
                result = getBodyText(body);
            }

            return  result;
        }
    }


    public static void FadeInTextView(final TextView textView)
    {
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(300);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                textView.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        textView.startAnimation(fadeOut);
    }
}
