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
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;

public class ThemeChooser {

    private static final String TAG = ThemeChooser.class.getCanonicalName();

    public enum THEME { LIGHT, DARK, OLED }


    // Contains the selected theme defined in the settings (used for checking whether the app needs
    // to restart after changing the theme
    private static Integer mSelectedThemeFromPreferences;
    private static Boolean mOledMode;
    private static SharedPreferences mPrefs;

    // Contains the current selected theme
    private static THEME mSelectedTheme = THEME.LIGHT;

    private ThemeChooser() { }

    public static void chooseTheme(Activity act) {
        switch(getSelectedThemeFromPreferences(false)) {
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

    public static void afterOnCreate(Activity act) {
        //int uiNightMode = Configuration.UI_MODE_NIGHT_NO;

        if(isDarkTheme(act)) {
            mSelectedTheme = THEME.DARK; // this is required for auto mode at night

            if (isOledMode(false) && isDarkTheme(act)) {
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
    public static boolean themeRequiresRestartOfUI() {
        boolean themeChanged = !mSelectedThemeFromPreferences.equals(getSelectedThemeFromPreferences(true));
        boolean oledChanged = !mOledMode.equals(isOledMode(true));
        return themeChanged || oledChanged;
    }

    private static boolean isDarkTheme(Context context) {
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

    public static boolean isOledMode(boolean forceReloadCache) {
        if(mOledMode == null || forceReloadCache) {
            mOledMode = mPrefs.getBoolean(SettingsActivity.CB_OLED_MODE, false);
        }
        return mOledMode;
    }

    public static THEME getSelectedTheme() {
        return mSelectedTheme;
    }

    private static int getSelectedThemeFromPreferences(boolean forceReloadCache) {
        if(mSelectedThemeFromPreferences == null || forceReloadCache) {
            mSelectedThemeFromPreferences = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_APP_THEME, "0"));
        }
        return mSelectedThemeFromPreferences;
    }

    public static void init(SharedPreferences prefs) {
        mPrefs = prefs;
        getSelectedThemeFromPreferences(false); // Init cache
        isOledMode(false); // Init cache
    }
}
