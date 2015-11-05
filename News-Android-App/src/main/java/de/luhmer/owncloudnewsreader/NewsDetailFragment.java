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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.ColorHelper;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.WebViewLinkLongClickInterface;

public class NewsDetailFragment extends Fragment {
	public static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";

	public final String TAG = getClass().getCanonicalName();

	public static int background_color = Integer.MIN_VALUE;

	@InjectView(R.id.webview) WebView mWebView;
    @InjectView(R.id.progressBarLoading) ProgressBar mProgressBarLoading;
	@InjectView(R.id.progressbar_webview) ProgressBar mProgressbarWebView;
	private int section_number;
    public List<String> urls = new ArrayList<>();


    public NewsDetailFragment() {
        //setRetainInstance(true);
    }

    public int getSectionNumber() {
        return section_number;
    }

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

    public void PauseCurrentPage()
    {
        if(mWebView != null) {
            mWebView.onPause();
            mWebView.pauseTimers();
        }
    }

    public void ResumeCurrentPage()
    {
        if(mWebView != null) {
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
        AsyncTaskHelper.StartAsyncTask(new LoadRssItemToWebViewAsyncTask());
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

            RssItem rssItem = ndActivity.rssItems.get(section_number);

            return getHtmlPage(ndActivity, rssItem, true);
        }

        @Override
        protected void onPostExecute(String htmlPage) {
            mWebView.setVisibility(View.VISIBLE);
            mProgressBarLoading.setVisibility(View.GONE);

            SetSoftwareRenderModeForWebView(htmlPage, mWebView);

            mWebView.loadDataWithBaseURL("file:///android_asset/", htmlPage, "text/html", "UTF-8", "");
            super.onPostExecute(htmlPage);
        }
    }

    /**
     * This function has no effect on devices with api level < HONEYCOMB
     * @param htmlPage
     * @param webView
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void SetSoftwareRenderModeForWebView(String htmlPage, WebView webView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        if(htmlPage.contains(".gif")) {
            webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            Log.v("NewsDetailFragment", "Using LAYER_TYPE_SOFTWARE");
        } else {
            //webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
            //Log.v("NewsDetailFragment", "Using LAYER_TYPE_HARDWARE");

            if(webView.getLayerType() == WebView.LAYER_TYPE_HARDWARE) {
                Log.v("NewsDetailFragment", "Using LAYER_TYPE_HARDWARE");
            } else if (webView.getLayerType() == WebView.LAYER_TYPE_SOFTWARE){
                Log.v("NewsDetailFragment", "Using LAYER_TYPE_SOFTWARE");
            } else {
                Log.v("NewsDetailFragment", "Using LAYER_TYPE_DEFAULT");
            }
        }
    }


    boolean changedUrl = false;

	@SuppressLint("SetJavaScriptEnabled")
	private void init_webView()
	{
        int backgroundColor = ColorHelper.getColorFromAttribute(getContext(),
                R.attr.news_detail_background_color);
        mWebView.setBackgroundColor(backgroundColor);

		WebSettings webSettings = mWebView.getSettings();
	    //webSettings.setPluginState(WebSettings.PluginState.ON);
	    webSettings.setJavaScriptEnabled(true);
	    webSettings.setAllowFileAccess(true);
	    //webSettings.setPluginsEnabled(true);
	    //webSettings.setDomStorageEnabled(true);

	    webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
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
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.v(TAG, cm.message() + " at " + cm.sourceId() + ":" + cm.lineNumber());
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && mProgressbarWebView.getVisibility() == ProgressBar.GONE) {
                    mProgressbarWebView.setVisibility(ProgressBar.VISIBLE);
                }
                mProgressbarWebView.setProgress(progress);
                if (progress == 100) {
                    mProgressbarWebView.setVisibility(ProgressBar.GONE);

                    //The following three lines are a workaround for websites which don't use a background colour
                    NewsDetailActivity ndActivity = ((NewsDetailActivity) getActivity());
                    mWebView.setBackgroundColor(getResources().getColor(R.color.slider_listview_text_color_dark_theme));
                    ndActivity.mViewPager.setBackgroundColor(getResources().getColor(R.color.slider_listview_text_color_dark_theme));


                    if (ThemeChooser.isDarkTheme(getActivity())) {
                        mWebView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }

                    String jsLinkLongClick = getTextFromAssets("LinkLongClick.js", getActivity());
                    mWebView.loadUrl("javascript:(function(){ " + jsLinkLongClick + " })()");
                }
            }
        });


        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (changedUrl) {
                    changedUrl = false;

                    if (!url.equals("file:///android_asset/") && (urls.isEmpty() || !urls.get(0).equals(url))) {
                        urls.add(0, url);

                        Log.v(TAG, "Page finished (added): " + url);
                    }
                }

                super.onPageStarted(view, url, favicon);
            }
        });

        mWebView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.webview && event.getAction() == MotionEvent.ACTION_DOWN) {
                    changedUrl = true;
                }

                return false;
            }
        });
	}



	@SuppressLint("SimpleDateFormat")
	public static String getHtmlPage(Context context, RssItem rssItem, boolean showHeader)
	{
        String feedTitle = "Undefined";
        String favIconUrl = null;

        Feed feed = rssItem.getFeed();
        int[] colors = ColorHelper.getColorsFromAttributes(context,
                R.attr.dividerLineColor,
                R.attr.rssItemListBackground);
        int feedColor = colors[0];
        if(feed != null) {
            feedTitle = feed.getFeedTitle();
            favIconUrl = feed.getFaviconUrl();
            if(feed.getAvgColour() != null)
                feedColor = Integer.parseInt(feed.getAvgColour());
        }

        if(favIconUrl != null)
        {
            DiskCache diskCache = ImageLoader.getInstance().getDiskCache();
            File file = diskCache.get(favIconUrl);
            if(file != null)
                favIconUrl = "file://" + file.getAbsolutePath();
        } else {
            favIconUrl = "file:///android_res/drawable/default_feed_icon_light.png";
        }

        String body_id;
        if(ThemeChooser.isDarkTheme(context)) {
            body_id = "darkTheme";
        } else
            body_id = "lightTheme";

        StringBuilder builder = new StringBuilder();

        builder.append("<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=0\" />");
        builder.append("<script type=\"text/javascript\" src=\"web.js\"></script>");
        builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"web.css\" />");
        builder.append("<style type=\"text/css\">");
        builder.append(String.format(
                        "#top_section { border-left: 4px solid %s; border-bottom: 1px solid %s; background: %s }",
                        ColorHelper.getCssColor(feedColor),
                        ColorHelper.getCssColor(colors[0]),
                        ColorHelper.getCssColor(colors[1]))
        );
        builder.append("</style>");
        builder.append(String.format("</head><body id=\"%s\">",body_id));

        if(showHeader) {
            builder.append("<div id=\"top_section\">");
            builder.append("<div id=\"header\">");
            String title = rssItem.getTitle();
            String linkToFeed = rssItem.getLink();
            builder.append(String.format("<a href=\"%s\">%s</a>", linkToFeed, title));
            builder.append("</div>");

            String authorOfArticle = rssItem.getAuthor();
            if (authorOfArticle != null)
                if (!authorOfArticle.trim().equals(""))
                    feedTitle += " - " + authorOfArticle.trim();

            builder.append("<div id=\"header_small_text\">");

            builder.append("<div id=\"subscription\">");
            builder.append(String.format("<img id=\"imgFavicon\" src=\"%s\" />", favIconUrl));
            builder.append(feedTitle.trim());
            builder.append("</div>");

            Date date = rssItem.getPubDate();
            if (date != null) {
                String dateString = (String) DateUtils.getRelativeTimeSpanString(date.getTime());
                builder.append("<div id=\"datetime\">");
                builder.append(dateString);
                builder.append("</div>");
            }

            builder.append("</div>");

            builder.append("</div>");
        }

        String description = rssItem.getBody();
        builder.append("<div id=\"content\">");
        builder.append(getDescriptionWithCachedImages(description).trim());
        builder.append("</div>");

        builder.append("</body></html>");

        String htmlData = builder.toString().replaceAll("\"//", "\"https://");

		return htmlData;
	}


	private static String getDescriptionWithCachedImages(String text)
	{
		List<String> links = ImageHandler.getImageLinksFromText(text);
        DiskCache diskCache = ImageLoader.getInstance().getDiskCache();

		for(String link : links)
		{
			link = link.trim();
			try
			{
				File file = diskCache.get(link);
				if(file != null)
					text = text.replace(link, "file://" + file.getAbsolutePath());
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

		return text;
	}






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
