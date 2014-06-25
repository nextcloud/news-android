package de.luhmer.owncloudnewsreader.events.podcast;

/**
 * Created by David on 20.06.2014.
 */
public class UpdatePodcastStatusEvent {

    private long current;
    private long max;
    private String title;
    private boolean playing;
    private boolean preparingFile;

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

    public boolean isPreparingFile() {
        return preparingFile;
    }

    public UpdatePodcastStatusEvent(long current, long max, boolean playing, String title, boolean preparingFile) {
        this.current = current;
        this.max = max;
        this.playing = playing;
        this.title = title;
        this.preparingFile = preparingFile;
    }

}
