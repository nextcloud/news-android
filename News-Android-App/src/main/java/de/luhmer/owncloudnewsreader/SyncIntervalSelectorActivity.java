package de.luhmer.owncloudnewsreader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;


public class SyncIntervalSelectorActivity extends SherlockFragmentActivity {

    SharedPreferences mPrefs;
    PlaceholderFragment mFragment;
    String[] items_values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_interval_selector);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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
        getSupportMenuInflater()
                .inflate(R.menu.sync_interval_selector, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_save) {
            int checkedPosition = mFragment.lvItems.getCheckedItemPosition();

            Integer minutes = Integer.parseInt(items_values[checkedPosition]);

            mPrefs.edit().putInt(SYNC_INTERVAL_IN_MINUTES_STRING, minutes).commit();

            long SYNC_INTERVAL = minutes * SECONDS_PER_MINUTE;

            AccountManager mAccountManager = AccountManager.get(this);
            Account[] accounts = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
            for(Account account : accounts) {

                ContentResolver.setSyncAutomatically(account, AccountGeneral.ACCOUNT_TYPE, true);

                Bundle bundle = new Bundle();
                ContentResolver.addPeriodicSync(
                        account,
                        AccountGeneral.ACCOUNT_TYPE,
                        bundle,
                        SYNC_INTERVAL);
            }

            finish();
        }


        return super.onOptionsItemSelected(item);
    }



    /**
     * A placeholder fragment containing a simple view.
     */

    // Sync interval constants
    public static final long MILLISECONDS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_MINUTE = 60L;
    //public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final String SYNC_INTERVAL_IN_MINUTES_STRING = "SYNC_INTERVAL_IN_MINUTES_STRING";

    public static class PlaceholderFragment extends SherlockFragment {

        public ListView lvItems;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sync_interval_selector, container, false);

            String[] items = getResources().getStringArray(R.array.array_sync_interval);

            lvItems = (ListView) rootView.findViewById(R.id.lv_sync_interval_items);
            lvItems.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_single_choice, android.R.id.text1, items);


            lvItems.setAdapter(adapter);

            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(!mPrefs.contains(SYNC_INTERVAL_IN_MINUTES_STRING))
                lvItems.setItemChecked(items.length - 1, true);//The last item is 24hours. This is the default value!
            else {
                int position = 0;
                int minutes = mPrefs.getInt(SYNC_INTERVAL_IN_MINUTES_STRING, 0);
                for(String item : ((SyncIntervalSelectorActivity)getActivity()).items_values) {
                    if(Integer.parseInt(item) == minutes)
                        break;
                    position++;
                }
                lvItems.setItemChecked(position, true);//The last item is 24hours. This is the default value!
            }

            return rootView;
        }
    }

}
