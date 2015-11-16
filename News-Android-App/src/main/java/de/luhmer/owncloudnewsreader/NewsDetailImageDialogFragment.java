package de.luhmer.owncloudnewsreader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by david on 15.11.15.
 */
public class NewsDetailImageDialogFragment extends DialogFragment {

    public enum TYPE { IMAGE, URL }

    static NewsDetailImageDialogFragment newInstanceImage(String dialogTitle, Integer titleIcon, String dialogText, URL imageUrl) {
        NewsDetailImageDialogFragment f = new NewsDetailImageDialogFragment();

        if(titleIcon == null) {
            titleIcon = android.R.drawable.ic_menu_info_details;
        }

        Bundle args = new Bundle();
        args.putSerializable("dialogType", TYPE.IMAGE);
        args.putInt("titleIcon", titleIcon);
        args.putString("title", dialogTitle);
        args.putString("text", dialogText);
        args.putSerializable("imageUrl", imageUrl);
        f.setArguments(args);
        return f;
    }

    static NewsDetailImageDialogFragment newInstanceUrl(String dialogTitle, String dialogText) {
        NewsDetailImageDialogFragment f = new NewsDetailImageDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("dialogType", TYPE.URL);
        args.putInt("titleIcon", android.R.drawable.ic_menu_info_details);
        args.putString("title", dialogTitle);
        args.putString("text", dialogText);
        f.setArguments(args);
        return f;
    }

    private int mDialogIcon;
    private String mDialogTitle;
    private String mDialogText;
    private URL mImageUrl;
    private TYPE mDialogType;

    private long downloadID;
    private DownloadManager dlManager;
    private BroadcastReceiver downloadCompleteReceiver;

    private HashMap<String, MenuAction> mMenuItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialogIcon = getArguments().getInt("titleIcon");
        mDialogTitle = getArguments().getString("title");
        mDialogText = getArguments().getString("text");
        mImageUrl = (URL) getArguments().getSerializable("imageUrl");
        mDialogType = (TYPE) getArguments().getSerializable("dialogType");

        mMenuItems = new HashMap<>();

        //Build the menu
        switch(mDialogType) {
            case IMAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    mMenuItems.put("Download Image", new MenuAction() {
                        @Override
                        public void execute() {
                            downloadImage(mImageUrl);
                        }
                    });
                }
                mMenuItems.put("Share Image", new MenuAction() {
                    @Override
                    public void execute() {
                        shareImage();
                    }
                });
                mMenuItems.put("Open Image in browser", new MenuAction() {
                    @Override
                    public void execute() {
                        openLinkInBrowser(mImageUrl);
                    }
                });
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    mMenuItems.put("Copy Link", new MenuAction() {
                        @Override
                        public void execute() {
                            copyToCipboard(mDialogTitle, mImageUrl.toString());
                        }
                    });
                }
                break;
            case URL:
                mMenuItems.put("Share Link", new MenuAction() {
                    @Override
                    public void execute() {
                        shareLink();
                    }
                });
                mMenuItems.put("Open Link in browser", new MenuAction() {
                    @Override
                    public void execute() {
                        try {
                            openLinkInBrowser(new URL(mDialogText));
                        } catch (MalformedURLException e) {
                            Toast.makeText(getActivity(), "Can not parse url!", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    mMenuItems.put("Copy Link", new MenuAction() {
                        @Override
                        public void execute() {
                            copyToCipboard(mDialogTitle, mDialogText);
                        }
                    });
                }
                break;
        }


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

        /*
        //Hide the header (title and image) for urls
        if(mDialogType == TYPE.URL) {
            tvTitle.setVisibility(View.GONE);
            imgTitle.setVisibility(View.GONE);
        }*/

        tvTitle.setText(mDialogTitle);
        tvText.setText(mDialogText);
        imgTitle.setImageResource(mDialogIcon);

        ListView mListView = (ListView) v.findViewById(R.id.ic_menu_item_list);
        List<String> menuItemsList = new ArrayList<>(mMenuItems.keySet());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                menuItemsList);

        mListView.setAdapter(arrayAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String key = arrayAdapter.getItem(i);
                MenuAction mAction = mMenuItems.get(key);
                mAction.execute();
            }
        });
        return v;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void copyToCipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
        getDialog().dismiss();
    }

    private void shareImage() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mDialogText);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mImageUrl.toString());
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
        getDialog().dismiss();
    }

    private void shareLink() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mDialogTitle);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mDialogText);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
        getDialog().dismiss();
    }


    private void openLinkInBrowser(URL url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url.toString()));
        startActivity(i);
        getDialog().dismiss();
    }

    @Override
    public void onDestroyView() {
        unregisterImageDownloadReceiver();
        super.onDestroyView();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void downloadImage(URL url) {
        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_img_download_wait), Toast.LENGTH_SHORT).show();
        getDialog().setCancelable(false);

        if(isExternalStorageWritable()) {
            String filename = url.getFile().substring(url.getFile().lastIndexOf('/') + 1, url.getFile().length());
            dlManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.toString()));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            request.setTitle("Downloading image");
            request.setDescription(filename);
            request.setVisibleInDownloadsUi(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            }
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
            @TargetApi(Build.VERSION_CODES.GINGERBREAD)
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


    interface MenuAction {
        void execute();
    }
}
