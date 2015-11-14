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
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.owncloudnewsreader.async_tasks.RssItemToHtmlTask;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.ColorHelper;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;

public class NewsDetailFragment extends Fragment implements RssItemToHtmlTask.Listener {
	public static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";
    private static final String RSS_ITEM_PAGE_URL = "about:blank";

	public final String TAG = getClass().getCanonicalName();

	@InjectView(R.id.webview) WebView mWebView;
    @InjectView(R.id.progressBarLoading) ProgressBar mProgressBarLoading;
	@InjectView(R.id.progressbar_webview) ProgressBar mProgressbarWebView;


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
        unregisterImageDownloadReceiver();
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

        ButterKnife.inject(this, rootView);

        startLoadRssItemToWebViewTask();
        registerImageDownloadReceiver();

		return rootView;
	}

    private void startLoadRssItemToWebViewTask() {
        mWebView.setVisibility(View.GONE);
        mProgressBarLoading.setVisibility(View.VISIBLE);

        init_webView();

        NewsDetailActivity ndActivity = ((NewsDetailActivity) getActivity());
        RssItem rssItem = ndActivity.rssItems.get(section_number);

        RssItemToHtmlTask task = new RssItemToHtmlTask(ndActivity, rssItem, this);
        AsyncTaskHelper.StartAsyncTask(task);
    }

    @Override
    public void onRssItemParsed(String htmlPage) {
        mWebView.setVisibility(View.VISIBLE);
        mProgressBarLoading.setVisibility(View.GONE);

        SetSoftwareRenderModeForWebView(htmlPage, mWebView);

        html = htmlPage;
        mWebView.loadDataWithBaseURL("file:///android_asset/", htmlPage, "text/html", "UTF-8", RSS_ITEM_PAGE_URL);
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

	@SuppressLint("SetJavaScriptEnabled")
	private void init_webView() {
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

        registerForContextMenu(mWebView);

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
                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                //The following three lines are a workaround for websites which don't use a background colour
                NewsDetailActivity ndActivity = ((NewsDetailActivity) getActivity());
                mWebView.setBackgroundColor(getResources().getColor(R.color.slider_listview_text_color_dark_theme));
                ndActivity.mViewPager.setBackgroundColor(getResources().getColor(R.color.slider_listview_text_color_dark_theme));

                if (ThemeChooser.isDarkTheme(getActivity())) {
                    mWebView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
            }
        });
	}


    private URL imageUrl;
    private long downloadID;
    private DownloadManager dlManager;
    private BroadcastReceiver downloadCompleteReceiver;

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v instanceof WebView) {
            WebView.HitTestResult result = ((WebView) v).getHitTestResult();
            if (result != null) {
                int type = result.getType();

                Document htmldoc = Jsoup.parse(html);

                if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    String imageUrl = result.getExtra();
                    if (imageUrl.startsWith("http")) {

                        String imgaltval = "";
                        String imgsrcval = "";

                        try {
                            Elements imgtag = htmldoc.getElementsByAttributeValueContaining("src", imageUrl);
                            imgaltval = imgtag.first().attr("alt");
                            imgsrcval = imageUrl.substring(imageUrl.lastIndexOf('/') + 1, imageUrl.length());
                            this.imageUrl = new URL(imageUrl);
                        } catch (MalformedURLException e) {
                            return;
                        }

                        super.onCreateContextMenu(menu, v, menuInfo);
                        menu.setHeaderTitle(imgsrcval);
                        menu.setHeaderIcon(android.R.drawable.ic_menu_gallery);
                        if( !imgsrcval.equals(imgaltval) && imgaltval.length() > 0  ) {
                            menu.add(imgaltval).setEnabled(false).setIcon(android.R.drawable.ic_menu_info_details);
                        }
                        MenuInflater inflater = getActivity().getMenuInflater();
                        inflater.inflate(R.menu.news_detail_context_img, menu);
                    }
                }
                //else if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE) { }
                //else if (type == WebView.HitTestResult.EMAIL_TYPE) { }
                //else if (type == WebView.HitTestResult.GEO_TYPE) { }
                //else if (type == WebView.HitTestResult.PHONE_TYPE) { }
                //else if (type == WebView.HitTestResult.EDIT_TEXT_TYPE) { }
            }
        }




    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if( !getUserVisibleHint() ) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_downloadimg:
                downloadImage(imageUrl);
                return true;
            case R.id.action_shareimg:
                //Intent Share
                return true;
            case R.id.action_openimg:
                openImageInBrowser(imageUrl);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void openImageInBrowser(URL url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url.toString()));
        startActivity(i);
    }

    private void downloadImage(URL url) {
        if(isExternalStorageWritable()) {
            String filename = url.getFile().substring(url.getFile().lastIndexOf('/') + 1, url.getFile().length());
            dlManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.toString()));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            request.setTitle("Downloading image");
            request.setDescription(filename);
            request.setVisibleInDownloadsUi(false);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            downloadID = dlManager.enqueue(request);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_notwriteable), Toast.LENGTH_LONG).show();
        }
    }

    private void unregisterImageDownloadReceiver() {
        if (downloadCompleteReceiver != null) {
            getActivity().unregisterReceiver(downloadCompleteReceiver);
            downloadCompleteReceiver = null;
        }
    }

    private void registerImageDownloadReceiver() {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if(downloadCompleteReceiver != null) return;

        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long refID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadID == refID) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(refID);
                    Cursor cursor = dlManager.query(query);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = cursor.getInt(columnIndex);
                    //int fileNameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                    //String savedFilePath = cursor.getString(fileNameIndex);
                    int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                    int reason = cursor.getInt(columnReason);

                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_imgsaved), Toast.LENGTH_LONG).show();
                            break;
                        case DownloadManager.STATUS_FAILED:
                            Toast.makeText(getActivity().getApplicationContext(), "FAILED: " +reason, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        };
        getActivity().registerReceiver(downloadCompleteReceiver, intentFilter);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
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
        String currentPageUrl = mWebView.copyBackForwardList().getCurrentItem().getUrl();
        return currentPageUrl.equals(RSS_ITEM_PAGE_URL);
    }
}
