package screengrab;

import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.test.UiThreadTest;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.NewsReaderListFragment;
import de.luhmer.owncloudnewsreader.R;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.ViewHolder;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.model.UserInfo;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by David on 06.03.2016.
 */
@RunWith(AndroidJUnit4.class)
public class ScreenshotTest {
    @ClassRule
    public static final LocaleTestRule localTestRule = new LocaleTestRule();
    @Rule
    public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(NewsReaderListActivity.class);


    private MenuItem menuItem;
    private NewsReaderListActivity activity;
    private NewsReaderListFragment nrlf;
    private NewsReaderDetailFragment nrdf;
    private int itemPos = 0;

    @Before
    public void setup() {
        menuItem = Mockito.mock(MenuItem.class);
        activity = mActivityRule.getActivity();
        nrlf = mActivityRule.getActivity().getSlidingListFragment();
        nrdf = mActivityRule.getActivity().getNewsReaderDetailFragment();


        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mActivityRule.getActivity());

        UserInfo userInfo = new UserInfo.Builder()
                .setUserId("1")
                .setDisplayName("David")
                .setAvatar(null)
                .build();

        try {
            mPrefs.edit().putString("USER_INFO", NewsReaderListFragment.toString(userInfo)).commit();
            mPrefs.edit().putBoolean(SettingsActivity.CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING, false).commit();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    @Test
    public void testTakeScreenshots() {
        Screengrab.screenshot("startup");
        Mockito.when(menuItem.getItemId()).thenReturn(android.R.id.home);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                //Set url to mock
                nrlf.bindUserInfoToUI(true);

                mActivityRule.getActivity().onOptionsItemSelected(menuItem); //Open Drawer

                nrlf.getListView().expandGroup(2);
            }
        });

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("slider_open");




        activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.onOptionsItemSelected(menuItem); //Close Drawer

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                activity.onClick(null, itemPos); //Select item

            }
        });

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("detail_activity");

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NewsListRecyclerAdapter na = (NewsListRecyclerAdapter) nrdf.getRecyclerView().getAdapter();
                ViewHolder vh = (ViewHolder) nrdf.getRecyclerView().getChildViewHolder(nrdf.getRecyclerView().getLayoutManager().findViewByPosition(itemPos));
                na.ChangeReadStateOfItem(vh, false);
            }
        });
    }


    @Test
    public void testPodcast() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                //Set url to mock
                nrlf.bindUserInfoToUI(true);

                mActivityRule.getActivity().onOptionsItemSelected(menuItem); //Open Drawer

                nrlf.getListView().expandGroup(2);

                nrlf.onChildClickListener.onChildClick(null, null, 2, 2, 0); //Click on Android Central Podcast
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("podcast_list");

        activity.runOnUiThread(new Runnable() {
            public void run() {
                ViewHolder vh = (ViewHolder) nrdf.getRecyclerView().getChildViewHolder(nrdf.getRecyclerView().getLayoutManager().findViewByPosition(1));

                PodcastItem podcastItem = DatabaseConnectionOrm.ParsePodcastItemFromRssItem(activity, vh.getRssItem());
                activity.openMediaItem(podcastItem);
            }
        });


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("podcast_running");


        activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.pausePodcast();
            }
        });


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void testVideoPodcast() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                //Set url to mock
                nrlf.bindUserInfoToUI(true);

                mActivityRule.getActivity().onOptionsItemSelected(menuItem); //Open Drawer



                nrlf.onChildClickListener.onChildClick(null, null, 0, 7, 0); //Click on Android Central Podcast
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        activity.runOnUiThread(new Runnable() {
            public void run() {
                ViewHolder vh = (ViewHolder) nrdf.getRecyclerView().getChildViewHolder(nrdf.getRecyclerView().getLayoutManager().findViewByPosition(1));

                PodcastItem podcastItem = DatabaseConnectionOrm.ParsePodcastItemFromRssItem(activity, vh.getRssItem());
                activity.openMediaItem(podcastItem);
            }
        });


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Screengrab.screenshot("video_podcast_running");


        activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.pausePodcast();
            }
        });


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
