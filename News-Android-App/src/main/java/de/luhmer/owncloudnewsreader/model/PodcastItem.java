package de.luhmer.owncloudnewsreader.model;

public class PodcastItem extends MediaItem {

    public PodcastItem() {

    }

    public PodcastItem(long itemId, String author, String title, String link, String mimeType, boolean offlineCached, String favIcon, boolean isVideoPodcast, String fingerprint) {
        this.itemId = itemId;
        this.author = author;
        this.title = title;
        this.link = link;
        this.mimeType = mimeType;
        this.offlineCached = offlineCached;
        this.favIcon = favIcon;
        this.isVideoPodcast = isVideoPodcast;
        this.fingerprint = fingerprint;
    }

    public String mimeType;
    public String fingerprint;
    public boolean offlineCached;
    public boolean isVideoPodcast;

    public Integer downloadProgress;

    public static Integer DOWNLOAD_COMPLETED = -1;
    public static Integer DOWNLOAD_NOT_STARTED = -2;


    /*
    public boolean isYoutubeVideo() {
        return link.matches("^https?://(www.)?youtube.com/.*");
    }
    */
}
