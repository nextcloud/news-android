package de.luhmer.owncloudnewsreader.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection.SORT_DIRECTION;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetTodoViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	private static final String TAG = "WidgetTodoViewsFactory";
	
	DatabaseConnection dbConn;
	Cursor cursor;
	private Context context = null;
		
	private int appWidgetId;
	
	public WidgetTodoViewsFactory(Context context, Intent intent) {
		this.context = context;
		appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		
		if(Constants.debugModeWidget)
			Log.d(TAG, "CONSTRUCTOR CALLED - " + appWidgetId);
	}
	
	@Override
	public void onCreate() {
		if(Constants.debugModeWidget)
			Log.d(TAG, "onCreate");
		
		dbConn = new DatabaseConnection(context);
		//onDataSetChanged();
	}
	
	@Override
	public void onDestroy() {
		if(dbConn != null)
			dbConn.closeDatabase();
	}
	
	@Override
	public int getCount() {
		return cursor.getCount();
	}
	
	// Given the position (index) of a WidgetItem in the array, use the item's text value in 
    // combination with the app widget item XML file to construct a RemoteViews object.
    @SuppressLint("SimpleDateFormat")
	public RemoteViews getViewAt(int position) {    	
    	//RemoteViews rv = new RemoteViews(context.getPackageName(), android.R.layout.simple_list_item_2);
    	RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_item);
    	cursor.moveToPosition(position);
    	
    	//Cursor feedCursor = dbConn.getFeedByFeedID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_RSSITEM_ID)));
    	Cursor feedCursor = dbConn.getFeedByDbID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID)));
    	feedCursor.moveToFirst();
    	String header = feedCursor.getString(feedCursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_HEADERTEXT)).trim();
    	String colorString = dbConn.getAvgColourOfFeedByDbId(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID)));
    	feedCursor.close();
    	
    	String authorOfArticle = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_AUTHOR));
    	header += authorOfArticle == null ? "" : " - " + authorOfArticle.trim();
    	String title = Html.fromHtml(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE))).toString().trim();
    	String id = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_RSSITEM_ID));
    	//rv.setTextViewText(android.R.id.text1, header);
    	//rv.setTextViewText(android.R.id.text2, title);
    	
    	Date date = new Date(cursor.getLong(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_PUBDATE)));
    	String dateString = "";
        if(date != null)
        {	
        	SimpleDateFormat formater = new SimpleDateFormat();
        	dateString = formater.format(date);
        }
        
        rv.setTextViewText(R.id.feed_datetime, dateString);        
    	rv.setTextViewText(R.id.feed_author_source, header);
    	rv.setTextViewText(R.id.feed_title, title);
    	
    	    	
        //View viewColor = view.findViewById(R.id.color_line_feed);
        if(colorString != null)
        	rv.setInt(R.id.color_line_feed, "setBackgroundColor", Integer.parseInt(colorString));
        	//rv.set(R.id.color_line_feed, Integer.parseInt(colorString));
    	
    	
    	//Get a fresh new intent
    	Intent ei = new Intent();
    	//Load it with whatever extra you want
    	ei.putExtra(WidgetProvider.UID_TODO, id);
    	//Set it on the list remote view
    	rv.setOnClickFillInIntent(android.R.id.text1, ei);
    	    	
        // Return the RemoteViews object.
        return rv;
    }	
//	
	@Override
	public RemoteViews getLoadingView() {
		return(null);
	}
	
	@Override
	public int getViewTypeCount() {
		return(1);
	}
	
	@Override
	public long getItemId(int position) {
		return(position);
	}
	
	@Override
	public boolean hasStableIds() {
		return(true);
	}
	
	@Override
	public void onDataSetChanged() {
		if(Constants.debugModeWidget)
			Log.d(TAG, "DataSetChanged - WidgetID: " + appWidgetId);
		
		
		cursor = dbConn.getAllItemsForFolder(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS, false, SORT_DIRECTION.desc);
		cursor.moveToFirst();
	}
}
