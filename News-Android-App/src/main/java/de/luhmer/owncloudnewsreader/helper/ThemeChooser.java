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
import android.preference.PreferenceManager;

import de.luhmer.owncloudnewsreader.R;
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
                act.setTheme(R.style.AppTheme);
                break;
            case 1: // Light Theme
                act.setTheme(R.style.AppThemeLight);
                break;
            case 2: // Dark Theme for OLED
                act.setTheme(R.style.AppThemeOLED);
                break;
        }
	}

    // Check if the currently loaded theme is different from the one set in the settings
    public boolean themeRequiresRestartOfUI(Context context) {
        return mSelectedTheme != getSelectedTheme(context, true);
    }

	public boolean isDarkTheme()
	{
        return  mSelectedTheme == 0 || mSelectedTheme == 2;
	}

    public Integer getSelectedTheme(Context context, boolean forceReloadCache) {
        if(mSelectedTheme == null || forceReloadCache) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            mSelectedTheme = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_APP_THEME, "1"));
        }
        return mSelectedTheme;
    }
}
