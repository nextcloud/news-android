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
    private boolean fileLoaded;
    private boolean isVideoFile;
    private long rssItemId;

    public long getRssItemId() {
        return rssItemId;
    }

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

    public boolean isFileLoaded() {
        return fileLoaded;
    }

    public boolean isVideoFile() { return isVideoFile; }

    public UpdatePodcastStatusEvent(long current, long max, boolean playing, String title, boolean preparingFile, boolean fileLoaded, boolean isVideoFile, long rssItemId) {
        this.current = current;
        this.max = max;
        this.playing = playing;
        this.title = title;
        this.preparingFile = preparingFile;
        this.fileLoaded = fileLoaded;
        this.isVideoFile = isVideoFile;
        this.rssItemId = rssItemId;
    }

}
