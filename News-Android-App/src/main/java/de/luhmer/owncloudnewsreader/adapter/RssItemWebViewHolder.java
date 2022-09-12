package de.luhmer.owncloudnewsreader.adapter;

import android.content.SharedPreferences;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import de.luhmer.owncloudnewsreader.async_tasks.RssItemToHtmlTask;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemWebLayoutBinding;

public class RssItemWebViewHolder extends RssItemViewHolder<SubscriptionDetailListItemWebLayoutBinding> {


    public RssItemWebViewHolder(@NonNull ViewBinding binding, SharedPreferences sharedPreferences) {
        super(binding, sharedPreferences);
    }

    @Override
    protected ImageView getImageViewFavIcon() {
        return binding.layoutThumbnail.imgViewFavIcon;
    }

    @Override
    protected ImageView getStar() {
        return binding.layoutThumbnail.starImageview;
    }

    @Override
    protected ImageView getPlayPausePodcastButton() {
        return binding.layoutThumbnail.podcastWrapper.btnPlayPausePodcast;
    }

    @Override
    protected ImageView getColorFeed() {
        return null;
    }

    @Override
    protected TextView getTextViewTitle() {
        return binding.layoutThumbnail.tvSubscription;
    }

    @Override
    protected TextView getTextViewSummary() {
        return binding.layoutThumbnail.summary;
    }

    @Override
    protected TextView getTextViewBody() {
        return binding.layoutThumbnail.body;
    }

    @Override
    protected TextView getTextViewItemDate() {
        return binding.layoutThumbnail.tvItemDate;
    }

    @Override
    protected FrameLayout getPlayPausePodcastWrapper() {
        return binding.layoutThumbnail.podcastWrapper.flPlayPausePodcastWrapper;
    }

    @Override
    protected ProgressBar getPodcastDownloadProgress() {
        return binding.layoutThumbnail.podcastWrapper.podcastDownloadProgress;
    }

    @CallSuper
    public void bind(@NonNull RssItem rssItem) {
        super.bind(rssItem);

        String htmlPage = RssItemToHtmlTask.getHtmlPage(this.mGlide, rssItem, false, mPrefs, itemView.getContext());
        binding.webViewBody.loadDataWithBaseURL("file:///android_asset/", htmlPage, "text/html", "UTF-8", "");

    }
}