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

import java.util.Collections;
import java.util.List;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemThumbnailBinding;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.SquareRoundedBitmapDisplayer;

import static android.view.View.GONE;

public class RssItemThumbnailViewHolder extends RssItemViewHolder {
    private final DisplayImageOptions displayImageOptionsThumbnail;
    SubscriptionDetailListItemThumbnailBinding binding;

    RssItemThumbnailViewHolder(@NonNull SubscriptionDetailListItemThumbnailBinding binding, SharedPreferences sharedPreferences) {
        super(binding.getRoot(), sharedPreferences);
        this.binding = binding;


        Drawable feedIcon = VectorDrawableCompat.create(itemView.getResources(), R.drawable.feed_icon, null);
        displayImageOptionsThumbnail = new DisplayImageOptions.Builder()
                .displayer(new SquareRoundedBitmapDisplayer(30))
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
        return binding.starImageview;
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

        binding.imgViewThumbnail.setColorFilter(null);
        String body = rssItem.getBody();
        List<String> images;
        if (rssItem.getMediaThumbnail() != null && !rssItem.getMediaThumbnail().isEmpty()) {
            images = Collections.singletonList(rssItem.getMediaThumbnail());
        } else {
            images = ImageHandler.getImageLinksFromText(body);
        }

        if (images.size() > 0) {
            binding.imgViewThumbnail.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(images.get(0), binding.imgViewThumbnail, displayImageOptionsThumbnail);
        } else {
            // Show Podcast Icon if no thumbnail is available but it is a podcast (otherwise the podcast button will go missing)
            if (DatabaseConnectionOrm.ALLOWED_PODCASTS_TYPES.contains(rssItem.getEnclosureMime())) {
                binding.imgViewThumbnail.setVisibility(View.VISIBLE);
                //imgViewThumbnail.setColorFilter(Color.parseColor("#d8d8d8"));
                Drawable feedIcon = VectorDrawableCompat.create(itemView.getResources(), R.drawable.feed_icon, null);
                binding.imgViewThumbnail.setImageDrawable(feedIcon);
            } else {
                binding.imgViewThumbnail.setVisibility(GONE);
            }
        }
    }
}