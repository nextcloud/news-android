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

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;

public class WidgetTodoViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	private static final String TAG = WidgetTodoViewsFactory.class.getCanonicalName();

    private DatabaseConnectionOrm dbConn;
    private List<RssItem> rssItems;
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

		dbConn = new DatabaseConnectionOrm(context);
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public int getCount() {
        Log.v(TAG, "getCount");

		return rssItems.size();
	}

	// Given the position (index) of a WidgetItem in the array, use the item's text value in
    // combination with the app widget item XML file to construct a RemoteViews object.
    @SuppressLint("SimpleDateFormat")
	public RemoteViews getViewAt(int position) {
        if(Constants.debugModeWidget)
            Log.d(TAG, "getViewAt: " + position);

        RssItem rssItem = rssItems.get(position);

    	RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_item);

        try
        {
            String header = rssItem.getFeed().getFeedTitle();
            String colorString = rssItem.getFeed().getAvgColour();

            String authorOfArticle = rssItem.getAuthor();
            header += authorOfArticle == null ? "" : " - " + authorOfArticle.trim();
            String title = Html.fromHtml(rssItem.getTitle()).toString();
            long id = rssItem.getId();

            Date date = rssItem.getPubDate();
            String dateString = "";
            if(date != null)
            {
                SimpleDateFormat formater = new SimpleDateFormat();
                dateString = formater.format(date);
            }

            rv.setTextViewText(R.id.feed_datetime, dateString);
            rv.setTextViewText(R.id.feed_author_source, header);
            rv.setTextViewText(R.id.feed_title, title);


            int resId = rssItem.getRead_temp() ? R.drawable.ic_check_box_white : R.drawable.ic_check_box_outline_blank_white;
            int contentDescriptionId = rssItem.getRead_temp() ? R.string.content_desc_mark_as_unread : R.string.content_desc_mark_as_read;
            rv.setInt(R.id.cb_lv_item_read, "setBackgroundResource", resId);
            rv.setContentDescription(R.id.cb_lv_item_read, context.getString(contentDescriptionId));

            if(colorString != null)
                rv.setInt(R.id.color_line_feed, "setBackgroundColor", Integer.parseInt(colorString));

            //Get a fresh new intent
            Intent ei = new Intent();
            //Load it with whatever extra you want
            ei.putExtra(WidgetProvider.RSS_ITEM_ID, id);
            //Set it on the list remote view
            rv.setOnClickFillInIntent(R.id.cb_lv_item_read_wrapper, ei);

            //Get a fresh new intent
            Intent iCheck = new Intent();
            //Load it with whatever extra you want
            iCheck.putExtra(WidgetProvider.RSS_ITEM_ID, id);
            iCheck.putExtra(WidgetProvider.ACTION_CHECKED_CLICK, true);
            rv.setOnClickFillInIntent(R.id.cb_lv_item_read, iCheck);
        } catch(Exception ex) {
            Log.e(TAG, "Error: " + ex.getLocalizedMessage());
        }

        // Return the RemoteViews object.
        return rv;
    }

	@Override
	public RemoteViews getLoadingView() {
        Log.v(TAG, "getLoadingView");
		return(null);
	}

	@Override
	public int getViewTypeCount() {
		return(1);
	}

	@Override
	public long getItemId(int position) {
        Log.v(TAG, "getItemId: " + position);
		return(position);
	}

	@Override
	public boolean hasStableIds() {
        Log.v(TAG, "hasStableIds: " + appWidgetId);
		return(true);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDataSetChanged() {
        Log.v(TAG, "DataSetChanged - WidgetID: " + appWidgetId);

        rssItems = dbConn.getAllUnreadRssItemsForWidget();

        Log.v(TAG, "DataSetChanged finished!");
	}
}
