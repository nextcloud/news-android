/*
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import javax.inject.Inject;

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
    public static final String CB_SHOW_FAST_ACTIONS = "cb_ShowFastActions";
    public static final String CB_DISABLE_HOSTNAME_VERIFICATION_STRING = "cb_DisableHostnameVerification";
    public static final String CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING = "cb_openInBrowserDirectly";

    //public static final String CB_ENABLE_PODCASTS_STRING = "cb_enablePodcasts";

    public static final String PREF_SERVER_SETTINGS = "pref_server_settings";
    public static final String PREF_SYNC_SETTINGS = "pref_sync_settings";
    public static final String SYNC_INTERVAL_IN_MINUTES_STRING_DEPRECATED = "SYNC_INTERVAL_IN_MINUTES_STRING";

    public static final String SP_APP_THEME = "sp_app_theme";
    public static final String CB_OLED_MODE = "cb_oled_mode";

    public static final String SP_FEED_LIST_LAYOUT = "sp_feed_list_layout"; // used for shared prefs
    public static final String RI_FEED_LIST_LAYOUT = "ai_feed_list_layout"; // used for result intents
    public static final String SP_FONT_SIZE = "sp_font_size";

    public static final String RI_CACHE_CLEARED = "CACHE_CLEARED"; // used for result intents
    public static final String SP_MAX_CACHE_SIZE = "sp_max_cache_size";
    public static final String SP_SORT_ORDER = "sp_sort_order";
    public static final String SP_DISPLAY_BROWSER = "sp_display_browser";
    public static final String SP_SEARCH_IN = "sp_search_in";
    public static final String SP_SWIPE_RIGHT_ACTION = "sp_swipe_right_action";
    public static final String SP_SWIPE_LEFT_ACTION = "sp_swipe_left_action";
    public static final String SP_SWIPE_RIGHT_ACTION_DEFAULT = "1";
    public static final String SP_SWIPE_LEFT_ACTION_DEFAULT = "2";

    public static final String CB_VERSION = "cb_version";
    public static final String CB_REPORT_ISSUE = "cb_reportIssue";

    protected @Inject SharedPreferences mPrefs;

    public Intent resultIntent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((NewsReaderApplication) getApplication()).getAppComponent().injectActivity(this);

        ThemeChooser.chooseTheme(this);
        super.onCreate(savedInstanceState);
        ThemeChooser.afterOnCreate(this);

        setContentView(R.layout.activity_settings);

        setupActionBar();

        // some settings might add a few flags to the result Intent at runtime
        // (e.g. clearing cache / switching list layout / theme / ...)
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_settings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Fix GHSL-2021-1033
        String feedListLayout = mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0");
        resultIntent.putExtra(SettingsActivity.RI_FEED_LIST_LAYOUT, feedListLayout);
    }
}
