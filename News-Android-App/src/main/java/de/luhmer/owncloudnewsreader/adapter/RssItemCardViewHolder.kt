package de.luhmer.owncloudnewsreader.adapter

import android.content.SharedPreferences
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.CallSuper
import com.bumptech.glide.RequestManager
import de.luhmer.owncloudnewsreader.database.model.RssItem
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemCardViewBinding
import de.luhmer.owncloudnewsreader.helper.FavIconHandler

class RssItemCardViewHolder internal constructor(
    binding: SubscriptionDetailListItemCardViewBinding,
    faviconHandler: FavIconHandler,
    glide: RequestManager,
    sharedPreferences: SharedPreferences,
) : RssItemViewHolder<SubscriptionDetailListItemCardViewBinding>(
        binding,
        faviconHandler,
        glide,
        sharedPreferences,
    ) {
    override fun getImageViewFavIcon(): ImageView = binding.imgViewFavIcon

    override fun getStar(): ImageView = binding.starImageview

    override fun getPlayPausePodcastButton(): ImageView = binding.podcastWrapper.btnPlayPausePodcast

    override fun getColorFeed(): View = binding.colorLineFeed

    override fun getTextViewTitle(): TextView = binding.tvSubscription

    override fun getTextViewSummary(): TextView = binding.summary

    override fun getTextViewBody(): TextView = binding.body

    override fun getTextViewItemDate(): TextView = binding.tvItemDate

    override fun getPlayPausePodcastWrapper(): FrameLayout = binding.podcastWrapper.flPlayPausePodcastWrapper

    override fun getPodcastDownloadProgress(): ProgressBar = binding.podcastWrapper.podcastDownloadProgress

    @CallSuper
    override fun bind(rssItem: RssItem) {
        super.bind(rssItem)
    }
}
