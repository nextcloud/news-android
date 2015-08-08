package de.luhmer.owncloudnewsreader.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

import de.luhmer.owncloudnewsreader.NewsDetailFragment;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.ColorHelper;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;

/**
 * Created by daniel on 28.06.15.
 */
public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final static String TAG = "RecyclerView.ViewHolder";

    @Optional
    @InjectView(R.id.star_imageview)
    protected View starImageView;

    @InjectView(R.id.summary)
    protected TextView textViewSummary;

    @InjectView(R.id.tv_item_date)
    protected TextView textViewItemDate;

    @InjectView(R.id.tv_subscription)
    protected TextView textViewTitle;

    @InjectView(R.id.imgViewFavIcon)
    protected ImageView imgViewFavIcon;

    @InjectView(R.id.color_line_feed)
    protected View colorLineFeed;

    @InjectView(R.id.btn_playPausePodcast)
    protected ImageView btnPlayPausePodcast;

    @InjectView(R.id.podcast_wrapper)
    View flPlayPausePodcastWrapper;

     // Only in extended with webview layout
    @Optional @InjectView(R.id.body)
    protected TextView textViewBody;

    // only in extended layout
    @Optional @InjectView(R.id.webView_body)
    protected WebView webView_body;

    private RecyclerItemClickListener clickListener;
    private RssItem rssItem;
    private boolean stayUnread = false;
    private static FavIconHandler favIconHandler = null;
    private final int LengthBody = 400;
    private ForegroundColorSpan bodyForegroundColor;

    public ViewHolder(View itemView, int titleLineCount, Activity activity) {
        super(itemView);

        bodyForegroundColor = new ForegroundColorSpan(activity.getResources().getColor(android.R.color.secondary_text_dark));

        if(favIconHandler == null)
            favIconHandler = new FavIconHandler(itemView.getContext());
        ButterKnife.inject(this, itemView);
        if(ThemeChooser.isDarkTheme(itemView.getContext())) {
            if(textViewBody != null)
                textViewBody.setTextColor(itemView.getResources().getColor(R.color.extended_listview_item_body_text_color_dark_theme));
        }
        if(textViewSummary != null) textViewSummary.setLines(titleLineCount);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        clickListener.onClick(this, getLayoutPosition());
    }

    public void setClickListener(RecyclerItemClickListener clickListener) {
        this.clickListener = clickListener;
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                ((View) textViewSummary.getParent()).setAlpha(alpha);
            }
            //itemView.invalidate();
            //textViewSummary.invalidate();
        }
    }

    public void setStarred(boolean isStarred) {
        if(isStarred)
            starImageView.setVisibility(View.VISIBLE);
        else
            starImageView.setVisibility(View.GONE);
    }

    public void setPlaying(boolean isPlaying) {
        int[] state = new int[]{ (isPlaying ? 1 : -1)  * android.R.attr.state_active };
        btnPlayPausePodcast.getDrawable().setState(state);
    }

    public RssItem getRssItem() {
        return rssItem;
    }

    public void setRssItem(RssItem rssItem) {
        this.rssItem = rssItem;
        String title = null;
        String body = rssItem.getBody();
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

        if(textViewSummary != null)
            textViewSummary.setText(Html.fromHtml(rssItem.getTitle()).toString());

        if(textViewTitle != null)
            textViewTitle.setText(title);

        if(textViewBody != null)
            // Strip html from String
            textViewBody.setText(getBodyText(body));

        if(textViewItemDate != null)
            textViewItemDate.setText(DateUtils.getRelativeTimeSpanString(rssItem.getPubDate().getTime()));

        if(imgViewFavIcon != null)
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

    private String getBodyText(String body)
    {
        body = body.replaceAll("<img[^>]*>", "");
        body = body.replaceAll("<video[^>]*>", "");

        SpannableString bodyStringSpannable = new SpannableString(Html.fromHtml(body));
        bodyStringSpannable.setSpan(bodyForegroundColor, 0, bodyStringSpannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        String bodyString = bodyStringSpannable.toString().trim();

        if(bodyString.length() > LengthBody)
            bodyString = bodyString.substring(0, LengthBody) + "...";

        return bodyString;
    }
}
