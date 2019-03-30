package com.nextcloud.android.sso.api;


import com.nextcloud.android.sso.aidl.NextcloudRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import de.luhmer.owncloudnewsreader.NewFeedActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.reader.nextcloud.API;
import retrofit2.NextcloudRetrofitApiBuilder;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.verify;


//@RunWith(AndroidJUnit4.class)
@RunWith(MockitoJUnitRunner.class)
@LargeTest
public class NewFeedTests {

    @Rule
    public ActivityTestRule<NewFeedActivity> activityRule = new ActivityTestRule<>(NewFeedActivity.class);


    private final String mApiEndpoint = "/index.php/apps/news/api/v1-2/";
    //private API mApi;


    @Mock
    private NextcloudAPI nextcloudApiMock;

    @Mock
    private API mApi;

    //@Rule
    //public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        //MockitoAnnotation.initMocks(this);
        //when(nextcloudApiMock.getGson()).thenReturn(new GsonBuilder().create());
        //mApi = new NextcloudRetrofitApiBuilder(nextcloudApiMock, mApiEndpoint).create(API.class);
        //mApi = new NextcloudRetrofitApiBuilder(nextcloudApiMock, mApiEndpoint).create(API.class);
    }


    @Test
    public void changeText_sameActivity() {
        // Type text and then press the button.
        onView(withId(R.id.et_feed_url)).perform(typeText("http://test.html"), closeSoftKeyboard());
        onView(withId(R.id.btn_addFeed)).perform(click());

        // Check that the text was changed.
        //onView(withId(R.id.et_feed_url))
        //        .check(matches(withText(stringToBetyped)));



        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "feeds")
                .build();

        //Robolectric.flushForegroundThreadScheduler();
        //Robolectric.flushBackgroundThreadScheduler();



        try {
            verify(mApi, after(5000)).createFeed(any());
            verify(nextcloudApiMock, after(5000)).performNetworkRequest(eq(request));


            //verify(mApi, timeout(5000)).createFeed(any(), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
