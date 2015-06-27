package de.luhmer.owncloudnewsreader.interfaces;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;

/**
 * Created by David on 25.03.2014.
 */
public class WebViewLinkLongClickInterface {
    Context mContext;

    /** Instantiate the interface and set the context */
    public WebViewLinkLongClickInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void openLinkInBrowser(String urlTemp) {
        if (!urlTemp.startsWith("http://") && !urlTemp.startsWith("https://"))
            urlTemp = "http://" + urlTemp;

        final String url = urlTemp;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext)
                .setNegativeButton("Abort", null)
                .setNeutralButton("Open in browser", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        mContext.startActivity(browserIntent);
                    }
                })
                .setTitle("Link")
                .setMessage("Choose an option below");




        alertDialog.setPositiveButton("Copy link", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText(url, url);
                clipboard.setPrimaryClip(clip);
            }
        });


        alertDialog.show();

        //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        //mContext.startActivity(browserIntent);
    }
}
