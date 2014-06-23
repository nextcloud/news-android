package de.luhmer.owncloudnewsreader.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.events.AudioPodcastClicked;
import de.luhmer.owncloudnewsreader.model.AudioPodcastItem;

/**
 * Created by David on 21.06.2014.
 */
public class PodcastArrayAdapter extends ArrayAdapter<AudioPodcastItem> {
    LayoutInflater inflater;
    EventBus eventBus;

    public PodcastArrayAdapter(Context context, AudioPodcastItem[] values) {
        super(context, R.layout.podcast_audio_row, values);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        eventBus = EventBus.getDefault();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.podcast_audio_row, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        AudioPodcastItem podcastItem = getItem(position);

        holder.tvTitle.setText(podcastItem.title);
        holder.tvBody.setText(podcastItem.mimeType);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPodcastClicked audioPodcastClicked = new AudioPodcastClicked();
                audioPodcastClicked.position = position;
                eventBus.post(audioPodcastClicked);
            }
        });

        return view;
    }


    static class ViewHolder {
        @InjectView(R.id.tv_title) TextView tvTitle;
        @InjectView(R.id.tv_body) TextView tvBody;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
