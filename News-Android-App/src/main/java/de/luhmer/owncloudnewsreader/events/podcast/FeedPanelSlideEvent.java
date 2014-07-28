package de.luhmer.owncloudnewsreader.events.podcast;

/**
 * Created by David on 20.07.2014.
 */
public class FeedPanelSlideEvent {

    public FeedPanelSlideEvent(boolean panelOpen) {
        this.panelOpen = panelOpen;
    }

    public boolean isPanelOpen() {
        return panelOpen;
    }

    boolean panelOpen;
}
