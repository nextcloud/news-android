import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.ViewHolder;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class NewsReaderListActivityUiTests
        extends ActivityInstrumentationTestCase2<NewsReaderListActivity> {

    public NewsReaderListActivityUiTests() {
        super(NewsReaderListActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        getActivity();

        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape());
        sleep(0.3f);
    }

    @Override
    protected void tearDown() throws Exception {
        onView(isRoot()).perform(OrientationChangeAction.orientationLandscape());

        super.tearDown();
    }

    public void testPositionAfterOrientationChange_sameActivity() {
        NewsReaderDetailFragment ndf = (NewsReaderDetailFragment) waitForFragment(R.id.content_frame, 5000);

        // Type text and then press the button.

        //onView(withId(R.id.editTextUserInput)).perform(typeText(STRING_TO_BE_TYPED), closeSoftKeyboard());
        //onView(withId(R.id.changeTextButton)).perform(click());

        int mPosition = 20;

        onView(withId(R.id.list)).perform(
                RecyclerViewActions.scrollToPosition(mPosition));

        onView(isRoot()).perform(OrientationChangeAction.orientationPortrait());

        sleep(1);

        LinearLayoutManager llm = (LinearLayoutManager) ndf.getRecyclerView().getLayoutManager();
        onView(withId(R.id.list)).check(new RecyclerViewAssertions(mPosition-(mPosition-llm.findFirstVisibleItemPosition())));
        onView(withId(R.id.tv_no_items_available)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }


    public void testPositionAfterActivityRestart_sameActivity() {
        NewsReaderDetailFragment ndf = (NewsReaderDetailFragment) waitForFragment(R.id.content_frame, 5000);

        int mPosition = 20;

        onView(withId(R.id.list)).perform(
                RecyclerViewActions.scrollToPosition(mPosition));

        onView(withId(R.id.list)).perform(
                RecyclerViewActions.actionOnItemAtPosition(mPosition, click()));

        sleep(2);

        Espresso.pressBack();

        NewsListRecyclerAdapter na = (NewsListRecyclerAdapter) ndf.getRecyclerView().getAdapter();
        ViewHolder vh = (ViewHolder) ndf.getRecyclerView().getChildViewHolder(ndf.getRecyclerView().getLayoutManager().findViewByPosition(mPosition));
        LinearLayoutManager llm = (LinearLayoutManager) ndf.getRecyclerView().getLayoutManager();
        na.ChangeReadStateOfItem(vh, false);

        onView(withId(R.id.list)).check(new RecyclerViewAssertions(mPosition-(mPosition-llm.findFirstVisibleItemPosition())));
        onView(withId(R.id.tv_no_items_available)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }


    public void testSyncFinishedRefreshRecycler_sameActivity() {
        syncResultTest(true);
    }

    public void testSyncFinishedSnackbar_sameActivity() {
        syncResultTest(false);
    }

    private void syncResultTest(boolean testFirstPosition) {
        if(!testFirstPosition) {
            onView(withId(R.id.list)).perform(RecyclerViewActions.scrollToPosition(20));
        }

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPrefs.edit().putInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 5).commit();

        try {
            final Method method = NewsReaderListActivity.class.getDeclaredMethod("syncFinishedHandler", null);
            method.setAccessible(true);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!(boolean) method.invoke(getActivity(), null))
                            throw new RuntimeException("No new items! Something went wrong!");
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });
            getInstrumentation().waitForIdleSync();

            if(!testFirstPosition)
                onView(withId(android.support.design.R.id.snackbar_text)).check(matches(isDisplayed()));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void sleep(float seconds) {
        try {
            Thread.sleep((long) seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    protected Fragment waitForFragment(int id, int timeout) {
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