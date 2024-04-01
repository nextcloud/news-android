package de.luhmer.owncloudnewsreader.adapter

import android.content.SharedPreferences
import com.bumptech.glide.RequestManager
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemTextBinding
import de.luhmer.owncloudnewsreader.helper.FavIconHandler

class RssItemFullTextViewHolder internal constructor(
    binding: SubscriptionDetailListItemTextBinding,
    faviconHandler: FavIconHandler,
    glide: RequestManager,
    sharedPreferences: SharedPreferences,
) : RssItemTextViewHolder(binding, faviconHandler, glide, sharedPreferences)
