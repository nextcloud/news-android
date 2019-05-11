package de.luhmer.owncloudnewsreader.tests;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.TestApplication;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.ViewHolder;
import de.luhmer.owncloudnewsreader.di.TestComponent;
import helper.OrientationChangeAction;
import helper.RecyclerViewAssertions;

import static androidx.core.util.Preconditions.checkNotNull;
import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.InstrumentationRegistry.registerInstance;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NewsReaderListActivityUiTests {

    private int scrollPosition = 10;

    @Rule
    public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(NewsReaderListActivity.class);

    protected @Inject SharedPreferences mPrefs;

    private NewsReaderListActivity getActivity() {
        return mActivityRule.getActivity();
    }

    @Before
    public void setUp() {
        registerInstance(getInstrumentation(), new Bundle());
        sleep(0.3f);

        TestComponent ac = (TestComponent) ((TestApplication)(getActivity().getApplication())).getAppComponent();
        ac.inject(this);
    }

    @Test
    public void testPositionAfterOrientationChange_sameActivity() {
        NewsReaderDetailFragment ndf = (NewsReaderDetailFragment) waitForFragment(R.id.content_frame, 5000);

        onView(withId(R.id.list)).perform(
                RecyclerViewActions.scrollToPosition(scrollPosition));

        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(getActivity()));
        //onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(getActivity()));

        sleep(1.0f);

        LinearLayoutManager llm = (LinearLayoutManager) ndf.getRecyclerView().getLayoutManager();
        onView(withId(R.id.list)).check(new RecyclerViewAssertions(scrollPosition-(scrollPosition-llm.findFirstVisibleItemPosition())));
        onView(withId(R.id.tv_no_items_available)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        //onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(getActivity()));
    }

    @Test
    public void testPositionAfterActivityRestart_sameActivity() {
        onView(withId(R.id.list)).perform(RecyclerViewActions.scrollToPosition(scrollPosition));

        onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(scrollPosition, click()));

        sleep(2);

        Espresso.pressBack();

        NewsReaderDetailFragment ndf = (NewsReaderDetailFragment) waitForFragment(R.id.content_frame, 5000);
        assertNotNull(ndf);
        final NewsListRecyclerAdapter na = (NewsListRecyclerAdapter) ndf.getRecyclerView().getAdapter();
        assertNotNull(na);
        final ViewHolder vh = (ViewHolder) ndf.getRecyclerView().getChildViewHolder(ndf.getRecyclerView().getLayoutManager().findViewByPosition(scrollPosition));
        assertNotNull(vh);
        LinearLayoutManager llm = (LinearLayoutManager) ndf.getRecyclerView().getLayoutManager();

        getActivity().runOnUiThread(() -> na.changeReadStateOfItem(vh, false));
        sleep(1.0f);

        onView(withId(R.id.list)).check(new RecyclerViewAssertions(scrollPosition-(scrollPosition-llm.findFirstVisibleItemPosition())));
        onView(withId(R.id.tv_no_items_available)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }


    @Test
    public void testSyncFinishedRefreshRecycler_sameActivity() {
        assertTrue(syncResultTest(true));
    }

    @Test
    public void testSyncFinishedSnackbar_sameActivity() {
        assertTrue(syncResultTest(false));
    }


    @Test
    public void searchTest() {
        String firstItem = "These are the best screen protectors for the Huawei P30 Pro";

        sleep(500);

        // Check first item
        checkRecyclerViewFirstItemText(firstItem);

        // Open search menu
        onView(allOf(withId(R.id.menu_search), withContentDescription(getString(R.string.action_search)), isDisplayed())).perform(click());

        // Type in "test" into searchbar
        onView(allOf(withClassName(is("android.widget.SearchView$SearchAutoComplete")), isDisplayed())).perform(typeText("test"));
        sleep(1000);
        checkRecyclerViewFirstItemText("Testfahrt im Mercedes E 300 de mit 90-kW-Elektromotor und Vierzylinder-Diesel");

        // Close search bar
        onView(withContentDescription("Collapse")).perform(click());

        sleep(500);

        // Test if search reset was successful
        checkRecyclerViewFirstItemText(firstItem);
    }

    @Test
    public void syncTest() {
        // Open navigation drawer
        onView(allOf(withContentDescription(getString(R.string.news_list_drawer_text)), isDisplayed())).perform(click());

        sleep(1000);

        // Click on Got it
        onView(allOf(withText("GOT IT"), isDisplayed())).perform(click());

        sleep(1000);

        // Trigger refresh
        onView(allOf(withContentDescription(getString(R.string.content_desc_tap_to_refresh)), isDisplayed())).perform(click());

        sleep(1000);
        assertTrue(true);
    }



    private void checkRecyclerViewFirstItemText(String text) {
        onView(withId(R.id.list)).check(matches(atPosition(0, hasDescendant(withText(text)))));
    }

    private String getString(@IdRes int resId) {
        return mActivityRule.getActivity().getString(resId);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

    private boolean syncResultTest(boolean testFirstPosition) {
        if(!testFirstPosition) {
            onView(withId(R.id.list)).perform(RecyclerViewActions.scrollToPosition(scrollPosition));
        }

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
            sleep(1.0f);

            if(!testFirstPosition) {
                onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()));
            } else {
                onView(withId(com.google.android.material.R.id.snackbar_text)).check(doesNotExist());
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return true;
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