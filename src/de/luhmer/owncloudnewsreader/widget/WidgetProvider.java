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

package de.luhmer.owncloudnewsreader.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;

public class WidgetProvider extends AppWidgetProvider {

	//private static final String ACTION_CLICK = "ACTION_CLICK";
    
    public static final String ACTION_WIDGET_CONFIGURE = "ConfigureWidget";
    public static final String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
    public static final String ACTION_LIST_CLICK = "ACTION_LIST_CLICK";
    public static final String UID_TODO = "UID_TODO";
    
    public static final String EXTRA_ITEM = null;
	private static final String TAG = "WidgetProvider";
	    
    @Override
    public void onReceive(Context context, Intent intent) {
    	final int appWidgetId;
    	if(intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
    		appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    	else
    		appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    	
    	if(Constants.debugModeWidget)
    		Log.d(TAG, "onRecieve - WidgetID: " + appWidgetId);
    	
    	String action = intent.getAction();
    	if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {    		
    		if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
    			this.onDeleted(context, new int[] { appWidgetId });
    		}
    	} else if (intent.getAction().equals(ACTION_WIDGET_RECEIVER)) {
    		 
			Intent intentRefresh = new Intent(context, WidgetProvider.class);
			intentRefresh.setAction("android.appwidget.action.APPWIDGET_UPDATE");
			// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
			// since it seems the onUpdate() is only fired on that:							
			intentRefresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,new int[] { appWidgetId });
			context.sendBroadcast(intentRefresh);
    		 
    	} else if (intent.getAction().equals(ACTION_LIST_CLICK)) {
    		try
    		{
	    		String uid = intent.getExtras().getString(UID_TODO);
	    		//Intent intentToDoListAct = new Intent(context, TodoListActivity.class);
	    		Intent intentToDoListAct = new Intent(context, NewsReaderListActivity.class);
	    		intentToDoListAct.putExtra(UID_TODO, uid);
	    		intentToDoListAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    		context.startActivity(intentToDoListAct);
	    		
	    		if(Constants.debugModeWidget)
	    			Log.d(TAG, "ListItem Clicked Starting Activity for Item: " + uid);
    		}
    		catch(Exception ex)
    		{
    			ex.printStackTrace();
    		}
    	}
    	
        super.onReceive(context, intent);
    }
	    
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);		
		SharedPreferences.Editor mPrefsEditor = mPrefs.edit();
		
		for(int appWidgetId : appWidgetIds)	{
			mPrefsEditor.remove("widget_" + appWidgetId);
			
			if(Constants.debugModeWidget)
				Log.d(TAG, "DELETE WIDGET - WIDGET_ID: " + appWidgetId);
		}
		
		/*
		//Delete All App Widgets
		for(int appWidgetId = 0; appWidgetId < 1000; appWidgetId++)	{
			mPrefsEditor.remove("widget_" + appWidgetId);
			
			if(Constants.debugModeWidget)
				Log.d(TAG, "DELETE WIDGET - WIDGET_ID: " + appWidgetId);
		}*/
		
		mPrefsEditor.commit();		
		
		
		super.onDeleted(context, appWidgetIds);
	}

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	if(Constants.debugModeWidget)
    		Log.d(TAG, "onUpdate");
    	    	
        // update each of the app widgets with the remote adapter    	
        for (int appWidgetId : appWidgetIds) {
        	updateAppWidget(context, appWidgetManager, appWidgetId);
        	//appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
        	
        	if(Constants.debugModeWidget)
        		Log.d(TAG, "UPDATE WIDGET - WIDGET_ID: " + appWidgetId);
        }
        
        //int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        
        
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressWarnings("deprecation")
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
    	RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
    	
    	Intent intent = new Intent(context, WidgetService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		rv.setRemoteAdapter(appWidgetId, R.id.list_view, intent);		
		
    	
        Intent onListClickIntent = new Intent(context, WidgetProvider.class);
        onListClickIntent.setAction(ACTION_LIST_CLICK);
        onListClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        onListClickIntent.setData(Uri.parse(onListClickIntent.toUri(Intent.URI_INTENT_SCHEME)));    
        
        final PendingIntent onListClickPendingIntent = PendingIntent.getBroadcast(context, 0,
										        		onListClickIntent,
										        		PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.list_view, onListClickPendingIntent);
        
        Intent intentToDoListAct = new Intent(context, NewsReaderListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentToDoListAct, 0);
        rv.setOnClickPendingIntent(R.id.tV_widget_header, pendingIntent);
        
       
        appWidgetManager.updateAppWidget(appWidgetId, rv);
                
        if(Constants.debugModeWidget)
        	Log.d(TAG, "updateAppWidget - WidgetID: " + appWidgetId);
    }
}
