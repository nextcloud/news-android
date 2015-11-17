package de.luhmer.owncloudnewsreader.model;

import java.io.Serializable;

public abstract class MediaItem implements Serializable {
    public long itemId;
    public String title;
    public String favIcon;
    public String link;
}
