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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.devspark.robototextview.widget.RobotoCheckBox;
import com.devspark.robototextview.widget.RobotoTextView;

import de.luhmer.owncloudnewsreader.NewsDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.FontHelper;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;

public class NewsListCursorAdapter extends CursorAdapter {
	//private static final String TAG = "NewsListCursorAdapter";
	DatabaseConnection dbConn;
	IReader _Reader;
    SimpleDateFormat simpleDateFormat;
    final int LengthBody = 300;
    ForegroundColorSpan bodyForegroundColor;
    IOnStayUnread onStayUnread;
    
    PostDelayHandler pDelayHandler;
    
    int selectedDesign = 0;
    
	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	public NewsListCursorAdapter(Context context, Cursor c, IOnStayUnread onStayUnread) {
		super(context, c);

		this.onStayUnread = onStayUnread;
		
		pDelayHandler = new PostDelayHandler(context);
		
        simpleDateFormat = new SimpleDateFormat("EEE, d. MMM HH:mm:ss");
        bodyForegroundColor = new ForegroundColorSpan(context.getResources().getColor(android.R.color.secondary_text_dark));

        _Reader = new OwnCloud_Reader();
		dbConn = new DatabaseConnection(context);
		
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		selectedDesign = Integer.valueOf(mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0"));
	}

	@Override
	public void bindView(final View view, final Context context, Cursor cursor) {
        final String idItemDb = cursor.getString(0);
        
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
        
        FontHelper fHelper = new FontHelper(context);
        fHelper.setFontForAllChildren(view, fHelper.getFont());
        
        RobotoCheckBox cb = (RobotoCheckBox) view.findViewById(R.id.cb_lv_item_starred);
        cb.setOnCheckedChangeListener(null);

        Boolean isStarred = dbConn.isFeedUnreadStarred(cursor.getString(0), false);//false => starred will be checked
        //Log.d("ISSTARRED", "" + isStarred + " - Cursor: " + cursor.getString(0));
        cb.setChecked(isStarred);
        cb.setClickable(true);        
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dbConn.updateIsStarredOfItem(idItemDb, isChecked);

                if(isChecked)
                	UpdateIsReadCheckBox(buttonView, idItemDb);
                
                pDelayHandler.DelayTimer();
			}
		});
        
        
        RobotoCheckBox cbRead = (RobotoCheckBox) view.findViewById(R.id.cb_lv_item_read);
        cbRead.setOnCheckedChangeListener(null);
        Boolean isChecked = dbConn.isFeedUnreadStarred(cursor.getString(0), true);
        //Log.d("ISREAD", "" + isChecked + " - Cursor: " + cursor.getString(0));
        cbRead.setChecked(isChecked);
        if(!isChecked) {
        	RobotoTextView textView = (RobotoTextView) view.findViewById(R.id.summary);
        	fHelper.setFontStyleForSingleView(textView, fHelper.getFontUnreadStyle());
        }
        	
        
        cbRead.setClickable(true);
        cbRead.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dbConn.updateIsReadOfItem(idItemDb, isChecked);
                UpdateListCursor(mContext);
                
                pDelayHandler.DelayTimer();
                
                RobotoTextView textView = (RobotoTextView) view.findViewById(R.id.summary);
                if(textView != null)
                {
                	FontHelper fHelper = new FontHelper(context);
                	if(isChecked)
                		fHelper.setFontStyleForSingleView(textView, fHelper.getFont());
                		//textView.setTextAppearance(mContext, R.style.RobotoFontStyle);
                	else {
                		fHelper.setFontStyleForSingleView(textView, fHelper.getFontUnreadStyle());
                		onStayUnread.stayUnread((RobotoCheckBox)buttonView);
                	}
                		//textView.setTextAppearance(mContext, R.style.RobotoFontStyleBold);
                		
                	textView.invalidate();
                }
			}
		});
        
        
        String colorString = dbConn.getAvgColourOfFeedByDbId(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID)));
        View viewColor = view.findViewById(R.id.color_line_feed);
        if(colorString != null)
        	viewColor.setBackgroundColor(Integer.parseInt(colorString));
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
        WebView webViewContent = (WebView) view.findViewById(R.id.webView_body);
        webViewContent.setClickable(false);
        webViewContent.setFocusable(false);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        //	webViewContent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        
        webViewContent.loadDataWithBaseURL("", NewsDetailFragment.getHtmlPage(mContext, dbConn , cursor.getInt(0)), "text/html", "UTF-8", "");
	}
				
	public void CloseDatabaseConnection()
	{
		if(dbConn != null)
			dbConn.closeDatabase();
	}
	
	private void UpdateIsReadCheckBox(View view, String idItemDb)
	{
		LinearLayout lLayout = (LinearLayout) view.getParent();
		Boolean isChecked = dbConn.isFeedUnreadStarred(idItemDb, true);
		RobotoCheckBox cbRead = (RobotoCheckBox) lLayout.findViewById(R.id.cb_lv_item_read);    
        cbRead.setChecked(isChecked);
	}
	
	public static void ChangeCheckBoxState(RobotoCheckBox cb, boolean state, Context context)
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

	@Override
	public View newView(Context cont, Cursor cursor, ViewGroup parent) {
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
        
        return retView;
	}
}
