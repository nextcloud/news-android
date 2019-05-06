package de.luhmer.owncloudnewsreader.services.podcast;

import android.support.v4.media.session.PlaybackStateCompat;

import de.luhmer.owncloudnewsreader.model.MediaItem;

/**
 * Created by david on 31.01.17.
 */

public abstract class PlaybackService {

    public interface PodcastStatusListener {
        void podcastStatusUpdated();
        void podcastCompleted();
    }

    public enum VideoType { None, Video, VideoType, YouTube }

    private @PlaybackStateCompat.State int mStatus = PlaybackStateCompat.STATE_NONE;
    private PodcastStatusListener podcastStatusListener;
    private MediaItem mediaItem;

    public PlaybackService(PodcastStatusListener podcastStatusListener, MediaItem mediaItem) {
        this.podcastStatusListener = podcastStatusListener;
        this.mediaItem = mediaItem;
    }

    public abstract void destroy();
    public abstract void play();
    public abstract void pause();
    public abstract void playbackSpeedChanged(float currentPlaybackSpeed);


    public void seekTo(int position) { }
    public int getCurrentPosition() { return 0; }
    public int getTotalDuration() { return 0; }
    public VideoType getVideoType() { return VideoType.None; }

    public MediaItem getMediaItem() {
        return mediaItem;
    }

    public @PlaybackStateCompat.State int getStatus() {
        return mStatus;
    }

    protected void setStatus(@PlaybackStateCompat.State int status) {
        this.mStatus = status;
        podcastStatusListener.podcastStatusUpdated();
    }

    protected void podcastCompleted() {
        podcastStatusListener.podcastCompleted();
    }

    public boolean isMediaLoaded() {
        return     getStatus() != PlaybackStateCompat.STATE_NONE
                && getStatus() != PlaybackStateCompat.STATE_CONNECTING
                && getStatus() != PlaybackStateCompat.STATE_ERROR;
    }
}
