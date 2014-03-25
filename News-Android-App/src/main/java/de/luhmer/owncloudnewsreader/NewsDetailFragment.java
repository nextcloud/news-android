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

package de.luhmer.owncloudnewsreader;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockFragment;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.FontHelper;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.WebViewLinkLongClickInterface;

public class NewsDetailFragment extends SherlockFragment {
	public static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";

	public static final String TAG = "NewsDetailFragment";

	public static String web_template;
	public static int background_color = Integer.MIN_VALUE;

	public WebView webview;
	private ProgressBar progressbar_webview;
	private int section_number;

	public NewsDetailFragment() {
		//setRetainInstance(true);
	}


	/*
	@Override
	public void onSaveInstanceState(Bundle outState) {
		webview.saveState(outState);
	}*/

    @Override
    public void onResume() {
        super.onResume();
        ResumeCurrentPage();
    }

    @Override
	public void onPause() {
		super.onPause();
        PauseCurrentPage();
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(webview != null) {
            webview.destroy();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void PauseCurrentPage()
    {
			/*
	        Class.forName("android.webkit.WebView")
	                .getMethod("onPause", (Class[]) null)
	                            .invoke(webview, (Object[]) null);
	        */
        if(webview != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
                webview.onPause();
            webview.pauseTimers();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void ResumeCurrentPage()
    {
			/*
	        Class.forName("android.webkit.WebView")
	                .getMethod("onResume", (Class[]) null)
	                            .invoke(webview, (Object[]) null);
	 */
        if(webview != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
                webview.onResume();
            webview.resumeTimers();
        }
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_news_detail, container, false);

		section_number = (Integer) getArguments().get(ARG_SECTION_NUMBER);

		webview = (WebView) rootView.findViewById(R.id.webview);

		progressbar_webview = (ProgressBar) rootView.findViewById(R.id.progressbar_webview);

        LoadRssItemInWebView();

		return rootView;
	}

	public void LoadRssItemInWebView()
	{
		NewsDetailActivity ndActivity = ((NewsDetailActivity)getActivity());

		if(background_color != Integer.MIN_VALUE && ThemeChooser.isDarkTheme(ndActivity))
		{
			webview.setBackgroundColor(background_color);
			ndActivity.mViewPager.setBackgroundColor(background_color);
		}

		init_webView();
		NewsDetailActivity nrda = ((NewsDetailActivity)getActivity());
		String idItem = nrda.getIdCurrentFeed(section_number - 1);
		webview.loadDataWithBaseURL("", getHtmlPage(ndActivity, ndActivity.dbConn, Integer.parseInt(idItem)), "text/html", "UTF-8", "");
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void init_webView()
	{
		WebSettings webSettings = webview.getSettings();
	    //webSettings.setPluginState(WebSettings.PluginState.ON);
	    webSettings.setJavaScriptEnabled(true);
	    webSettings.setAllowFileAccess(true);
	    //webSettings.setPluginsEnabled(true);
	    //webSettings.setDomStorageEnabled(true);

	    webSettings.setJavaScriptCanOpenWindowsAutomatically(
                false);
	    webSettings.setSupportMultipleWindows(false);
	    webSettings.setSupportZoom(false);
	    //webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
	    //webSettings.setSavePassword(false);
	    //webview.setVerticalScrollBarEnabled(false);
	    //webview.setHorizontalScrollBarEnabled(false);
        webSettings.setAppCacheEnabled(true);
        //webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //webSettings.setAppCacheMaxSize(200);
        //webSettings.setDatabaseEnabled(true);
        //webview.clearCache(true);


        webview.addJavascriptInterface(new WebViewLinkLongClickInterface(getActivity()), "Android");

	    webview.setWebChromeClient(new WebChromeClient() {
	    	public void onProgressChanged(WebView view, int progress)
	    	{
	    		if(progress < 100 && progressbar_webview.getVisibility() == ProgressBar.GONE){
					progressbar_webview.setVisibility(ProgressBar.VISIBLE);
	    		}
	    		progressbar_webview.setProgress(progress);
	    		if(progress == 100) {
	    			progressbar_webview.setVisibility(ProgressBar.GONE);

                    //The following three lines are a workaround for websites which don't use a background colour
                    NewsDetailActivity ndActivity = ((NewsDetailActivity)getActivity());
                    webview.setBackgroundColor(getResources().getColor(R.color.slider_listview_text_color));
                    ndActivity.mViewPager.setBackgroundColor(getResources().getColor(R.color.slider_listview_text_color));


                    if(ThemeChooser.isDarkTheme(getActivity())) {
                        webview.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }



                    String jsLinkLongClick = getTextFromAssets("LinkLongClick.js", getActivity());
                    //webview.loadUrl("javascript:(function(){document.getElementById('buttonClick').click();})()");
                    webview.loadUrl("javascript:(function(){ " + jsLinkLongClick + " })()");





                    /*
                    image.addEventListener("touchstart", function(e){
                        timer = window.setTimeout(function() {
                            e.preventDefault();
                            alert(image.getAttribute('title'));
                            //alert("fired - touchstart");
                        }, 1000);
                    });

                    image.addEventListener('touchend', function() {
                        clearTimeout(timer);
                    });
                    */



	    		}
	    	}
	    });

	    webview.setWebViewClient(new WebViewClient() {

	    });
	}



	@SuppressLint("SimpleDateFormat")
	public static String getHtmlPage(Context context, DatabaseConnection dbConn, int idItem)
	{
		init_webTemplate(context);
		String htmlData = web_template;

		//RssFile rssFile = ((NewsDetailActivity)getActivity()).rssFiles.get(section_number - 1);
        //int idItem = ndActivity.databaseItemIds.get(section_number - 1);

        Cursor cursor = dbConn.getArticleByID(String.valueOf(idItem));
        cursor.moveToFirst();

        String favIconUrl = "";

		try {
			Cursor favIconCursor = dbConn.getFeedByDbID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID)));
	        try
	        {
	        	favIconCursor.moveToFirst();
	        	if(favIconCursor.getCount() > 0)
	        	{
	        		favIconUrl = favIconCursor.getString(favIconCursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_FAVICON_URL));
	        		if(favIconUrl != null)
	        		{
	        			File file = ImageHandler.getFullPathOfCacheFile(favIconUrl, ImageHandler.getPathFavIcons(context));
	        			if(file.isFile())
	        				favIconUrl = "file://" + file.getAbsolutePath().toString();
	        		}
	        		else
	        			favIconUrl = "file:///android_res/drawable/default_feed_icon_light.png";
	        	}
	        } catch(Exception ex) {
	        	ex.printStackTrace();
	        } finally {
	        	favIconCursor.close();
	        }


	        String divHeader = "<div id=\"header\">";
	        StringBuilder sb = new StringBuilder(htmlData);
	        //htmlData = sb.insert(htmlData.indexOf(divHeader) + divHeader.length(), rssFile.getTitle().trim()).toString();
	        String title = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE));
	        String linkToFeed = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_LINK));
	        title = "<a href=\"" + linkToFeed + "\">" + title + "</a>";
	        htmlData = sb.insert(htmlData.indexOf(divHeader) + divHeader.length(), title.trim()).toString();


	        //String divSubscription = "<div id=\"subscription\">";
	        //htmlData = sb.insert(htmlData.indexOf(divSubscription) + divSubscription.length(), rssFile.getStreamID().trim()).toString();

	        Date date = new Date(cursor.getLong(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_PUBDATE)));
	        if(date != null)
	        {
	        	String divDate = "<div id=\"datetime\">";
	        	//SimpleDateFormat formater = new SimpleDateFormat();
	        	//String dateString = formater.format(date);
	        	String dateString = (String) DateUtils.getRelativeTimeSpanString(date.getTime());
	        	htmlData = sb.insert(htmlData.indexOf(divDate) + divDate.length(), dateString).toString();
	        }


	        //String subscription = ((NewsDetailActivity) getActivity()).dbConn.getTitleOfSubscriptionByRowID(String.valueOf(rssFile.getFeedID_Db()));
	        //String subscription = dbConn.getTitleOfSubscriptionByDBItemID(String.valueOf(idItem));
	        Cursor cursorFeed = dbConn.getFeedByDbID(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID)));
	        cursorFeed.moveToFirst();
	        String subscription = cursorFeed.getString(cursorFeed.getColumnIndex(DatabaseConnection.SUBSCRIPTION_HEADERTEXT)).trim();
	        cursorFeed.close();

	        String authorOfArticle = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_AUTHOR));
	        if(authorOfArticle != null)
	        	if(!authorOfArticle.trim().equals(""))
	        		subscription += " - " + authorOfArticle.trim();

	        String divSubscription = "<div id=\"subscription\">";
	        int pos = htmlData.indexOf(divSubscription) + divSubscription.length();
	        pos = htmlData.indexOf("/>", pos) + 2;//Wegen des Favicon <img />
	        htmlData = sb.insert(pos, subscription.trim()).toString();

	        String divContent = "<div id=\"content\">";
	        String description = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_BODY));
	        //htmlData = sb.insert(htmlData.indexOf(divContent) + divContent.length(), rssFile.getDescription().trim()).toString();
	        htmlData = sb.insert(htmlData.indexOf(divContent) + divContent.length(), getDescriptionWithCachedImages(description, context).trim()).toString();

	        //String link = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_LINK));
	        //Uri uri = Uri.parse(rssFile.getLink());
	        //Uri uri = Uri.parse(link);
	        //String domainName = uri.getHost();
	        //String searchString = "http://s2.googleusercontent.com/s2/favicons?domain=";
	        //htmlData = sb.insert(htmlData.indexOf(searchString) + searchString.length(), domainName).toString();

	        String searchString = "<img id=\"imgFavicon\" src=";
	        htmlData = sb.insert(htmlData.indexOf(searchString) + searchString.length() + 1, favIconUrl).toString();

	        //htmlData = URLEncoder.encode(htmlData).replaceAll("\\+"," ");

	        //webview.loadDataWithBaseURL("", htmlData, "text/html", "UTF-8", "");
	        //webview.loadData(htmlData, "text/html; charset=UTF-8", "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}

		return htmlData;
	}


	private static String getDescriptionWithCachedImages(String text, Context context)
	{
		List<String> links = ImageHandler.getImageLinksFromText(text);

		for(String link : links)
		{
			link = link.trim();
			try
			{
				File file = ImageHandler.getFullPathOfCacheFile(link, ImageHandler.getPathImageCache(context));
				if(file.isFile())
					text = text.replace(link, "file://" + file.getAbsolutePath().toString());
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

		return text;
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












    static String getTextFromAssets(String fileName, Context context) {
        InputStream input;
        try {
            input = context.getAssets().open(fileName);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            return new String(buffer);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private static void init_webTemplate(Context context)
	{
		if(web_template == null)
		{
			try {
		        web_template = getTextFromAssets("web_template.html", context);

		        String background_color_string = SearchString(web_template, "background-color:", ";");

		        if(background_color_string != null)
				{
					//if(background_color.matches("^#.{3,6}$"))
					if(background_color_string.matches("^#.{3}$"))
						background_color = Color.parseColor(convertHexColorFrom3To6Characters(background_color_string));
					else if(background_color_string.matches("^#.{6}$"))
						background_color = Color.parseColor(background_color_string);
				}

		        if(ThemeChooser.isDarkTheme(context))
		        	web_template = web_template.replace("<body id=\"lightTheme\">", "<body id=\"darkTheme\">");


	        	FontHelper fHelper = new FontHelper(context);

	        	web_template = web_template.replace("ROBOTO_FONT_STYLE", fHelper.getFontName());

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

	private static String SearchString(String data, String startString, String endString)
	{
		int start = data.indexOf(startString) + startString.length();
		int end = data.indexOf(endString, start);
		if(start != (-1 + startString.length()) && end != -1)
			data = data.substring(start, end).trim();

		return data;
	}

	private static String convertHexColorFrom3To6Characters(String color)
	{
		for(int i = 1; i < 6; i += 2)
			color = color.substring(0, i) + color.charAt(i) + color.substring(i);

		return color;
	}
}
