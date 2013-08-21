package de.luhmer.owncloudnewsreader.helper;

import com.actionbarsherlock.R.color;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;

public class ThemeChooser {
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void chooseTheme(Activity act)
	{
		if(isDarkTheme(act))
		{
			//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			//	act.setTheme(android.R.style.Theme_Holo);
			//else
				//act.setTheme(R.style.Sherlock___Theme);
			act.setTheme(R.style.Theme_Sherlock);			
		} else {
			//if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			//	act.setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
			//else
				//act.setTheme(R.style.Sherlock___Theme_DarkActionBar);
			act.setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		}
	}
	
	/*
	public static void ChangeBackgroundOfSlider(Activity activity) {
		View navigation_drawer = activity.getWindow().getDecorView().findViewById(R.id.left_drawer);
		if(isDarkTheme(activity))
			navigation_drawer.setBackgroundColor(color.abs__background_holo_light);
		else
			navigation_drawer.setBackgroundColor(color.abs__background_holo_light);
	}
	*/
	
	public static boolean isDarkTheme(Context context)
	{
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String value = mPrefs.getString(SettingsActivity.SP_APP_THEME, "0");
		
		if(value.equals("0"))
			return true;
		
		return false;
	}
}
