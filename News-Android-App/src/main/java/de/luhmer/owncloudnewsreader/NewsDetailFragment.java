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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.adapter.ProgressBarWebChromeClient;
import de.luhmer.owncloudnewsreader.async_tasks.RssItemToHtmlTask;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.AdBlocker;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.ColorHelper;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;

public class NewsDetailFragment extends Fragment implements RssItemToHtmlTask.Listener {

	public  static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";
    private static final String RSS_ITEM_PAGE_URL = "about:blank";

	public final String TAG = getClass().getCanonicalName();

	public static int background_color = Integer.MIN_VALUE;

	@BindView(R.id.webview) WebView mWebView;
    @BindView(R.id.progressBarLoading) ProgressBar mProgressBarLoading;
	@BindView(R.id.progressbar_webview) ProgressBar mProgressbarWebView;


	private int section_number;
    protected String html;


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

    /**
     * @return true when calls to NewsDetailFragment#navigateBack()
     * can be processed right now
     * @see NewsDetailFragment#navigateBack()
     */
    public boolean canNavigateBack() {
        return !isCurrentPageRssItem();
    }

    /**
     * Navigates back to the last displayed page. Call NewsDetailFragment#canNavigateBack()
     * to check if back navigation is possible right now. Use e.g. for back button handling.
     * @see NewsDetailFragment#navigateBack()
     */
    public void navigateBack() {
        if (isLastPageRssItem()) {
            mWebView.clearHistory();
            startLoadRssItemToWebViewTask();
        } else if (!isCurrentPageRssItem()){
            mWebView.goBack();
        }
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_detail, container, false);

		section_number = (Integer) getArguments().get(ARG_SECTION_NUMBER);

        ButterKnife.bind(this, rootView);

        startLoadRssItemToWebViewTask();

		return rootView;
	}

    private void startLoadRssItemToWebViewTask() {
        mWebView.setVisibility(View.GONE);
        mProgressBarLoading.setVisibility(View.VISIBLE);

        NewsDetailActivity ndActivity = ((NewsDetailActivity)getActivity());
        if(background_color != Integer.MIN_VALUE && ThemeChooser.getInstance(ndActivity).isDarkTheme())
        {
            mWebView.setBackgroundColor(background_color);
            ndActivity.mViewPager.setBackgroundColor(background_color);
        }

        init_webView();

        RssItem rssItem = ndActivity.rssItems.get(section_number);

        RssItemToHtmlTask task = new RssItemToHtmlTask(ndActivity, rssItem, this);
        AsyncTaskHelper.StartAsyncTask(task);
    }

    @Override
    public void onRssItemParsed(String htmlPage) {
        mWebView.setVisibility(View.VISIBLE);
        mProgressBarLoading.setVisibility(View.GONE);

        setSoftwareRenderModeForWebView(htmlPage, mWebView);

        html = htmlPage;
        mWebView.loadDataWithBaseURL("file:///android_asset/", htmlPage, "text/html", "UTF-8", RSS_ITEM_PAGE_URL);
    }

    /**
     * This function has no effect on devices with api level < HONEYCOMB
     * @param htmlPage
     * @param webView
     */
    private void setSoftwareRenderModeForWebView(String htmlPage, WebView webView) {
        if (htmlPage.contains(".gif")) {
            webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            Log.v("NewsDetailFragment", "Using LAYER_TYPE_SOFTWARE");
        } else {
            if (webView.getLayerType() == WebView.LAYER_TYPE_HARDWARE) {
                Log.v("NewsDetailFragment", "Using LAYER_TYPE_HARDWARE");
            } else if (webView.getLayerType() == WebView.LAYER_TYPE_SOFTWARE) {
                Log.v("NewsDetailFragment", "Using LAYER_TYPE_SOFTWARE");
            } else {
                Log.v("NewsDetailFragment", "Using LAYER_TYPE_DEFAULT");
            }
        }
    }


    boolean changedUrl = false;

	@SuppressLint("SetJavaScriptEnabled")
	private void init_webView() {
        int backgroundColor = ColorHelper.getColorFromAttribute(getContext(),
                R.attr.news_detail_background_color);
        mWebView.setBackgroundColor(backgroundColor);

		WebSettings webSettings = mWebView.getSettings();
	    webSettings.setJavaScriptEnabled(true);
	    webSettings.setAllowFileAccess(true);
	    webSettings.setDomStorageEnabled(true);
	    webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
	    webSettings.setSupportMultipleWindows(false);
	    webSettings.setSupportZoom(false);
        webSettings.setAppCacheEnabled(true);

        registerForContextMenu(mWebView);

        mWebView.setWebChromeClient(new ProgressBarWebChromeClient(mProgressbarWebView));

        mWebView.setWebViewClient(new WebViewClient() {

            private Map<String, Boolean> loadedUrls = new HashMap<>();

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                boolean isAd;
                if (!loadedUrls.containsKey(url)) {
                    isAd = AdBlocker.isAd(url);
                    loadedUrls.put(url, isAd);
                } else {
                    isAd = loadedUrls.get(url);
                }
                return isAd ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                int selectedBrowser = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_DISPLAY_BROWSER, "0"));

                boolean result = true;
                switch(selectedBrowser) {
                    case 0: // Custom Tabs
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDarkTheme));
                        builder.setShowTitle(true);
                        builder.setStartAnimations(getActivity(), R.anim.slide_in_right, R.anim.slide_out_left);
                        builder.setExitAnimations(getActivity(), R.anim.slide_in_left, R.anim.slide_out_right);
                        builder.build().launchUrl(getActivity(), Uri.parse(url));
                        result = true;
                        break;
                    case 1: // External Browser
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                        break;
                    case 2: // Built in
                        result = super.shouldOverrideUrlLoading(view, url);
                        break;
                }
                return result;
                //return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // the following lines are a workaround for websites which don't use a background color
                NewsDetailActivity ndActivity = ((NewsDetailActivity) getActivity());
                int backgroundColor = ColorHelper.getColorFromAttribute(getContext(),
                        R.attr.news_detail_background_color);
                mWebView.setBackgroundColor(backgroundColor);
                ndActivity.mViewPager.setBackgroundColor(backgroundColor);
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

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if (!(view instanceof WebView))
            return;

        WebView.HitTestResult result = ((WebView) view).getHitTestResult();
        if (result == null)
            return;

        int type = result.getType();
        Document htmlDoc = Jsoup.parse(html);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        String text;
        DialogFragment newFragment;


        switch (type) {
            case WebView.HitTestResult.IMAGE_TYPE:
            case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                String imageUrl = result.getExtra();

                if (imageUrl.startsWith("http") || imageUrl.startsWith("file")) {
                    URL mImageUrl;
                    String imgtitle;
                    String imgaltval;
                    String imgsrcval;

                    imgsrcval = imageUrl.substring(imageUrl.lastIndexOf('/') + 1, imageUrl.length());
                    Elements imgtag = htmlDoc.getElementsByAttributeValueContaining("src", imageUrl);

                    try {
                        imgtitle = imgtag.first().attr("title");
                    } catch (NullPointerException e) {
                        imgtitle = "";
                    }
                    try {
                        imgaltval = imgtag.first().attr("alt");
                    } catch (NullPointerException e) {
                        imgaltval = "";
                    }
                    try {
                        mImageUrl = new URL(imageUrl);
                    } catch (MalformedURLException e) {
                        return;
                    }

                    String title = imgsrcval;
                    int titleIcon = android.R.drawable.ic_menu_gallery;
                    text = (imgtitle.isEmpty()) ? imgaltval : imgtitle;

                    // Create and show the dialog.
                    newFragment = NewsDetailImageDialogFragment.newInstanceImage(title, titleIcon, text, mImageUrl);
                    newFragment.show(ft, "menu_fragment_dialog");
                }
                break;

            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                String url = result.getExtra();
                URL mUrl;
                try {
                    Elements urltag = htmlDoc.getElementsByAttributeValueContaining("href", url);
                    text = urltag.text();
                    mUrl = new URL(url);
                } catch (MalformedURLException e) {
                    return;
                }

                // Create and show the dialog.
                newFragment = NewsDetailImageDialogFragment.newInstanceUrl(text, mUrl.toString());
                newFragment.show(ft, "menu_fragment_dialog");
                break;
            case WebView.HitTestResult.EMAIL_TYPE:
            case WebView.HitTestResult.GEO_TYPE:
            case WebView.HitTestResult.PHONE_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
        }
    }

    /**
     * @return true when the last page on the webview's history stack is
     * the original rss item page
     */
    private boolean isLastPageRssItem() {
        WebBackForwardList list = mWebView.copyBackForwardList();
        WebHistoryItem lastItem = list.getItemAtIndex(list.getCurrentIndex() - 1);
        return lastItem != null && lastItem.getUrl().equals(RSS_ITEM_PAGE_URL);
    }

    /**
     * @return true when the current page on the webview's history stack is
     * the original rss item page
     */
    private boolean isCurrentPageRssItem() {
        if(mWebView.copyBackForwardList().getCurrentItem() != null) {
            String currentPageUrl = mWebView.copyBackForwardList().getCurrentItem().getOriginalUrl();
            return currentPageUrl.equals("data:text/html;charset=utf-8;base64,");
        }
        return true;
    }
}
