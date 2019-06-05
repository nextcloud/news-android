package de.luhmer.owncloudnewsreader.events.podcast;

import android.support.v4.media.session.PlaybackStateCompat;

import de.luhmer.owncloudnewsreader.services.podcast.PlaybackService;

public class UpdatePodcastStatusEvent {

    private long current;
    private long max;
    private String author;
    private String title;
    private @PlaybackStateCompat.State int status;
    private PlaybackService.VideoType videoType;
    private long rssItemId;
    private float speed;

    public long getRssItemId() {
        return rssItemId;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public @PlaybackStateCompat.State int getStatus() {
        return status;
    }

    public boolean isPlaying() {
        return status == PlaybackStateCompat.STATE_PLAYING;
    }

    public long getCurrent() {
        return current;
    }

    public long getMax() {
        return max;
    }

    public float getSpeed() { return speed; }

    public PlaybackService.VideoType getVideoType() { return videoType; }

    public boolean isVideoFile() { return !(videoType == PlaybackService.VideoType.None); }

    public UpdatePodcastStatusEvent(long current, long max, @PlaybackStateCompat.State int status, String author, String title, PlaybackService.VideoType videoType, long rssItemId, float speed) {
        this.current = current;
        this.max = max;
        this.status = status;
        this.author = author;
        this.title = title;
        this.videoType = videoType;
        this.rssItemId = rssItemId;
        this.speed = speed;
    }

}
