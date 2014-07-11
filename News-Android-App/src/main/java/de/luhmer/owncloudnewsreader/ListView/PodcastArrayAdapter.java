package de.luhmer.owncloudnewsreader.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.events.podcast.AudioPodcastClicked;
import de.luhmer.owncloudnewsreader.events.podcast.StartDownloadPodcast;
import de.luhmer.owncloudnewsreader.helper.FileUtils;
import de.luhmer.owncloudnewsreader.helper.JavaYoutubeDownloader;
import de.luhmer.owncloudnewsreader.model.PodcastItem;

/**
 * Created by David on 21.06.2014.
 */
public class PodcastArrayAdapter extends ArrayAdapter<PodcastItem> {
    LayoutInflater inflater;
    EventBus eventBus;

    public PodcastArrayAdapter(Context context, PodcastItem[] values) {
        super(context, R.layout.podcast_row, values);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        eventBus = EventBus.getDefault();
        //eventBus.register(this);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.podcast_row, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        final PodcastItem podcastItem = getItem(position);

        holder.tvTitle.setText(podcastItem.title);
        holder.tvBody.setText(podcastItem.mimeType);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPodcast(position);
            }
        });


        holder.flDownloadPodcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.flDownloadPodcast.setVisibility(View.GONE);

                Toast.makeText(getContext(), "Starting download.. Please wait", Toast.LENGTH_SHORT).show();

                eventBus.post(new StartDownloadPodcast() {{ podcast = podcastItem; }});
            }
        });

        holder.flPlayPodcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPodcast(position);
            }
        });

        holder.flDeletePodcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FileUtils.DeletePodcastFile(getContext(), podcastItem.link)) {
                    podcastItem.offlineCached = false;
                    podcastItem.downloadProgress = PodcastItem.DOWNLOAD_NOT_STARTED;
                    notifyDataSetChanged();
                }
            }
        });


        holder.pbDownloadPodcast.setProgress(podcastItem.downloadProgress);
        if(podcastItem.downloadProgress >= 0) {
            holder.tvDownloadPodcastProgress.setVisibility(View.VISIBLE);
            holder.pbDownloadPodcast.setVisibility(View.VISIBLE);
            holder.tvDownloadPodcastProgress.setText(podcastItem.downloadProgress + "%");
        }
        else {
            holder.tvDownloadPodcastProgress.setVisibility(View.GONE);
            holder.pbDownloadPodcast.setVisibility(View.GONE);
        }


        if(podcastItem.link.contains(JavaYoutubeDownloader.host)) {
            if(podcastItem.downloadProgress == PodcastItem.DOWNLOAD_NOT_STARTED) {
                holder.flPlayPodcast.setVisibility(View.GONE);//Youtube Videos can't be streamed
                holder.flDownloadPodcast.setVisibility(View.VISIBLE);
            } else {
                holder.flPlayPodcast.setVisibility(View.VISIBLE);
                holder.flDownloadPodcast.setVisibility(View.GONE);
            }
        } else if(podcastItem.downloadProgress == PodcastItem.DOWNLOAD_NOT_STARTED) {
            holder.flDownloadPodcast.setVisibility(View.VISIBLE);
        } else {
            holder.flDownloadPodcast.setVisibility(View.GONE);
        }

        holder.flDeletePodcast.setVisibility((podcastItem.downloadProgress == PodcastItem.DOWNLOAD_COMPLETED) ? View.VISIBLE : View.GONE );

        /*
        File podcastFile = new File(PodcastDownloadService.getUrlToPodcastFile(getContext(), podcastItem.link, true));
        File podcastFileCache = new File(PodcastDownloadService.getUrlToPodcastFile(getContext(), podcastItem.link, true) + ".download");
        if(podcastFile.exists()) {
            holder.flDownloadPodcast.setVisibility(View.GONE);
        }
        else if(podcastFileCache.exists()) {
            holder.flDownloadPodcast.setVisibility(View.GONE);
        }
        else
            holder.flDownloadPodcast.setVisibility(View.VISIBLE);
        */

        return view;
    }


    private void playPodcast(int position) {
        AudioPodcastClicked audioPodcastClicked = new AudioPodcastClicked();
        audioPodcastClicked.position = position;
        eventBus.post(audioPodcastClicked);
    }



    static class ViewHolder {
        @InjectView(R.id.tv_title) TextView tvTitle;
        @InjectView(R.id.tv_body) TextView tvBody;
        @InjectView(R.id.fl_downloadPodcastWrapper) FrameLayout flDownloadPodcast;
        @InjectView(R.id.fl_PlayPodcastWrapper) FrameLayout flPlayPodcast;
        @InjectView(R.id.fl_deletePodcastWrapper) FrameLayout flDeletePodcast;
        @InjectView(R.id.pbDownloadPodcast) ProgressBar pbDownloadPodcast;
        @InjectView(R.id.tvDownloadPodcastProgress) TextView tvDownloadPodcastProgress;



        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
