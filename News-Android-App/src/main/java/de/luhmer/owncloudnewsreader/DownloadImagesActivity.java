package de.luhmer.owncloudnewsreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import de.luhmer.owncloudnewsreader.services.DownloadImagesService;

public class DownloadImagesActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final long highestItemIdBeforeSync = getIntent().getLongExtra("highestItemIdBeforeSync", 0);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(getString(R.string.no_wifi_available));

        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.do_you_want_to_download_without_wifi))
                .setCancelable(true)
                .setPositiveButton(getString(android.R.string.yes) ,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        Intent service = new Intent(DownloadImagesActivity.this, DownloadImagesService.class);
                        service.putExtra(DownloadImagesService.LAST_ITEM_ID, highestItemIdBeforeSync);
                        service.putExtra(DownloadImagesService.DOWNLOAD_MODE_STRING, DownloadImagesService.DownloadMode.PICTURES_ONLY);
                        DownloadImagesActivity.this.startService(service);

                        DownloadImagesActivity.this.finish();
                    }
                })
                .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DownloadImagesActivity.this.finish();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}