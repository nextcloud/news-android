package de.luhmer.owncloudnewsreader.model;

/**
 * Created by David on 10.01.2015.
 */
public class TTSItem {

    public TTSItem() {

    }

    public TTSItem(long itemId, String title, String text, String favIcon) {
        this.itemId = itemId;
        this.title = title;
        this.text = text;
        this.favIcon = favIcon;
    }

    public long itemId;
    public String title;
    public String link;
    public String text;
    public String favIcon;
}
