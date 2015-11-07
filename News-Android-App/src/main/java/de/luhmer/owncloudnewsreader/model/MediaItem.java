package de.luhmer.owncloudnewsreader.model;

import java.io.Serializable;

/**
 * Created by daniel on 29.07.15.
 */
public abstract class MediaItem implements Serializable {
    public long itemId;
    public String title;
    public String favIcon;
    public String link;
}
