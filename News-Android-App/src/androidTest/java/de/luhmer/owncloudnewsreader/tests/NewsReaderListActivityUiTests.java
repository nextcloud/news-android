package de.luhmer.owncloudnewsreader.tests;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.ViewHolder;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.InstrumentationRegistry.registerInstance;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NewsReaderListActivityUiTests {

    private int scrollPosition = 10;

    @Rule
    public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(NewsReaderListActivity.class);


    private NewsReaderListActivity getActivity() {
        return mActivityRule.getActivity();
    }

    @Before
    public void setUp() {
        registerInstance(getInstrumentation(), new Bundle());
        sleep(0.3f);
    }

    @Test
    public void testPositionAfterOrientationChange_sameActivity() {
        NewsReaderDetailFragment ndf = (NewsReaderDetailFragment) waitForFragment(R.id.content_frame, 5000);

        onView(withId(R.id.list)).perform(
                RecyclerViewActions.scrollToPosition(scrollPosition));

        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(getActivity()));
        //onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(getActivity()));

        sleep(0.5f);

        LinearLayoutManager llm = (LinearLayoutManager) ndf.getRecyclerView().getLayoutManager();
        onView(withId(R.id.list)).check(new RecyclerViewAssertions(scrollPosition-(scrollPosition-llm.findFirstVisibleItemPosition())));
        onView(withId(R.id.tv_no_items_available)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        //onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(getActivity()));
    }

    @Test
    public void testPositionAfterActivityRestart_sameActivity() {
        onView(withId(R.id.list)).perform(
                RecyclerViewActions.scrollToPosition(scrollPosition));

        onView(withId(R.id.list)).perform(
                RecyclerViewActions.actionOnItemAtPosition(scrollPosition, click()));

        sleep(2);

        Espresso.pressBack();

        NewsReaderDetailFragment ndf = (NewsReaderDetailFragment) waitForFragment(R.id.content_frame, 5000);
        assertNotNull(ndf);
        final NewsListRecyclerAdapter na = (NewsListRecyclerAdapter) ndf.getRecyclerView().getAdapter();
        assertNotNull(na);
        final ViewHolder vh = (ViewHolder) ndf.getRecyclerView().getChildViewHolder(ndf.getRecyclerView().getLayoutManager().findViewByPosition(scrollPosition));
        assertNotNull(vh);
        LinearLayoutManager llm = (LinearLayoutManager) ndf.getRecyclerView().getLayoutManager();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                na.changeReadStateOfItem(vh, false);
            }
        });
        sleep(0.5f);

        onView(withId(R.id.list)).check(new RecyclerViewAssertions(scrollPosition-(scrollPosition-llm.findFirstVisibleItemPosition())));
        onView(withId(R.id.tv_no_items_available)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }


    @Test
    public void testSyncFinishedRefreshRecycler_sameActivity() {
        syncResultTest(true);
    }

    @Test
    public void testSyncFinishedSnackbar_sameActivity() {
        syncResultTest(false);
    }

    private void syncResultTest(boolean testFirstPosition) {
        if(!testFirstPosition) {
            onView(withId(R.id.list)).perform(RecyclerViewActions.scrollToPosition(scrollPosition));
        }

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPrefs.edit().putInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 5).commit();

        try {
            final Method method = NewsReaderListActivity.class.getDeclaredMethod("syncFinishedHandler");
            method.setAccessible(true);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!(boolean) method.invoke(getActivity())) {
                            fail("Method invocation failed!");
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        fail(e.getMessage());
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        fail(e.getMessage());
                    }
                }
            });
            getInstrumentation().waitForIdleSync();
            sleep(0.5f);

            if(!testFirstPosition) {
                onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()));
            } else {
                onView(withId(com.google.android.material.R.id.snackbar_text)).check(doesNotExist());
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void sleep(float seconds) {
        try {
            Thread.sleep((long) seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private Fragment waitForFragment(int id, int timeout) {
        long endTime = SystemClock.uptimeMillis() + timeout;
        while (SystemClock.uptimeMillis() <= endTime) {
            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(id);
            if (fragment != null) {
                return fragment;
            }
        }
        return null;
    }


}