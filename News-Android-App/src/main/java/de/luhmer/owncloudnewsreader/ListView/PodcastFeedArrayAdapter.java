package de.luhmer.owncloudnewsreader.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.events.podcast.PodcastFeedClicked;
import de.luhmer.owncloudnewsreader.model.PodcastFeedItem;

public class PodcastFeedArrayAdapter extends ArrayAdapter<PodcastFeedItem> {
    LayoutInflater inflater;
    EventBus eventBus;

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
            view = inflater.inflate(R.layout.podcast_feed_row, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        final PodcastFeedItem feedItem = getItem(position);

        holder.tvTitle.setText(feedItem.mFeed.getFeedTitle());
        holder.tvBody.setText(feedItem.mPodcastCount + " Podcasts available");

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PodcastFeedClicked podcastFeedClicked = new PodcastFeedClicked();
                podcastFeedClicked.position = position;
                eventBus.post(podcastFeedClicked);
            }
        });

        return view;
    }


    static class ViewHolder {
        @Bind(R.id.tv_title) TextView tvTitle;
        @Bind(R.id.tv_body) TextView tvBody;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
