/*
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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.adapter.ProgressBarWebChromeClient;
import de.luhmer.owncloudnewsreader.async_tasks.RssItemToHtmlTask;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.databinding.FragmentNewsDetailBinding;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.ColorHelper;
import de.luhmer.owncloudnewsreader.services.DownloadWebPageService;

public class NewsDetailFragment extends Fragment implements RssItemToHtmlTask.Listener {

	public  static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";
    private static final String RSS_ITEM_PAGE_URL = "about:blank";

	public final String TAG = getClass().getCanonicalName();

	protected FragmentNewsDetailBinding binding;

    protected @Inject SharedPreferences mPrefs;

    private int section_number;
    protected String html;
    private String title = "";
    private String baseUrl = null;
    // private GestureDetector mGestureDetector;


    public NewsDetailFragment() { }

    public int getSectionNumber() {
        return section_number;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((NewsReaderApplication) requireActivity().getApplication()).getAppComponent().injectFragment(this);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeCurrentPage();

        registerForContextMenu(binding.webview);
    }

    @Override
	public void onPause() {
		super.onPause();
        pauseCurrentPage();
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Log.d(TAG, "onDestroy: " + title);
        binding.webview.destroy();
    }

    public void pauseCurrentPage() {
        binding.webview.onPause();
        binding.webview.pauseTimers();
    }

    public void resumeCurrentPage() {
        applyWebSettings();
        binding.webview.onResume();
        binding.webview.resumeTimers();
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
            binding.webview.clearHistory();
            startLoadRssItemToWebViewTask((NewsDetailActivity) getActivity());
        } else if (!isCurrentPageRssItem()){
            binding.webview.goBack();
        }
    }

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewsDetailBinding.inflate(inflater, container, false);

		section_number = (Integer) requireArguments().get(ARG_SECTION_NUMBER);

        NewsDetailActivity ndActivity = ((NewsDetailActivity)getActivity());
        assert ndActivity != null;

        /*
		// Do not reload webView if retained
        if (savedInstanceState != null) {
            Log.d(TAG, "onCreateView restore webview");
            binding.webview.restoreState(savedInstanceState);
            setWebViewBackgroundColor(ndActivity);
            binding.progressBarLoading.setVisibility(View.GONE);
            binding.progressbarWebview.setVisibility(View.GONE);
            // Make sure to sync the incognitio on retained views
            syncIncognitoState();
            this.addBottomPaddingForFastActions(binding.webview);
        } else {
            Log.d(TAG, "onCreateView new webview");
            startLoadRssItemToWebViewTask(ndActivity);
        }
        // setUpGestureDetector();
        */

        // the whole process of saving and restoring instances is way too expensive - especially
        // for huge pages (such as android central has them) - it'll just freeze the webview
        startLoadRssItemToWebViewTask(ndActivity);

        return binding.getRoot();
    }

	private void setWebViewBackgroundColor(NewsDetailActivity ndActivity) {
        int backgroundColor = ContextCompat.getColor(ndActivity, R.color.news_detail_background_color);
        binding.webview.setBackgroundColor(backgroundColor);
        ndActivity.setBackgroundColorOfViewPager(backgroundColor);
    }

	protected void syncIncognitoState() {
        NewsDetailActivity ndActivity = ((NewsDetailActivity) requireActivity());
        boolean isIncognito = ndActivity.isIncognitoEnabled();
        binding.webview.getSettings().setBlockNetworkLoads(isIncognito);
        // binding.webview.getSettings().setBlockNetworkImage(isIncognito);
    }

    /*
	@Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: " + title);
        //binding.webview.saveState(outState);
    }
    */

    /**
     * Double tap to star listener (double tap the webview to mark the current item as read)
     */
    /*
	private void setUpGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener());

        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener()
        {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.v(TAG, "onDoubleTap() called with: e = [" + e + "]");
                NewsDetailActivity ndActivity = ((NewsDetailActivity)getActivity());
                if(ndActivity != null) {
                    ((NewsDetailActivity) getActivity()).toggleRssItemStarredState();

                    // Star has 5 corners. So we can rotate it by 2/5
                    View view = getActivity().findViewById(R.id.action_starred);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", view.getRotation() + (2*(360f/5f)));
                    animator.start();
                }
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });
    }
    */

    protected void startLoadRssItemToWebViewTask(NewsDetailActivity ndActivity) {
        binding.webview.setVisibility(View.GONE);
        binding.progressBarLoading.setVisibility(View.VISIBLE);

        setWebViewBackgroundColor(ndActivity);

        init_webView();
        RssItem rssItem = ndActivity.rssItems.get(section_number);
        title = rssItem.getTitle();
        Log.d(TAG, "startLoadRssItemToWebViewTask: " + title);
        RssItemToHtmlTask task = new RssItemToHtmlTask(ndActivity, rssItem, this, mPrefs);
        AsyncTaskHelper.StartAsyncTask(task);
    }

    @Override
    public void onRssItemParsed(String htmlPage) {
        binding.webview.setVisibility(View.VISIBLE);
        binding.progressBarLoading.setVisibility(View.GONE);
        Log.d(TAG, "progressBarLoading gone");

        setSoftwareRenderModeForWebView(htmlPage, binding.webview);

        html = htmlPage;
        binding.webview.loadDataWithBaseURL("file:///android_asset/", htmlPage, "text/html", "UTF-8", RSS_ITEM_PAGE_URL);
    }

    /**
     * This function has no effect on devices with api level < HONEYCOMB
     */
    private void setSoftwareRenderModeForWebView(String htmlPage, WebView webView) {
        if (htmlPage.contains(".gif")) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                // Workaround some playback issues with gifs on devices below android oreo
                webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            }

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

    private void applyWebSettings() {
        WebSettings webSettings = binding.webview.getSettings();
        //webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setSupportZoom(false);
        webSettings.setMediaPlaybackRequiresUserGesture(true);

        syncIncognitoState();
    }

    @SuppressLint("SetJavaScriptEnabled")
	private void init_webView() {
        int backgroundColor = ColorHelper.getColorFromAttribute(getContext(),
                R.attr.news_detail_background_color);
        binding.webview.setBackgroundColor(backgroundColor);

        applyWebSettings();

        syncIncognitoState();

        binding.webview.setWebChromeClient(new ProgressBarWebChromeClient(binding.progressbarWebview));

        binding.webview.setWebViewClient(new WebViewClient() {

            /*
            private final Map<String, Boolean> loadedUrls = new HashMap<>();

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                //Log.d(TAG, "shouldInterceptRequest: " + url);

                boolean isAd;
                if (!loadedUrls.containsKey(url)) {
                    isAd = AdBlocker.isAd(url);
                    loadedUrls.put(url, isAd);
                } else {
                    isAd = loadedUrls.get(url);
                }
                return isAd ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, url);
            }
            */

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                NewsDetailFragment.this.loadURL(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                addBottomPaddingForFastActions(view);
            }
        });

        /*
        binding.webview.setOnTouchListener((v, event) -> {
            mGestureDetector.onTouchEvent(event);
            return false;
        });
        */
	}

    /**
     * Add free space to bottom of web-site if Fast-Actions are switched on.
     * Otherwise the fast action bar might hide the article content.
     * Method to modify the body margins with JavaScript seems to be dirty, but no other
     * solution seems to be available.
     *
     * This method does (for unknown reasons) not work if WebView gets restored. The Javascript is
     * called but not executed.
     *
     * This is (only) a problem, if user swipes back in viewpager to already loaded articles.
     * Solution might be to switch to a different design.
     *  - Bottom App Bar -- overall cleanest solution but interferes with current implementation
     *    of Podcast Player
     *  - Auto-hiding ActionBar. Hard to implement as scroll behaviour of WebView has to be used
     *    for hiding/showing ActionBar.
     *
     * @param view WebView with article
     */
	private void addBottomPaddingForFastActions(WebView view) {
        if (mPrefs.getBoolean(SettingsActivity.CB_SHOW_FAST_ACTIONS,true)) {
            view.loadUrl("javascript:document.body.style.marginBottom=\"100px\"; void 0");
        }
    }

    /**
     * Loads the given url in the selected view based on user settings (Custom Chrome Tabs, webview or external)
     *
     * @param url address to load
     */
	public void loadURL(String url) {
        int selectedBrowser = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_DISPLAY_BROWSER, "0"));

        File webArchiveFile = DownloadWebPageService.getWebPageArchiveFileForUrl(getActivity(), url);
        if(webArchiveFile.exists()) { // Test if WebArchive exists for url
            binding.tvOfflineVersion.setVisibility(View.VISIBLE);
            binding.webview.loadUrl("file://" + webArchiveFile.getAbsolutePath());
        } else {
            binding.tvOfflineVersion.setVisibility(View.GONE);
            switch (selectedBrowser) {
                case 0: // Custom Tabs
                    final FragmentActivity activity = requireActivity();
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .setStartAnimations(activity, R.anim.slide_in_right, R.anim.slide_out_left)
                            .setExitAnimations(activity, R.anim.slide_in_left, R.anim.slide_out_right)
                            .addDefaultShareMenuItem();
                    try {
                        builder.build().launchUrl(activity, Uri.parse(url));
                    } catch(Exception ex) {
                        Toast.makeText(NewsDetailFragment.this.getContext(), "Invalid URL: " + url, Toast.LENGTH_LONG).show();
                    }
                    break;
                case 1: // External Browser
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    break;
                case 2: // Built in
                    binding.webview.loadUrl(url);
                    break;
                default:
                    throw new IllegalStateException("Unknown selection!");
            }
        }
    }


    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View view, ContextMenu.ContextMenuInfo menuInfo) {
        if (!(view instanceof WebView)) {
            Log.w(TAG, "onCreateContextMenu - no webview reference found");
            return;
        }

        if (view != binding.webview) {
            Log.d(TAG, "onCreateContextMenu - wrong webview - skip creation of context menu");
        }

        WebView.HitTestResult result = ((WebView) view).getHitTestResult();
        if (result == null) {
            Log.d(TAG, "onCreateContextMenu - no webview hit result");
            return;
        }

        if (html == null) {
            Log.e(TAG, "onCreateContextMenu - html is not set - failed to load RSS item");
            return;
        }

        int type = result.getType();
        DialogFragment newFragment = null;

        switch (type) {
            case WebView.HitTestResult.IMAGE_TYPE:
            case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                String imageUrl = result.getExtra();

                if (imageUrl.startsWith("http") || imageUrl.startsWith("file")) {
                    URL mImageUrl;
                    String imgtitle;
                    String imgaltval;
                    String imgsrcval;

                    imgsrcval = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                    Document htmlDoc = Jsoup.parse(html);
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
                    String text = (imgtitle.isEmpty()) ? imgaltval : imgtitle;

                    // Create and show the dialog.
                    newFragment = NewsDetailImageDialogFragment.newInstanceImage(title, titleIcon, text, mImageUrl);
                }
                break;

            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                String url = result.getExtra();
                URL mUrl;
                String text;
                try {
                    Document htmlDoc = Jsoup.parse(html);
                    Elements urltag = htmlDoc.getElementsByAttributeValueContaining("href", url);
                    text = urltag.text();
                    mUrl = new URL(url);
                } catch (MalformedURLException e) {
                    return;
                }

                // Create and show the dialog.
                newFragment = NewsDetailImageDialogFragment.newInstanceUrl(text, mUrl.toString());
                break;
            case WebView.HitTestResult.EMAIL_TYPE:
            case WebView.HitTestResult.GEO_TYPE:
            case WebView.HitTestResult.PHONE_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default:
                Log.v(TAG, "Unknown type: " + type + ". Skipping..");
        }

        if (newFragment != null) {
            FragmentTransaction ft = getParentFragmentManager().beginTransaction();
            newFragment.show(ft, "menu_fragment_dialog");
        }
    }

    /**
     * @return true when the last page on the webview's history stack is
     * the original rss item page
     */
    private boolean isLastPageRssItem() {
        WebBackForwardList list = binding.webview.copyBackForwardList();
        WebHistoryItem lastItem = list.getItemAtIndex(list.getCurrentIndex() - 1);
        return lastItem != null && lastItem.getUrl().equals(RSS_ITEM_PAGE_URL);
    }

    /**
     * @return true when the current page on the webview's history stack is
     * the original rss item page
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isCurrentPageRssItem() {
        if(binding.webview.copyBackForwardList().getCurrentItem() != null) {
            String currentPageUrl = binding.webview.copyBackForwardList().getCurrentItem().getOriginalUrl();
            return currentPageUrl.equals("data:text/html;charset=utf-8;base64,");
        }
        return true;
    }
}
