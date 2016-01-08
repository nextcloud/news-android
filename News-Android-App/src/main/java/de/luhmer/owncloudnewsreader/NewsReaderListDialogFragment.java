package de.luhmer.owncloudnewsreader;

import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.reader.HttpJsonRequest;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.apiv2.APIv2;


public class NewsReaderListDialogFragment extends DialogFragment{

    static NewsReaderListDialogFragment newInstance(long feedId, String dialogTitle, String iconurl, String feedurl) {
        NewsReaderListDialogFragment f = new NewsReaderListDialogFragment();

        Bundle args = new Bundle();
        args.putLong("feedid", feedId);
        args.putString("title", dialogTitle);
        args.putString("iconurl", iconurl);
        args.putString("feedurl", feedurl);

        f.setArguments(args);
        return f;
    }


    private long mFeedId;
    private String mDialogTitle;
    private String mDialogText;
    private String mDialogIconUrl;

    private RemoveFeedTask mRemoveFeedTask = null;
    private RenameFeedTask mRenameFeedTask = null;
    private LinkedHashMap<String, MenuAction> mMenuItems;

    private NewsReaderListActivity parentActivity;

    private RelativeLayout mRemoveFeedDialogView, mRenameFeedDialogView, mProgressView;
    private Button mButtonRemoveConfirm, mButtonRemoveCancel, mButtonRenameConfirm, mButtonRenameCancel;
    private ListView mListView;
    private EditText mFeedName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFeedId = getArguments().getLong("feedid");
        mDialogTitle = getArguments().getString("title");
        mDialogIconUrl = getArguments().getString("iconurl");
        mDialogText = getArguments().getString("feedurl");
        mMenuItems = new LinkedHashMap<>();

        mMenuItems.put(getString(R.string.action_feed_rename), new MenuAction() {
            @Override
            public void execute() {
                showRenameFeedView(mFeedId, mDialogTitle);
            }
        });

        mMenuItems.put(getString(R.string.action_feed_remove), new MenuAction() {
            @Override
            public void execute() {
                showRemoveFeedView(mFeedId);
            }
        });

        int style = DialogFragment.STYLE_NO_TITLE;
        int theme = ThemeChooser.isDarkTheme(getActivity())
                ? R.style.FloatingDialog
                : R.style.FloatingDialogLight;
        setStyle(style, theme);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dialog_feedoptions, container, false);

        TextView tvTitle = (TextView) v.findViewById(R.id.tv_menu_title);
        TextView tvText = (TextView) v.findViewById(R.id.tv_menu_text);
        ImageView imgTitle = (ImageView) v.findViewById(R.id.ic_menu_feedicon);

        mRemoveFeedDialogView = (RelativeLayout) v.findViewById(R.id.remove_feed_dialog);
        mRenameFeedDialogView = (RelativeLayout) v.findViewById(R.id.rename_feed_dialog);
        mProgressView = (RelativeLayout) v.findViewById(R.id.progressView);
        mButtonRemoveConfirm = (Button) v.findViewById(R.id.button_remove_confirm);
        mButtonRemoveCancel = (Button) v.findViewById(R.id.button_remove_cancel);
        mButtonRenameConfirm = (Button) v.findViewById(R.id.button_rename_confirm);
        mButtonRenameCancel = (Button) v.findViewById(R.id.button_rename_cancel);
        mFeedName = (EditText) v.findViewById(R.id.renamefeed_feedname);

        FavIconHandler favIconHandler = new FavIconHandler(getContext());
        favIconHandler.loadFavIconForFeed(mDialogIconUrl, imgTitle);

        tvTitle.setText(mDialogTitle);
        tvText.setText(mDialogText);

        tvText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialogText != null) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(mDialogText));
                    startActivity(i);
                }
            }
        });

        mListView = (ListView) v.findViewById(R.id.lv_menu_list);
        List<String> menuItemsList = new ArrayList<>(mMenuItems.keySet());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.fragment_dialog_listviewitem,
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


    public void setActivity(Activity parentActivity) {
        this.parentActivity =  (NewsReaderListActivity)parentActivity;

    }


    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mRenameFeedDialogView.setVisibility(show ? View.GONE : View.VISIBLE);
        mRemoveFeedDialogView.setVisibility(show ? View.GONE : View.VISIBLE);

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
        });
    }


    private void showRenameFeedView(final long feedId, final String feedName) {
        mFeedName.setText(feedName);
        mButtonRenameConfirm.setEnabled(false);

        mListView.setVisibility(View.GONE);
        mRenameFeedDialogView.setVisibility(View.VISIBLE);

        mFeedName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.toString().equals(feedName) || s.length() == 0) {
                    mButtonRenameConfirm.setEnabled(false);
                } else {
                    mButtonRenameConfirm.setEnabled(true);
                }
            }
        });

        mButtonRenameCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        mButtonRenameConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showProgress(true);
                setCancelable(false);
                getDialog().setCanceledOnTouchOutside(false);
                mRenameFeedTask = new RenameFeedTask(feedId, mFeedName.getText().toString() );
                mRenameFeedTask.execute((Void) null);
            }
        });
    }


    private void showRemoveFeedView(final long feedId) {
        mListView.setVisibility(View.GONE);
        mRemoveFeedDialogView.setVisibility(View.VISIBLE);

        mButtonRemoveCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        mButtonRemoveConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showProgress(true);
                setCancelable(false);
                getDialog().setCanceledOnTouchOutside(false);
                mRemoveFeedTask = new RemoveFeedTask(feedId);
                mRemoveFeedTask.execute((Void) null);
            }
        });
    }


    public class RemoveFeedTask extends AsyncTask<Void, Void, Boolean> {

        private final long mFeedId;

        RemoveFeedTask(long feedId) {
            this.mFeedId = feedId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            API api = new APIv2(HttpJsonRequest.getInstance().getRootUrl());

            try {
                int status = HttpJsonRequest.getInstance().performRemoveFeedRequest(api.getFeedUrl(),
                        mFeedId);
                if(status == 200) {
                    return true;
                }

                Log.d("NewFeedActivity", "Status: " + status);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRemoveFeedTask = null;

            if (success) {
                DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
                dbConn.removeFeedById(mFeedId);

                Long currentFeedId = parentActivity.getNewsReaderDetailFragment().getIdFeed();
                if(currentFeedId != null && currentFeedId == mFeedId) {
                    parentActivity.switchToAllUnreadItemsFolder();
                }
                parentActivity.getSlidingListFragment().ReloadAdapter();
                parentActivity.updateCurrentRssView();
            } else {
                Toast.makeText(getContext().getApplicationContext(), getString(R.string.login_dialog_text_something_went_wrong), Toast.LENGTH_LONG).show();
            }
            dismiss();
        }

        @Override
        protected void onCancelled() {
            mRemoveFeedTask = null;
            dismiss();
        }
    }


    public class RenameFeedTask extends AsyncTask<Void, Void, Boolean> {

        private final long mFeedId;
        private final String mFeedName;

        RenameFeedTask(long feedId, String newName) {
            mFeedId = feedId;
            mFeedName = newName;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            API api = new APIv2(HttpJsonRequest.getInstance().getRootUrl());

            try {
                int status = HttpJsonRequest.getInstance().performRenameFeedRequest(api.getFeedUrl(),
                        mFeedId, mFeedName);
                if(status == 200) {
                    return true;
                }
                Log.d("NewFeedActivity", "Status: " + status);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRenameFeedTask = null;

            if (success) {
                DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
                dbConn.renameFeedById(mFeedId, mFeedName);

                parentActivity.getSlidingListFragment().ReloadAdapter();
                parentActivity.startSync();
            } else {
                Toast.makeText(getContext().getApplicationContext(), getString(R.string.login_dialog_text_something_went_wrong), Toast.LENGTH_LONG).show();
            }
            dismiss();
        }

        @Override
        protected void onCancelled() {
            mRenameFeedTask = null;
            dismiss();
        }
    }


    interface MenuAction {
        void execute();
    }
}
