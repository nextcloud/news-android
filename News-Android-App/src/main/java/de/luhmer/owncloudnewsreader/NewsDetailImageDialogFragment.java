package de.luhmer.owncloudnewsreader;

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
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15.11.15.
 */
public class NewsDetailImageDialogFragment extends DialogFragment {

    static NewsDetailImageDialogFragment newInstance(String title, int titleIcon, String text, URL imageUrl) {
        NewsDetailImageDialogFragment f = new NewsDetailImageDialogFragment();
        Bundle args = new Bundle();
        args.putInt("titleIcon", titleIcon);
        args.putString("title", title);
        args.putString("text", text);
        args.putSerializable("imageUrl", imageUrl);
        f.setArguments(args);
        return f;
    }

    private int mTitleIcon;
    private String mTitle;
    private String mText;
    private URL mImageUrl;

    private long downloadID;
    private DownloadManager dlManager;
    private BroadcastReceiver downloadCompleteReceiver;


    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitleIcon = getArguments().getInt("titleIcon");
            mTitle = getArguments().getString("title");
            mText = getArguments().getString("text");
            mImageUrl = (URL) getArguments().getSerializable("imageUrl");

            int style = DialogFragment.STYLE_NO_TITLE;
            int theme = android.R.style.Theme_Holo_Dialog;
            setStyle(style, theme);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            registerImageDownloadReceiver();

            View v = inflater.inflate(R.layout.fragment_dialog_image, container, false);

            TextView tvTitle = (TextView) v.findViewById(R.id.ic_menu_title);
            TextView tvText = (TextView) v.findViewById(R.id.ic_menu_item_text);
            ImageView imgTitle = (ImageView) v.findViewById(R.id.ic_menu_gallery);

            tvTitle.setText(mTitle);
            tvText.setText(mText);
            imgTitle.setImageResource(mTitleIcon);

            ListView mListView = (ListView) v.findViewById(R.id.ic_menu_item_list);

            List<String> menuItem = new ArrayList<>();
            menuItem.add("Download Image");
            menuItem.add("Share Image");
            menuItem.add("Open Image in browser");
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.simple_list_item_1,
                    menuItem );

            mListView.setAdapter(arrayAdapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    switch (i) {
                        case 0:
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_img_download_wait), Toast.LENGTH_SHORT).show();
                            getDialog().setCancelable(false);
                            downloadImage(mImageUrl);
                            break;
                        case 1:
                            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mText);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mImageUrl.toString());
                            startActivity(Intent.createChooser(sharingIntent, "Share via"));
                            getDialog().dismiss();
                            break;
                        case 2:
                            openImageInBrowser(mImageUrl);
                            getDialog().dismiss();
                            break;
                    }
                }
            });
            return v;
        }

    @Override
    public void onDestroyView() {
        unregisterImageDownloadReceiver();
        super.onDestroyView();
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
                            getDialog().dismiss();
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

}
