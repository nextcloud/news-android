package de.luhmer.owncloudnewsreader.model;

import java.io.Serializable;

/**
 * Created by David on 21.06.2014.
 */
public class PodcastItem implements Serializable {

    public String itemId;
    public String title;
    public String link;
    public String mimeType;

    public int downloadProgress;

    public static int DOWNLOAD_COMPLETED = -1;
    public static int DOWNLOAD_NOT_STARTED = -2;
}
