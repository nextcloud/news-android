package de.luhmer.owncloudnewsreader.tests;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;

import static android.preference.PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.PreferenceMatchers.withKey;
import static android.support.test.espresso.matcher.PreferenceMatchers.withSummary;
import static android.support.test.espresso.matcher.PreferenceMatchers.withTitle;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_OLED_MODE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_APP_THEME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

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
    public void initTestData() {

    }

    @Test
    public void testBackgroundDaylightTheme() {
        launchActivity();

        // Type text and then press the button.
        boolean isDarkTheme = ThemeChooser.getInstance(getActivity()).isDarkTheme(getActivity());
        assertThat(isDarkTheme, equalTo(false));
        //onView(withId(R.id.sliding_layout)).check(ViewAssertions.matches(CustomMatchers.withBackgroundColor(android.R.color.white, getActivity())));
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
        boolean isDarkTheme = ThemeChooser.getInstance(getActivity()).isDarkTheme(getActivity());
        assertFalse(isDarkTheme);

        sleep();

        //onView(withId(R.id.sliding_layout)).check(ViewAssertions.matches(CustomMatchers.withBackgroundColor(android.R.color.white, getActivity())));
        assertThat(ThemeChooser.getInstance(getActivity()).OLEDActive, equalTo(false));
        assertThat(ThemeChooser.getInstance(getActivity()).DarkThemeActive, equalTo(false));
    }

    @Test
    public void testLightTheme() {
        launchActivity();

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        openSettings();

        changeAppTheme(R.string.pref_display_apptheme_light);
        navigateUp();

        boolean isDarkTheme = ThemeChooser.getInstance(getActivity()).isDarkTheme(getActivity());
        assertThat(ThemeChooser.getInstance(getActivity()).isOledMode(getActivity(), false), equalTo(false));
        assertThat(isDarkTheme, equalTo(false));
        assertThat(ThemeChooser.getInstance(getActivity()).OLEDActive, equalTo(false));
        assertThat(ThemeChooser.getInstance(getActivity()).DarkThemeActive, equalTo(false));
        sleep();
    }

    @Test
    public void testDarkTheme() {
        launchActivity();

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        openSettings();

        changeAppTheme(R.string.pref_display_apptheme_dark);
        navigateUp();
        sleep();
        boolean isDarkTheme = ThemeChooser.getInstance(getActivity()).isDarkTheme(getActivity());
        assertThat(ThemeChooser.getInstance(getActivity()).isOledMode(getActivity(), false), equalTo(false));
        assertThat(isDarkTheme, equalTo(true));
        assertThat(ThemeChooser.getInstance(getActivity()).OLEDActive, equalTo(false));
        assertThat(ThemeChooser.getInstance(getActivity()).DarkThemeActive, equalTo(true));
        sleep();
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
        boolean isDarkTheme = ThemeChooser.getInstance(getActivity()).isDarkTheme(getActivity());
        assertThat(ThemeChooser.getInstance(getActivity()).isOledMode(getActivity(), false), equalTo(true));
        assertThat(isDarkTheme, equalTo(true));
        assertThat(ThemeChooser.getInstance(getActivity()).OLEDActive, equalTo(true));
        assertThat(ThemeChooser.getInstance(getActivity()).DarkThemeActive, equalTo(true));
        sleep();
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
}