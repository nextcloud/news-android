package de.luhmer.owncloudnewsreader.helper;

import de.luhmer.owncloudnewsreader.model.RssFile;

/**
 * Created by David on 19.06.2014.
 */
public class AudioPodcast {

    public static boolean IsAudioPodcast(RssFile item) {
        return (item.getLink() != null && item.getLink().endsWith(".mp3"));
    }

}
