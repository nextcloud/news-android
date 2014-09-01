package de.luhmer.owncloudnewsreader.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoCheckBox;
import com.devspark.robototextview.widget.RobotoTextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import de.greenrobot.dao.query.LazyList;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.NewsDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.PodcastFragment;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.async_tasks.IGetTextForTextViewAsyncTask;
import de.luhmer.owncloudnewsreader.cursor.IOnStayUnread;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.events.podcast.OpenPodcastEvent;
import de.luhmer.owncloudnewsreader.helper.FillTextForTextViewHelper;
import de.luhmer.owncloudnewsreader.helper.FontHelper;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;

/**
 * Created by David on 18.07.2014.
 */
public class NewsListArrayAdapter extends GreenDaoListAdapter<RssItem> {

    private static final String TAG = "NewsListArrayAdapter";

    DatabaseConnectionOrm dbConn;
    IReader _Reader;
    final int LengthBody = 400;
    ForegroundColorSpan bodyForegroundColor;
    IOnStayUnread onStayUnread;
    PostDelayHandler pDelayHandler;
    int selectedDesign = 0;
    FragmentActivity mActivity;

    public NewsListArrayAdapter(FragmentActivity activity, LazyList<RssItem> lazyList, IOnStayUnread onStayUnread) {
        super(activity, lazyList);

        mActivity = activity;
        this.onStayUnread = onStayUnread;

        pDelayHandler = new PostDelayHandler(context);

        //simpleDateFormat = new SimpleDateFormat("EEE, d. MMM HH:mm:ss");
        bodyForegroundColor = new ForegroundColorSpan(context.getResources().getColor(android.R.color.secondary_text_dark));

        _Reader = new OwnCloud_Reader();
        dbConn = new DatabaseConnectionOrm(context);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        selectedDesign = Integer.valueOf(mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0"));
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

        RobotoCheckBox cbStarred = (RobotoCheckBox) view.findViewById(R.id.cb_lv_item_starred);

        FontHelper fHelper = new FontHelper(context);
        fHelper.setFontForAllChildren(view, fHelper.getFont());

        if(ThemeChooser.isDarkTheme(mActivity))
            cbStarred.setBackgroundResource(R.drawable.checkbox_background_holo_dark);

        cbStarred.setOnCheckedChangeListener(null);

        cbStarred.setChecked(item.getStarred_temp());
        cbStarred.setClickable(true);

        cbStarred.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setStarred_temp(isChecked);
                dbConn.updateRssItem(item);

                if(isChecked)
                    UpdateIsReadCheckBox(buttonView, item);

                pDelayHandler.DelayTimer();
            }
        });


        LinearLayout ll_cb_starred_wrapper = (LinearLayout) view.findViewById(R.id.ll_cb_starred_wrapper);
        if(ll_cb_starred_wrapper != null) {
            ll_cb_starred_wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RobotoCheckBox cbStarred = (RobotoCheckBox) view.findViewById(R.id.cb_lv_item_starred);
                    cbStarred.setChecked(!cbStarred.isChecked());
                }
            });
        }

        RobotoCheckBox cbRead = (RobotoCheckBox) view.findViewById(R.id.cb_lv_item_read);
        cbRead.setTag(item.getId());
        cbRead.setOnCheckedChangeListener(null);

        Boolean isRead = item.getRead_temp();
        //Log.d("ISREAD", "" + isChecked + " - Cursor: " + cursor.getString(0));
        cbRead.setChecked(isRead);
        if(!isRead) {
            RobotoTextView textView = (RobotoTextView) view.findViewById(R.id.summary);
            fHelper.setFontStyleForSingleView(textView, fHelper.getFontUnreadStyle());
        }



        cbRead.setClickable(true);
        cbRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ChangeReadStateOfItem((RobotoCheckBox) buttonView, view, isChecked, mActivity);
            }
        });


        String colorString = item.getFeed().getAvgColour();
        View viewColor = view.findViewById(R.id.color_line_feed);
        if(colorString != null)
            viewColor.setBackgroundColor(Integer.parseInt(colorString));
        else
            Log.v(TAG, "NO COLOR SET! - " + item.getFeed().getFeedTitle());
    }

    public void ChangeReadStateOfItem(RobotoCheckBox checkBox, View parentView, boolean isChecked, Context context) {

        RssItem rssItem = dbConn.getRssItemById((Long)checkBox.getTag());
        rssItem.setRead_temp(isChecked);
        dbConn.updateRssItem(rssItem);

        UpdateListCursor(mActivity);

        pDelayHandler.DelayTimer();

        RobotoTextView textView = (RobotoTextView) parentView.findViewById(R.id.summary);
        if(textView != null && parentView.getTop() >= 0)
        {
            FontHelper fHelper = new FontHelper(context);
            if(isChecked)
                fHelper.setFontStyleForSingleView(textView, fHelper.getFont());
            else {
                fHelper.setFontStyleForSingleView(textView, fHelper.getFontUnreadStyle());
                onStayUnread.stayUnread(checkBox);
            }

            textView.invalidate();
        }
    }



    static class SimpleLayout {
        @InjectView(R.id.divider) View viewDivider;
        @InjectView(R.id.summary) TextView textViewSummary;
        @InjectView(R.id.tv_item_date) TextView textViewItemDate;
        @InjectView(R.id.tv_subscription) TextView textViewTitle;
        @Optional @InjectView(R.id.btn_playPodcast) ImageView btnPlayPodcast;
        @Optional @InjectView(R.id.fl_playPodcastWrapper) FrameLayout flPlayPodcastWrapper;

        SimpleLayout(View view) {
            ButterKnife.inject(this, view);
        }
    }

    static class ExtendedLayout extends SimpleLayout {
        @InjectView(R.id.body) TextView textViewItemBody;

        ExtendedLayout(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }



    public void setSimpleLayout(View view, final RssItem rssItem)
    {
        SimpleLayout simpleLayout = new SimpleLayout(view);

        simpleLayout.textViewSummary.setText(Html.fromHtml(rssItem.getTitle()).toString());

        long pubDate = rssItem.getPubDate().getTime();
        String dateString = (String) DateUtils.getRelativeTimeSpanString(pubDate);
        simpleLayout.textViewItemDate.setText(dateString);

        simpleLayout.textViewTitle.setText(rssItem.getFeed().getFeedTitle());

        if(!ThemeChooser.isDarkTheme(mActivity)) {
            simpleLayout.viewDivider.setBackgroundColor(mActivity.getResources().getColor(R.color.divider_row_color_light_theme));
        }

        if(DatabaseConnectionOrm.ALLOWED_PODCASTS_TYPES.contains(rssItem.getEnclosureMime())) {
            simpleLayout.btnPlayPodcast.setVisibility(View.VISIBLE);
            if(ThemeChooser.isDarkTheme(mActivity)) {
                simpleLayout.btnPlayPodcast.setBackgroundResource(android.R.drawable.ic_media_play);
            }

            simpleLayout.flPlayPodcastWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PodcastFragment podcastFragment = (PodcastFragment) mActivity.getSupportFragmentManager().findFragmentById(R.id.podcast_frame);
                    podcastFragment.OpenPodcast(context, rssItem);
                }
            });

        } else {
            simpleLayout.btnPlayPodcast.setVisibility(View.GONE);
        }
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
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        //	webViewContent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        webViewContent.loadDataWithBaseURL("", NewsDetailFragment.getHtmlPage(mActivity, rssItem), "text/html", "UTF-8", "");
    }



    private void UpdateIsReadCheckBox(View view, RssItem item)
    {
        LinearLayout lLayout = (LinearLayout) view.getParent();
        Boolean read = item.getRead_temp();
        RobotoCheckBox cbRead = (RobotoCheckBox) lLayout.findViewById(R.id.cb_lv_item_read);
        if(cbRead == null) {//In the default layout the star checkbox is nested two times.
            lLayout = (LinearLayout) lLayout.getParent();
            cbRead = (RobotoCheckBox) lLayout.findViewById(R.id.cb_lv_item_read);
        }
        cbRead.setChecked(read);
    }

    public static void ChangeCheckBoxState(RobotoCheckBox cb, boolean state, Context context)
    {
        if(cb != null && cb.isChecked() != state)
            cb.setChecked(state);
    }

    public static void UpdateListCursor(Context context)
    {
        FragmentActivity sfa = (FragmentActivity) context;

        if(sfa instanceof NewsReaderListActivity && ((NewsReaderListActivity) sfa).isSlidingPaneOpen())
            ((NewsReaderListActivity) sfa).updateAdapter();
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

            String body = rssItem.getBody();
            String result = getBodyText(body);

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
