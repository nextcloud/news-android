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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.FileUtils;
import de.luhmer.owncloudnewsreader.helper.FontHelper;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.WebViewLinkLongClickInterface;

public class NewsDetailFragment extends Fragment {
	public static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";

	public static final String TAG = "NewsDetailFragment";

	public static String web_template;
	public static int background_color = Integer.MIN_VALUE;

	@InjectView(R.id.webview) WebView mWebView;
    @InjectView(R.id.progressBarLoading) ProgressBar mProgressBarLoading;
	@InjectView(R.id.progressbar_webview) ProgressBar mProgressbarWebView;
	private int section_number;

    public NewsDetailFragment() {
        setRetainInstance(true);
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
        if(mWebView != null) {
            mWebView.destroy();
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
        if(mWebView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
                mWebView.onPause();
            mWebView.pauseTimers();
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
        if(mWebView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
                mWebView.onResume();
            mWebView.resumeTimers();
        }
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_news_detail, container, false);

		section_number = (Integer) getArguments().get(ARG_SECTION_NUMBER);

        ButterKnife.inject(this, rootView);

        startLoadRssItemToWebViewTask();

		return rootView;
	}

    public void startLoadRssItemToWebViewTask() {
        AsyncTaskHelper.StartAsyncTask(new LoadRssItemToWebViewAsyncTask(), ((Void) null));
    }

    private class LoadRssItemToWebViewAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            NewsDetailActivity ndActivity = ((NewsDetailActivity)getActivity());

            if(background_color != Integer.MIN_VALUE && ThemeChooser.isDarkTheme(ndActivity))
            {
                mWebView.setBackgroundColor(background_color);
                ndActivity.mViewPager.setBackgroundColor(background_color);
            }

            init_webView();

            mWebView.setVisibility(View.GONE);
            mProgressBarLoading.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            NewsDetailActivity ndActivity = ((NewsDetailActivity)getActivity());

            RssItem rssItem = ndActivity.rssItems.get(section_number - 1);

            return getHtmlPage(ndActivity, rssItem);
        }

        @Override
        protected void onPostExecute(String htmlPage) {
            mWebView.setVisibility(View.VISIBLE);
            mProgressBarLoading.setVisibility(View.GONE);

            mWebView.loadDataWithBaseURL("", htmlPage, "text/html", "UTF-8", "");
            super.onPostExecute(htmlPage);
        }
    }


	@SuppressLint("SetJavaScriptEnabled")
	private void init_webView()
	{
		WebSettings webSettings = mWebView.getSettings();
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


        mWebView.addJavascriptInterface(new WebViewLinkLongClickInterface(getActivity()), "Android");

        mWebView.setWebChromeClient(new WebChromeClient() {
	    	public void onProgressChanged(WebView view, int progress)
	    	{
	    		if(progress < 100 && mProgressbarWebView.getVisibility() == ProgressBar.GONE){
                    mProgressbarWebView.setVisibility(ProgressBar.VISIBLE);
	    		}
                mProgressbarWebView.setProgress(progress);
	    		if(progress == 100) {
                    mProgressbarWebView.setVisibility(ProgressBar.GONE);

                    //The following three lines are a workaround for websites which don't use a background colour
                    NewsDetailActivity ndActivity = ((NewsDetailActivity)getActivity());
                    mWebView.setBackgroundColor(getResources().getColor(R.color.slider_listview_text_color_dark_theme));
                    ndActivity.mViewPager.setBackgroundColor(getResources().getColor(R.color.slider_listview_text_color_dark_theme));


                    if(ThemeChooser.isDarkTheme(getActivity())) {
                        mWebView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }



                    String jsLinkLongClick = getTextFromAssets("LinkLongClick.js", getActivity());
                    //webview.loadUrl("javascript:(function(){document.getElementById('buttonClick').click();})()");
                    mWebView.loadUrl("javascript:(function(){ " + jsLinkLongClick + " })()");





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

        mWebView.setWebViewClient(new WebViewClient() {

	    });
	}



	@SuppressLint("SimpleDateFormat")
	public static String getHtmlPage(Context context, RssItem rssItem)
	{
		init_webTemplate(context);
		String htmlData = web_template;

        String favIconUrl = rssItem.getFeed().getFaviconUrl();

		try {
            if(favIconUrl != null)
            {
                File file = ImageHandler.getFullPathOfCacheFile(favIconUrl, FileUtils.getPathFavIcons(context));
                if(file.isFile())
                    favIconUrl = "file://" + file.getAbsolutePath().toString();
            }
            else {
                favIconUrl = "file:///android_res/drawable/default_feed_icon_light.png";
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }


        String divHeader = "<div id=\"header\">";
        StringBuilder sb = new StringBuilder(htmlData);
        //htmlData = sb.insert(htmlData.indexOf(divHeader) + divHeader.length(), rssFile.getTitle().trim()).toString();
        String title = rssItem.getTitle();
        String linkToFeed = rssItem.getLink();
        title = "<a href=\"" + linkToFeed + "\">" + title + "</a>";
        htmlData = sb.insert(htmlData.indexOf(divHeader) + divHeader.length(), title.trim()).toString();


        //String divSubscription = "<div id=\"subscription\">";
        //htmlData = sb.insert(htmlData.indexOf(divSubscription) + divSubscription.length(), rssFile.getStreamID().trim()).toString();

        Date date = rssItem.getPubDate();
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

        String feedTitle = rssItem.getFeed().getFeedTitle();


        String authorOfArticle = rssItem.getAuthor();
        if(authorOfArticle != null)
            if(!authorOfArticle.trim().equals(""))
                feedTitle += " - " + authorOfArticle.trim();

        String divSubscription = "<div id=\"subscription\">";
        int pos = htmlData.indexOf(divSubscription) + divSubscription.length();
        pos = htmlData.indexOf("/>", pos) + 2;//Wegen des Favicon <img />
        htmlData = sb.insert(pos, feedTitle.trim()).toString();

        String divContent = "<div id=\"content\">";
        String description = rssItem.getBody();
        htmlData = sb.insert(htmlData.indexOf(divContent) + divContent.length(), getDescriptionWithCachedImages(description, context).trim()).toString();

        String searchString = "<img id=\"imgFavicon\" src=";
        htmlData = sb.insert(htmlData.indexOf(searchString) + searchString.length() + 1, favIconUrl).toString();

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
				File file = ImageHandler.getFullPathOfCacheFile(link, FileUtils.getPathImageCache(context));
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

    private static synchronized void init_webTemplate(Context context)
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
