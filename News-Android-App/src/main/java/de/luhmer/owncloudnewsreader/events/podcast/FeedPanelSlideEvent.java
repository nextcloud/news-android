package de.luhmer.owncloudnewsreader.events.podcast;

public class FeedPanelSlideEvent {

    public FeedPanelSlideEvent(boolean panelOpen) {
        this.panelOpen = panelOpen;
    }

    public boolean isPanelOpen() {
        return panelOpen;
    }

    boolean panelOpen;
}
