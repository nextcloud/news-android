package de.luhmer.owncloudnewsreader.helper;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

public class ThemeChooser {
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void chooseTheme(Activity act)
	{
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(act);
		String value = mPrefs.getString(SettingsActivity.SP_APP_THEME, "0");
		
		if(value.equals("0"))
		{
			//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			//	act.setTheme(android.R.style.Theme_Holo);
			//else
				act.setTheme(R.style.Sherlock___Theme);
		}
		else if(value.equals("1"))
		{
			//if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			//	act.setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
			//else
				act.setTheme(R.style.Sherlock___Theme_DarkActionBar);
		}
	}
	
	public static boolean isDarkTheme(Context context)
	{
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String value = mPrefs.getString(SettingsActivity.SP_APP_THEME, "0");
		
		if(value.equals("0"))
			return true;
		
		return false;
	}
}
