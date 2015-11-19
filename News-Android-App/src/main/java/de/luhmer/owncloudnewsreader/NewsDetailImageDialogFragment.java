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
import android.content.SharedPreferences;
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


import com.nononsenseapps.filepicker.FilePickerActivity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import de.luhmer.owncloudnewsreader.helper.ThemeChooser;

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
    private DownloadManager downloadManager;
    private BroadcastReceiver downloadCompleteReceiver;

    private LinkedHashMap<String, MenuAction> mMenuItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialogIcon = getArguments().getInt("titleIcon");
        mDialogTitle = getArguments().getString("title");
        mDialogText = getArguments().getString("text");
        mImageUrl = (URL) getArguments().getSerializable("imageUrl");
        mDialogType = (TYPE) getArguments().getSerializable("dialogType");

        mMenuItems = new LinkedHashMap<>();

        //Build the menu
        switch(mDialogType) {
            case IMAGE:
                mMenuItems.put(getString(R.string.action_img_download), new MenuActionLongClick() {
                    @Override
                    public void execute() {
                        downloadImage(mImageUrl);
                    }
                    public void executeLongClick() {
                        configureDownloadPath(mImageUrl);
                    }
                });
                mMenuItems.put(getString(R.string.action_img_open), new MenuAction() {
                    @Override
                    public void execute() {
                        openLinkInBrowser(mImageUrl);
                    }
                });
                mMenuItems.put(getString(R.string.action_img_sharelink), new MenuAction() {
                    @Override
                    public void execute() {
                        shareImage();
                    }
                });
                mMenuItems.put(getString(R.string.action_img_copylink), new MenuAction() {
                    @Override
                    public void execute() {
                        copyToCipboard(mDialogTitle, mImageUrl.toString());
                    }
                });
                break;
            case URL:
                mMenuItems.put(getString(R.string.action_link_open), new MenuAction() {
                    @Override
                    public void execute() {
                        try {
                            openLinkInBrowser(new URL(mDialogText));
                        } catch (MalformedURLException e) {
                            Toast.makeText(getActivity(), getString(R.string.error_invalid_url), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
                mMenuItems.put(getString(R.string.action_link_share), new MenuAction() {
                    @Override
                    public void execute() {
                        shareLink();
                    }
                });
                mMenuItems.put(getString(R.string.action_link_copy), new MenuAction() {
                    @Override
                    public void execute() {
                        copyToCipboard(mDialogTitle, mDialogText);
                    }
                });
                break;
        }

        int style = DialogFragment.STYLE_NO_TITLE;
        int theme = ThemeChooser.isDarkTheme(getActivity())
                ? R.style.Theme_Material_Dialog_Floating
                : R.style.Theme_Material_Light_Dialog_Floating;
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

        tvTitle.setText(mDialogTitle);
        tvText.setText(mDialogText);
        imgTitle.setImageResource(mDialogIcon);

        if(mDialogType == TYPE.IMAGE) {
            if(mDialogText.equals(mDialogTitle) || mDialogText.equals("")) {
                tvText.setVisibility(View.GONE);
            }
        }

        final ListView mListView = (ListView) v.findViewById(R.id.ic_menu_item_list);
        List<String> menuItemsList = new ArrayList<>(mMenuItems.keySet());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.fragment_dialog_listviewitem,
                menuItemsList);

        mListView.setAdapter(arrayAdapter);
        mListView.setLongClickable(true);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String key = arrayAdapter.getItem(i);
                MenuAction mAction = mMenuItems.get(key);
                mAction.execute();
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                String key = arrayAdapter.getItem(position);
                try {
                    MenuActionLongClick mAction = (MenuActionLongClick) mMenuItems.get(key);
                    mAction.executeLongClick();
                } catch (ClassCastException e) {
                    return false;
                }
                return true;
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        unregisterImageDownloadReceiver();
        super.onDestroyView();
    }


    private void copyToCipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), getString(R.string.toast_copied_to_clipboard), Toast.LENGTH_SHORT).show();
        getDialog().dismiss();
    }

    private void shareImage() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mDialogText);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mImageUrl.toString());
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.intent_title_share)));
        getDialog().dismiss();
    }

    private void shareLink() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mDialogTitle);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mDialogText);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.intent_title_share)));
        getDialog().dismiss();
    }


    private void openLinkInBrowser(URL url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url.toString()));
        startActivity(i);
        getDialog().dismiss();
    }

    private void downloadImage(URL url) {
        System.out.println("DOWNLOADDIR DOWNLOADDIR DOWNLOADDIR DOWNLOADDIR");
        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_img_download_wait), Toast.LENGTH_SHORT).show();

        if(isExternalStorageWritable()) {
            String filename = url.getFile().substring(url.getFile().lastIndexOf('/') + 1, url.getFile().length());
            downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.toString()));
            //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            request.setDestinationUri(getDownloadDir(filename));
            request.setTitle("Downloading image");
            request.setDescription(filename);
            request.setVisibleInDownloadsUi(false);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            downloadID = downloadManager.enqueue(request);
            getDialog().hide();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_img_notwriteable), Toast.LENGTH_LONG).show();
            getDialog().dismiss();
        }
    }

    private Uri getDownloadDir(String filename) {
        System.out.println("*************************************: getDownloadDir");
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String savedDir = sharedPref.getString("manualImageDownloadLocation", "");
        String def = "";
        if(savedDir == "") {
            def = Environment.getExternalStorageDirectory().toString();//.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
            System.out.println("********************************EMPTY: " +def);
        } else {
            def = savedDir;
            System.out.println("********************************FULL: " +def);
        }
        String tmp = "file://" +def +"/" +filename;
        System.out.println("********************************return: " +tmp);
        return Uri.parse(tmp);
    }

    private void configureDownloadPath(URL url) {
        //getDialog().hide();
        Intent i = new Intent(getContext(), FilePickerActivity.class);
        // This works if you defined the intent filter
         //Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
        //i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, getActivity().getPreferences(Context.MODE_PRIVATE).getString("manualImageDownloadLocation", ""));


        startActivityForResult(i, 1000);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("§§§§§§§§§§§§§§§§§§§§§§0: onActivityResult" );
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            System.out.println("§§§§§§§§§§§§§§§§§§§§§§1: onActivityResult");

            Uri uri = data.getData();
            System.out.println("§§§§§§§§§§§§§§§§§§§§§§3 uri: " +uri.getPath() );

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("manualImageDownloadLocation", uri.getPath());
            editor.commit();




        }
        //downloadImage(mImageUrl);
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
                    Cursor cursor = downloadManager.query(query);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = cursor.getInt(columnIndex);
                    int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                    int reason = cursor.getInt(columnReason);

                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_img_saved), Toast.LENGTH_LONG).show();
                            getDialog().dismiss();
                            break;
                        case DownloadManager.STATUS_FAILED:
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_download_failed) +": " +reason, Toast.LENGTH_LONG).show();
                            getDialog().dismiss();
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
    interface MenuActionLongClick extends MenuAction {
        void executeLongClick();
    }
}
