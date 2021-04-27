package screengrab;

import androidx.core.view.GravityCompat;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.NewsReaderListFragment;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.RssItemViewHolder;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static de.luhmer.owncloudnewsreader.helper.Utils.clearFocus;
import static de.luhmer.owncloudnewsreader.helper.Utils.initMaterialShowCaseView;

/**
 * Created by David on 06.03.2016.
 */
@RunWith(JUnit4.class)
@LargeTest
public class ScreenshotTest {

    @ClassRule
    public static final LocaleTestRule localTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(NewsReaderListActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);


    private NewsReaderListActivity mActivity;
    private NewsReaderListFragment nrlf;
    private NewsReaderDetailFragment nrdf;
    private int itemPos = 0;

    //private int podcastGroupPosition = 3;

    @Before
    public void setUp() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());

        mActivity = mActivityRule.getActivity();
        nrlf = mActivity.getSlidingListFragment();
        nrdf = mActivity.getNewsReaderDetailFragment();

        clearFocus();

        initMaterialShowCaseView(mActivity);
    }



    @Test
    public void testTakeScreenshots() {
        Screengrab.screenshot("startup");

        mActivity.runOnUiThread(() -> {
            openDrawer();
            //nrlf.getListView().expandGroup(podcastGroupPosition);
        });

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("slider_open");


        mActivity.runOnUiThread(() -> {
            closeDrawer();

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mActivity.onClick(null, itemPos); //Select item

        });

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("detail_activity");

        mActivity.runOnUiThread(() -> {
            NewsListRecyclerAdapter na = (NewsListRecyclerAdapter) nrdf.getRecyclerView().getAdapter();
            RssItemViewHolder vh = (RssItemViewHolder) nrdf.getRecyclerView().getChildViewHolder(nrdf.getRecyclerView().getLayoutManager().findViewByPosition(itemPos));
            na.changeReadStateOfItem(vh, false);
        });
    }




    @Test
    public void testAudioPodcast() {
        mActivity.runOnUiThread(() -> {
            openDrawer();
            //nrlf.getListView().expandGroup(podcastGroupPosition);
            //openFeed(podcastGroupPosition, 0);
            openFeed(2, 1); // Open Android Podcast
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Screengrab.screenshot("podcast_list");

        mActivity.runOnUiThread(() -> {
            RssItemViewHolder vh = (RssItemViewHolder) nrdf.getRecyclerView().getChildViewHolder(nrdf.getRecyclerView().getLayoutManager().findViewByPosition(0));
            PodcastItem podcastItem = DatabaseConnectionOrm.ParsePodcastItemFromRssItem(mActivity, vh.getRssItem());
            mActivity.openMediaItem(podcastItem);
        });


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("podcast_running");


        mActivity.runOnUiThread(() -> mActivity.pausePodcast());


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void testVideoPodcast() {
        mActivity.runOnUiThread(() -> {
            //Set url to mock
            nrlf.bindUserInfoToUI();

            openDrawer();
            //openFeed(0, 13); //Click on ARD Podcast
            openFeed(7, -1);
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mActivity.runOnUiThread(() -> {
            RssItemViewHolder vh = (RssItemViewHolder) nrdf.getRecyclerView().getChildViewHolder(nrdf.getRecyclerView().getLayoutManager().findViewByPosition(1));
            PodcastItem podcastItem = DatabaseConnectionOrm.ParsePodcastItemFromRssItem(mActivity, vh.getRssItem());
            mActivity.openMediaItem(podcastItem);
        });


        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Screengrab.screenshot("video_podcast_running");

        mActivity.runOnUiThread(() -> mActivity.pausePodcast());


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
        if(mActivity.binding.drawerLayout != null) {
            mActivity.binding.drawerLayout.openDrawer(GravityCompat.START, true);
        }
    }

    private void closeDrawer() {
        if(mActivity.binding.drawerLayout != null) {
            mActivity.binding.drawerLayout.closeDrawer(GravityCompat.START, true);
        }
    }
}
