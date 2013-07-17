package de.luhmer.owncloudnewsreader.cursor;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.luhmer.owncloudnewsreader.NewsDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;

public class NewsListCursorAdapter extends CursorAdapter {
	DatabaseConnection dbConn;
	IReader _Reader;
    SimpleDateFormat simpleDateFormat;
    final int LengthBody = 300;
    ForegroundColorSpan bodyForegroundColor;

    PostDelayHandler pDelayHandler;
    
    int selectedDesign = 0;
    
	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	public NewsListCursorAdapter(Context context, Cursor c) {
		super(context, c);

		pDelayHandler = new PostDelayHandler(context);
		
        simpleDateFormat = new SimpleDateFormat("EEE, d. MMM HH:mm:ss");
        bodyForegroundColor = new ForegroundColorSpan(context.getResources().getColor(android.R.color.secondary_text_dark));

        _Reader = new OwnCloud_Reader();
		dbConn = new DatabaseConnection(context);
		
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		selectedDesign = Integer.valueOf(mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0"));
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
        final String idItemDb = cursor.getString(0);
        //final String idItem = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_RSSITEM_ID));
        
        switch (selectedDesign) {
			case 0:
				setSimpleLayout(view, cursor);
				break;
				
			case 1:
				setExtendedLayout(view, cursor);				
				break;
				
			case 2:
				setExtendedLayoutWebView(view, cursor);				
				break;
	
			default:
				break;
	    }
        
        CheckBox cb = (CheckBox) view.findViewById(R.id.cb_lv_item_starred);
        cb.setOnCheckedChangeListener(null);

        Boolean isStarred = dbConn.isFeedUnreadStarred(cursor.getString(0), false);//false => starred will be checked
        //Log.d("ISSTARRED", "" + isStarred + " - Cursor: " + cursor.getString(0));
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

                dbConn.updateIsStarredOfItem(idItemDb, isChecked);

                if(isChecked)
                	UpdateIsReadCheckBox(buttonView, idItemDb);
                
                pDelayHandler.DelayTimer();
                
                /*
                List<String> idItems = new ArrayList<String>();
                idItems.add(idItem);                
                if(isChecked)
				    _Reader.Start_AsyncTask_PerformTagAction(0, context, asyncTaskCompletedPerformTagRead, idItems, FeedItemTags.TAGS.MARK_ITEM_AS_STARRED);
                else
                    _Reader.Start_AsyncTask_PerformTagAction(0, context, asyncTaskCompletedPerformTagRead, idItems, FeedItemTags.TAGS.MARK_ITEM_AS_UNSTARRED);
                */
                        
			}
		});
        
        
        CheckBox cbRead = (CheckBox) view.findViewById(R.id.cb_lv_item_read);
        cbRead.setOnCheckedChangeListener(null);
        Boolean isChecked = dbConn.isFeedUnreadStarred(cursor.getString(0), true);
        //Log.d("ISREAD", "" + isChecked + " - Cursor: " + cursor.getString(0));
        cbRead.setChecked(isChecked);
        cbRead.setClickable(true);
        cbRead.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {								
                //GoogleReaderMethods.MarkItemAsRead(isChecked, getCursorForCurrentRow(buttonView), dbConn, context, asyncTaskCompletedPerformTagStarred);

                dbConn.updateIsReadOfItem(idItemDb, isChecked);
                UpdateListCursor(mContext);
                
                pDelayHandler.DelayTimer();
                
                /*
                //TODO THIS IS IMPORTANT CODE !
                List<String> idItems = new ArrayList<String>();
                idItems.add(idItem);
                if(isChecked)
                    _Reader.Start_AsyncTask_PerformTagActionForSingleItem(0, context, asyncTaskCompletedPerformTagRead, idItems, FeedItemTags.TAGS.MARK_ITEM_AS_READ);
                else
                    _Reader.Start_AsyncTask_PerformTagActionForSingleItem(0, context, asyncTaskCompletedPerformTagRead, idItems, FeedItemTags.TAGS.MARK_ITEM_AS_UNREAD);
                */
			}
		});
        
        //Log.d("NewsListCursor", "BIND VIEW..");
        //((CheckBox) view.findViewById(R.id.cb_lv_item_starred)).setButtonDrawable(R.drawable.btn_rating_star_off_normal_holo_light);
	}
	
	public void setSimpleLayout(View view, Cursor cursor)
	{
		TextView textViewSummary = (TextView) view.findViewById(R.id.summary);
        textViewSummary.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE))).toString());

        TextView textViewItemDate = (TextView) view.findViewById(R.id.tv_item_date);
        long pubDate = cursor.getLong(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_PUBDATE));
        textViewItemDate.setText(simpleDateFormat.format(new Date(pubDate)));

        TextView textViewTitle = (TextView) view.findViewById(R.id.tv_subscription);        
        textViewTitle.setText(dbConn.getTitleOfSubscriptionByRowID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID))));
        textViewSummary.setTag(cursor.getString(0));
	}
	
	public void setExtendedLayout(View view, Cursor cursor)
	{
		TextView textViewSummary = (TextView) view.findViewById(R.id.summary);
        textViewSummary.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE))).toString());

        TextView textViewItemDate = (TextView) view.findViewById(R.id.tv_item_date);
        long pubDate = cursor.getLong(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_PUBDATE));
        textViewItemDate.setText(simpleDateFormat.format(new Date(pubDate)));

        TextView textViewItemBody = (TextView) view.findViewById(R.id.body);
        String body = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_BODY));        
        textViewItemBody.setText(getBodyText(body));

        TextView textViewTitle = (TextView) view.findViewById(R.id.tv_subscription);        
        textViewTitle.setText(dbConn.getTitleOfSubscriptionByRowID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID))));
        textViewSummary.setTag(cursor.getString(0));
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setExtendedLayoutWebView(View view, Cursor cursor)
	{
		
		/*
		TextView textViewSummary = (TextView) view.findViewById(R.id.summary);
        textViewSummary.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE))).toString());

        TextView textViewItemDate = (TextView) view.findViewById(R.id.tv_item_date);
        long pubDate = cursor.getLong(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_PUBDATE));
        textViewItemDate.setText(simpleDateFormat.format(new Date(pubDate)));
		 */
				
        WebView webViewContent = (WebView) view.findViewById(R.id.webView_body);
        webViewContent.setClickable(false);
        webViewContent.setFocusable(false);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        //	webViewContent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        
        webViewContent.loadDataWithBaseURL("", NewsDetailFragment.getHtmlPage(mContext, dbConn , cursor.getInt(0)), "text/html", "UTF-8", "");

        /*
        TextView textViewTitle = (TextView) view.findViewById(R.id.tv_subscription);        
        textViewTitle.setText(dbConn.getTitleOfSubscriptionByRowID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID))));
        textViewSummary.setTag(cursor.getString(0));
        */
	}
	
	
	/*
	class ItemHolder {
		TextView txt_feed;
		TextView txt_item_date;
		TextView txt_summary;
		TextView txt_body;
		CheckBox cb_starred;
		CheckBox cb_read;
	}*/
		
	public void CloseDatabaseConnection()
	{
		if(dbConn != null)
			dbConn.closeDatabase();
	}
	
	private void UpdateIsReadCheckBox(View view, String idItemDb)
	{
		LinearLayout lLayout = (LinearLayout) view.getParent();
		Boolean isChecked = dbConn.isFeedUnreadStarred(idItemDb, true);
        CheckBox cbRead = (CheckBox) lLayout.findViewById(R.id.cb_lv_item_read);
        //ChangeCheckBoxState(cbRead, isChecked, mContext);
        cbRead.setChecked(isChecked);
	}
	
	public static void ChangeCheckBoxState(CheckBox cb, boolean state, Context context)
	{
		if(cb != null)
		{
			if(cb.isChecked() != state)
			{
				cb.setChecked(state);
				
				UpdateListCursor(context);
			}
		}
	}
	
	public static void UpdateListCursor(Context context)//TODO make this better
	{
		SherlockFragmentActivity sfa = (SherlockFragmentActivity) context;
		
		//if tablet view is enabled --> update the listview 
		if(sfa instanceof NewsReaderListActivity)
			((NewsReaderListActivity) sfa).updateAdapter();
	}
	

    private String getBodyText(String body)
    {
        //if(body.length() > LengthBody)
        //    body = body.substring(0, LengthBody);

        body = body.replaceAll("<img[^>]*>", "");
        body = body.replaceAll("<video[^>]*>", "");        

        SpannableString bodyStringSpannable = new SpannableString(Html.fromHtml(body));
        bodyStringSpannable.setSpan(bodyForegroundColor, 0, bodyStringSpannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        String bodyString = bodyStringSpannable.toString().trim();
        
        if(bodyString.length() > LengthBody)
            bodyString = bodyString.substring(0, LengthBody);

        return bodyString;
    }

    /*
	private Cursor getCursorForCurrentRow(CompoundButton buttonView)
	{
		TextView tv = (TextView) ((ViewGroup)((ViewGroup) buttonView.getParent()).getChildAt(1)).getChildAt(1);
		String id_DB_Feed = (String) tv.getTag();
		//String id_DB_Feed = (String) ((View)buttonView.getParent()).getTag();
		
		Cursor cur = dbConn.getFeedByID(id_DB_Feed);
		cur.moveToFirst();
		return cur;
	}*/

	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup parent) {
		// when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());        
        View retView = null;
        
        switch (selectedDesign) {
			case 0:
				retView = inflater.inflate(R.layout.subscription_detail_list_item_simple, parent, false);
				break;
			case 1:
				retView = inflater.inflate(R.layout.subscription_detail_list_item_extended, parent, false);				
				break;
				
			case 2:
				retView = inflater.inflate(R.layout.subscription_detail_list_item_extended_webview, parent, false);				
				break;
				
			default:
				break;
        }
        
        if(retView != null)
        	retView.setTag(cursor.getString(0));
        
        
        
       
        //retView.getLocationOnScreen(location);        
        //Log.d("NewsListCursor", "NEW VIEW..");
        
        return retView;
	}

    /*
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
	};*/
}
