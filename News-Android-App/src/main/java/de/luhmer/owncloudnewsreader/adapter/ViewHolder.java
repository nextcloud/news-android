package de.luhmer.owncloudnewsreader.adapter;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.NewsDetailFragment;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.ColorHelper;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;

public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    private final static String TAG = "RecyclerView.ViewHolder";

    private static SparseArray<Integer> downloadProgressList = new SparseArray<>();

    @Nullable
    @Bind(R.id.star_imageview)
    protected ImageView starImageView;

    @Bind(R.id.summary)
    protected TextView textViewSummary;

    @Bind(R.id.tv_item_date)
    protected TextView textViewItemDate;

    @Bind(R.id.tv_subscription)
    protected TextView textViewTitle;

    @Bind(R.id.imgViewFavIcon)
    protected ImageView imgViewFavIcon;

    @Bind(R.id.color_line_feed)
    protected View colorLineFeed;

    @Bind(R.id.btn_playPausePodcast)
    protected ImageView btnPlayPausePodcast;

    @Bind(R.id.podcastDownloadProgress)
    protected HoloCircularProgressBar pbPodcastDownloadProgress;

    @Bind(R.id.podcast_wrapper)
    View flPlayPausePodcastWrapper;

    // only in extended layout
    @Nullable @Bind(R.id.body)
    protected TextView textViewBody;

    // Only in extended with webview layout
    @Nullable @Bind(R.id.webView_body)
    protected WebView webView_body;

    private RecyclerItemClickListener clickListener;
    private RssItem rssItem;
    private boolean stayUnread = false;
    private static FavIconHandler favIconHandler = null;
    private final int LengthBody = 400;
    private ForegroundColorSpan bodyForegroundColor;
    private boolean playing;
    private int selectedListLayout;
    private int starColor;
    private int inactiveStarColor;

    public ViewHolder(View itemView, int titleLineCount) {
        super(itemView);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
        selectedListLayout = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0"));

        bodyForegroundColor = new ForegroundColorSpan(ContextCompat.getColor(itemView.getContext(), android.R.color.secondary_text_dark));

        if(favIconHandler == null)
            favIconHandler = new FavIconHandler(itemView.getContext());
        ButterKnife.bind(this, itemView);
        if(textViewSummary != null)
            textViewSummary.setLines(titleLineCount);

        int[] attribute = new int[]{ R.attr.starredColor, R.attr.unstarredColor };
        TypedArray array = starImageView.getContext().getTheme().obtainStyledAttributes(attribute);
        starColor = array.getColor(0, Color.TRANSPARENT);
        inactiveStarColor = array.getColor(1, Color.LTGRAY);
        array.recycle();

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Subscribe
    public void onEvent(PodcastDownloadService.DownloadProgressUpdate downloadProgress) {
        downloadProgressList.put((int) downloadProgress.podcast.itemId, downloadProgress.podcast.downloadProgress);
        if (rssItem.getId().equals(downloadProgress.podcast.itemId)) {
            pbPodcastDownloadProgress.setProgress(downloadProgress.podcast.downloadProgress / 100f);
        }
    }

    public void setDownloadPodcastProgressbar() {
        float progress;
        if(PodcastDownloadService.PodcastAlreadyCached(itemView.getContext(), rssItem.getEnclosureLink()))
            progress = 1f;
        else {
            if(downloadProgressList.get(rssItem.getId().intValue()) != null) {
                progress = downloadProgressList.get(rssItem.getId().intValue()) / 100f;
            } else {
                progress = 0;
            }
        }
        pbPodcastDownloadProgress.setProgress(progress);
    }

    @Override
    public void onClick(View v) {
        clickListener.onClick(this, getLayoutPosition());
    }

    public void setClickListener(RecyclerItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public boolean onLongClick(View v) {
        return clickListener.onLongClick(this, getLayoutPosition());
    }

    public void setFeedColor(int color) {
        colorLineFeed.setBackgroundColor(color);
    }

    public void setReadState(boolean isRead) {
        if(textViewSummary != null) {
            float alpha = 1f;
            if (isRead) {
                textViewSummary.setTypeface(Typeface.DEFAULT);
                alpha = 0.7f;
            } else {
                textViewSummary.setTypeface(Typeface.DEFAULT_BOLD);
            }

            ((View) textViewSummary.getParent()).setAlpha(alpha);
        }
    }

    public void setStarred(boolean isStarred) {
        int color = isStarred ? starColor : inactiveStarColor;
        int contentDescriptionId = isStarred ?
                R.string.content_desc_remove_from_favorites :
                R.string.content_desc_add_to_favorites;
        starImageView.setColorFilter(color);
        starImageView.setContentDescription(starImageView.getContext().getString(contentDescriptionId));
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        
        int imageId = playing ? R.drawable.ic_action_pause : R.drawable.ic_action_play_arrow;
        int contentDescriptionId = playing ? R.string.content_desc_pause : R.string.content_desc_play;

        String contentDescription = btnPlayPausePodcast.getContext().getString(contentDescriptionId);
        btnPlayPausePodcast.setContentDescription(contentDescription);
        btnPlayPausePodcast.setImageResource(imageId);
    }

    public boolean isPlaying() {
        return playing;
    }

    public RssItem getRssItem() {
        return rssItem;
    }

    public void setRssItem(RssItem rssItem) {
        this.rssItem = rssItem;
        String title = null;
        String favIconUrl = null;
        if(rssItem.getFeed() != null) {
            title = rssItem.getFeed().getFeedTitle();
            favIconUrl = rssItem.getFeed().getFaviconUrl();
        } else {
            Log.v(TAG, "Feed not found!!!");
        }

        setReadState(rssItem.getRead_temp());
        setStarred(rssItem.getStarred_temp());

        setFeedColor(ColorHelper.getFeedColor(itemView.getContext(), rssItem.getFeed()));

        if(textViewSummary != null) {
            try {
                //byte[] arrByteForSpanish = rssItem.getTitle().getBytes("ISO-8859-1");
                //String spanish = new String(arrByteForSpanish);//.getBytes("UTF-8");
                //textViewSummary.setText(Html.fromHtml(spanish));

                textViewSummary.setText(Html.fromHtml(rssItem.getTitle()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(textViewTitle != null) {
            textViewTitle.setText(Html.fromHtml(title));
        }

        if(textViewBody != null) {
            String body = rssItem.getBody();
            // Strip html from String
            if(selectedListLayout == 3) {
                textViewBody.setMaxLines(200);
                body = getBodyText(body, false);
            } else {
                body = getBodyText(body, true);
            }
            textViewBody.setText(Html.fromHtml(body));
        }

        if(textViewItemDate != null)
            textViewItemDate.setText(DateUtils.getRelativeTimeSpanString(rssItem.getPubDate().getTime()));

        if (imgViewFavIcon != null)
            favIconHandler.loadFavIconForFeed(favIconUrl, imgViewFavIcon);

        if(webView_body != null) {
            String htmlPage = NewsDetailFragment.getHtmlPage(itemView.getContext(),rssItem,false);
            webView_body.loadDataWithBaseURL("file:///android_asset/", htmlPage, "text/html", "UTF-8", "");
        }
    }

    public boolean shouldStayUnread() {
        return stayUnread;
    }

    public void setStayUnread(boolean shouldStayUnread) {
        this.stayUnread = shouldStayUnread;
    }

    private String getBodyText(String body, boolean limitLength)
    {
        body = body.replaceAll("<img[^>]*>", "");
        body = body.replaceAll("<video[^>]*>", "");

        SpannableString bodyStringSpannable = new SpannableString(Html.fromHtml(body));
        bodyStringSpannable.setSpan(bodyForegroundColor, 0, bodyStringSpannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        String bodyString = bodyStringSpannable.toString().trim();


        if(limitLength && bodyString.length() > LengthBody)
            bodyString = bodyString.substring(0, LengthBody) + "...";

        return bodyString;
    }
}
