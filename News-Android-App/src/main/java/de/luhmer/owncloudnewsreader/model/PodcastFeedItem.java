package de.luhmer.owncloudnewsreader.model;

import de.luhmer.owncloudnewsreader.database.model.Feed;

/**
 * Created by David on 21.06.2014.
 */
public class PodcastFeedItem {

    public PodcastFeedItem(Feed feed, int podcastCount) {
        this.mFeed = feed;
        this.mPodcastCount = podcastCount;
    }

    public Feed mFeed;
    public int mPodcastCount;

}
