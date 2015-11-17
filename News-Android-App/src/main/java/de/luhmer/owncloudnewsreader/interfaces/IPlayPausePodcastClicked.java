package de.luhmer.owncloudnewsreader.interfaces;

import de.luhmer.owncloudnewsreader.database.model.RssItem;

public interface IPlayPausePodcastClicked {
    void openPodcast(RssItem rssItem);
    void pausePodcast();
}
