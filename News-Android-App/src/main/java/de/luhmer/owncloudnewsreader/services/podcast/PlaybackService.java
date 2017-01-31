package de.luhmer.owncloudnewsreader.services.podcast;

import android.content.Context;

import de.luhmer.owncloudnewsreader.model.MediaItem;

/**
 * Created by david on 31.01.17.
 */

public abstract class PlaybackService {

    public interface PodcastStatusListener {
        void podcastStatusUpdated();
        void podcastCompleted();
    }

    public enum Status { NOT_INITIALIZED, FAILED, PREPARING, PLAYING, PAUSED, STOPPED };
    public enum VideoType { None, Video, VideoType, YouTube }

    private Status mStatus = Status.NOT_INITIALIZED;
    private PodcastStatusListener podcastStatusListener;
    private MediaItem mediaItem;

    public PlaybackService(Context context, PodcastStatusListener podcastStatusListener, MediaItem mediaItem) {
        this.podcastStatusListener = podcastStatusListener;
        this.mediaItem = mediaItem;
    }

    public abstract void destroy();
    public abstract void play();
    public abstract void pause();


    public void seekTo(double percent) { }
    public int getCurrentDuration() { return 0; }
    public int getTotalDuration() { return 0; }
    public VideoType getVideoType() { return VideoType.None; }

    public MediaItem getMediaItem() {
        return mediaItem;
    }

    public Status getStatus() {
        return mStatus;
    }

    protected void setStatus(Status status) {
        this.mStatus = status;
        podcastStatusListener.podcastStatusUpdated();
    }

    protected void podcastCompleted() {
        podcastStatusListener.podcastCompleted();
    }

    public boolean isMediaLoaded() {
        return getStatus() != Status.NOT_INITIALIZED
                && getStatus() != Status.PREPARING
                && getStatus() != Status.FAILED;
    }

}
