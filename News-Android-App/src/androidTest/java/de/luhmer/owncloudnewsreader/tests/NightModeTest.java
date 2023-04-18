package de.luhmer.owncloudnewsreader.tests;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.TestApplication;
import de.luhmer.owncloudnewsreader.di.TestComponent;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NightModeTest {

    protected @Inject SharedPreferences mPrefs;

    /**
     * NOTE: These tests only work during "daylight".. (this is because there is no way to check
     * the current state of the android day/night mode)
     */

    @Rule
    public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(NewsReaderListActivity.class);
    //public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(NewsReaderListActivity.class, true, false);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    private Activity getActivity() {
        return mActivityRule.getActivity();
    }

    @Before
    public void resetSharedPrefs() {
        TestComponent ac = (TestComponent) ((TestApplication)(getActivity().getApplication())).getAppComponent();
        ac.inject(this);

        /*
        // Set Fixed time
        Instant.now(
                Clock.fixed(
                        Instant.parse( "2019-04-05T18:00:00Z"), ZoneOffset.UTC
                )
        );
        */
    }


    @Test
    public void testBackgroundDaylightTheme() {
        assertFalse(isDarkTheme());
        //onView(withId(R.id.sliding_layout)).check(matches(withBackgroundColor(android.R.color.white, getActivity())));
    }

    @Test
    public void testOledAutoMode() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        openSettings();

        changeAppTheme(R.string.pref_display_apptheme_auto);
        switchOled();
        navigateUp();
        assertFalse(isDarkTheme());
        sleep();

        //onView(withId(R.id.sliding_layout)).check(ViewAssertions.matches(CustomMatchers.withBackgroundColor(android.R.color.white, getActivity())));
        assertEquals(ThemeChooser.THEME.LIGHT, getPrivateField("mSelectedTheme"));
    }

    @Test
    public void testLightTheme() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        openSettings();

        changeAppTheme(R.string.pref_display_apptheme_light);
        navigateUp();
        sleep();
        boolean isDarkTheme = isDarkTheme();
        assertFalse(ThemeChooser.isOledMode(false));
        assertFalse(isDarkTheme);
        assertEquals(ThemeChooser.THEME.LIGHT, getPrivateField("mSelectedTheme"));
        //sleep();
    }

    @Test
    public void testDarkTheme() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        openSettings();

        changeAppTheme(R.string.pref_display_apptheme_dark);
        navigateUp();
        sleep();
        boolean isDarkTheme = isDarkTheme();
        assertFalse(ThemeChooser.isOledMode(false));
        assertTrue(isDarkTheme);
        assertEquals(ThemeChooser.THEME.DARK, getPrivateField("mSelectedTheme"));
        //sleep();
    }

    @Test
    public void testDarkOledTheme() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        openSettings();

        changeAppTheme(R.string.pref_display_apptheme_dark);
        switchOled();
        navigateUp();
        sleep();
        boolean isDarkTheme = isDarkTheme();
        assertTrue(ThemeChooser.isOledMode(false));
        assertTrue(isDarkTheme);
        assertEquals(ThemeChooser.THEME.OLED, getPrivateField("mSelectedTheme"));
        //sleep();
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void navigateUp() {
        onView(withContentDescription(androidx.appcompat.R.string.abc_action_bar_up_description)).perform(click());
    }

    private void openSettings() {
        onView(withText(getActivity().getString(R.string.action_settings))).perform(click());
    }

    private void changeAppTheme(int appThemeText) {
        String title = getActivity().getString(R.string.pref_title_app_theme);

        onView(is(instanceOf(RecyclerView.class)))
            .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(title))))
            .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(title)), click()));

        onView(withText(getActivity().getString(appThemeText)))
                .perform(click());
    }

    private void switchOled() {
        String title = getActivity().getString(R.string.pref_oled_mode);

        onView(is(instanceOf(RecyclerView.class)))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(title))))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(title)), click()));
    }

    private boolean isDarkTheme() {
        try {
            Method method = ThemeChooser.class.getDeclaredMethod("isDarkTheme", Context.class);
            method.setAccessible(true);
            boolean isDarkTheme = (boolean) method.invoke(null, getActivity());
            return isDarkTheme;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail(e.toString() + " - " + e.getMessage());
        }
        return false;
    }

    private Object getPrivateField(String fieldName) {
        try {
            Field[] fields = ThemeChooser.class.getDeclaredFields();
            for (Field field : fields) {
                if(fieldName.equals(field.getName())) {
                    field.setAccessible(true);
                    return field.get(null);
                }
            }
        } catch (IllegalAccessException e) {
            fail(e.getMessage());
        }
        return null;
    }

}