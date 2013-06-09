package de.luhmer.owncloudnewsreader;

import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.actionbarsherlock.app.SherlockFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import de.luhmer.owncloudnewsreader.data.RssFile;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;

public class NewsDetailFragment extends SherlockFragment {	
	public static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";

	public static final String TAG = "NewsDetailFragment";
	
	public static String web_template;
	public static int background_color = Integer.MIN_VALUE;
	
	private WebView webview;	
	private ProgressBar progressbar_webview;
	
	public NewsDetailFragment() {		
	}
		
	@SuppressWarnings("deprecation")
	@SuppressLint({ "SimpleDateFormat", "SetJavaScriptEnabled" })
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		init_webTemplate();		
		
		
		
		View rootView = inflater.inflate(R.layout.fragment_news_detail, container, false);
		
		int section_number = (Integer) getArguments().get(ARG_SECTION_NUMBER);
		
		webview = (WebView) rootView.findViewById(R.id.webview);
		//webview.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
		if(background_color != Integer.MIN_VALUE)
			webview.setBackgroundColor(background_color);		
		
		progressbar_webview = (ProgressBar) rootView.findViewById(R.id.progressbar_webview);
		//getActivity().getWindow().requestFeature(Window.FEATURE_PROGRESS);
		
		
        WebSettings webSettings = webview.getSettings();        
        //webSettings.setPluginState(WebSettings.PluginState.ON);        
        webSettings.setJavaScriptEnabled(true);        
        //webSettings.setPluginsEnabled(true);
        //webSettings.setDomStorageEnabled(true);        
        webview.setWebChromeClient(new WebChromeClient() {        	
        	public void onProgressChanged(WebView view, int progress) 
        	{
        		if(progress < 100 && progressbar_webview.getVisibility() == ProgressBar.GONE){
    				progressbar_webview.setVisibility(ProgressBar.VISIBLE);                       
        		}
        		progressbar_webview.setProgress(progress);
        		if(progress == 100) {
        			progressbar_webview.setVisibility(ProgressBar.GONE);                       
        		}
        	}
        });
        
        //webSettings.setLoadWithOverviewMode(true);
        //webSettings.setUseWideViewPort(true);
        
        //webview.addJavascriptInterface(new JavaScriptInterfaceStundenplan(this, this), "StundenplanTermineAndroid");
        
        webview.setWebViewClient(new WebViewClient() {
        	public void onPageFinished(WebView view, String url) {        		
        		//if(menuItemUpdater != null)
        		//	menuItemUpdater.setActionView(null);
            }
        	
            public boolean shouldOverrideUrlLoading(WebView view, String url){            	
            	view.loadUrl(url);
            	return false;// then it is not handled by default action
           }
        });
        
        //registerForContextMenu(webview);
        
        if(Constants.DEBUG_MODE)
			Log.d(TAG, "AsyncTask_Starting..");
        //new LoadPageContentAsync().execute(section_number);
        
        if(Constants.DEBUG_MODE)
			Log.d(TAG, "AsyncTask_Started");
				
        NewsDetailActivity ndActivity = ((NewsDetailActivity)getActivity());
        
		//RssFile rssFile = ((NewsDetailActivity)getActivity()).rssFiles.get(section_number - 1);
        int idItem = ndActivity.databaseItemIds.get(section_number -1);
        Cursor cursor = ndActivity.dbConn.getFeedByID(String.valueOf(idItem));
        cursor.moveToFirst();
        
		try {
	        String htmlData = web_template;
			
	        String divHeader = "<div id=\"header\">";
	        StringBuilder sb = new StringBuilder(htmlData);
	        //htmlData = sb.insert(htmlData.indexOf(divHeader) + divHeader.length(), rssFile.getTitle().trim()).toString();
	        String title = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE));
	        htmlData = sb.insert(htmlData.indexOf(divHeader) + divHeader.length(), title.trim()).toString();
	        
	        
	        //String divSubscription = "<div id=\"subscription\">";
	        //htmlData = sb.insert(htmlData.indexOf(divSubscription) + divSubscription.length(), rssFile.getStreamID().trim()).toString();
	        
	        Date date = new Date(cursor.getLong(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_PUBDATE)));
	        if(date != null)
	        {
	        	String divDate = "<div id=\"datetime\">";
	        	SimpleDateFormat formater = new SimpleDateFormat();
	        	String dateString = formater.format(date);
	        	htmlData = sb.insert(htmlData.indexOf(divDate) + divDate.length(), dateString).toString();
	        }
	        
	        
	        //String subscription = ((NewsDetailActivity) getActivity()).dbConn.getTitleOfSubscriptionByRowID(String.valueOf(rssFile.getFeedID_Db()));
	        String subscription = ndActivity.dbConn.getTitleOfSubscriptionByFeedItemID(String.valueOf(idItem));
	        String divSubscription = "<div id=\"subscription\">";
	        int pos = htmlData.indexOf(divSubscription) + divSubscription.length();
	        pos = htmlData.indexOf("/>", pos) + 2;//Wegen des Favicon <img /> 
	        htmlData = sb.insert(pos, subscription.trim()).toString();
	        		
	        String divContent = "<div id=\"content\">";
	        String description = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_BODY));
	        //htmlData = sb.insert(htmlData.indexOf(divContent) + divContent.length(), rssFile.getDescription().trim()).toString();
	        htmlData = sb.insert(htmlData.indexOf(divContent) + divContent.length(), description.trim()).toString();
	        
	        String link = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_LINK)); 
	        //Uri uri = Uri.parse(rssFile.getLink());
	        Uri uri = Uri.parse(link);
	        String domainName = uri.getHost();
	        String searchString = "http://s2.googleusercontent.com/s2/favicons?domain=";
	        htmlData = sb.insert(htmlData.indexOf(searchString) + searchString.length(), domainName).toString();
	        	        	        
	        
	        //htmlData = htmlData.replace("<img ", imgWidth);
	        htmlData = URLEncoder.encode(htmlData).replaceAll("\\+"," ");
	        
	        webview.loadData(htmlData, "text/html; charset=UTF-8", "UTF-8");        
	        //webview.loadData(htmlData, "text/html; charset=UTF-8", null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		
		if(Constants.DEBUG_MODE)
			Log.d(TAG, "AsyncTask_Finished");
        
		return rootView;
	}
	
	/*
	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
	    super.onCreateContextMenu(menu);

	    HitTestResult result = getHitTestResult();

	    MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {
	        public boolean onMenuItemClick(MenuItem item) {
	                // do the menu action
	                return true;
	        }
	    };

	    if (result.getType() == HitTestResult.IMAGE_TYPE ||
	            result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
	        // Menu options for an image.
	        //set the header title to the image url
	        menu.setHeaderTitle(result.getExtra());
	        menu.add(0, ID_SAVEIMAGE, 0, "Save Image").setOnMenuItemClickListener(handler);
	        menu.add(0, ID_VIEWIMAGE, 0, "View Image").setOnMenuItemClickListener(handler);
	    } else if (result.getType() == HitTestResult.ANCHOR_TYPE ||
	            result.getType() == HitTestResult.SRC_ANCHOR_TYPE) {
	        // Menu options for a hyperlink.
	        //set the header title to the link url
	        menu.setHeaderTitle(result.getExtra());
	        menu.add(0, ID_SAVELINK, 0, "Save Link").setOnMenuItemClickListener(handler);
	        menu.add(0, ID_SHARELINK, 0, "Share Link").setOnMenuItemClickListener(handler);
	    }
	}*/
	
	/*
	private class LoadPageContentAsync extends AsyncTask<Integer, Void, String> {

		@SuppressLint("SimpleDateFormat")
		@Override
		protected String doInBackground(Integer... params) {
			if(Constants.DEBUG_MODE)
				Log.d(TAG, "AsyncTask_Started");
			
			int section_number = params[0];
			
			RssFile rssFile = ((NewsDetailActivity)getActivity()).rssFiles.get(section_number - 1);
	        
			try {
		        String htmlData = web_template;
				
		        String divHeader = "<div id=\"header\">";
		        StringBuilder sb = new StringBuilder(htmlData);
		        htmlData = sb.insert(htmlData.indexOf(divHeader) + divHeader.length(), rssFile.getTitle().trim()).toString();
		        
		        
		        //String divSubscription = "<div id=\"subscription\">";
		        //htmlData = sb.insert(htmlData.indexOf(divSubscription) + divSubscription.length(), rssFile.getStreamID().trim()).toString();
		        
		        if(rssFile.getDate() != null)
		        {
		        	String divDate = "<div id=\"datetime\">";
		        	SimpleDateFormat formater = new SimpleDateFormat();
		        	String date = formater.format(rssFile.getDate());
		        	htmlData = sb.insert(htmlData.indexOf(divDate) + divDate.length(), date).toString();
		        }
		        
		        String subscription = ((NewsDetailActivity) getActivity()).dbConn.getTitleOfSubscriptionByID(String.valueOf(rssFile.getSubscription_ID()));
		        String divSubscription = "<div id=\"subscription\">";
		        int pos = htmlData.indexOf(divSubscription) + divSubscription.length();
		        pos = htmlData.indexOf("/>", pos) + 2;//Wegen des Favicon <img /> 
		        htmlData = sb.insert(pos, subscription.trim()).toString();
		        		
		        String divContent = "<div id=\"content\">";
		        htmlData = sb.insert(htmlData.indexOf(divContent) + divContent.length(), rssFile.getDescription().trim()).toString();
		        
		        Uri uri = Uri.parse(rssFile.getLink());
		        String domainName = uri.getHost();
		        String searchString = "http://s2.googleusercontent.com/s2/favicons?domain=";
		        htmlData = sb.insert(htmlData.indexOf(searchString) + searchString.length(), domainName).toString();
		        	        	        
		        
		        //htmlData = htmlData.replace("<img ", imgWidth);
		        return htmlData;        
		        //webview.loadData(htmlData, "text/html; charset=UTF-8", null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(String htmlData) {
			webview.loadData(htmlData, "text/html; charset=UTF-8", null);
			
			if(Constants.DEBUG_MODE)
				Log.d(TAG, "AsyncTask_Finished");			
		}		
	}*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void init_webTemplate()
	{
		if(web_template == null)
		{
			InputStream input = null;
			try {
				Activity act = getActivity();
				input = act.getAssets().open("web_template.html");
		        int size = input.available();
		        byte[] buffer = new byte[size];
		        input.read(buffer);
		        input.close();
		
		        // byte buffer into a string
		        web_template = new String(buffer);
		        
		        String background_color_string = SearchString(web_template, "background-color:", ";");
		        
		        if(background_color_string != null)
				{					
					//if(background_color.matches("^#.{3,6}$"))
					if(background_color_string.matches("^#.{3}$"))			
						background_color = Color.parseColor(convertHexColorFrom3To6Characters(background_color_string));
					else if(background_color_string.matches("^#.{6}$"))
						background_color = Color.parseColor(background_color_string);
					
					 ((NewsDetailActivity) getActivity()).mViewPager.setBackgroundColor(background_color);
				}
		        
		        
		        /*
		        DisplayMetrics displaymetrics = new DisplayMetrics();
		        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		        //int height = displaymetrics.heightPixels;
		        int width = displaymetrics.widthPixels;
		        */		        
			} catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	private String SearchString(String data, String startString, String endString)
	{
		int start = data.indexOf(startString) + startString.length();
		int end = data.indexOf(endString, start);
		if(start != (-1 + startString.length()) && end != -1)
			data = data.substring(start, end).trim();
		
		return data;
	}
	
	private String convertHexColorFrom3To6Characters(String color)
	{
		for(int i = 1; i < 6; i += 2)
			color = color.substring(0, i) + color.charAt(i) + color.substring(i);
		
		return color;
	}
}
