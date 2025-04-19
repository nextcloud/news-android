package de.luhmer.owncloudnewsreader.adapter

import android.content.SharedPreferences
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import de.luhmer.owncloudnewsreader.R
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm
import de.luhmer.owncloudnewsreader.database.model.RssItem
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemThumbnailBinding
import de.luhmer.owncloudnewsreader.helper.FavIconHandler

private const val RADIUS_IN_DP = 60

class RssItemThumbnailViewHolder internal constructor(
    binding: SubscriptionDetailListItemThumbnailBinding,
    faviconHandler: FavIconHandler,
    glide: RequestManager,
    sharedPreferences: SharedPreferences,
) : RssItemViewHolder<SubscriptionDetailListItemThumbnailBinding>(
        binding,
        faviconHandler,
        glide,
        sharedPreferences,
    ) {
    var feedIcon = VectorDrawableCompat.create(itemView.resources, R.drawable.feed_icon, null)

    override fun getImageViewFavIcon(): ImageView = binding.imgViewFavIcon

    override fun getStar(): ImageView = binding.starImageview

    override fun getPlayPausePodcastButton(): ImageView = binding.podcastWrapper.btnPlayPausePodcast

    override fun getColorFeed(): View? = null

    override fun getTextViewTitle(): TextView = binding.tvSubscription

    override fun getTextViewSummary(): TextView = binding.summary

    override fun getTextViewBody(): TextView = binding.body

    override fun getTextViewItemDate(): TextView = binding.tvItemDate

    override fun getPlayPausePodcastWrapper(): FrameLayout = binding.podcastWrapper.flPlayPausePodcastWrapper

    override fun getPodcastDownloadProgress(): ProgressBar = binding.podcastWrapper.podcastDownloadProgress

    @CallSuper
    override fun bind(rssItem: RssItem) {
        super.bind(rssItem)
        binding.imgViewThumbnail.colorFilter = null
        val mediaThumbnail = rssItem.mediaThumbnail
        if (!mediaThumbnail.isNullOrEmpty()) {
            binding.imgViewThumbnail.visibility = View.VISIBLE
            mGlide
                .load(mediaThumbnail)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .placeholder(feedIcon)
                .error(feedIcon)
                .transform(MultiTransformation(CenterCrop(), RoundedCorners(RADIUS_IN_DP)))
                .into(binding.imgViewThumbnail)
        } else {
            // Show Podcast Icon if no thumbnail is available but it is a podcast
            // (otherwise the podcast button will go missing)
            if (DatabaseConnectionOrm.ALLOWED_PODCASTS_TYPES.contains(rssItem.enclosureMime)) {
                binding.imgViewThumbnail.visibility = View.VISIBLE
                // imgViewThumbnail.setColorFilter(Color.parseColor("#d8d8d8"));
                binding.imgViewThumbnail.setImageDrawable(feedIcon)
            } else {
                binding.imgViewThumbnail.visibility = View.GONE
            }
        }
    }
}
