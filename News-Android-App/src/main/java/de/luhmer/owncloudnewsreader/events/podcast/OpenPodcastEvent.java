package de.luhmer.owncloudnewsreader.events.podcast;

/**
 * Created by David on 21.06.2014.
 */
public class OpenPodcastEvent {

    public OpenPodcastEvent(String pathToFile, String mediaTitle, boolean isVideoFile) {
        this.pathToFile = pathToFile;
        this.mediaTitle = mediaTitle;
        this.isVideoFile = isVideoFile;
    }

    public boolean isVideoFile;
    public String pathToFile;
    public String mediaTitle;
}
