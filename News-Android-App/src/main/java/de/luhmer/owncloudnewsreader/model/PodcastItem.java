package de.luhmer.owncloudnewsreader.model;

import java.io.Serializable;

/**
 * Created by David on 21.06.2014.
 */
public class PodcastItem implements Serializable {

    public PodcastItem() {

    }

    public PodcastItem(String itemId, String title, String link, String mimeType, boolean offlineCached) {
        this.itemId = itemId;
        this.title = title;
        this.link = link;
        this.mimeType = mimeType;
        this.offlineCached = offlineCached;
    }

    public String itemId;
    public String title;
    public String link;
    public String mimeType;
    public boolean offlineCached;

    public Integer downloadProgress;

    public static Integer DOWNLOAD_COMPLETED = -1;
    public static Integer DOWNLOAD_NOT_STARTED = -2;
}
