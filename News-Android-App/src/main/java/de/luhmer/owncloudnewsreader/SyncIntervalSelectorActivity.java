package de.luhmer.owncloudnewsreader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;
import de.luhmer.owncloudnewsreader.databinding.ActivitySyncIntervalSelectorBinding;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;


public class SyncIntervalSelectorActivity extends AppCompatActivity {

    private PlaceholderFragment mFragment;
    private String[] items_values;
    protected ActivitySyncIntervalSelectorBinding binding;
    protected @Inject SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((NewsReaderApplication) getApplication()).getAppComponent().injectActivity(this);

        ThemeChooser.chooseTheme(this);
        super.onCreate(savedInstanceState);
        ThemeChooser.afterOnCreate(this);

        binding = ActivitySyncIntervalSelectorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarLayout.toolbar);

        items_values = getResources().getStringArray(R.array.array_sync_interval_values);

        if (savedInstanceState == null) {
            mFragment = new PlaceholderFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sync_interval_selector, menu);
        return true;
    }

    public static final int SYNC_DEFAULT_INTERVAL = 15;

    public static void setAccountSyncInterval(Context context, SharedPreferences mPrefs) {
        int minutes = mPrefs.getInt(SYNC_INTERVAL_IN_MINUTES_STRING, SYNC_DEFAULT_INTERVAL);

        AccountManager mAccountManager = AccountManager.get(context);
        String accountType = AccountGeneral.getAccountType(context);
        Account[] accounts = mAccountManager.getAccountsByType(accountType);
        for (Account account : accounts) {
            if (minutes != 0) {
                long SYNC_INTERVAL = minutes * SECONDS_PER_MINUTE;
                ContentResolver.setSyncAutomatically(account, accountType, true);

                Bundle bundle = new Bundle();
                ContentResolver.addPeriodicSync(
                        account,
                        accountType,
                        bundle,
                        SYNC_INTERVAL);

            } else {
                ContentResolver.setSyncAutomatically(account, accountType, false);
            }
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */

    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final String SYNC_INTERVAL_IN_MINUTES_STRING = "SYNC_INTERVAL_IN_MINUTES_STRING";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically StartYoutubePlayer clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            int checkedPosition = mFragment.lvItems.getCheckedItemPosition();
            int minutes = Integer.parseInt(items_values[checkedPosition]);
            mPrefs.edit().putInt(SYNC_INTERVAL_IN_MINUTES_STRING, minutes).commit();
            setAccountSyncInterval(this, mPrefs);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
    // public static final int SYNC_DEFAULT_INTERVAL = 60*24;

    public static class PlaceholderFragment extends Fragment {

        private ListView lvItems;
        protected @Inject
        SharedPreferences mPrefs;

        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ((NewsReaderApplication) requireActivity().getApplication()).getAppComponent().injectFragment(this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sync_interval_selector, container, false);

            String[] items = getResources().getStringArray(R.array.array_sync_interval);

            lvItems = rootView.findViewById(R.id.lv_sync_interval_items);
            lvItems.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_single_choice, android.R.id.text1, items);

            lvItems.setAdapter(adapter);

            int position = 0;
            int minutes = mPrefs.getInt(SYNC_INTERVAL_IN_MINUTES_STRING, SYNC_DEFAULT_INTERVAL);
            for (String item : ((SyncIntervalSelectorActivity) requireActivity()).items_values) {
                if (Integer.parseInt(item) == minutes)
                    break;
                position++;
            }
            lvItems.setItemChecked(position, true);

            return rootView;
        }
    }

}
