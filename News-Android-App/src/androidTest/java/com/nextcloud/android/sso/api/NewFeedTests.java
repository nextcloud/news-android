package com.nextcloud.android.sso.api;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Inject;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import de.luhmer.owncloudnewsreader.NewFeedActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.TestApplication;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.di.TestComponent;
import de.luhmer.owncloudnewsreader.reader.nextcloud.API;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;


//@RunWith(AndroidJUnit4.class)
@RunWith(MockitoJUnitRunner.class)
@LargeTest
public class NewFeedTests {

    @Rule
    public ActivityTestRule<NewFeedActivity> activityRule = new ActivityTestRule<>(NewFeedActivity.class);

    @Inject ApiProvider mApi;

    @Before
    public void setUp() {
        TestComponent ac = (TestComponent) ((TestApplication)(activityRule.getActivity().getApplication())).getAppComponent();
        ac.inject(this);
    }


    @Test
    public void addNewFeed_New_sameActivity() {
        String feed = "http://test.de/new";

        // Type text and then press the button.
        onView(withId(R.id.et_feed_url)).perform(typeText(feed), closeSoftKeyboard());
        onView(withId(R.id.btn_addFeed)).perform(click());

        try {
            //API api = mApi.getAPI();
            //verify(api, timeout(2000)).createFeed(feed, 0L);

            //onView(withId(R.id.et_feed_url)).check(matches(hasErrorText(nullValue(String.class))));

            // Check Activity existed
            Thread.sleep(1000);
            assertFalse(activityRule.getActivity().getWindow().getDecorView().isShown());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void addNewFeed_Existing_sameActivity() {
        String feed = "http://test.de/existing";

        // Type text and then press the button.
        onView(withId(R.id.et_feed_url)).perform(typeText(feed), closeSoftKeyboard());
        onView(withId(R.id.btn_addFeed)).perform(click());

        try {
            //API api = mApi.getAPI();
            //verify(api, timeout(2000)).createFeed(feed, 0L);

            // Check Activity still open
            Thread.sleep(1000);
            assertTrue(activityRule.getActivity().getWindow().getDecorView().isShown());

            onView(withId(R.id.et_feed_url)).check(matches(hasErrorText(is("Feed konnte nicht hinzugefügt werden:  Existiert bereits"))));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void addNewFeed_Invalid_sameActivity() {
        String feed = "http://test.de/fail";

        // Type text and then press the button.
        onView(withId(R.id.et_feed_url)).perform(typeText(feed), closeSoftKeyboard());
        onView(withId(R.id.btn_addFeed)).perform(click());

        try {
            //API api = mApi.getAPI();
            //verify(api, timeout(2000)).createFeed(feed, 0L);

            // Check Activity still open
            Thread.sleep(1000);
            assertTrue(activityRule.getActivity().getWindow().getDecorView().isShown());

            onView(withId(R.id.et_feed_url)).check(matches(hasErrorText(is("FeedIo\\Adapter\\NotFoundException: Client error: `GET http://feeds2.feedburner.com/stadt-bremerhaven/dqXM222` resulted in a `404 Feed not found error: ..."))));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
