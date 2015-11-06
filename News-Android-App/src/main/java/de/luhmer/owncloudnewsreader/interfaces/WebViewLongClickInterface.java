package de.luhmer.owncloudnewsreader.interfaces;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.ViewConfiguration;
import android.webkit.JavascriptInterface;

import de.luhmer.owncloudnewsreader.R;

/**
 * Created by David on 25.03.2014.
 */
public class WebViewLongClickInterface {
    Context mContext;

    /** Instantiate the interface and set the context */
    public WebViewLongClickInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @SuppressWarnings("unused")
    @JavascriptInterface
    public void openLinkInBrowser(String urlTemp) {
        if (!urlTemp.startsWith("http://") && !urlTemp.startsWith("https://"))
            urlTemp = "http://" + urlTemp;

        final String url = urlTemp;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext)
                .setTitle(url)
                .setItems(R.array.dialog_share_link_items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // view in browser
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                mContext.startActivity(browserIntent);
                                break;
                            case 1:
                                // share link
                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                sharingIntent.putExtra(Intent.EXTRA_TEXT, url);
                                mContext.startActivity(sharingIntent);
                                break;
                            case 2:
                                // copy link
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText(url, url);
                                clipboard.setPrimaryClip(clip);
                                break;
                        }
                    }
                });


        alertDialog.show();
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void showImage(final String title, final String url) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext)
                .setTitle(url)
                .setMessage(title)
                .setPositiveButton(android.R.string.ok, null);

        alertDialog.show();
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public int getLongPressTimeout() {
        return ViewConfiguration.get(mContext).getLongPressTimeout();
    }
}
