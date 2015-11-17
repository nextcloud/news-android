package de.luhmer.owncloudnewsreader.model;

public class TTSItem extends MediaItem {

    public TTSItem() {

    }

    public TTSItem(long itemId, String title, String text, String favIcon) {
        this.itemId = itemId;
        this.title = title;
        this.text = text;
        this.favIcon = favIcon;
    }

    public String text;
}
