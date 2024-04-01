package de.luhmer.owncloudnewsreader.adapter

import android.content.SharedPreferences
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.RequestManager
import de.luhmer.owncloudnewsreader.database.model.RssItem
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemTextBinding
import de.luhmer.owncloudnewsreader.helper.FavIconHandler

open class RssItemTextViewHolder internal constructor(
    binding: ViewBinding,
    faviconHandler: FavIconHandler,
    glide: RequestManager,
    sharedPreferences: SharedPreferences,
) : RssItemViewHolder<SubscriptionDetailListItemTextBinding>(
        binding,
        faviconHandler,
        glide,
        sharedPreferences,
    ) {
    override fun getImageViewFavIcon(): ImageView {
        return binding.imgViewFavIcon
    }

    override fun getStar(): ImageView {
        return binding.starImageview
    }

    override fun getPlayPausePodcastButton(): ImageView {
        return binding.podcastWrapper.btnPlayPausePodcast
    }

    override fun getColorFeed(): View {
        return binding.colorLineFeed
    }

    override fun getTextViewTitle(): TextView {
        return binding.tvSubscription
    }

    override fun getTextViewSummary(): TextView {
        return binding.summary
    }

    override fun getTextViewBody(): TextView {
        return binding.body
    }

    override fun getTextViewItemDate(): TextView {
        return binding.tvItemDate
    }

    override fun getPlayPausePodcastWrapper(): FrameLayout {
        return binding.podcastWrapper.flPlayPausePodcastWrapper
    }

    override fun getPodcastDownloadProgress(): ProgressBar {
        return binding.podcastWrapper.podcastDownloadProgress
    }

    @CallSuper
    override fun bind(rssItem: RssItem) {
        super.bind(rssItem)
    }
}
