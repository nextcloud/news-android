package de.luhmer.owncloudnewsreader.model;

public class TTSItem extends MediaItem {

    public TTSItem(long itemId, String author, String title, String text, String favIcon) {
        this.itemId = itemId;
        this.author = author;
        this.title = title;
        this.text = text;
        this.favIcon = favIcon;
    }

    public String text;
}
