/*
package de.luhmer.owncloudnewsreader.junit_tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.RssItem;

@RunWith(RobolectricTestRunner.class)
public class TestDbTest {

    private NewsReaderListActivity activity;
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(NewsReaderListActivity.class).create().get();
    }

    @Test
    public void testDatabaseOversize() {
        final DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(activity);
        dbConn.resetDatabase();

        Feed feed = new Feed();
        feed.setId(0);
        feed.setFeedTitle("Test");
        feed.setFolderId(0l);

        List<Feed> feedList = new ArrayList<>();
        feedList.add(feed);
        dbConn.insertNewFeed(feedList);

        String randomBody = randomString(1000000);

        for (int i = 0; i < 1; i++) {
            List<RssItem> buffer = new ArrayList<>();
            for (int x = 0; x < 100; x++) {
                RssItem rssItem = new RssItem();
                rssItem.setId((i + 1) * x);
                rssItem.setGuid("http://grulja.wordpress.com/?p=76");
                rssItem.setGuidHash("3059047a572cd9cd5d0bf645faffd077");
                rssItem.setLink("http://grulja.wordpress.com/2013/04/29/plasma-nm-after-the-solid-sprint/");
                rssItem.setTitle(randomString(10));
                rssItem.setAuthor("Jan Grulich (grulja)");
                rssItem.setPubDate(new java.util.Date());
                rssItem.setFeedId(0);
                rssItem.setRead(false);
                rssItem.setRead_temp(false);
                rssItem.setStarred(false);
                rssItem.setStarred_temp(false);
                rssItem.setLastModified(new java.util.Date());
                rssItem.setFingerprint(randomString(20));
                rssItem.setBody("<p>" + randomBody + "</p>");
                buffer.add(rssItem);
            }
            dbConn.insertNewItems(buffer);
        }
    }

    private String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
*/