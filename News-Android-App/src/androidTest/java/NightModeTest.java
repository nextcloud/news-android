import org.hamcrest.CustomMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NightModeTest {

    private String mStringToBetyped;

    @Rule
    public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(
            NewsReaderListActivity.class);

    @Before
    public void initTestData() {
        // Specify a valid string.
        mStringToBetyped = "Espresso";
    }

    @Test
    public void testBackgroundDaylight_sameActivity() {
        // Type text and then press the button.
        onView(withId(R.id.sliding_layout))
                .check(ViewAssertions.matches(CustomMatchers.withBackgroundColor(android.R.color.white)));
    }
}