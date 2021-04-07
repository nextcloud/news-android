package de.luhmer.owncloudnewsreader.adapter;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemCardViewBinding;

public class RssItemCardViewHolder extends RssItemViewHolder<SubscriptionDetailListItemCardViewBinding> {
    SubscriptionDetailListItemCardViewBinding binding;

    RssItemCardViewHolder(@NonNull SubscriptionDetailListItemCardViewBinding binding, SharedPreferences sharedPreferences) {
        super(binding, sharedPreferences);
    }

    @Override
    protected ImageView getImageViewFavIcon() {
        return binding.imgViewFavIcon;
    }

    @Override
    protected ImageView getStar() {
        return binding.starImageview;
    }

    @Override
    protected ImageView getPlayPausePodcastButton() {
        return binding.podcastWrapper.btnPlayPausePodcast;
    }

    @Override
    protected View getColorFeed() {
        return binding.colorLineFeed;
    }

    @Override
    protected TextView getTextViewTitle() {
        return binding.tvSubscription;
    }

    @Override
    protected TextView getTextViewSummary() {
        return binding.summary;
    }

    @Override
    protected TextView getTextViewBody() {
        return binding.body;
    }

    @Override
    protected TextView getTextViewItemDate() {
        return binding.tvItemDate;
    }

    @Override
    protected FrameLayout getPlayPausePodcastWrapper() {
        return binding.podcastWrapper.flPlayPausePodcastWrapper;
    }

    @Override
    protected ProgressBar getPodcastDownloadProgress() {
        return binding.podcastWrapper.podcastDownloadProgress;
    }

    @CallSuper
    public void bind(@NonNull RssItem rssItem) {
        super.bind(rssItem);
    }
}