package de.luhmer.owncloudnewsreader.adapter

import android.content.SharedPreferences
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.RequestManager
import de.luhmer.owncloudnewsreader.async_tasks.RssItemToHtmlTask
import de.luhmer.owncloudnewsreader.database.model.RssItem
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemWebLayoutBinding
import de.luhmer.owncloudnewsreader.helper.FavIconHandler

class RssItemWebViewHolder(
    binding: ViewBinding,
    faviconHandler: FavIconHandler,
    glide: RequestManager,
    sharedPreferences: SharedPreferences,
) : RssItemViewHolder<SubscriptionDetailListItemWebLayoutBinding>(
        binding,
        faviconHandler,
        glide,
        sharedPreferences,
    ) {
    override fun getImageViewFavIcon(): ImageView {
        return binding.layoutThumbnail.imgViewFavIcon
    }

    override fun getStar(): ImageView {
        return binding.layoutThumbnail.starImageview
    }

    override fun getPlayPausePodcastButton(): ImageView {
        return binding.layoutThumbnail.podcastWrapper.btnPlayPausePodcast
    }

    override fun getColorFeed(): ImageView? {
        return null
    }

    override fun getTextViewTitle(): TextView {
        return binding.layoutThumbnail.tvSubscription
    }

    override fun getTextViewSummary(): TextView {
        return binding.layoutThumbnail.summary
    }

    override fun getTextViewBody(): TextView {
        return binding.layoutThumbnail.body
    }

    override fun getTextViewItemDate(): TextView {
        return binding.layoutThumbnail.tvItemDate
    }

    override fun getPlayPausePodcastWrapper(): FrameLayout {
        return binding.layoutThumbnail.podcastWrapper.flPlayPausePodcastWrapper
    }

    override fun getPodcastDownloadProgress(): ProgressBar {
        return binding.layoutThumbnail.podcastWrapper.podcastDownloadProgress
    }

    @CallSuper
    override fun bind(rssItem: RssItem) {
        super.bind(rssItem)
        val htmlPage: String = RssItemToHtmlTask.getHtmlPage(mGlide, rssItem, false, mPrefs, itemView.context)
        binding.webViewBody.loadDataWithBaseURL("file:///android_asset/", htmlPage, "text/html", "UTF-8", "")
    }
}
