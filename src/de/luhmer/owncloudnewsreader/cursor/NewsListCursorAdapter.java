package de.luhmer.owncloudnewsreader.cursor;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.GoogleReaderApi.GoogleReaderMethods;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;

public class NewsListCursorAdapter extends CursorAdapter {
	DatabaseConnection dbConn;
	IReader _Reader;

	@SuppressWarnings("deprecation")
	public NewsListCursorAdapter(Context context, Cursor c) {
		super(context, c);		

        _Reader = new OwnCloud_Reader();
		dbConn = new DatabaseConnection(context);
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
        final String idItemDb = cursor.getString(0);
        final String idItem = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_RSSITEM_ID));

        TextView textViewSummary = (TextView) view.findViewById(R.id.summary);
        textViewSummary.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE)))); 
               
        TextView textViewTitle = (TextView) view.findViewById(R.id.tv_subscription);
        textViewTitle.setText(dbConn.getTitleOfSubscriptionByID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID))));
        textViewSummary.setTag(cursor.getString(0));
        
        CheckBox cb = (CheckBox) view.findViewById(R.id.cb_lv_item_starred);
        cb.setOnCheckedChangeListener(null);

        Boolean isStarred = dbConn.isFeedUnreadStarred(cursor.getString(0), false);//false => starred will be checked
        Log.d("ISSTARRED", "" + isStarred + " - Cursor: " + cursor.getString(0));
        cb.setChecked(isStarred);/*        
        if(isStarred)
        	cb.setButtonDrawable(R.drawable.btn_rating_star_on_normal_holo_light);
        else
        	cb.setButtonDrawable(R.drawable.btn_rating_star_off_normal_holo_light);
        */
        cb.setClickable(true);        
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				/*
				if(isChecked)
					buttonView.setButtonDrawable(R.drawable.btn_rating_star_on_normal_holo_light);
				else
					buttonView.setButtonDrawable(R.drawable.btn_rating_star_off_normal_holo_light);
*/

                dbConn.updateIsStarredOfFeed(idItemDb, isChecked);
                if(isChecked)
				    _Reader.Start_AsyncTask_PerformTagActionForSingleItem(0, context, asyncTaskCompletedPerformTagRead, idItem, FeedItemTags.TAGS.MARK_ITEM_AS_STARRED);
                else
                    _Reader.Start_AsyncTask_PerformTagActionForSingleItem(0, context, asyncTaskCompletedPerformTagRead, idItem, FeedItemTags.TAGS.MARK_ITEM_AS_UNSTARRED);
                        //.MarkItemAsStarred(isChecked, getCursorForCurrentRow(buttonView), dbConn, context, asyncTaskCompletedPerformTagRead);
			}
		});
        
        
        CheckBox cbRead = (CheckBox) view.findViewById(R.id.cb_lv_item_read);
        cbRead.setOnCheckedChangeListener(null);
        Boolean isChecked = dbConn.isFeedUnreadStarred(cursor.getString(0), true);
        Log.d("ISREAD", "" + isChecked + " - Cursor: " + cursor.getString(0));
        cbRead.setChecked(isChecked);
        cbRead.setClickable(true);
        cbRead.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {								
                //GoogleReaderMethods.MarkItemAsRead(isChecked, getCursorForCurrentRow(buttonView), dbConn, context, asyncTaskCompletedPerformTagStarred);

                dbConn.updateIsReadOfFeed(idItemDb, isChecked);
                if(isChecked)
                    _Reader.Start_AsyncTask_PerformTagActionForSingleItem(0, context, asyncTaskCompletedPerformTagRead, idItem, FeedItemTags.TAGS.MARK_ITEM_AS_READ);
                else
                    _Reader.Start_AsyncTask_PerformTagActionForSingleItem(0, context, asyncTaskCompletedPerformTagRead, idItem, FeedItemTags.TAGS.MARK_ITEM_AS_UNREAD);
			}
		});
        
        Log.d("NewsListCursor", "BIND VIEW..");
        
        //((CheckBox) view.findViewById(R.id.cb_lv_item_starred)).setButtonDrawable(R.drawable.btn_rating_star_off_normal_holo_light);
	}
	
	private Cursor getCursorForCurrentRow(CompoundButton buttonView)
	{
		TextView tv = (TextView) ((ViewGroup)((ViewGroup) buttonView.getParent()).getChildAt(1)).getChildAt(1);
		String id_DB_Feed = (String) tv.getTag();
		//String id_DB_Feed = (String) ((View)buttonView.getParent()).getTag();
		
		Cursor cur = dbConn.getFeedByID(id_DB_Feed);
		cur.moveToFirst();
		return cur;
	}

	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup parent) {
		// when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.subscription_detail_list_item, parent, false);
        retView.setTag(cursor.getString(0));
        
        Log.d("NewsListCursor", "NEW VIEW..");
        
        return retView;
	}
	
	OnAsyncTaskCompletedListener asyncTaskCompletedPerformTagRead = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			Log.d("FINISHED PERFORM TAG READ ", "" + task_result);			
		}
	};
	
	OnAsyncTaskCompletedListener asyncTaskCompletedPerformTagStarred = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			Log.d("FINISHED PERFORM TAG STARRED ", "" + task_result);			
		}
	};

}
