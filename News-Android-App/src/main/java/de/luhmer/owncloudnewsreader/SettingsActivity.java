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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.MenuItem;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.helper.FileUtils;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
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
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	public static final String EDT_USERNAME_STRING = "edt_username";
	public static final String EDT_PASSWORD_STRING = "edt_password";
	public static final String EDT_OWNCLOUDROOTPATH_STRING = "edt_owncloudRootPath";
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
    public static final String SP_FEED_LIST_LAYOUT = "sp_feed_list_layout";
    public static final String SP_MAX_CACHE_SIZE = "sp_max_cache_size";
    public static final String SP_FONT = "sp_font";
    public static final String SP_SORT_ORDER = "sp_sort_order";


    static //public static final String PREF_SIGN_IN_DIALOG = "sPref_signInDialog";


    //public static final String SP_MAX_ITEMS_SYNC = "sync_max_items";

    EditTextPreference clearCachePref;
    static Activity _mActivity;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeChooser.chooseTheme(this);

		super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		_mActivity = this;

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		PreferenceCategory header = new PreferenceCategory(this);
		header.setTitle(R.string.pref_header_display);
		getPreferenceScreen().addPreference(header);
		addPreferencesFromResource(R.xml.pref_display);


		header = new PreferenceCategory(this);
		header.setTitle(R.string.pref_header_data_sync);
		getPreferenceScreen().addPreference(header);
		addPreferencesFromResource(R.xml.pref_data_sync);

        header = new PreferenceCategory(this);
        header.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(header);
        addPreferencesFromResource(R.xml.pref_notification);

        header = new PreferenceCategory(this);
        header.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(header);
        addPreferencesFromResource(R.xml.pref_podcast);

		bindGeneralPreferences(null, this);
		bindDisplayPreferences(null, this);
		bindDataSyncPreferences(null, this);
        bindNotificationPreferences(null, this);
        //bindPodcastPreferences(null, this);
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

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	@SuppressLint("InlinedApi")
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);
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
		}
	};

    private static Preference.OnPreferenceChangeListener sBindPreferenceBooleanToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            CheckBoxPreference cbPreference = ((CheckBoxPreference) preference);
            cbPreference.setChecked((Boolean)newValue);
            return true;
        }
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

    /**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);

			bindGeneralPreferences(this, null);
		}
	}

    /**
     * This fragment shows podcast preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    /*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PodcastPreferenceFragment extends
            PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_podcast);

            bindPodcastPreferences(this, null);
        }
    }
    */


	/**
	 * This fragment shows notification preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends
			PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notification);

            bindNotificationPreferences(this, null);
		}
	}

	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DataSyncPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_data_sync);

			bindDataSyncPreferences(this, null);
		}
	}


	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DisplayPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_display);

			bindDisplayPreferences(this, null);
		}
	}



	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void bindDisplayPreferences(PreferenceFragment prefFrag, PreferenceActivity prefAct)
	{
		if(prefFrag != null)
		{
			bindPreferenceSummaryToValue(prefFrag.findPreference(SP_APP_THEME));
			bindPreferenceSummaryToValue(prefFrag.findPreference(SP_FEED_LIST_LAYOUT));
			bindPreferenceSummaryToValue(prefFrag.findPreference(SP_FONT));
		}
		else
		{
			bindPreferenceSummaryToValue(prefAct.findPreference(SP_APP_THEME));
			bindPreferenceSummaryToValue(prefAct.findPreference(SP_FEED_LIST_LAYOUT));
			bindPreferenceSummaryToValue(prefAct.findPreference(SP_FONT));
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void bindGeneralPreferences(PreferenceFragment prefFrag, final PreferenceActivity prefAct)
	{
		if(prefFrag != null)
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
		}
		else
		{
			/*
			bindPreferenceSummaryToValue(prefAct.findPreference(EDT_USERNAME_STRING));
			bindPreferenceSummaryToValue(prefAct.findPreference(EDT_PASSWORD_STRING));
			bindPreferenceSummaryToValue(prefAct.findPreference(EDT_OWNCLOUDROOTPATH_STRING));
			*/
	        //bindPreferenceBooleanToValue(prefAct.findPreference(CB_ALLOWALLSSLCERTIFICATES_STRING));
	        bindPreferenceBooleanToValue(prefAct.findPreference(CB_SYNCONSTARTUP_STRING));
	        bindPreferenceBooleanToValue(prefAct.findPreference(CB_SHOWONLYUNREAD_STRING));
	        bindPreferenceBooleanToValue(prefAct.findPreference(CB_NAVIGATE_WITH_VOLUME_BUTTONS_STRING));
	        bindPreferenceBooleanToValue(prefAct.findPreference(CB_MARK_AS_READ_WHILE_SCROLLING_STRING));
            bindPreferenceBooleanToValue(prefAct.findPreference(CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING));
	        bindPreferenceSummaryToValue(prefAct.findPreference(SP_SORT_ORDER));
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void bindDataSyncPreferences(PreferenceFragment prefFrag, PreferenceActivity prefAct)
	{
        String[] authorities = { "de.luhmer.owncloudnewsreader" };
        Intent intentSyncSettings = new Intent(Settings.ACTION_SYNC_SETTINGS);
        intentSyncSettings.putExtra(Settings.EXTRA_AUTHORITIES, authorities);

		if(prefFrag != null)
		{
            prefFrag.findPreference(PREF_SYNC_SETTINGS).setIntent(intentSyncSettings);
			//bindPreferenceSummaryToValue(prefFrag.findPreference(SP_MAX_ITEMS_SYNC));
			clearCachePref = (EditTextPreference) prefFrag.findPreference(EDT_CLEAR_CACHE);
			bindPreferenceSummaryToValue(prefFrag.findPreference(LV_CACHE_IMAGES_OFFLINE_STRING));
			bindPreferenceSummaryToValue(prefFrag.findPreference(SP_MAX_CACHE_SIZE));
		}
		else
		{
            prefAct.findPreference(PREF_SYNC_SETTINGS).setIntent(intentSyncSettings);
			//bindPreferenceSummaryToValue(prefAct.findPreference(SP_MAX_ITEMS_SYNC));
			clearCachePref = (EditTextPreference) prefAct.findPreference(EDT_CLEAR_CACHE);
            bindPreferenceSummaryToValue(prefAct.findPreference(LV_CACHE_IMAGES_OFFLINE_STRING));
			bindPreferenceSummaryToValue(prefAct.findPreference(SP_MAX_CACHE_SIZE));

		}

		//clearCache.setText("")
		clearCachePref.setSummary(_mActivity.getString(R.string.calculating_cache_size));

		new GetCacheSizeAsync().execute((Void)null);
		clearCachePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				((EditTextPreference) preference).getDialog().dismiss();

                CheckForUnsycedChangesInDatabaseAndResetDatabase(_mActivity);
				return false;
			}
		});
	}


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void bindNotificationPreferences(PreferenceFragment prefFrag, PreferenceActivity prefAct)
    {
        if(prefFrag != null)
        {
            bindPreferenceBooleanToValue(prefFrag.findPreference(CB_SHOW_NOTIFICATION_NEW_ARTICLES_STRING));
        }
        else
        {
            bindPreferenceBooleanToValue(prefAct.findPreference(CB_SHOW_NOTIFICATION_NEW_ARTICLES_STRING));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void bindPodcastPreferences(PreferenceFragment prefFrag, PreferenceActivity prefAct)
    {
        if(prefFrag != null)
        {
            //bindPreferenceBooleanToValue(prefFrag.findPreference(CB_ENABLE_PODCASTS_STRING));
        }
        else
        {
            //bindPreferenceBooleanToValue(prefAct.findPreference(CB_ENABLE_PODCASTS_STRING));
        }
    }


	public static void CheckForUnsycedChangesInDatabaseAndResetDatabase(final Context context) {
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
				.setPositiveButton(context.getString(android.R.string.ok), new OnClickListener() {

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

    public static class ResetDatabaseAsyncTask extends AsyncTask<Void, Void, Boolean> {

        ProgressDialog pd;
        Context context;

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
        protected Boolean doInBackground(Void... params) {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(_mActivity);
            dbConn.resetDatabase();
            boolean success = ImageHandler.clearCache(_mActivity);
            new GetCacheSizeAsync().execute((Void)null);
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            pd.dismiss();
            if(result)
                LoginDialogFragment.ShowAlertDialog("Information" , "Cache is cleared!", _mActivity);
            else
                LoginDialogFragment.ShowAlertDialog("Information", context.getString(R.string.login_dialog_text_something_went_wrong), _mActivity);
            super.onPostExecute(result);
        };
    }

	public static class GetCacheSizeAsync extends AsyncTask<Void, Void, Void> {

		String mSize = "0MB";
		String mCount = "0 Files";
		int count = 0;
		long size = 0;
		DecimalFormat dcmFormat = new DecimalFormat("#.##");

		@Override
		protected Void doInBackground(Void... params) {
			try
			{
				getFolderSize(new File(FileUtils.getPath(_mActivity)));
				mSize = dcmFormat.format(size / 1024d / 1024d) + "MB";
				mCount = String.valueOf(count) + " Files";
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(clearCachePref != null)
				clearCachePref.setSummary(mCount + " - " + mSize);
			super.onPostExecute(result);
		};

		public long getFolderSize(File dir) {
			if(dir.isDirectory())
			{
				for (File file : dir.listFiles()) {
					//File file = new File(fileS);
				    if (file.isFile()) {
				        //System.out.println(file.getName() + " " + file.length());
				        size += file.length();
				        count++;
				    }
				    else
				        getFolderSize(file);
				}
			}
			return size;
		}
	}
}
