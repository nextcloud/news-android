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

package de.luhmer.owncloudnewsreader.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;

public class ThemeChooser {

    private static final String TAG = ThemeChooser.class.getCanonicalName();
    private static ThemeChooser mInstance;

    // Contains the selected theme defined in the settings (used for checking whether the app needs
    // to restart after changing the theme
    private Integer mSelectedThemeFromPreferences;
    private Boolean mOledMode;

    // Contains the current selected theme
    public enum THEME { LIGHT, DARK, OLED }
    private THEME mSelectedTheme = THEME.LIGHT;

    public static ThemeChooser getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new ThemeChooser(context);
        }
        return mInstance;
    }

    private ThemeChooser(Context context) {
        getSelectedThemeFromPreferences(context, false); // Init cache
        isOledMode(context, false); // Init cache
    }

    public void chooseTheme(Activity act) {
        switch(getInstance(act).getSelectedThemeFromPreferences(act, false)) {
            case 0: // Auto (Light / Dark)
                Log.v(TAG, "Auto (Light / Dark)");
                act.setTheme(R.style.AppTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                mSelectedTheme = THEME.LIGHT;
                break;
            case 1: // Light Theme
                Log.v(TAG, "Light");
                act.setTheme(R.style.AppTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                mSelectedTheme = THEME.LIGHT;
                break;
            case 2: // Dark Theme
                Log.v(TAG, "Dark");
                act.setTheme(R.style.AppTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                mSelectedTheme = THEME.DARK;
                break;
            default:
                // This should never happen - just in case.. use the light theme..
                Log.v(TAG, "Default");
                act.setTheme(R.style.AppTheme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                mSelectedTheme = THEME.LIGHT;
                break;
        }
    }

    public void afterOnCreate(Activity act) {
        //int uiNightMode = Configuration.UI_MODE_NIGHT_NO;

        if(isDarkTheme(act)) {
            mSelectedTheme = THEME.DARK; // this is required for auto mode at night

            if (isOledMode(act, false) && isDarkTheme(act)) {
                act.setTheme(R.style.AppThemeOLED);
                Log.v(TAG, "activate OLED mode");
                //uiNightMode = Configuration.UI_MODE_NIGHT_YES;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                mSelectedTheme = THEME.OLED;
            }
        }

        /*
        Configuration newConfig = new Configuration(act.getResources().getConfiguration());
        newConfig.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
        newConfig.uiMode |= uiNightMode;
        act.getResources().updateConfiguration(newConfig, null);
        */
    }

    // Check if the currently loaded theme is different from the one set in the settings, or if OLED mode changed
    public boolean themeRequiresRestartOfUI(Context context) {
        boolean themeChanged = !mSelectedThemeFromPreferences.equals(getSelectedThemeFromPreferences(context, true));
        boolean oledChanged = !mOledMode.equals(isOledMode(context, true));
        return themeChanged || oledChanged;
    }

    private boolean isDarkTheme(Context context) {
        switch(AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                Log.v(TAG, "MODE_NIGHT_YES (Dark Theme)");
                return true;
            case AppCompatDelegate.MODE_NIGHT_AUTO:
                //Log.v(TAG, "MODE_NIGHT_AUTO");
                int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                if(Configuration.UI_MODE_NIGHT_YES == nightModeFlags) {
                    Log.v(TAG, "MODE_NIGHT_AUTO (Dark Theme)");
                    return true;
                }
                Log.v(TAG, "MODE_NIGHT_AUTO (Light Theme)");
                return false;
            case AppCompatDelegate.MODE_NIGHT_NO:
                Log.v(TAG, "MODE_NIGHT_NO (Light Theme)");
                return false;
            default:
                Log.v(TAG, "Undefined Night-Mode");
                return false;
        }
    }

    public boolean isOledMode(Context context, boolean forceReloadCache) {
        if(mOledMode == null || forceReloadCache) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            mOledMode = mPrefs.getBoolean(SettingsActivity.CB_OLED_MODE, false);
        }
        return mOledMode;
    }

    public THEME getSelectedTheme() {
        return mSelectedTheme;
    }

    private int getSelectedThemeFromPreferences(Context context, boolean forceReloadCache) {
        if(mSelectedThemeFromPreferences == null || forceReloadCache) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            mSelectedThemeFromPreferences = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_APP_THEME, "0"));
        }
        return mSelectedThemeFromPreferences;
    }
}
