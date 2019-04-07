/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.owncloudnewsreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.ContactsContract;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import org.apache.velocity.util.ArrayListWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.preference.CheckBoxPreference;
import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.helper.AppCompatPreferenceActivity;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.NewsFileUtils;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;

/**
* A {@link PreferenceActivity} that presents a set of application settings. On
* handset devices, settings are presented as a single list. On tablets,
* settings are split by category, with category headers shown to the left of
* the list of settings.
* <p>
* See <a href="http://developer.android.com/design/patterns/settings.html">
* Android Design: Settings</a> for design guidelines and the <a
* href="http://developer.android.com/guide/topics/ui/settings.html">Settings
* API Guide</a> for more information on developing a Settings UI.
*/
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getCanonicalName();

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    public static final String EDT_USERNAME_STRING = "edt_username";
    public static final String EDT_PASSWORD_STRING = "edt_password";
    public static final String EDT_OWNCLOUDROOTPATH_STRING = "edt_owncloudRootPath";
    public static final String SW_USE_SINGLE_SIGN_ON = "sw_use_single_sign_on";
    public static final String EDT_CLEAR_CACHE = "edt_clearCache";

    //public static final String CB_ALLOWALLSSLCERTIFICATES_STRING = "cb_AllowAllSSLCertificates";
    public static final String CB_SYNCONSTARTUP_STRING = "cb_AutoSyncOnStart";
    public static final String CB_SHOWONLYUNREAD_STRING = "cb_ShowOnlyUnread";
    public static final String CB_NAVIGATE_WITH_VOLUME_BUTTONS_STRING = "cb_NavigateWithVolumeButtons";

    public static final String LV_CACHE_IMAGES_OFFLINE_STRING = "lv_cacheImagesOffline";

    public static final String CB_MARK_AS_READ_WHILE_SCROLLING_STRING = "cb_MarkAsReadWhileScrolling";
    public static final String CB_DISABLE_HOSTNAME_VERIFICATION_STRING = "cb_DisableHostnameVerification";
    public static final String CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING = "cb_openInBrowserDirectly";
    public static final String CB_SHOW_NOTIFICATION_NEW_ARTICLES_STRING = "cb_showNotificationNewArticles";

    //public static final String CB_ENABLE_PODCASTS_STRING = "cb_enablePodcasts";

    public static final String PREF_SYNC_SETTINGS = "pref_sync_settings";

    public static final String SP_APP_THEME = "sp_app_theme";
    public static final String CB_OLED_MODE = "cb_oled_mode";

    public static final String SP_FEED_LIST_LAYOUT = "sp_feed_list_layout";
    public static final String SP_FONT_SIZE = "sp_font_size";

    public static final String CACHE_CLEARED = "CACHE_CLEARED";
    public static final String SP_MAX_CACHE_SIZE = "sp_max_cache_size";
    public static final String SP_SORT_ORDER = "sp_sort_order";
    public static final String SP_DISPLAY_BROWSER = "sp_display_browser";
    public static final String SP_SEARCH_IN = "sp_search_in";

    public static final String CB_VERSION = "cb_version";

    private static EditTextPreference clearCachePref;
    private static String version = "<loading>";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.getInstance(this).chooseTheme(this);
        super.onCreate(savedInstanceState);
        ThemeChooser.getInstance(this).afterOnCreate(this);

        setContentView(R.layout.activity_settings);

        version = VersionInfoDialogFragment.getVersionString(this);

        setupActionBar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new PhonePreferenceFragment())
                .commit();
    }


    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_settings);
    }



    /**
     * This fragment shows all preferences for phones.
     */
    public static class PhonePreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            // In the simplified UI, fragments are not used at all and we instead
            // use the older PreferenceActivity APIs.

            // This is to initialize the settings panel to allow adding a first-section header (below)
            // without running into stupid exceptions (otherwise getPreferenceScreen() will return null)
            addPreferencesFromResource(R.xml.pref_empty);

            // Add 'general' preferences.
            PreferenceCategory header = new PreferenceCategory(getContext());
            header.setTitle(R.string.pref_header_general);
            getPreferenceScreen().addPreference(header);
            addPreferencesFromResource(R.xml.pref_general);

            //header = new PreferenceCategory(getContext());
            //header.setTitle(R.string.pref_header_display);
            //getPreferenceScreen().addPreference(header);
            addPreferencesFromResource(R.xml.pref_display);


            //header = new PreferenceCategory(getContext());
            //header.setTitle(R.string.pref_header_data_sync);
            //getPreferenceScreen().addPreference(header);
            addPreferencesFromResource(R.xml.pref_data_sync);

            //header = new PreferenceCategory(getContext());
            //header.setTitle(R.string.pref_header_notifications);
            //getPreferenceScreen().addPreference(header);
            addPreferencesFromResource(R.xml.pref_notification);

            //header = new PreferenceCategory(getContext());
            //header.setTitle(R.string.pref_header_about);
            //getPreferenceScreen().addPreference(header);
            addPreferencesFromResource(R.xml.pref_about);

            //header = new PreferenceCategory(this);
            //header.setTitle(R.string.pref_header_podcast);
            //getPreferenceScreen().addPreference(header);
            //addPreferencesFromResource(R.xml.pref_podcast);

            bindGeneralPreferences(this);
            bindDisplayPreferences(this);
            bindDataSyncPreferences(this);
            bindNotificationPreferences(this);
            bindAboutPreferences(this, getActivity());
            //bindPodcastPreferences(this);
        }
    }


    /* (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockPreferenceActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                //NavUtils.navigateUpTo(this, new Intent(this,
                //		NewsReaderListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        intent.putExtra(
                SettingsActivity.SP_FEED_LIST_LAYOUT,
                PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0")
        );
        setResult(RESULT_OK,intent);
    }


    ///** {@inheritDoc} */
    //@Override
    public boolean onIsMultiPane() {
        return this.getResources().getBoolean(R.bool.isTablet);
    }



    /*
    @Override
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        super.onBuildHeaders(target);
        if (onIsMultiPane()) {
            loadHeadersFromResource(R.xml.pref_headers, target);

            // Fix settings page header ("breadcrumb") text color for dark mode
            // Thank you Stackoverflow: https://stackoverflow.com/a/27078485
            final View breadcrumb = findViewById(android.R.id.title);
            if (breadcrumb == null) {
                // Single pane layout
                return;
            }
            try {
                final Field titleColor = breadcrumb.getClass().getDeclaredField("mTextColor");
                titleColor.setAccessible(true);
                titleColor.setInt(breadcrumb, ContextCompat.getColor(this, R.color.primaryTextColor));
            } catch (final Exception e) {
                Log.e(TAG, "onBuildHeaders failed", e);
            }
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
    */


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            // only enable black-bg setting if light or auto theme is selected
            if(SP_APP_THEME.equals(preference.getKey())) {
                if (value.equals("1")) 	// value "1" means Light theme
                    preference.getPreferenceManager().findPreference(CB_OLED_MODE).setEnabled(false);
                else
                    preference.getPreferenceManager().findPreference(CB_OLED_MODE).setEnabled(true);
            }

        } else {
            String key = preference.getKey();
            // For all other preferences, set the summary to the value's
            // simple string representation.
            if(key.equals(EDT_PASSWORD_STRING))
                preference.setSummary(null);
            else
                preference.setSummary(stringValue);
        }
        return true;
    };

    private static Preference.OnPreferenceChangeListener sBindPreferenceBooleanToValueListener = (preference, newValue) -> {
        if(preference instanceof CheckBoxPreference) { //For legacy Android support
            CheckBoxPreference cbPreference = ((CheckBoxPreference) preference);
            cbPreference.setChecked((Boolean) newValue);
        } else {
            TwoStatePreference twoStatePreference = ((TwoStatePreference) preference);
            twoStatePreference.setChecked((Boolean) newValue);
        }
        return true;
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getString(preference.getKey(),
                        ""));
    }

    private static void bindPreferenceBooleanToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceBooleanToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceBooleanToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getBoolean(preference.getKey(), false));
    }


    // TODO DO WE NEED THE CODE BELOW?!!
    /*
    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // Allow super to try and create a view first
        final View result = super.onCreateView(name, context, attrs);
        if (result != null) {
            return result;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // If we're running pre-L, we need to 'inject' our tint aware Views in place of the
            // standard framework versions
            switch (name) {
                case "EditText":
                    return new AppCompatEditText(this, attrs);
                case "Spinner":
                    return new AppCompatSpinner(this, attrs);
                case "CheckBox":
                    return new AppCompatCheckBox(this, attrs);
                case "RadioButton":
                    return new AppCompatRadioButton(this, attrs);
                case "CheckedTextView":
                    return new AppCompatCheckedTextView(this, attrs);
                default:
                    Log.v(TAG, "Error. Didn't find view of type: " + name);
            }
        }
        return null;
    }
    */

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class EmptyPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_empty, rootKey);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_general, rootKey);
            bindGeneralPreferences(this);
        }
    }

    /**
     * This fragment shows podcast preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    /*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PodcastPreferenceFragment extends PreferenceFragmentCompar {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_podcast);
            bindPodcastPreferences(this);
        }
    }
    */


    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class NotificationPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_notification, rootKey);
            bindNotificationPreferences(this);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class DataSyncPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_data_sync, rootKey);

            bindDataSyncPreferences(this);
        }
    }


    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class DisplayPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_display, rootKey);
            bindDisplayPreferences(this);
        }
    }


    /**
     * This fragment shows about preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class AboutPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_about, rootKey);
            bindAboutPreferences(this, getActivity());
        }
    }



    @SuppressWarnings("deprecation")
    private static void bindDisplayPreferences(DialogPreference.TargetFragment prefFrag)
    {
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_APP_THEME));
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_OLED_MODE));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_FEED_LIST_LAYOUT));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_FONT_SIZE));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_DISPLAY_BROWSER));
    }

    @SuppressWarnings("deprecation")
    private static void bindGeneralPreferences(DialogPreference.TargetFragment prefFrag)
    {
        /*
        bindPreferenceSummaryToValue(prefFrag.findPreference(EDT_USERNAME_STRING));
        bindPreferenceSummaryToValue(prefFrag.findPreference(EDT_PASSWORD_STRING));
        bindPreferenceSummaryToValue(prefFrag.findPreference(EDT_OWNCLOUDROOTPATH_STRING));
         */
        //bindPreferenceBooleanToValue(prefFrag.findPreference(CB_ALLOWALLSSLCERTIFICATES_STRING));
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_SYNCONSTARTUP_STRING));
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_SHOWONLYUNREAD_STRING));
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_NAVIGATE_WITH_VOLUME_BUTTONS_STRING));
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_MARK_AS_READ_WHILE_SCROLLING_STRING));
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_SORT_ORDER));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_SEARCH_IN));
    }

    @SuppressWarnings("deprecation")
    private static void bindDataSyncPreferences(final DialogPreference.TargetFragment prefFrag)
    {
        String[] authorities = { "de.luhmer.owncloudnewsreader" };
        Intent intentSyncSettings = new Intent(Settings.ACTION_SYNC_SETTINGS);
        intentSyncSettings.putExtra(Settings.EXTRA_AUTHORITIES, authorities);


        prefFrag.findPreference(PREF_SYNC_SETTINGS).setIntent(intentSyncSettings);
        //bindPreferenceSummaryToValue(prefFrag.findPreference(SP_MAX_ITEMS_SYNC));
        clearCachePref = prefFrag.findPreference(EDT_CLEAR_CACHE);
        bindPreferenceSummaryToValue(prefFrag.findPreference(LV_CACHE_IMAGES_OFFLINE_STRING));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_MAX_CACHE_SIZE));


        clearCachePref.setOnPreferenceClickListener(preference -> {
            //((EditTextPreference) preference).getDialog().dismiss(); // TODO we need this line again!!!!!!!!!!!!

            //mPrefs. // TODO INJECT MPREFS HERE !!!!!! FIX LINE BELOW!!
            //PreferenceManager.getDefaultSharedPreferences(prefFrag.getActivity()).edit().remove("USER_INFO").apply();
            checkForUnsycedChangesInDatabaseAndResetDatabase(_mActivity);
            return false;
        });
    }


    private static void bindNotificationPreferences(DialogPreference.TargetFragment prefFrag)
    {
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_SHOW_NOTIFICATION_NEW_ARTICLES_STRING));
    }


    private static void bindAboutPreferences(final DialogPreference.TargetFragment prefFrag, Activity activity) {
        prefFrag.findPreference(CB_VERSION).setSummary(version);
        Preference changelogPreference = prefFrag.findPreference(CB_VERSION);

        changelogPreference.setOnPreferenceClickListener(preference -> {
            DialogFragment dialog = new VersionInfoDialogFragment();
            dialog.show(activity.getFragmentManager(), "VersionChangelogDialogFragment");
            return true;
        });
    }


    private static void bindPodcastPreferences(DialogPreference.TargetFragment prefFrag)
    {
        //bindPreferenceBooleanToValue(prefFrag.findPreference(CB_ENABLE_PODCASTS_STRING));
    }


    public static void checkForUnsycedChangesInDatabaseAndResetDatabase(final Context context) {
        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
        boolean resetDatabase = true;
        if(dbConn.areThereAnyUnsavedChangesInDatabase())
            resetDatabase = false;

        if(resetDatabase) {
            new ResetDatabaseAsyncTask(context).execute();
        } else {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.warning))
                    .setMessage(context.getString(R.string.reset_cache_unsaved_changes))
                    .setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PostDelayHandler pDelayHandler = new PostDelayHandler(context);
                            pDelayHandler.stopRunningPostDelayHandler();

                            new ResetDatabaseAsyncTask(context).execute();
                        }
                    })
                    .setNegativeButton(context.getString(android.R.string.no), null)
                    .create()
                    .show();
        }
    }

    public static class ResetDatabaseAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pd;
        private Context context;

        public ResetDatabaseAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(context);
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.setTitle(context.getString(R.string.dialog_clearing_cache));
            pd.setMessage(context.getString(R.string.dialog_clearing_cache_please_wait));
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
            dbConn.resetDatabase();
            ImageHandler.clearCache();
            NewsFileUtils.clearWebArchiveCache(context);
            NewsFileUtils.clearPodcastCache(context);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if(context instanceof Activity) {
                Intent intent = ((Activity) context).getIntent();
                intent.putExtra(SettingsActivity.CACHE_CLEARED, true);
                ((Activity) context).setResult(RESULT_OK,intent);
            }

            pd.dismiss();
            Toast.makeText(context, context.getString(R.string.cache_is_cleared), Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
        }
    }
}
