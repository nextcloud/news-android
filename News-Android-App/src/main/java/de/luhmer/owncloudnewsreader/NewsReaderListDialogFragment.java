package de.luhmer.owncloudnewsreader;

import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.databinding.FragmentDialogFeedoptionsBinding;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class NewsReaderListDialogFragment extends DialogFragment {

    protected @Inject ApiProvider mApi;

    private long mFeedId;
    private String mDialogTitle;
    private String mDialogText;
    private String mDialogIconUrl;

    private LinkedHashMap<String, MenuAction> mMenuItems;
    private NewsReaderListActivity parentActivity;

    protected FragmentDialogFeedoptionsBinding binding;


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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((NewsReaderApplication) getActivity().getApplication()).getAppComponent().injectFragment(this);

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

        mMenuItems.put(getString(R.string.action_feed_move), new MenuAction() {
            @Override
            public void execute() { showMoveFeedView(mFeedId); }
        });

        int style = DialogFragment.STYLE_NO_TITLE;
        int theme = R.style.FloatingDialog;
        setStyle(style, theme);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDialogFeedoptionsBinding.inflate(inflater, container, false);

        FavIconHandler favIconHandler = new FavIconHandler(getContext());
        favIconHandler.loadFavIconForFeed(mDialogIconUrl, binding.icMenuFeedicon);

        binding.tvMenuTitle.setText(mDialogTitle);
        binding.tvMenuText.setText(mDialogText);

        binding.tvMenuText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialogText != null) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(mDialogText));
                    startActivity(i);
                }
            }
        });

        List<String> menuItemsList = new ArrayList<>(mMenuItems.keySet());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.fragment_dialog_listviewitem,
                menuItemsList);

        binding.lvMenuList.setAdapter(arrayAdapter);

        binding.lvMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String key = arrayAdapter.getItem(i);
                MenuAction mAction = mMenuItems.get(key);
                mAction.execute();
            }
        });
        return binding.getRoot();
    }


    public void setActivity(Activity parentActivity) {
        this.parentActivity =  (NewsReaderListActivity)parentActivity;

    }


    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        binding.renameFeedDialog.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.removeFeedDialog.setVisibility(show ? View.GONE : View.VISIBLE);

        binding.progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
        });
    }


    private void showRenameFeedView(final long feedId, final String feedName) {
        binding.renamefeedFeedname.setText(feedName);
        binding.buttonRenameConfirm.setEnabled(false);

        binding.lvMenuList.setVisibility(View.GONE);
        binding.renameFeedDialog.setVisibility(View.VISIBLE);

        binding.renamefeedFeedname.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.toString().equals(feedName) || s.length() == 0) {
                    binding.buttonRenameConfirm.setEnabled(false);
                } else {
                    binding.buttonRenameConfirm.setEnabled(true);
                }
            }
        });

        binding.buttonRenameCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        binding.buttonRenameConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showProgress(true);
                setCancelable(false);
                getDialog().setCanceledOnTouchOutside(false);


                Map<String, String> paramMap = new LinkedHashMap<>();
                paramMap.put("feedTitle", binding.renamefeedFeedname.getText().toString());
                mApi.getNewsAPI().renameFeed(feedId, paramMap)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
                                dbConn.renameFeedById(mFeedId, binding.renamefeedFeedname.getText().toString());

                                parentActivity.getSlidingListFragment().reloadAdapter();
                                parentActivity.startSync();

                                dismiss();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                Toast.makeText(getContext().getApplicationContext(), getString(R.string.login_dialog_text_something_went_wrong) + " - " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                                dismiss();
                            }
                        });
            }
        });
    }


    private void showRemoveFeedView(final long feedId) {
        binding.lvMenuList.setVisibility(View.GONE);
        binding.removeFeedDialog.setVisibility(View.VISIBLE);

        binding.buttonRemoveCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        binding.buttonRemoveConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showProgress(true);
                setCancelable(false);
                getDialog().setCanceledOnTouchOutside(false);


                mApi.getNewsAPI().deleteFeed(feedId)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
                                dbConn.removeFeedById(mFeedId);

                                Long currentFeedId = parentActivity.getNewsReaderDetailFragment().getIdFeed();
                                if(currentFeedId != null && currentFeedId == mFeedId) {
                                    parentActivity.switchToAllUnreadItemsFolder();
                                }
                                parentActivity.getSlidingListFragment().reloadAdapter();
                                parentActivity.updateCurrentRssView();

                                dismiss();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                Toast.makeText(getContext().getApplicationContext(), getString(R.string.login_dialog_text_something_went_wrong) + " - " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                                dismiss();
                            }
                        });
            }
        });
    }


    /**
     * https://github.com/nextcloud/news/blob/master/docs/externalapi/Legacy.md#move-a-feed-to-a-different-folder
     * @param mFeedId Feed to move
     */
    private void showMoveFeedView(final long mFeedId) {
        binding.lvMenuList.setVisibility(View.GONE);
        binding.moveFeedDialog.setVisibility(View.VISIBLE);

        binding.tvMenuText.setText(getString(R.string.feed_move_list_description));

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
        final List<Folder> folders = dbConn.getListOfFolders();
        folders.add(new Folder(0, getString(R.string.move_feed_root_folder))); // root folder (fake insert it here since this folder is not synced)
        List<String> folderNames = new ArrayList<>();

        for(Folder folder: folders) {
            folderNames.add(folder.getLabel());
        }

        ArrayAdapter<String> folderAdapter = new ArrayAdapter<> (getActivity(), R.layout.dialog_list_folder, android.R.id.text1, folderNames);
        binding.folderList.setAdapter(folderAdapter);
        binding.folderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Folder folder = folders.get(position);

                showProgress(true);
                setCancelable(false);
                getDialog().setCanceledOnTouchOutside(false);

                Map<String, Long> paramMap = new LinkedHashMap<>();
                paramMap.put("folderId", folder.getId());
                mApi.getNewsAPI().moveFeed(mFeedId, paramMap)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action() {
                            @Override
                            public void run() {
                                DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
                                Feed feed = dbConn.getFeedById(mFeedId);
                                feed.setFolder(folder);

                                parentActivity.getSlidingListFragment().reloadAdapter();
                                parentActivity.startSync();

                                dismiss();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                Toast.makeText(getContext().getApplicationContext(), getString(R.string.login_dialog_text_something_went_wrong) + " - " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                                dismiss();
                            }
                        });
            }
        });

    }

    interface MenuAction {
        void execute();
    }
}
