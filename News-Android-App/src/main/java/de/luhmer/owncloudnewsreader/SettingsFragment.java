package de.luhmer.owncloudnewsreader;

import static de.luhmer.owncloudnewsreader.Constants.USER_INFO_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_MARK_AS_READ_WHILE_SCROLLING_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_NAVIGATE_WITH_VOLUME_BUTTONS_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_OLED_MODE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_PREF_BACK_OPENS_DRAWER;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_REPORT_ISSUE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_SHOWONLYUNREAD_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_SHOW_FAST_ACTIONS;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_SYNCONSTARTUP_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_VERSION;
import static de.luhmer.owncloudnewsreader.SettingsActivity.EDT_CLEAR_CACHE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.EDT_PASSWORD_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.EDT_USERNAME_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.LV_CACHE_IMAGES_OFFLINE_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.PREF_SYNC_SETTINGS;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_APP_THEME;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_DISPLAY_BROWSER;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_FEED_LIST_LAYOUT;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_FONT_SIZE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_MAX_CACHE_SIZE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_SEARCH_IN;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_SORT_ORDER;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_SWIPE_LEFT_ACTION;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_SWIPE_RIGHT_ACTION;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SYNC_INTERVAL_IN_MINUTES_STRING_DEPRECATED;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.NewsFileUtils;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;

public class SettingsFragment extends PreferenceFragmentCompat {

    protected @Inject SharedPreferences mPrefs;
    protected @Inject @Named("sharedPreferencesFileName") String sharedPreferencesFileName;
    private static String version = "<loading>";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        ((NewsReaderApplication) requireActivity().getApplication()).getAppComponent().injectFragment(this);

        // Define the settings file to use by this settings fragment
        getPreferenceManager().setSharedPreferencesName(sharedPreferencesFileName);

        version = VersionInfoDialogFragment.getVersionString(getActivity());

        migrateSyncIntervalValue(); // migrates pref SYNC_INTERVAL_IN_MINUTES_STRING to pref_sync_settings

        addPreferencesFromResource(R.xml.pref_general);
        bindGeneralPreferences(this);

        addPreferencesFromResource(R.xml.pref_display);
        bindDisplayPreferences(this);

        addPreferencesFromResource(R.xml.pref_data_sync);
        bindDataSyncPreferences(this);

        addPreferencesFromResource(R.xml.pref_about);
        bindAboutPreferences(this);

        //addPreferencesFromResource(R.xml.pref_podcast);
        //bindPodcastPreferences(this);


        /*
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
        */
    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference listPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            // only enable black-bg setting if light or auto theme is selected
            if(SP_APP_THEME.equals(preference.getKey())) {
                // value "1" means Light theme
                preference.getPreferenceManager().findPreference(CB_OLED_MODE).setEnabled(!value.equals("1"));
            }
            else if(PREF_SYNC_SETTINGS.equals(preference.getKey())) {
                // set the sync value in account
                setAccountSyncInterval(preference.getContext(), Integer.parseInt(stringValue));
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

    private static final Preference.OnPreferenceChangeListener sBindPreferenceBooleanToValueListener = (preference, newValue) -> {
        if(preference instanceof CheckBoxPreference cbPreference) { //For legacy Android support
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
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                mPrefs.getString(preference.getKey(),
                        ""));
    }

    private void bindPreferenceBooleanToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceBooleanToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceBooleanToValueListener.onPreferenceChange(
                preference,
                mPrefs.getBoolean(preference.getKey(), false));
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

        return null;
    }
    */


    private void bindDisplayPreferences(PreferenceFragmentCompat prefFrag)
    {
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_APP_THEME));
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_OLED_MODE));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_FEED_LIST_LAYOUT));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_FONT_SIZE));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_DISPLAY_BROWSER));
    }

    private void bindGeneralPreferences(DialogPreference.TargetFragment prefFrag)
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
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_SHOW_FAST_ACTIONS));
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING));
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_PREF_BACK_OPENS_DRAWER));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_SORT_ORDER));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_SEARCH_IN));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_SWIPE_RIGHT_ACTION));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_SWIPE_LEFT_ACTION));
    }

    /**
     * migrates pref SYNC_INTERVAL_IN_MINUTES_STRING to pref_sync_settings
     * temporary function, could be removed whenever is wished
     */
    private void migrateSyncIntervalValue() {
        // For migration compatibility, in case preference SYNC_INTERVAL_IN_MINUTES_STRING is there
        // we migrate its value in PREF_SYNC_SETTINGS
        int minutes = mPrefs.getInt(SYNC_INTERVAL_IN_MINUTES_STRING_DEPRECATED, -1);
        if (minutes != -1) { // we need to migrate
            mPrefs.edit().putString(PREF_SYNC_SETTINGS, String.valueOf(minutes)).commit();
            mPrefs.edit().remove(SYNC_INTERVAL_IN_MINUTES_STRING_DEPRECATED).commit();
        }
        // impact if the above code is removed:
        //   the list will show the default sync interval value of 15min
        //   whereas the user may have configured some other value
        //   once the user selects a value, this new value is actually used; and no more impact is expected

    }

    private void bindDataSyncPreferences(final PreferenceFragmentCompat prefFrag)
    {

        // handle the sync interval list:
        bindPreferenceSummaryToValue(prefFrag.findPreference(PREF_SYNC_SETTINGS));

        // String[] authorities = { "de.luhmer.owncloudnewsreader" };
        // Intent intentSyncSettings = new Intent(Settings.ACTION_SYNC_SETTINGS);
        // intentSyncSettings.putExtra(Settings.EXTRA_AUTHORITIES, authorities);

        // String[] authorities = { "de.luhmer.owncloudnewsreader" };
        // Intent intentSyncSettings = new Intent(Settings.ACTION_SYNC_SETTINGS);
        // intentSyncSettings.putExtra(Settings.EXTRA_AUTHORITIES, authorities);

        //bindPreferenceSummaryToValue(prefFrag.findPreference(SP_MAX_ITEMS_SYNC));
        Preference clearCachePref = prefFrag.findPreference(EDT_CLEAR_CACHE);
        bindPreferenceSummaryToValue(prefFrag.findPreference(LV_CACHE_IMAGES_OFFLINE_STRING));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_MAX_CACHE_SIZE));


        clearCachePref.setOnPreferenceClickListener(preference -> {
            mPrefs.edit().remove(USER_INFO_STRING).apply();
            checkForUnsycedChangesInDatabaseAndResetDatabase(prefFrag.getActivity());
            return true;
        });
    }

    private void bindAboutPreferences(final PreferenceFragmentCompat prefFrag) {
        prefFrag.findPreference(CB_VERSION).setSummary(version);
        Preference changelogPreference = prefFrag.findPreference(CB_VERSION);
        changelogPreference.setOnPreferenceClickListener(preference -> {
            DialogFragment dialog = new VersionInfoDialogFragment();
            dialog.show(prefFrag.requireActivity().getSupportFragmentManager(), "VersionChangelogDialogFragment");
            return true;
        });

        findPreference(CB_REPORT_ISSUE).setOnPreferenceClickListener(preference -> {
            openBugReport();
            return true;
        });

    }


    private void bindPodcastPreferences(PreferenceFragmentCompat prefFrag)
    {
        //bindPreferenceBooleanToValue(prefFrag.findPreference(CB_ENABLE_PODCASTS_STRING));
    }


    public void checkForUnsycedChangesInDatabaseAndResetDatabase(final Context context) {
        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
        boolean resetDatabase = !dbConn.areThereAnyUnsavedChangesInDatabase();

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

    private void openBugReport() {
        String title = "";
        String body = "";
        StringBuilder debugInfo = new StringBuilder("Please describe your bug here...\n\n---\n");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
                debugInfo.append("\nApp Version: ").append(pInfo.versionName);
                debugInfo.append("\nApp Version Code: ").append(pInfo.versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            debugInfo.append("\n\n---\n");

            debugInfo.append("\nSSO enabled: ").append(mPrefs.getBoolean(SettingsActivity.SW_USE_SINGLE_SIGN_ON, false));


            debugInfo.append("\n\n---\n");
            debugInfo.append("\nOS Version: ").append(System.getProperty("os.version")).append("(").append(Build.VERSION.INCREMENTAL).append(")");
            debugInfo.append("\nOS API Level: ").append(Build.VERSION.SDK_INT);
            debugInfo.append("\nDevice: ").append(Build.DEVICE);
            debugInfo.append("\nModel (and Product): ").append(Build.MODEL).append(" (").append(Build.PRODUCT).append(")");

            debugInfo.append("\n\n---\n\n");

            List<String> excludedSettings = Arrays.asList(EDT_USERNAME_STRING, EDT_PASSWORD_STRING, EDT_OWNCLOUDROOTPATH_STRING, Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, USER_INFO_STRING);
            Map<String, ?> allEntries = mPrefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                if (!excludedSettings.contains(key)) {
                    debugInfo.append(entry).append("\n");
                }
            }

            body = URLEncoder.encode(debugInfo.toString(), StandardCharsets.UTF_8);
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nextcloud/news-android/issues/new?title=" + title + "&body=" + body));
        startActivity(browserIntent);
    }


    public static final long SECONDS_PER_MINUTE = 60L;

    public static void setAccountSyncInterval(Context context, int minutes) {
        AccountManager mAccountManager = AccountManager.get(context);
        String accountType = AccountGeneral.getAccountType(context);
        Account[] accounts = mAccountManager.getAccountsByType(accountType);
        for (Account account : accounts) {
            if (minutes != 0) {
                long SYNC_INTERVAL = minutes * SECONDS_PER_MINUTE;
                ContentResolver.setSyncAutomatically(account, accountType, true);

                Bundle bundle = new Bundle();
                ContentResolver.addPeriodicSync(
                        account,
                        accountType,
                        bundle,
                        SYNC_INTERVAL);

            } else {
                ContentResolver.setSyncAutomatically(account, accountType, false);
            }
        }
    }

    public static class ResetDatabaseAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pd;
        private final Context context;

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
            //Thread.sleep(1000);

            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
            dbConn.resetDatabase();
            NewsFileUtils.clearWebArchiveCache(context);
            NewsFileUtils.clearPodcastCache(context);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // needs to be executed on main thread
            ImageHandler.clearCache(context);

            pd.dismiss();
            Toast.makeText(context, context.getString(R.string.cache_is_cleared), Toast.LENGTH_SHORT).show();

            if(context instanceof SettingsActivity sa) {
                sa.resultIntent.putExtra(SettingsActivity.RI_CACHE_CLEARED, true);
            }
        }
    }
}
