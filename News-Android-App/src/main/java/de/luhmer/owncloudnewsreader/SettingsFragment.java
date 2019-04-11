package de.luhmer.owncloudnewsreader;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.NewsFileUtils;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;

import static android.app.Activity.RESULT_OK;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_MARK_AS_READ_WHILE_SCROLLING_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_NAVIGATE_WITH_VOLUME_BUTTONS_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_OLED_MODE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_SHOWONLYUNREAD_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_SHOW_NOTIFICATION_NEW_ARTICLES_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_SYNCONSTARTUP_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.CB_VERSION;
import static de.luhmer.owncloudnewsreader.SettingsActivity.EDT_CLEAR_CACHE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.EDT_PASSWORD_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.LV_CACHE_IMAGES_OFFLINE_STRING;
import static de.luhmer.owncloudnewsreader.SettingsActivity.PREF_SYNC_SETTINGS;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_APP_THEME;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_DISPLAY_BROWSER;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_FEED_LIST_LAYOUT;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_FONT_SIZE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_MAX_CACHE_SIZE;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_SEARCH_IN;
import static de.luhmer.owncloudnewsreader.SettingsActivity.SP_SORT_ORDER;

public class SettingsFragment extends PreferenceFragmentCompat {

    protected @Inject SharedPreferences mPrefs;
    protected @Inject @Named("sharedPreferencesFileName") String sharedPreferencesFileName;
    private static String version = "<loading>";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        ((NewsReaderApplication) getActivity().getApplication()).getAppComponent().injectFragment(this);

        // Define the settings file to use by this settings fragment
        getPreferenceManager().setSharedPreferencesName(sharedPreferencesFileName);

        version = VersionInfoDialogFragment.getVersionString(getActivity());

        addPreferencesFromResource(R.xml.pref_general);
        bindGeneralPreferences(this);

        addPreferencesFromResource(R.xml.pref_display);
        bindDisplayPreferences(this);

        addPreferencesFromResource(R.xml.pref_data_sync);
        bindDataSyncPreferences(this);

        addPreferencesFromResource(R.xml.pref_notification);
        bindNotificationPreferences(this);

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
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_SORT_ORDER));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_SEARCH_IN));
    }

    private void bindDataSyncPreferences(final PreferenceFragmentCompat prefFrag)
    {
        String[] authorities = { "de.luhmer.owncloudnewsreader" };
        Intent intentSyncSettings = new Intent(Settings.ACTION_SYNC_SETTINGS);
        intentSyncSettings.putExtra(Settings.EXTRA_AUTHORITIES, authorities);


        prefFrag.findPreference(PREF_SYNC_SETTINGS).setIntent(intentSyncSettings);
        //bindPreferenceSummaryToValue(prefFrag.findPreference(SP_MAX_ITEMS_SYNC));
        Preference clearCachePref = prefFrag.findPreference(EDT_CLEAR_CACHE);
        bindPreferenceSummaryToValue(prefFrag.findPreference(LV_CACHE_IMAGES_OFFLINE_STRING));
        bindPreferenceSummaryToValue(prefFrag.findPreference(SP_MAX_CACHE_SIZE));


        clearCachePref.setOnPreferenceClickListener(preference -> {
            mPrefs.edit().remove("USER_INFO").apply();
            checkForUnsycedChangesInDatabaseAndResetDatabase(prefFrag.getActivity());
            return true;
        });
    }


    private void bindNotificationPreferences(PreferenceFragmentCompat prefFrag)
    {
        bindPreferenceBooleanToValue(prefFrag.findPreference(CB_SHOW_NOTIFICATION_NEW_ARTICLES_STRING));
    }


    private void bindAboutPreferences(final PreferenceFragmentCompat prefFrag) {
        prefFrag.findPreference(CB_VERSION).setSummary(version);
        Preference changelogPreference = prefFrag.findPreference(CB_VERSION);

        changelogPreference.setOnPreferenceClickListener(preference -> {
            DialogFragment dialog = new VersionInfoDialogFragment();
            dialog.show(prefFrag.getActivity().getFragmentManager(), "VersionChangelogDialogFragment");
            return true;
        });
    }


    private void bindPodcastPreferences(PreferenceFragmentCompat prefFrag)
    {
        //bindPreferenceBooleanToValue(prefFrag.findPreference(CB_ENABLE_PODCASTS_STRING));
    }


    public void checkForUnsycedChangesInDatabaseAndResetDatabase(final Context context) {
        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
        boolean resetDatabase = true;
        if(dbConn.areThereAnyUnsavedChangesInDatabase()) {
            resetDatabase = false;
        }

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
            //Thread.sleep(1000);

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
                ((Activity) context).setResult(RESULT_OK, intent);
            }

            pd.dismiss();
            Toast.makeText(context, context.getString(R.string.cache_is_cleared), Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
        }
    }
}
