package de.luhmer.owncloudnewsreader.tests;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

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
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import com.nextcloud.android.sso.aidl.NextcloudRequest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.TestApplication;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.RssItemViewHolder;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.di.TestApiProvider;
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
import static de.luhmer.owncloudnewsreader.helper.Utils.clearFocus;
import static de.luhmer.owncloudnewsreader.helper.Utils.initMaterialShowCaseView;
import static de.luhmer.owncloudnewsreader.helper.Utils.sleep;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NewsReaderListActivityUiTests {

    private int scrollPosition = 10;

    @Rule
    public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(NewsReaderListActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    protected @Inject SharedPreferences mPrefs;
    protected @Inject ApiProvider mApi;

    private NewsReaderListActivity getActivity() {
        return mActivityRule.getActivity();
    }

    @Before
    public void setUp() {
        registerInstance(getInstrumentation(), new Bundle());
        sleep(0.3f);

        TestComponent ac = (TestComponent) ((TestApplication)(getActivity().getApplication())).getAppComponent();
        ac.inject(this);

        clearFocus();

        initMaterialShowCaseView(getActivity());
    }

    @Test
    public void testPositionAfterOrientationChange_sameActivity() {
        NewsReaderDetailFragment ndf = (NewsReaderDetailFragment) waitForFragment(R.id.content_frame, 5000);

        onView(withId(R.id.list)).perform(
                RecyclerViewActions.scrollToPosition(scrollPosition));

        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(getActivity()));
        //onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(getActivity()));

        sleep(2000);

        LinearLayoutManager llm = (LinearLayoutManager) ndf.getRecyclerView().getLayoutManager();
        int expectedPosition = scrollPosition-(scrollPosition-llm.findFirstVisibleItemPosition());

        // As there is a little offset when rotating.. we need to add one here..
        onView(withId(R.id.list)).check(new RecyclerViewAssertions(expectedPosition+1));
        onView(withId(R.id.tv_no_items_available)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        sleep(2000);

        onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(getActivity()));

        onView(withId(R.id.list)).check(new RecyclerViewAssertions(expectedPosition));
        onView(withId(R.id.tv_no_items_available)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void testPositionAfterActivityRestart_sameActivity() {
        onView(withId(R.id.list)).perform(RecyclerViewActions.scrollToPosition(scrollPosition));

        onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(scrollPosition, click()));

        sleep(2000);

        Espresso.pressBack();

        NewsReaderDetailFragment ndf = (NewsReaderDetailFragment) waitForFragment(R.id.content_frame, 5000);
        assertNotNull(ndf);
        final NewsListRecyclerAdapter na = (NewsListRecyclerAdapter) ndf.getRecyclerView().getAdapter();
        assertNotNull(na);
        final RssItemViewHolder vh = (RssItemViewHolder) ndf.getRecyclerView().getChildViewHolder(ndf.getRecyclerView().getLayoutManager().findViewByPosition(scrollPosition));
        assertNotNull(vh);
        LinearLayoutManager llm = (LinearLayoutManager) ndf.getRecyclerView().getLayoutManager();

        getActivity().runOnUiThread(() -> na.changeReadStateOfItem(vh, false));
        sleep(1.0f);

        int expectedPosition = scrollPosition-(scrollPosition-llm.findFirstVisibleItemPosition());
        onView(withId(R.id.list)).check(new RecyclerViewAssertions(expectedPosition));
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
        String firstItem = "Immer wieder sonntags KW 19";
        // String firstItem = "These are the best screen protectors for the Huawei P30 Pro";

        // Check first item
        checkRecyclerViewFirstItemText(firstItem);

        // Open search menu
        onView(allOf(withId(R.id.menu_search), withContentDescription(getString(R.string.action_search)), isDisplayed())).perform(click());

        // Type in "test" into searchbar
        onView(allOf(withClassName(is("android.widget.SearchView$SearchAutoComplete")), isDisplayed())).perform(typeText("test"));
        sleep(1000);
        checkRecyclerViewFirstItemText("VR ohne Kabel: Die Oculus Quest im Test, definitiv der richtige Ansatz");
        // checkRecyclerViewFirstItemText("Testfahrt im Mercedes E 300 de mit 90-kW-Elektromotor und Vierzylinder-Diesel");

        // Close search bar
        onView(withContentDescription("Collapse")).perform(click());

        sleep(1000);

        // Test if search reset was successful
        checkRecyclerViewFirstItemText(firstItem);
    }

    @Test
    public void syncTest() {
        // Open navigation drawer
        onView(allOf(withContentDescription(getString(R.string.news_list_drawer_text)), isDisplayed())).perform(click());

        sleep(1500);

        /*
        // Click on Got it
        onView(allOf(withText("GOT IT"), isDisplayed())).perform(click());

        sleep(1000);
        */

        // Trigger refresh
        onView(allOf(withContentDescription(getString(R.string.content_desc_tap_to_refresh)), isDisplayed())).perform(click());

        sleep(1000);
        try {
            verifySyncRequested();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    // Verify that the API was actually called
    private void verifySyncRequested() throws Exception {
        TestApiProvider.NewsTestNetworkRequest nr = ((TestApiProvider)mApi).networkRequestSpy;
        ArgumentCaptor<NextcloudRequest> argument = ArgumentCaptor.forClass(NextcloudRequest.class);
        verify(nr, times(6)).performNetworkRequest(argument.capture(), any());

        List<String> requestedUrls = argument.getAllValues().stream().map(nextcloudRequest -> nextcloudRequest.getUrl()).collect(Collectors.toList());

        assertTrue(requestedUrls.contains("/index.php/apps/news/api/v1-2/folders"));
        assertTrue(requestedUrls.contains("/index.php/apps/news/api/v1-2/feeds"));
        assertTrue(requestedUrls.contains("/index.php/apps/news/api/v1-2/items/unread/multiple"));
        assertTrue(requestedUrls.contains("/index.php/apps/news/api/v1-2/items")); // TODO Double check why /items is called twice... ?
        assertTrue(requestedUrls.contains("/index.php/apps/news/api/v1-2/user"));
    }



    private void checkRecyclerViewFirstItemText(String text) {
        onView(withId(R.id.list)).check(matches(atPosition(0, hasDescendant(withText(text)))));
    }

    private String getString(@IdRes int resId) {
        return mActivityRule.getActivity().getString(resId);
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