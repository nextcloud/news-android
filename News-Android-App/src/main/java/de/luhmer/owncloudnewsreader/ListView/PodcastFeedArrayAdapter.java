package de.luhmer.owncloudnewsreader.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.databinding.PodcastFeedRowBinding;
import de.luhmer.owncloudnewsreader.events.podcast.PodcastFeedClicked;
import de.luhmer.owncloudnewsreader.model.PodcastFeedItem;

public class PodcastFeedArrayAdapter extends ArrayAdapter<PodcastFeedItem> {

    private final LayoutInflater inflater;
    private final EventBus eventBus;

    public PodcastFeedArrayAdapter(Context context, PodcastFeedItem[] values) {
        super(context, R.layout.podcast_feed_row, values);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        eventBus = EventBus.getDefault();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            PodcastFeedRowBinding binding = PodcastFeedRowBinding.inflate(inflater, parent, false);
            view = binding.getRoot();
            holder = new ViewHolder(binding);
            binding.getRoot().setTag(holder);
        }

        final PodcastFeedItem feedItem = getItem(position);

        holder.binding.tvTitle.setText(feedItem.mFeed.getFeedTitle());
        holder.binding.tvBody.setText(feedItem.mPodcastCount + " Podcasts available");

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PodcastFeedClicked podcastFeedClicked = new PodcastFeedClicked(position);
                eventBus.post(podcastFeedClicked);
            }
        });

        return view;
    }


    static class ViewHolder {
        @NonNull final PodcastFeedRowBinding binding;

        public ViewHolder(@NonNull PodcastFeedRowBinding binding) {
            this.binding = binding;
        }
    }
}
