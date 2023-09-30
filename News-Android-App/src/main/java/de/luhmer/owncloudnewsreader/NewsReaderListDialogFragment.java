package de.luhmer.owncloudnewsreader;

import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.schedulers.Schedulers;


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
        ((NewsReaderApplication) requireActivity().getApplication()).getAppComponent().injectFragment(this);

        final Bundle args = requireArguments();
        mFeedId = args.getLong("feedid");
        mDialogTitle = args.getString("title");
        mDialogIconUrl = args.getString("iconurl");
        mDialogText = args.getString("feedurl");
        mMenuItems = new LinkedHashMap<>();

        mMenuItems.put(getString(R.string.action_feed_rename), () -> showRenameFeedView(mFeedId, mDialogTitle));

        mMenuItems.put(getString(R.string.action_feed_remove), () -> showRemoveFeedView(mFeedId));

        mMenuItems.put(getString(R.string.action_feed_move), () -> showMoveFeedView(mFeedId));

        mMenuItems.put(getString(R.string.action_feed_notification_settings), () -> showNotificationSettingsView(mFeedId));

        mMenuItems.put(getString(R.string.action_feed_open_in), () -> showOpenSettingsView(mFeedId));

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FloatingDialog);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDialogFeedoptionsBinding.inflate(inflater, container, false);

        FavIconHandler favIconHandler = new FavIconHandler(requireContext());
        favIconHandler.loadFavIconForFeed(mDialogIconUrl, binding.icMenuFeedicon);

        binding.tvMenuTitle.setText(mDialogTitle);
        binding.tvMenuText.setText(mDialogText);

        binding.tvMenuText.setOnClickListener(v -> {
            if (mDialogText != null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(mDialogText));
                startActivity(i);
            }
        });

        List<String> menuItemsList = new ArrayList<>(mMenuItems.keySet());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.fragment_dialog_listviewitem,
                menuItemsList);

        binding.lvMenuList.setAdapter(arrayAdapter);

        binding.lvMenuList.setOnItemClickListener((adapterView, view, i, l) -> {
            String key = arrayAdapter.getItem(i);
            MenuAction mAction = mMenuItems.get(key);
            mAction.execute();
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
                binding.buttonRenameConfirm.setEnabled(!s.toString().equals(feedName) && s.length() != 0);
            }
        });

        binding.buttonRenameCancel.setOnClickListener(v -> dismiss());

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
                        .subscribe(() -> {
                            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
                            dbConn.renameFeedById(mFeedId, binding.renamefeedFeedname.getText().toString());

                            parentActivity.getSlidingListFragment().reloadAdapter();
                            parentActivity.startSync();

                            dismiss();
                        }, throwable -> {
                            Toast.makeText(getContext().getApplicationContext(), getString(R.string.login_dialog_text_something_went_wrong) + " - " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            dismiss();
                        });
            }
        });
    }


    private void showRemoveFeedView(final long feedId) {
        binding.lvMenuList.setVisibility(View.GONE);
        binding.removeFeedDialog.setVisibility(View.VISIBLE);

        binding.buttonRemoveCancel.setOnClickListener(v -> dismiss());

        binding.buttonRemoveConfirm.setOnClickListener(v -> {
            showProgress(true);
            setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);


            mApi.getNewsAPI().deleteFeed(feedId)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
                        dbConn.removeFeedById(mFeedId);

                        Long currentFeedId = parentActivity.getNewsReaderDetailFragment().getIdFeed();
                        if(currentFeedId != null && currentFeedId == mFeedId) {
                            parentActivity.switchToAllUnreadItemsFolder();
                        }
                        parentActivity.getSlidingListFragment().reloadAdapter();
                        parentActivity.updateCurrentRssView();

                        dismiss();
                    }, throwable -> {
                        Toast.makeText(getContext().getApplicationContext(), getString(R.string.login_dialog_text_something_went_wrong) + " - " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        dismiss();
                    });
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
        final List<Folder> folders = new ArrayList<>(dbConn.getListOfFolders());
        folders.add(new Folder(0, getString(R.string.move_feed_root_folder))); // root folder (fake insert it here since this folder is not synced)
        List<String> folderNames = new ArrayList<>();

        for(Folder folder: folders) {
            folderNames.add(folder.getLabel());
        }

        ArrayAdapter<String> folderAdapter = new ArrayAdapter<> (getActivity(), R.layout.dialog_list_folder, android.R.id.text1, folderNames);
        binding.folderList.setAdapter(folderAdapter);
        binding.folderList.setOnItemClickListener((parent, view, position, id) -> {
            final Folder folder = folders.get(position);

            showProgress(true);
            setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);

            Map<String, Long> paramMap = new LinkedHashMap<>();
            paramMap.put("folderId", folder.getId());
            mApi.getNewsAPI().moveFeed(mFeedId, paramMap)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        DatabaseConnectionOrm dbConn1 = new DatabaseConnectionOrm(getContext());
                        Feed feed = dbConn1.getFeedById(mFeedId);
                        feed.setFolder(folder);

                        parentActivity.getSlidingListFragment().reloadAdapter();
                        parentActivity.startSync();

                        dismiss();
                    }, throwable -> {
                        Toast.makeText(getContext().getApplicationContext(), getString(R.string.login_dialog_text_something_went_wrong) + " - " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        dismiss();
                    });
        });
    }

    private void showOpenSettingsView(final long feedId) {
        binding.lvMenuList.setVisibility(View.GONE);
        binding.openFeedDialog.setVisibility(View.VISIBLE);

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
        Feed feed = dbConn.getFeedById(feedId);
        Long openIn = feed.getOpenIn();

        binding.openInUseGeneralSetting.setChecked(false);
        binding.openInDetailedView.setChecked(false);
        binding.openInBrowserCct.setChecked(false);
        binding.openInBrowserExternal.setChecked(false);

        if (openIn == null) {
            binding.openInUseGeneralSetting.setChecked(true);
        } else {
            switch (openIn.intValue()) {
                case 1:
                    binding.openInDetailedView.setChecked(true);
                    break;
                case 2:
                    binding.openInBrowserCct.setChecked(true);
                    break;
                case 3:
                    binding.openInBrowserExternal.setChecked(true);
                    break;
                default:
                    throw new RuntimeException("Unreachable: openIn has illegal value " + openIn);
            }
        }

        binding.openInUseGeneralSetting.setOnCheckedChangeListener((button, checked)
                -> setOpenInForFeed(feed, null, checked));
        binding.openInDetailedView.setOnCheckedChangeListener((button, checked)
                -> setOpenInForFeed(feed, 1L, checked));
        binding.openInBrowserCct.setOnCheckedChangeListener((button, checked)
                -> setOpenInForFeed(feed, 2L, checked));
        binding.openInBrowserExternal.setOnCheckedChangeListener((button, checked)
                -> setOpenInForFeed(feed, 3L, checked));
    }

    private void showNotificationSettingsView(final long feedId) {
        binding.lvMenuList.setVisibility(View.GONE);
        binding.notificationFeedDialog.setVisibility(View.VISIBLE);

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
        Feed feed = dbConn.getFeedById(feedId);
        String notificationChannel = feed.getNotificationChannel();

        binding.notificationSettingNone.setChecked(false);
        binding.notificationSettingDefault.setChecked(false);
        binding.notificationSettingUnique.setChecked(false);

        switch (notificationChannel) {
            case "none":
                binding.notificationSettingNone.setChecked(true);
                break;
            case "default":
                binding.notificationSettingDefault.setChecked(true);
                break;
            default:
                binding.notificationSettingUnique.setChecked(true);
                break;
        }

        binding.notificationSettingNone.setOnCheckedChangeListener((button, checked) ->
                setNotificationChannelForFeed(feed, "none", checked));
        binding.notificationSettingDefault.setOnCheckedChangeListener((button, checked) ->
                setNotificationChannelForFeed(feed, "default", checked));
        binding.notificationSettingUnique.setOnCheckedChangeListener((button, checked) ->
                // Use the feed name as notification channel name
                setNotificationChannelForFeed(feed, feed.getFeedTitle(), checked));
    }

    private void setOpenInForFeed(Feed feed, Long openIn, Boolean checked) {
        if (checked) {
            feed.setOpenIn(openIn);
            feed.update();
            this.showOpenSettingsView(feed.getId()); // reload dialog
        }
    }

    private void setNotificationChannelForFeed(Feed feed, String channel, Boolean checked) {
        if (checked) {
            feed.setNotificationChannel(channel);
            feed.update();
            this.showNotificationSettingsView(feed.getId()); // reload dialog
        }
    }

    interface MenuAction {
        void execute();
    }
}
