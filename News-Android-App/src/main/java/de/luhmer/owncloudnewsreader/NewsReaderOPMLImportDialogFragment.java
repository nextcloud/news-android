package de.luhmer.owncloudnewsreader;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.databinding.FragmentDialogOpmlImportBinding;
import de.luhmer.owncloudnewsreader.di.ApiProvider;
import io.reactivex.annotations.NonNull;


public class NewsReaderOPMLImportDialogFragment extends DialogFragment {

    private static final String TAG = NewsReaderOPMLImportDialogFragment.class.getCanonicalName();
    protected @Inject
    ApiProvider mApi;

    protected FragmentDialogOpmlImportBinding binding;


    static NewsReaderOPMLImportDialogFragment newInstance(boolean showOkButton) {
        var f = new NewsReaderOPMLImportDialogFragment();

        Bundle args = new Bundle();
        args.putBoolean("show_ok_button", showOkButton);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FloatingDialog);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDialogOpmlImportBinding.inflate(inflater, container, false);

        final Bundle args = requireArguments();
        boolean showOkButton = args.getBoolean("show_ok_button", true);
        setVisibilityOkButton(showOkButton);

        binding.okButton.setOnClickListener(v -> {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.remove(this);
            ft.commit();
        });

        return binding.getRoot();
    }

    public void updateProgress(final int current, final int max) {
        if (binding != null) {
            binding.opmlImportProgress.setMax(max);
            binding.opmlImportProgress.setProgress(current);

            int percentage = Math.round((float) current / (float) max * 100f);
            Log.d(TAG, current + "-" + max + "- " + percentage);
            binding.tvPercentage.setText(String.format("%d%%", percentage));
            binding.tvAbsoluteProgress.setText(String.format("%d / %d", current, max));
        } else {
            Log.e(TAG, "Binding is not ready yet");
        }
    }

    public void setMessage(final String message) {
        if (binding != null) {
            binding.tvMessage.setText(message);

            binding.messageScrollview.post(() -> binding.messageScrollview.fullScroll(View.FOCUS_DOWN));
        } else {
            Log.e(TAG, "Binding is not ready yet");
        }
    }

    public void setVisibilityOkButton(final boolean show) {
        if (binding != null) {
            binding.okButton.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            Log.e(TAG, "Binding is not ready yet");
        }
    }
}
