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

package de.luhmer.owncloudnewsreader.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.PendingIntentCompat;

import java.util.Arrays;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.NewsDetailActivity;
import de.luhmer.owncloudnewsreader.NewsReaderApplication;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;

public class WidgetProvider extends AppWidgetProvider {

	//private static final String ACTION_CLICK = "ACTION_CLICK";
    
    public static final String ACTION_WIDGET_CONFIGURE = "ConfigureWidget";
    public static final String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
    public static final String ACTION_LIST_CLICK = "ACTION_LIST_CLICK";
    public static final String ACTION_CHECKED_CLICK = "ACTION_CHECKED_CLICK";
    public static final String RSS_ITEM_ID = "RSS_ITEM_ID";
    
    public static final String EXTRA_ITEM = null;
	private static final String TAG = "WidgetProvider";

	protected @Inject SharedPreferences mPrefs;


    public static void UpdateWidget(Context context) {
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

        for(int appWidgetId : ids) {
            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
        }

        /*
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:

        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,ids);
        context.sendBroadcast(intent);
        */
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        inject(context);

    	final int[] appWidgetId;
        if(intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS))
            appWidgetId = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
    	else if(intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
    		appWidgetId = new int[] { intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID) };
    	else
    		appWidgetId = new int[] { AppWidgetManager.INVALID_APPWIDGET_ID };

        String action = intent.getAction();

        Log.v(TAG, "onRecieve - WidgetID: " + Arrays.toString(appWidgetId) + " - " + action);

        for (int anAppWidgetId : appWidgetId) {
            if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
                if (anAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    this.onDeleted(context, new int[]{anAppWidgetId});
                }
            } /*else if (intent.getAction().equals(ACTION_WIDGET_RECEIVER)) {

                Intent intentRefresh = new Intent(context, WidgetProvider.class);
                intentRefresh.setAction("android.appwidget.action.APPWIDGET_UPDATE");
                // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
                // since it seems the onUpdate() is only fired on that:
                intentRefresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId[i] });
                context.sendBroadcast(intentRefresh);

            } */ else if (action.equals(ACTION_LIST_CLICK)) {
                try {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        for (String key : bundle.keySet()) {
                            Log.e(TAG, key + ": " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                        }
                    }

                    long rssItemId = intent.getExtras().getLong(RSS_ITEM_ID, -1);

                    if (intent.hasExtra(ACTION_CHECKED_CLICK)) {
                        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
                        RssItem rssItem = dbConn.getRssItemById(rssItemId);
                        rssItem.setRead_temp(!rssItem.getRead_temp());
                        //rssItem.setRead_temp(true);

                        AppWidgetManager.getInstance(context)
                                .notifyAppWidgetViewDataChanged(anAppWidgetId, R.id.list_view);

                        Log.v(TAG, "I'm here!!! Widget update works!");
                    } else {
                        //Intent intentToDoListAct = new Intent(context, TodoListActivity.class);
                        Intent intentToDoListAct = new Intent(context, NewsDetailActivity.class);
                        intentToDoListAct.putExtra(RSS_ITEM_ID, rssItemId);
                        intentToDoListAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentToDoListAct);
                    }

                    Log.v(TAG, "ListItem Clicked Starting Activity for Item: " + rssItemId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } /*else if(action.equals("android.appwidget.action.APPWIDGET_UPDATE") || action.equals(ACTION_WIDGET_RECEIVER)) {
                onUpdate(context, AppWidgetManager.getInstance(context), new int[] { appWidgetId[i] });
            }*/
        }
    	
        super.onReceive(context, intent);
    }
	    
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
        inject(context);

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
        inject(context);

    	if(Constants.debugModeWidget)
    		Log.d(TAG, "onUpdate");
    	    	
        // update each of the app widgets with the remote adapter    	
        for (int appWidgetId : appWidgetIds) {
        	updateAppWidget(context, appWidgetManager, appWidgetId);
        	//appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
        }
        
        //int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
    	RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
    	
    	Intent intent = new Intent(context, WidgetService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		//rv.setRemoteAdapter(appWidgetId, R.id.list_view, intent);
        rv.setRemoteAdapter(R.id.list_view, intent);
		
    	
        Intent onListClickIntent = new Intent(context, WidgetProvider.class);
        onListClickIntent.setAction(ACTION_LIST_CLICK);
        onListClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        onListClickIntent.setData(Uri.parse(onListClickIntent.toUri(Intent.URI_INTENT_SCHEME)));    
        
        final PendingIntent onListClickPendingIntent = PendingIntentCompat.getBroadcast(
                context,
                0,
                onListClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT,
                true
        );
        rv.setPendingIntentTemplate(R.id.list_view, onListClickPendingIntent);


        /*
        Intent intentWidget = new Intent(context, WidgetProvider.class);
        PendingIntent pendingWidgetIntent = PendingIntent.getBroadcast(context, 0, intentWidget, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.cb_lv_item_read_wrapper, pendingWidgetIntent);
        */


        // Intent intentToDoListAct = new Intent(context, NewsReaderListActivity.class);
        // PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentToDoListAct, PendingIntent.FLAG_IMMUTABLE);
        // rv.setOnClickPendingIntent(R.id.tV_widget_header, pendingIntent);


        appWidgetManager.updateAppWidget(appWidgetId, rv);

        if (Constants.debugModeWidget)
            Log.d(TAG, "updateAppWidget - WidgetID: " + appWidgetId);
    }

    private void inject(Context context) {
        ((NewsReaderApplication) context.getApplicationContext()).getAppComponent().injectWidget(this);
    }
}
