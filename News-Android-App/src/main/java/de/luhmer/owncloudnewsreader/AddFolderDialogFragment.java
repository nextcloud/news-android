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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.databinding.FragmentDialogAddFolderBinding;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class AddFolderDialogFragment extends DialogFragment {

    protected @Inject ApiProvider mApi;
    private NewsReaderListActivity parentActivity;
    protected FragmentDialogAddFolderBinding binding;

    static AddFolderDialogFragment newInstance() {
        AddFolderDialogFragment f = new AddFolderDialogFragment();

        Bundle args = new Bundle();

        f.setArguments(args);
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((NewsReaderApplication) requireActivity().getApplication()).getAppComponent().injectFragment(this);

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FloatingDialog);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDialogAddFolderBinding.inflate(inflater, container, false);

        binding.buttonAddConfirm.setEnabled(false);
        binding.buttonAddCancel.setOnClickListener(v -> dismiss());

        binding.folderNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.buttonAddConfirm.setEnabled(s.length() != 0);
                binding.folderNameInput.setError(null);
            }
        });

        binding.buttonAddConfirm.setOnClickListener(v -> {
            String name = binding.folderNameInput.getText().toString();
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getContext());
            boolean alreadyExists = dbConn.getFolderByLabel(name) != null;
            if (alreadyExists) {
                binding.folderNameInput.setError(getString(R.string.folder_already_exists));
                return;
            }

            showProgress(true);
            setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);

            Map<String, Object> paramMap = new HashMap<>(0);
            paramMap.put("name", name);
            mApi.getNewsAPI().createFolderObservable(paramMap)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(folders -> {
                        dbConn.insertNewFolders(folders);
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

        return binding.getRoot();
    }


    public void setActivity(Activity parentActivity) {
        this.parentActivity =  (NewsReaderListActivity)parentActivity;
    }


    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        binding.folderNameInput.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.buttonAddConfirm.setEnabled(!show);

        binding.progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.progressView.animate().setDuration(shortAnimTime)
                .alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {});
    }
}
