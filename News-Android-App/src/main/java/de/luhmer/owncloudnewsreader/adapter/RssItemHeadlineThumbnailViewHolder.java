package de.luhmer.owncloudnewsreader.adapter;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemHeadlineThumbnailBinding;
import de.luhmer.owncloudnewsreader.helper.SquareRoundedBitmapDisplayer;

public class RssItemHeadlineThumbnailViewHolder extends RssItemViewHolder<SubscriptionDetailListItemHeadlineThumbnailBinding> {
    private final DisplayImageOptions displayImageOptionsThumbnail;


    RssItemHeadlineThumbnailViewHolder(@NonNull SubscriptionDetailListItemHeadlineThumbnailBinding binding, SharedPreferences sharedPreferences) {
        super(binding, sharedPreferences);

        Drawable feedIcon = VectorDrawableCompat.create(itemView.getResources(), R.drawable.feed_icon, null);
        int widthThumbnail = Math.round(88f * binding.imgViewThumbnail.getContext().getResources().getDisplayMetrics().density);
        displayImageOptionsThumbnail = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .preProcessor(new SquareRoundedBitmapDisplayer(30, 0, widthThumbnail))
                .showImageOnLoading(feedIcon)
                .showImageForEmptyUri(feedIcon)
                .showImageOnFail(feedIcon)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();
    }

    @Override
    protected ImageView getImageViewFavIcon() {
        return binding.imgViewFavIcon;
    }

    @Override
    protected ImageView getStar() {
        return null;
    }

    @Override
    protected ImageView getPlayPausePodcastButton() {
        return binding.podcastWrapper.btnPlayPausePodcast;
    }

    @Override
    protected View getColorFeed() {
        return null;
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
        return null;
    }

    @Override
    protected TextView getTextViewItemDate() {
        return null;
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

        binding.imgViewThumbnail.setColorFilter(null);
        String mediaThumbnail = rssItem.getMediaThumbnail();
        if (mediaThumbnail != null && !mediaThumbnail.isEmpty()) {
            binding.imgViewThumbnail.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(mediaThumbnail, binding.imgViewThumbnail, displayImageOptionsThumbnail);
        } else {
            // Show Podcast Icon if no thumbnail is available but it is a podcast (otherwise the podcast button will go missing)
            if (DatabaseConnectionOrm.ALLOWED_PODCASTS_TYPES.contains(rssItem.getEnclosureMime())) {
                binding.imgViewThumbnail.setVisibility(View.VISIBLE);
                //imgViewThumbnail.setColorFilter(Color.parseColor("#d8d8d8"));
                Drawable feedIcon = VectorDrawableCompat.create(itemView.getResources(), R.drawable.feed_icon, null);
                binding.imgViewThumbnail.setImageDrawable(feedIcon);
            } else {
                binding.imgViewThumbnail.setImageDrawable(null);
                binding.imgViewThumbnail.setVisibility(View.GONE);
            }
        }
    }
}