package de.luhmer.owncloudnewsreader.services.podcast;

import android.content.Context;

import de.luhmer.owncloudnewsreader.model.MediaItem;

/**
 * Created by david on 31.01.17.
 */

public class YoutubePlaybackService extends PlaybackService {

    public YoutubePlaybackService(Context context, PodcastStatusListener podcastStatusListener, MediaItem mediaItem) {
        super(context, podcastStatusListener, mediaItem);
        setStatus(Status.FAILED);
    }

    @Override
    public void destroy() { }

    @Override
    public void play() { }

    @Override
    public void pause() { }

    @Override
    public void playbackSpeedChanged(float currentPlaybackSpeed) { }

    public void seekTo(double percent) { }
    public int getCurrentDuration() {
        return 0;
    }

    public int getTotalDuration() {
        return 0;
    }

    @Override
    public VideoType getVideoType() {
        return VideoType.YouTube;
    }

    public void setYoutubePlayer(Object youTubePlayer, boolean wasRestored) { }
}
