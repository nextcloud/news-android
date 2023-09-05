package de.luhmer.owncloudnewsreader;

import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import de.luhmer.owncloudnewsreader.databinding.FragmentDialogFolderoptionsBinding;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import de.luhmer.owncloudnewsreader.reader.nextcloud.NewsAPI;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class FolderOptionsDialogFragment extends DialogFragment {

    protected @Inject ApiProvider mApi;

    private long mFolderId;
    private String mDialogTitle;

    private LinkedHashMap<String, MenuAction> mMenuItems;
    private NewsReaderListActivity parentActivity;

    protected FragmentDialogFolderoptionsBinding binding;


    static FolderOptionsDialogFragment newInstance(long folderId, String dialogTitle) {
        FolderOptionsDialogFragment f = new FolderOptionsDialogFragment();

        Bundle args = new Bundle();
        args.putLong("folderid", folderId);
        args.putString("title", dialogTitle);

        f.setArguments(args);
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((NewsReaderApplication) requireActivity().getApplication()).getAppComponent().injectFragment(this);

        final Bundle args = requireArguments();
        mFolderId = args.getLong("folderid");
        mDialogTitle = args.getString("title");
        mMenuItems = new LinkedHashMap<>();

        mMenuItems.put(getString(R.string.action_folder_rename), () -> showRenameFolderView(mFolderId, mDialogTitle));

        mMenuItems.put(getString(R.string.action_folder_remove), () -> showRemoveFolderView(mFolderId));

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FloatingDialog);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDialogFolderoptionsBinding.inflate(inflater, container, false);

        binding.tvMenuTitle.setText(mDialogTitle);

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

        binding.renameFolderDialog.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.removeFolderDialog.setVisibility(show ? View.GONE : View.VISIBLE);

        binding.progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
        });
    }


    private void showRenameFolderView(final long folderId, final String folderName) {
        binding.renamefolderFoldername.setText(folderName);
        binding.buttonRenameConfirm.setEnabled(false);

        binding.lvMenuList.setVisibility(View.GONE);
        binding.renameFolderDialog.setVisibility(View.VISIBLE);

        binding.renamefolderFoldername.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.buttonRenameConfirm.setEnabled(
                        !s.toString().equals(folderName) && s.length() != 0);
            }
        });

        binding.buttonRenameCancel.setOnClickListener(v -> dismiss());

        binding.buttonRenameConfirm.setOnClickListener(v -> {
            showProgress(true);
            setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);


            Map<String, String> paramMap = new LinkedHashMap<>();
            paramMap.put("name", binding.renamefolderFoldername.getText().toString());
            mApi.getNewsAPI().renameFolder(folderId, paramMap)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
                        dbConn.renameFolderById(folderId, binding.renamefolderFoldername.getText().toString());

                        parentActivity.getSlidingListFragment().reloadAdapter();
                        parentActivity.startSync();
                        dismiss();
                    }, throwable -> {
                        Context context = getContext();
                        if (context == null) {
                            return;
                        }
                        Toast.makeText(context.getApplicationContext(), getString(R.string.login_dialog_text_something_went_wrong) + " - " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        dismiss();
                    });
        });
    }


    private void showRemoveFolderView(final long folderId) {
        binding.lvMenuList.setVisibility(View.GONE);
        binding.removeFolderDialog.setVisibility(View.VISIBLE);

        binding.buttonRemoveCancel.setOnClickListener(v -> dismiss());

        binding.buttonRemoveConfirm.setOnClickListener(v -> {
            showProgress(true);
            setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);

            NewsAPI newsApi = mApi.getNewsAPI();
            Observable<Feed> deleteFeedsTask = newsApi.feeds()
                    .subscribeOn(Schedulers.newThread())
                    .flatMap(feedList -> Observable.fromIterable(feedList)
                            .filter(feed -> folderId == feed.getFolderId())
                    )
                    .flatMap(feed -> newsApi.deleteFeed(feed.getId())
                            .andThen(Observable.just(feed))
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(feed -> {
                        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
                        dbConn.removeFeedById(feed.getId());

                        Long currentFeedId = parentActivity.getNewsReaderDetailFragment().getIdFeed();
                        if(currentFeedId != null && currentFeedId == feed.getId()) {
                            parentActivity.switchToAllUnreadItemsFolder();
                        }
                    });
            Completable.fromObservable(deleteFeedsTask)
                    .observeOn(Schedulers.newThread())
                    .andThen(newsApi.deleteFolder(folderId))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
                        dbConn.removeFolderById(folderId);

                        Long currentFolderId = parentActivity.getNewsReaderDetailFragment().getIdFolder();
                        if(currentFolderId != null && currentFolderId == folderId) {
                            parentActivity.switchToAllUnreadItemsFolder();
                        }
                        parentActivity.getSlidingListFragment().reloadAdapter();
                        parentActivity.startSync();
                        dismiss();
                    }, throwable -> {
                        Context context = getContext();
                        if (context == null) {
                            return;
                        }
                        Toast.makeText(context.getApplicationContext(), getString(R.string.login_dialog_text_something_went_wrong) + " - " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        dismiss();
                    });
        });
    }

    interface MenuAction {
        void execute();
    }
}
