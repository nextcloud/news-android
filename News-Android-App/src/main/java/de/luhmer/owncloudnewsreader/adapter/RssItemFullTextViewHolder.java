package de.luhmer.owncloudnewsreader.adapter;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemTextBinding;

public class RssItemFullTextViewHolder extends RssItemTextViewHolder {
    RssItemFullTextViewHolder(@NonNull SubscriptionDetailListItemTextBinding binding, SharedPreferences sharedPreferences) {
        super(binding, sharedPreferences);
    }
}