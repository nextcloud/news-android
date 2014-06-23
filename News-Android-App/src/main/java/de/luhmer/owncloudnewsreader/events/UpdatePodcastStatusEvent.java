package de.luhmer.owncloudnewsreader.events;

/**
 * Created by David on 20.06.2014.
 */
public class UpdatePodcastStatusEvent {

    private long current;
    private long max;
    private String title;
    private boolean playing;

    public String getTitle() {
        return title;
    }

    public boolean isPlaying() {
        return playing;
    }

    public long getCurrent() {
        return current;
    }

    public long getMax() {
        return max;
    }

    public UpdatePodcastStatusEvent(long current, long max, boolean playing, String title) {
        this.current = current;
        this.max = max;
        this.playing = playing;
        this.title = title;
    }

}
