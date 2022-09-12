package de.luhmer.owncloudnewsreader.adapter;

import static android.view.View.GONE;

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

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemThumbnailBinding;

public class RssItemThumbnailViewHolder extends RssItemViewHolder<SubscriptionDetailListItemThumbnailBinding> {
    //private final DisplayImageOptions displayImageOptionsThumbnail;

    Drawable feedIcon = VectorDrawableCompat.create(itemView.getResources(), R.drawable.feed_icon, null);

    RssItemThumbnailViewHolder(@NonNull SubscriptionDetailListItemThumbnailBinding binding, SharedPreferences sharedPreferences) {
        super(binding, sharedPreferences);

        //int width = Math.round(88f * binding.imgViewThumbnail.getContext().getResources().getDisplayMetrics().density);
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
        String mediaThumbnail = rssItem.getMediaThumbnail();
        if (mediaThumbnail != null && !mediaThumbnail.isEmpty()) {
            binding.imgViewThumbnail.setVisibility(View.VISIBLE);
            //ImageLoader.getInstance().displayImage(mediaThumbnail, binding.imgViewThumbnail, displayImageOptionsThumbnail);
            // ImageLoader.getInstance().displayImage(mediaThumbnail, binding.imgViewThumbnail, displayImageOptionsThumbnail);

            mGlide
                    .load(mediaThumbnail)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .placeholder(feedIcon)
                    .error(feedIcon)
                    .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCorners(60)))
                    .into(binding.imgViewThumbnail);

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