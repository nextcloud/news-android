package de.luhmer.owncloudnewsreader.tests;


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
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.nextcloud.android.sso.aidl.NextcloudRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.NewFeedActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.TestApplication;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.di.TestApiProvider;
import de.luhmer.owncloudnewsreader.di.TestComponent;


//@RunWith(AndroidJUnit4.class)
@RunWith(MockitoJUnitRunner.class)
@LargeTest
public class NewFeedTests {

    @Rule
    public ActivityTestRule<NewFeedActivity> activityRule = new ActivityTestRule<>(NewFeedActivity.class);

    protected @Inject ApiProvider mApi;

    @Before
    public void setUp() {
        TestComponent ac = (TestComponent) ((TestApplication)(activityRule.getActivity().getApplication())).getAppComponent();
        ac.inject(this);

        // Reset Spy object
        mApi.initApi(null);
        //reset(((TestApiProvider)mApi).networkRequestSpy);
    }

    @Test
    public void addNewFeed() {
        String feed = TestApiProvider.NEW_FEED_SUCCESS;

        // Type text and then press the button.
        onView(withId(R.id.et_feed_url)).perform(typeText(feed), closeSoftKeyboard());
        onView(withId(R.id.btn_addFeed)).perform(click());

        try {
            verifyRequest(feed);

            //onView(withId(R.id.et_feed_url)).check(matches(hasErrorText(nullValue(String.class))));

            // Check Activity existed
            Thread.sleep(1000);
            assertFalse(activityRule.getActivity().getWindow().getDecorView().isShown());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void addExistingFeed() {
        String feed = TestApiProvider.NEW_FEED_EXISTING;

        // Type text and then press the button.
        onView(withId(R.id.et_feed_url)).perform(typeText(feed), closeSoftKeyboard());
        onView(withId(R.id.btn_addFeed)).perform(click());

        try {
            verifyRequest(feed);

            // Check Activity still open
            Thread.sleep(1000);
            assertTrue(activityRule.getActivity().getWindow().getDecorView().isShown());

            onView(withId(R.id.et_feed_url)).check(matches(hasErrorText(is("Feed konnte nicht hinzugefügt werden:  Existiert bereits"))));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void addInvalidFeed() {
        String feed = TestApiProvider.NEW_FEED_FAIL;

        // Type text and then press the button.
        onView(withId(R.id.et_feed_url)).perform(typeText(feed), closeSoftKeyboard());
        onView(withId(R.id.btn_addFeed)).perform(click());

        try {
            verifyRequest(feed);

            // Check Activity still open
            Thread.sleep(1000);
            assertTrue(activityRule.getActivity().getWindow().getDecorView().isShown());

            onView(withId(R.id.et_feed_url)).check(matches(hasErrorText(is("FeedIo\\Adapter\\NotFoundException: Client error: `GET http://feeds2.feedburner.com/stadt-bremerhaven/dqXM222` resulted in a `404 Feed not found error: ..."))));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    // Verify that the API was actually called
    private void verifyRequest(String feed) throws Exception {
        TestApiProvider.NewsTestNetworkRequest nr = ((TestApiProvider) mApi).networkRequestSpy;
        ArgumentCaptor<NextcloudRequest> argument = ArgumentCaptor.forClass(NextcloudRequest.class);
        verify(nr, timeout(2000)).performNetworkRequest(argument.capture(), any());
        assertEquals("/index.php/apps/news/api/v1-2/feeds", argument.getValue().getUrl());
        var url = argument.getValue().getParameterV2().stream().filter((s) -> s.key.equals(("url"))).findFirst().get().value;
        var folderId = argument.getValue().getParameterV2().stream().filter((s) -> s.key.equals(("folderId"))).findFirst().get().value;
        assertEquals(feed, url);
        assertEquals("0", folderId);
    }
}
