/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.owncloudnewsreader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.owncloudnewsreader.Constants.SYNC_TYPES;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.helper.AidlException;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
import de.luhmer.owncloudnewsreader.model.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncService;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncServiceCallback;
import de.luhmer.owncloudnewsreader.services.OwnCloudSyncService;

/**
 * A list fragment representing a list of NewsReader. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link NewsReaderDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class NewsReaderListFragment extends Fragment implements OnCreateContextMenuListener {

	IOwnCloudSyncService _ownCloudSyncService;
	private IOwnCloudSyncServiceCallback callback = new IOwnCloudSyncServiceCallback.Stub() {

		@Override
		public void throwException(AidlException ex) throws RemoteException {
			HandleExceptionMessages(ex.getmException());
		}

		@Override
		public void startedSync(String sync_type) throws RemoteException {
			Handler refresh = new Handler(Looper.getMainLooper());
			refresh.post(new Runnable() {
				public void run() {
                    ((NewsReaderListActivity)getActivity()).UpdateButtonLayout();;
				}
			});
		}

		@Override
		public void finishedSync(String sync_type) throws RemoteException {
			Handler refresh = new Handler(Looper.getMainLooper());
			refresh.post(new Runnable() {
				public void run() {
                    ((NewsReaderListActivity) getActivity()).UpdateButtonLayout();
				}
			});

			SYNC_TYPES st = SYNC_TYPES.valueOf(sync_type);

			switch(st) {
				case SYNC_TYPE__GET_API:
					break;
				case SYNC_TYPE__ITEM_STATES:
					break;
				case SYNC_TYPE__FOLDER:
					break;
				case SYNC_TYPE__FEEDS:
					break;
				case SYNC_TYPE__ITEMS:

					Log.d(TAG, "finished sync");
					refresh = new Handler(Looper.getMainLooper());
					refresh.post(new Runnable() {
						public void run() {
                        ReloadAdapter();
                        NewsReaderListActivity nlActivity = (NewsReaderListActivity) getActivity();
						if (nlActivity != null) {
                            nlActivity.UpdateItemList();

                            nlActivity.UpdatePodcastView();
                        }

                        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        int newItemsCount = mPrefs.getInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 0);
                        if(newItemsCount > 0) {
							Snackbar snackbar = Snackbar.make(nlActivity.findViewById(R.id.coordinator_layout), getResources().getQuantityString(R.plurals.message_bar_new_articles_available,newItemsCount,newItemsCount), Snackbar.LENGTH_LONG);
							snackbar.setAction(getString(R.string.message_bar_reload), mListener);
							snackbar.setActionTextColor(getResources().getColor(R.color.accent_material_dark));
							// Setting android:TextColor to #000 in the light theme results in black on black
							// text on the Snackbar, set the text back to white,
							// TODO: find a cleaner way to do this
							TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
							textView.setTextColor(Color.WHITE);
							snackbar.show();
                        }
						}
					});
					break;
			}
		}

	};

	/*
	public IOwnCloudSyncServiceCallback getCallback() {
		return callback;
	}*/

    public void ListViewNotifyDataSetChanged()  {
        lvAdapter.NotifyDataSetChangedAsync();
    }

    public void ReloadAdapter() {
        lvAdapter.ReloadAdapterAsync();
    }





	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	//private static final String STATE_ACTIVATED_POSITION = "activated_position";

	protected static final String TAG = "NewsReaderListFragment";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = null;

	public void setRefreshing(boolean isRefreshing) {
		if(isRefreshing) {
			headerLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher_background));
			headerLogoProgress.setVisibility(View.VISIBLE);
		} else {
			headerLogo.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
			headerLogoProgress.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * The current activated item position. Only used on tablets.
	 */
	//private static int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onChildItemClicked(long idFeed, Long optional_folder_id);
		public void onTopItemClicked(long idFeed, boolean isFolder, Long optional_folder_id);
	}


	//SubscriptionExpandableListAdapter lvAdapter;
	private SubscriptionExpandableListAdapter lvAdapter;
	//ExpandableListView eListView;
    @InjectView(R.id.expandableListView) ExpandableListView eListView;
	@InjectView(R.id.urlTextView) protected TextView urlTextView;
	@InjectView(R.id.userTextView) protected TextView userTextView;
	@InjectView(R.id.header_view) protected ViewGroup headerView;
	@InjectView(R.id.header_logo) protected ImageView headerLogo;
	@InjectView(R.id.header_logo_progress) protected View headerLogoProgress;
	//public static IReader _Reader = null;  //AsyncTask_GetGReaderTags asyncTask_GetUnreadFeeds = null;

	//public static String username;
	//public static String password;
	//AsyncUpdateFinished asyncUpdateFinished;
	ServiceConnection mConnection = null;

    private View.OnClickListener mListener = null;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NewsReaderListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				//Toast.makeText(getActivity(), "button 1 pressed", 3000).show();

				NewsReaderDetailFragment ndf = ((NewsReaderDetailFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.content_frame));
				if(ndf != null) {
					//ndf.reloadAdapterFromScratch();
					ndf.UpdateCurrentRssView(getActivity(), true);
				}
			}
		};
	}



	@Override
	public void onStart() {
		Intent serviceIntent = new Intent(getActivity(), OwnCloudSyncService.class);
		mConnection = generateServiceConnection();
        if(!isMyServiceRunning(OwnCloudSyncService.class)) {
            getActivity().startService(serviceIntent);
        }
        getActivity().bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
		super.onStart();
	}

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

	@Override
	public void onStop() {
		if(_ownCloudSyncService != null) {
			try {
                _ownCloudSyncService.unregisterCallback(callback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		getActivity().unbindService(mConnection);
		mConnection = null;
		super.onStop();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	/*
	@Override
	public void onResume() {
		try {
			if(mActivatedPosition != ListView.INVALID_POSITION)
				geteListView().getRefreshableView().setSelection(mActivatedPosition);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		super.onResume();
	}
	*/



	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		//mActivatedPosition = geteListView().getRefreshableView().getFirstVisiblePosition();
		super.onPause();
	}

	private ServiceConnection generateServiceConnection() {
		return new ServiceConnection() {

	    	@Override
	    	public void onServiceConnected(ComponentName name, IBinder binder) {
                _ownCloudSyncService = IOwnCloudSyncService.Stub.asInterface(binder);
	    		try {
                    _ownCloudSyncService.registerCallback(callback);

	    			//Start auto sync if enabled
	    			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    			if(mPrefs.getBoolean(SettingsActivity.CB_SYNCONSTARTUP_STRING, false))
	    				StartSync();

	    			if(getActivity() instanceof NewsReaderListActivity)
	    				((NewsReaderListActivity) getActivity()).UpdateButtonLayout();
	    		}
	    		catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}

	    	@Override
	    	public void onServiceDisconnected(ComponentName name) {
	    		try {
                    _ownCloudSyncService.unregisterCallback(callback);
	    		}
	    		catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
		};
    }


	public void StartSync()
	{
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		if(mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) == null)
			NewsReaderListActivity.StartLoginFragment((FragmentActivity) getActivity());
		else {
			try {
				if (!_ownCloudSyncService.isSyncRunning())
				{
					new PostDelayHandler(getActivity()).stopRunningPostDelayHandler();//Stop pending sync handler

					//_ownCloadSyncService.startSync();

					/*
			         * Request the sync for the default account, authority, and
			         * manual sync settings
			         */
					Bundle accBundle = new Bundle();
					accBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
					AccountManager mAccountManager = AccountManager.get(getActivity());
					Account[] accounts = mAccountManager.getAccounts();
					for(Account acc : accounts)
						if(acc.type.equals(AccountGeneral.ACCOUNT_TYPE))
							ContentResolver.requestSync(acc, AccountGeneral.ACCOUNT_TYPE, accBundle);
					//http://stackoverflow.com/questions/5253858/why-does-contentresolver-requestsync-not-trigger-a-sync
				} else {
                    ((NewsReaderListActivity) getActivity()).UpdateButtonLayout();
                }
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			//else
	            //_Reader.attachToRunningTask(-10, getActivity(), onAsyncTask_GetVersionFinished);
		}
	}



	private void HandleExceptionMessages(Exception ex) {
		if(ex instanceof HttpHostConnectException)
            ShowToastLong("Cannot connect to the Host !");
        else if(ex instanceof HttpResponseException)
        {
            HttpResponseException responseException = (HttpResponseException) ex;
            ShowToastLong(responseException.getLocalizedMessage());
        }
        else
            ShowToastLong(ex.getLocalizedMessage());

        ((NewsReaderListActivity) getActivity()).UpdateButtonLayout();
	}



    public void ShowToastLong(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_newsreader_list, container, false);

        if(!ThemeChooser.isDarkTheme(getActivity())) {
            view.setBackgroundColor(getResources().getColor(R.color.slider_listview_background_color_light_theme));
        }

        ButterKnife.inject(this, view);

        lvAdapter = new SubscriptionExpandableListAdapter(getActivity(), new DatabaseConnectionOrm(getActivity()), eListView);
        lvAdapter.setHandlerListener(expListTextClickedListener);

		eListView.setGroupIndicator(null);

		eListView.setOnChildClickListener(onChildClickListener);
		//eListView.setSmoothScrollbarEnabled(true);

		eListView.setClickable(true);
		eListView.setAdapter(lvAdapter);

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String mUsername = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String mOc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, getString(R.string.app_name));

		userTextView.setText(mUsername);
		urlTextView.setText(mOc_root_path);
		headerView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StartSync();
			}
		});

        lvAdapter.notifyDataSetChanged();
        ReloadAdapter();

		return view;
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mCallbacks = null;
	}

	ExpListTextClicked expListTextClickedListener = new ExpListTextClicked() {

		@Override
		public void onTextClicked(long idFeed, Context context, boolean isFolder, Long optional_folder_id) {
            mCallbacks.onTopItemClicked(idFeed, isFolder, optional_folder_id);
		}
    };

	/*
	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.

		//mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
	}*/


	OnChildClickListener onChildClickListener = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {

			long idItem = lvAdapter.getChildId(groupPosition, childPosition);

			Long optional_id_folder = null;
			FolderSubscribtionItem groupItem = (FolderSubscribtionItem) lvAdapter.getGroup(groupPosition);
			if(groupItem != null)
				optional_id_folder = groupItem.id_database;

			mCallbacks.onChildItemClicked(idItem, optional_id_folder);

			return false;
		}
	};

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.


		eListView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}
}