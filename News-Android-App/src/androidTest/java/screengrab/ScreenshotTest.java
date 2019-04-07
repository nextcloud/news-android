package screengrab;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.core.view.GravityCompat;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.NewsReaderListFragment;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.ViewHolder;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

/**
 * Created by David on 06.03.2016.
 */
@RunWith(AndroidJUnit4.class)
public class ScreenshotTest {

    @ClassRule
    public static final LocaleTestRule localTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(NewsReaderListActivity.class);


    private NewsReaderListActivity activity;
    private NewsReaderListFragment nrlf;
    private NewsReaderDetailFragment nrdf;
    private int itemPos = 0;

    private int podcastGroupPosition = 3;

    @Before
    public void setUp() {
        activity = mActivityRule.getActivity();
        nrlf = mActivityRule.getActivity().getSlidingListFragment();
        nrdf = mActivityRule.getActivity().getNewsReaderDetailFragment();
    }



    @Test
    public void testTakeScreenshots() {
        Screengrab.screenshot("startup");

        activity.runOnUiThread(() -> {
            openDrawer();
            nrlf.getListView().expandGroup(podcastGroupPosition);
        });

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("slider_open");


        activity.runOnUiThread(() -> {
            closeDrawer();

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            activity.onClick(null, itemPos); //Select item

        });

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("detail_activity");

        activity.runOnUiThread(() -> {
            NewsListRecyclerAdapter na = (NewsListRecyclerAdapter) nrdf.getRecyclerView().getAdapter();
            ViewHolder vh = (ViewHolder) nrdf.getRecyclerView().getChildViewHolder(nrdf.getRecyclerView().getLayoutManager().findViewByPosition(itemPos));
            na.changeReadStateOfItem(vh, false);
        });
    }




    @Test
    public void testPodcast() {
        activity.runOnUiThread(() -> {
            openDrawer();
            nrlf.getListView().expandGroup(podcastGroupPosition);
            openFeed(podcastGroupPosition, 0);
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("podcast_list");

        activity.runOnUiThread(() -> {
            ViewHolder vh = (ViewHolder) nrdf.getRecyclerView().getChildViewHolder(nrdf.getRecyclerView().getLayoutManager().findViewByPosition(0));
            PodcastItem podcastItem = DatabaseConnectionOrm.ParsePodcastItemFromRssItem(activity, vh.getRssItem());
            activity.openMediaItem(podcastItem);
        });


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("podcast_running");


        activity.runOnUiThread(() -> activity.pausePodcast());


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void testVideoPodcast() {
        activity.runOnUiThread(() -> {
            //Set url to mock
            nrlf.bindUserInfoToUI();

            openDrawer();
            openFeed(0, 13); //Click on ARD Podcast
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        activity.runOnUiThread(() -> {
            ViewHolder vh = (ViewHolder) nrdf.getRecyclerView().getChildViewHolder(nrdf.getRecyclerView().getLayoutManager().findViewByPosition(1));
            PodcastItem podcastItem = DatabaseConnectionOrm.ParsePodcastItemFromRssItem(activity, vh.getRssItem());
            activity.openMediaItem(podcastItem);
        });


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("video_podcast_running");


        activity.runOnUiThread(() -> activity.pausePodcast());


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openFeed(int groupPosition, int childPosition) {
        nrlf.onChildClickListener.onChildClick(null, null, groupPosition, childPosition, 0); //Click on ARD Podcast
    }

    private void openDrawer() {
        if(activity.drawerLayout != null) {
            activity.drawerLayout.openDrawer(GravityCompat.START, true);
        }
    }

    private void closeDrawer() {
        if(activity.drawerLayout != null) {
            activity.drawerLayout.closeDrawer(GravityCompat.START, true);
        }
    }
}
