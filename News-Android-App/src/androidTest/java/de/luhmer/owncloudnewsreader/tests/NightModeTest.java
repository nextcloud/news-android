package de.luhmer.owncloudnewsreader.tests;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import helper.CustomMatchers;

import static android.preference.PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES;
import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.PreferenceMatchers.withKey;
import static androidx.test.espresso.matcher.PreferenceMatchers.withSummary;
import static androidx.test.espresso.matcher.PreferenceMatchers.withTitle;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_OLED_MODE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_APP_THEME;
import static helper.CustomMatchers.withBackgroundColor;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NightModeTest {

    /**
     * NOTE: These tests only works during "daylight".. (this is because there is no way to check
     * the current state of the android day/night mode)
     */

    @Rule
    public ActivityTestRule<NewsReaderListActivity> mActivityRule = new ActivityTestRule<>(NewsReaderListActivity.class, true, false);

    private Activity getActivity() {
        return mActivityRule.getActivity();
    }

    @Before
    public void resetSharedPrefs() {
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Reset SharedPrefs
        // https://developer.android.com/guide/topics/ui/settings#Defaults
        mPrefs.edit()
                .remove(CB_OLED_MODE)
                .remove(SP_APP_THEME)
                .commit();

        assertThat(mPrefs.contains(SP_APP_THEME), equalTo(false));
        assertThat(mPrefs.contains(CB_OLED_MODE), equalTo(false));


        SharedPreferences defaultValueSp = context.getSharedPreferences(KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);
        defaultValueSp.edit().putBoolean(KEY_HAS_SET_DEFAULT_VALUES, false).commit();


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
    public void testBackgroundDaylightTheme() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        launchActivity();

        assertFalse(isDarkTheme());
        onView(withId(R.id.sliding_layout)).check(matches(withBackgroundColor(android.R.color.white, getActivity())));
    }

    @Test
    public void testOledAutoMode() {
        launchActivity();

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        openSettings();
        changeAppTheme(R.string.pref_display_apptheme_auto);
        sleep();
        switchOled();
        navigateUp();
        assertFalse(isDarkTheme());

        sleep();

        //onView(withId(R.id.sliding_layout)).check(ViewAssertions.matches(CustomMatchers.withBackgroundColor(android.R.color.white, getActivity())));
        assertEquals(ThemeChooser.THEME.LIGHT, getPrivateField("mSelectedTheme"));
    }

    @Test
    public void testLightTheme() {
        launchActivity();

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        openSettings();

        changeAppTheme(R.string.pref_display_apptheme_light);
        navigateUp();

        boolean isDarkTheme = isDarkTheme();
        assertFalse(ThemeChooser.getInstance(getActivity()).isOledMode(getActivity(), false));
        assertFalse(isDarkTheme);
        assertEquals(ThemeChooser.THEME.LIGHT, getPrivateField("mSelectedTheme"));
        //sleep();
    }

    @Test
    public void testDarkTheme() {
        launchActivity();

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        openSettings();

        changeAppTheme(R.string.pref_display_apptheme_dark);
        navigateUp();
        sleep();
        boolean isDarkTheme = isDarkTheme();
        assertThat(ThemeChooser.getInstance(getActivity()).isOledMode(getActivity(), false), equalTo(false));
        assertThat(isDarkTheme, equalTo(true));
        assertEquals(ThemeChooser.THEME.DARK, getPrivateField("mSelectedTheme"));
        //sleep();
    }

    @Test
    public void testDarkOledTheme() {
        launchActivity();

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        openSettings();

        changeAppTheme(R.string.pref_display_apptheme_dark);
        switchOled();
        navigateUp();
        sleep();
        boolean isDarkTheme = isDarkTheme();
        assertTrue(ThemeChooser.getInstance(getActivity()).isOledMode(getActivity(), false));
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
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
    }

    private void openSettings() {
        onView(withText(getActivity().getString(R.string.action_settings))).perform(click());
    }

    private void changeAppTheme(int appThemeText) {
        String title = getActivity().getString(R.string.pref_title_app_theme);
        onData(allOf(
                is(instanceOf(Preference.class)),
                withKey("sp_app_theme"),
                withTitle(R.string.pref_title_app_theme)))
                .onChildView(withText(title))
                .check(matches(isCompletelyDisplayed()))
                .perform(click());

        onView(withText(getActivity().getString(appThemeText)))
                .perform(click());
    }

    private void switchOled() {
        String title = getActivity().getString(R.string.pref_oled_mode);
        onData(allOf(
                is(instanceOf(Preference.class)),
                withKey("cb_oled_mode"),
                withSummary(R.string.pref_oled_mode_summary),
                withTitle(R.string.pref_oled_mode)))
                .onChildView(withText(title))
                .check(matches(isCompletelyDisplayed()))
                .perform(click());
    }




    private void launchActivity() {
        mActivityRule.launchActivity(new Intent());

        /*
        NewsReaderApplication nra = (NewsReaderApplication) getActivity().getApplication();
        AppComponent appComponent = DaggerAppComponent.builder()
                .apiModule(new TestApiModule(nra))
                .build();
        nra.setAppComponent(appComponent);
        */

        sleep();

        //assertFalse(ThemeChooser.getInstance(getActivity()).isDarkTheme(getActivity()));
        //assertFalse(ThemeChooser.getInstance(getActivity()).isOledMode(getActivity(), true));
    }

    private boolean isDarkTheme() {
        ThemeChooser themeChooser = ThemeChooser.getInstance(getActivity());

        try {
            Method method = ThemeChooser.class.getDeclaredMethod("isDarkTheme", Context.class);
            method.setAccessible(true);
            boolean isDarkTheme = (boolean) method.invoke(themeChooser, getActivity());
            return isDarkTheme;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail(e.toString() + " - " + e.getMessage());
        }
        return false;
    }

    private Object getPrivateField(String fieldName) {
        ThemeChooser themeChooser = ThemeChooser.getInstance(getActivity());

        try {
            Field[] fields = ThemeChooser.class.getDeclaredFields();
            for (Field field : fields) {
                if(fieldName.equals(field.getName())) {
                    field.setAccessible(true);
                    return field.get(themeChooser);
                }
            }
        } catch (IllegalAccessException e) {
            fail(e.getMessage());
        }
        return null;
    }

}