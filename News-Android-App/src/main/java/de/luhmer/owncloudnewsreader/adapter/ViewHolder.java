package de.luhmer.owncloudnewsreader.adapter;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
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

    @InjectView(R.id.cb_lv_item_starred)
    CheckBox cbStarred;

    @InjectView(R.id.cb_lv_item_read)
    CheckBox cbRead;

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
    ImageView btnPlayPausePodcast;

    @InjectView(R.id.fl_playPausePodcastWrapper)
    FrameLayout flPlayPausePodcastWrapper;

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

    public ViewHolder(View itemView, int titleLineCount) {
        super(itemView);
        if(favIconHandler == null)
            favIconHandler = new FavIconHandler(itemView.getContext());
        ButterKnife.inject(this, itemView);
        if(ThemeChooser.isDarkTheme(itemView.getContext())) {
            cbStarred.setButtonDrawable(itemView.getResources().getDrawable(R.drawable.checkbox_background_holo_dark));
            if(textViewBody != null)
                textViewBody.setTextColor(itemView.getResources().getColor(R.color.extended_listview_item_body_text_color_dark_theme));
        }
        if(textViewSummary != null) textViewSummary.setLines(titleLineCount);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        clickListener.onClick(this,getLayoutPosition());
    }

    public void setClickListener(RecyclerItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setFeedColor(int color) {
        colorLineFeed.setBackgroundColor(color);
    }

    public void setReadState(boolean isRead) {
        cbRead.setChecked(isRead);
        if(textViewSummary != null) {
            if (isRead)
                textViewSummary.setTypeface(Typeface.DEFAULT);
            else
                textViewSummary.setTypeface(Typeface.DEFAULT_BOLD);
            textViewSummary.invalidate();
        }
    }

    public void setStarred(boolean isStarred) {

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

        setFeedColor(ColorHelper.getFeedColor(itemView.getContext(), rssItem.getFeed()));

        if(textViewSummary != null)
            textViewSummary.setText(Html.fromHtml(rssItem.getTitle()).toString());

        if(textViewTitle != null)
            textViewTitle.setText(title);

        if(textViewBody != null)
            // Strip html from String
            textViewBody.setText(Html.fromHtml(body).toString());

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

}
