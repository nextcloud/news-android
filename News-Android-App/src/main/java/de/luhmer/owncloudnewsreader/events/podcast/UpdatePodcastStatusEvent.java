package de.luhmer.owncloudnewsreader.events.podcast;

import de.luhmer.owncloudnewsreader.services.podcast.PlaybackService;

public class UpdatePodcastStatusEvent {

    private long current;
    private long max;
    private String title;
    private PlaybackService.Status status;
    private PlaybackService.VideoType videoType;
    private long rssItemId;
    private float speed = -1;

    public long getRssItemId() {
        return rssItemId;
    }

    public String getTitle() {
        return title;
    }

    public PlaybackService.Status getStatus() {
        return status;
    }

    public boolean isPlaying() {
        return status == PlaybackService.Status.PLAYING;
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

    public UpdatePodcastStatusEvent(long current, long max, PlaybackService.Status status, String title, PlaybackService.VideoType videoType, long rssItemId, float speed) {
        this.current = current;
        this.max = max;
        this.status = status;
        this.title = title;
        this.videoType = videoType;
        this.rssItemId = rssItemId;
        this.speed = speed;
    }

}
