package de.luhmer.owncloudnewsreader;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
import android.preference.RingtonePreference;
import android.text.TextUtils;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;

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
	
    public static final String CB_ALLOWALLSSLCERTIFICATES_STRING = "cb_AllowAllSSLCertificates";
    public static final String CB_SYNCONSTARTUP_STRING = "cb_AutoSyncOnStart";
    public static final String CB_SHOWONLYUNREAD_STRING = "cb_ShowOnlyUnread";
    
    public static final String SP_APP_THEME = "sp_app_theme";
    public static final String SP_FEED_LIST_LAYOUT = "sp_feed_list_layout";
    
    static //public static final String PREF_SIGN_IN_DIALOG = "sPref_signInDialog";
    
    
    //public static final String SP_MAX_ITEMS_SYNC = "sync_max_items";
    
    EditTextPreference clearCachePref;
    static Activity _mActivity;
    
    
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
		

		
		/*
		// Add 'notifications' preferences, and a corresponding header.
		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_notifications);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_notification);

		// Add 'data and sync' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_data_sync);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_data_sync);
        */

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.

		bindGeneralPreferences(null, this);
		bindDisplayPreferences(null, this);
		bindDataSyncPreferences(null, this);
		
		//bindPreferenceSummaryToValue(findPreference("example_list"));
		//bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));//TODO comment this out
		//bindPreferenceSummaryToValue(findPreference("sync_frequency"));//TODO comment this out
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

			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone
								.getTitle(preference.getContext());
						preference.setSummary(name);
					}
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
		}
	};
	
	/*
	private static void ShowInfoDialog(String text)
	{
		// Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(text)
        	.setTitle("Security warning")
        	.setPositiveButton("Ok", null);
        
        // Create the AlertDialog object and return it
        builder.create().show();
	}*/
	

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

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			
			
			bindGeneralPreferences(this, null);
		}
	}
	

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

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			//bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
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

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
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

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
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
		}
		else
		{
			bindPreferenceSummaryToValue(prefAct.findPreference(SP_APP_THEME));
			bindPreferenceSummaryToValue(prefAct.findPreference(SP_FEED_LIST_LAYOUT));
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
		}
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void bindDataSyncPreferences(PreferenceFragment prefFrag, PreferenceActivity prefAct)
	{
		if(prefFrag != null)
		{		
			//bindPreferenceSummaryToValue(prefFrag.findPreference(SP_MAX_ITEMS_SYNC));
			clearCachePref = (EditTextPreference) prefFrag.findPreference(EDT_CLEAR_CACHE);
		}
		else
		{
			//bindPreferenceSummaryToValue(prefAct.findPreference(SP_MAX_ITEMS_SYNC));
			clearCachePref = (EditTextPreference) prefAct.findPreference(EDT_CLEAR_CACHE);
			
		}
		
		//clearCache.setText("")		
		new GetCacheSizeAsync().execute((Void)null);
		clearCachePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				((EditTextPreference) preference).getDialog().dismiss();
				
				DatabaseConnection dbConn = new DatabaseConnection(_mActivity);
				dbConn.resetDatabase();
				dbConn.closeDatabase();
				
				ImageHandler.clearCache(_mActivity);				
				LoginDialogFragment.ShowAlertDialog("Information" , "Cache is cleared!", _mActivity);
				new GetCacheSizeAsync().execute((Void)null);
				return false;
			}
		});
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
				getFolderSize(new File(ImageHandler.getPath(_mActivity)));
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
				    if (file.isFile()) {
				        //System.out.println(file.getName() + " " + file.length());
				        size += file.length();
				        count++;
				    }
				    else
				        size += getFolderSize(file);
				}
			}
			return size;
		}		
	}
}
