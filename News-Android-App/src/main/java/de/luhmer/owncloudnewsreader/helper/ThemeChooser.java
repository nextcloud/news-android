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

import de.luhmer.owncloudnewsreader.SettingsActivity;

public class ThemeChooser {

    private Integer mSelectedTheme;
    private static ThemeChooser mInstance;

    public static ThemeChooser getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new ThemeChooser(context);
        }
        return mInstance;
    }

    public ThemeChooser(Context context) {
        getSelectedTheme(context, false); // Init variable
    }

	public static void ChooseTheme(Activity act)
	{
	    switch(getInstance(act).getSelectedTheme(act, false)) {
            case 0: // Dark Theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 1: // Light Theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2: // Dark Theme for OLED
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
        }
	}

    // Check if the currently loaded theme is different from the one set in the settings
    public boolean themeRequiresRestartOfUI(Context context) {
        return mSelectedTheme != getSelectedTheme(context, true);
    }

	public boolean isDarkTheme(Context context) {
        if(AppCompatDelegate.MODE_NIGHT_YES == AppCompatDelegate.getDefaultNightMode()) {
            return true;
        }

        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(Configuration.UI_MODE_NIGHT_YES == nightModeFlags) {
            return true;
        }
        return false;
    }

    public Integer getSelectedTheme(Context context, boolean forceReloadCache) {
        if(mSelectedTheme == null || forceReloadCache) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            mSelectedTheme = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_APP_THEME, "0"));
        }
        return mSelectedTheme;
    }
}
