package de.luhmer.owncloudnewsreader.tests;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.runner.RunWith;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DownloadWebPageServiceTest {

    //private String expectedAppName;

    @Rule
    public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(NewsReaderListActivity.class);

    /*
    private UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

    private Activity getActivity() {
        return mActivityRule.getActivity();
    }

    @Before
    private void setUp() {
        expectedAppName = getActivity().getString(R.string.app_name);
    }

    @Test
    public void testStartDownload() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(getActivity().getString(R.string.action_download_articles_offline))).perform(click());

    }

    private void clearAllNotifications() {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        uiDevice.openNotification();
        long timeoutInMillis = 1000;
        uiDevice.wait(Until.hasObject(By.textStartsWith(expectedAppName)), timeoutInMillis);
        //UiObject2 clearAll = uiDevice.findObject(By.res(clearAllNotificationRes));
        //clearAll.click();
    }

    @Test
    void shouldSendNotificationWhichContainsTitleTextAndAllCities() {
        String expectedAppName = "Test";
        String expectedAllCities = "Test";
        String expectedTitle = "Test";
        String expectedText = "Test";
        // TODO do something here..!
        uiDevice.openNotification();
        //uiDevice.wait(Until.hasObject(By.textStartsWith(expectedAppName)), timeout);
        UiObject2 title = uiDevice.findObject(By.text(expectedTitle));
        UiObject2 text= uiDevice.findObject(By.textStartsWith(expectedText));
        //UiObject2 allCities= uiDevice.findObject(By.res(expectedAllCitiesActionRes));
        assertEquals(expectedTitle, title.getText());
        assertTrue(text.getText().startsWith(expectedText));
        //assertEquals(expectedAllCities.toLowerCase(), allCities.getText().toLowerCase());
        clearAllNotifications();
    }

    private class ClickOnSendNotification implements ViewAction {

        private final String TAG = ClickOnSendNotification.class.getCanonicalName();

        public String getDescription() {
            return "Click on the send notification button";
        }

        public Matcher<View> getConstraints() {
            return Matchers.allOf(isDisplayed(), isAssignableFrom(Button.class));
        }

        public void perform(@Nullable UiController uiController, @Nullable View view) {
            //view.findViewById(R.id.stop).performClick();
            Log.d(TAG, "perform() called with: uiController = [" + uiController + "], view = [" + view + "]");
        }
    }

    */
}
