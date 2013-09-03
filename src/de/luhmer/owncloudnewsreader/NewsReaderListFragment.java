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

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.handmark.pulltorefresh.library.BlockingExpandableListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;

import de.luhmer.owncloudnewsreader.Constants.SYNC_TYPES;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;
import de.luhmer.owncloudnewsreader.data.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.AidlException;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
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
public class NewsReaderListFragment extends SherlockFragment implements OnCreateContextMenuListener /*, 
																ExpandableListView.OnChildClickListener,
																ExpandableListView.OnGroupCollapseListener,
																ExpandableListView.OnGroupExpandListener*/ {

	IOwnCloudSyncService _ownCloadSyncService;	
	private IOwnCloudSyncServiceCallback callback = new IOwnCloudSyncServiceCallback.Stub() {

		@Override
		public void throwException(AidlException ex) throws RemoteException {
			HandleExceptionMessages(ex.getmException());
		}

		@Override
		public void startedSync(String sync_type) throws RemoteException {
			SYNC_TYPES st = SYNC_TYPES.valueOf(sync_type);
			String LastUpdatedLabelTextTemp = null;
			
			switch(st) {
				case SYNC_TYPE__GET_API:					
					break;			
				case SYNC_TYPE__ITEM_STATES:
					LastUpdatedLabelTextTemp = getString(R.string.pull_to_refresh_updateTags);
					break;
				case SYNC_TYPE__FOLDER:
					LastUpdatedLabelTextTemp = getString(R.string.pull_to_refresh_updateFolder);
					break;
				case SYNC_TYPE__FEEDS:
					LastUpdatedLabelTextTemp = getString(R.string.pull_to_refresh_updateFeeds);
					break;
				case SYNC_TYPE__ITEMS:	
					LastUpdatedLabelTextTemp = getString(R.string.pull_to_refresh_updateItems);
					break;				
			}
			
			final String LastUpdatedLabelText = LastUpdatedLabelTextTemp;
			
			Handler refresh = new Handler(Looper.getMainLooper());
			refresh.post(new Runnable() {
				public void run() {
					UpdateSyncButtonLayout();
					if (eListView != null)
						eListView.getLoadingLayoutProxy().setLastUpdatedLabel(LastUpdatedLabelText);
				}
			});
			
		}

		@Override
		public void finishedSync(String sync_type) throws RemoteException {
			Handler refresh = new Handler(Looper.getMainLooper());
			refresh.post(new Runnable() {
				public void run() {
					UpdateSyncButtonLayout();
					if (eListView != null)
						eListView.getLoadingLayoutProxy().setLastUpdatedLabel(null);
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
							lvAdapter.ReloadAdapter();
							
							NewsReaderListActivity nlActivity = (NewsReaderListActivity) getActivity();
							if (nlActivity != null)
								nlActivity.UpdateItemList();
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
	
	
	
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	protected static final String TAG = "NewsReaderListFragment";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sExpListCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onChildItemClicked(String idSubscription, String optional_folder_id);
		public void onTopItemClicked(String idSubscription, boolean isFolder, String optional_folder_id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sExpListCallbacks = new Callbacks() {
		@Override
		public void onChildItemClicked(String idSubscription, String optional_folder_id) {			
		}

		@Override
		public void onTopItemClicked(String idSubscription, boolean isFolder, String optional_folder_id) {			
		}
	};

	DatabaseConnection dbConn;
	//SubscriptionExpandableListAdapter lvAdapter;
	SubscriptionExpandableListAdapter lvAdapter;
	//ExpandableListView eListView;
	PullToRefreshExpandableListView eListView;
	//public static IReader _Reader = null;  //AsyncTask_GetGReaderTags asyncTask_GetUnreadFeeds = null;
	
	public static String username;
	public static String password;
	//AsyncUpdateFinished asyncUpdateFinished;
	ServiceConnection mConnection = null;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NewsReaderListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		
		try
		{
			dbConn = new DatabaseConnection(getActivity());
			
			//dbConn.resetDatabase();
			
			//dbConn.clearDatabaseOverSize();
			
			username = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("edt_username", "");
			password = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("edt_password", "");
			
			//dbConn.resetRssItemsDatabase();
			
			lvAdapter = new SubscriptionExpandableListAdapter(getActivity(), dbConn);
			lvAdapter.setHandlerListener(expListTextClickedListener);
			
			//if(_Reader == null)
			//	_Reader = new OwnCloud_Reader();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	
	
	@Override
	public void onStart() {
		Intent serviceIntent = new Intent(getActivity(), OwnCloudSyncService.class);
		mConnection = generateServiceConnection();
        getActivity().bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
		super.onStart();
	}

	@Override
	public void onStop() {
		if(_ownCloadSyncService != null) {
			try {
				_ownCloadSyncService.unregisterCallback(callback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		getActivity().unbindService(mConnection);
		mConnection = null;
		super.onStop();
	}

	
	private ServiceConnection generateServiceConnection() {
		return new ServiceConnection() {
	    	
	    	@Override
	    	public void onServiceConnected(ComponentName name, IBinder binder) {        	
	    		_ownCloadSyncService = IOwnCloudSyncService.Stub.asInterface(binder);
	    		try {
	    			_ownCloadSyncService.registerCallback(callback);
	    			
	    			//Start auto sync if enabled
	    			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    			if(mPrefs.getBoolean(SettingsActivity.CB_SYNCONSTARTUP_STRING, false))
	    				StartSync();
	    		}
	    		catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
	    	
	    	@Override
	    	public void onServiceDisconnected(ComponentName name) {
	    		try {
	    			_ownCloadSyncService.unregisterCallback(callback);
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
		//Update username and password again.. (might have been changed by the login dialog)
		//username = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("edt_username", "");
		//password = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("edt_password", "");
		
		
		if(mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) == null)
			NewsReaderListActivity.StartLoginFragment((SherlockFragmentActivity) getActivity());
		else {
			try {
				if (!_ownCloadSyncService.isSyncRunning())
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
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			//else
	            //_Reader.attachToRunningTask(-10, getActivity(), onAsyncTask_GetVersionFinished);
		}
		
		UpdateSyncButtonLayout();
	}
	
	
	
	private void HandleExceptionMessages(Exception ex) {
		if(ex instanceof HttpHostConnectException)
            ShowToastLong("Cannot connect to the Host !");
        else if(ex instanceof HttpResponseException)
        {
            HttpResponseException responseException = (HttpResponseException) ex;
            //if(responseException.getStatusCode() == 401)
            //    ShowToastLong("Authentication failed");
            //else
            ShowToastLong(responseException.getLocalizedMessage());
        }
        else
            ShowToastLong(ex.getLocalizedMessage());
		
		UpdateSyncButtonLayout();
	}
	
		

    public void ShowToastLong(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
    

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View V = inflater.inflate(R.layout.expandable_list_layout, container, false);			
		//eListView = (ExpandableListView) V.findViewById(R.id.expandableListView);
		eListView = (PullToRefreshExpandableListView) V.findViewById(R.id.expandableListView);
	
		
		//eListView.setGroupIndicator(getResources().getDrawable(R.drawable.expandable_group_indicator));
		eListView.setGroupIndicator(null);
		
		//eListView.demo();
    	eListView.setShowIndicator(false);
    	
		eListView.setOnRefreshListener(new OnRefreshListener<BlockingExpandableListView>() {
		    @Override
		    public void onRefresh(PullToRefreshBase<BlockingExpandableListView> refreshView) {
		        StartSync();
		    }
		});
		
		eListView.setOnChildClickListener(onChildClickListener);
		//eListView.setSmoothScrollbarEnabled(true);			
		
		View empty = inflater.inflate(R.layout.subscription_detail_list_item_empty, null, false);
		getActivity().addContentView(empty, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));			
		eListView.setEmptyView(empty);
		/*
		eListView.setClickable(true);
		eListView.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				Log.d("hi", String.valueOf(groupPosition));
				//return false;
				return true;
			}
		});*/
		eListView.setExpandableAdapter(lvAdapter);
		
		
		return V;
		//return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
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

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sExpListCallbacks;
	}

	ExpListTextClicked expListTextClickedListener = new ExpListTextClicked() {
		
		@Override
		public void onTextClicked(String idSubscription, Context context, boolean isFolder, String optional_folder_id) {
			mCallbacks.onTopItemClicked(idSubscription, isFolder, optional_folder_id);
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}
	
	OnChildClickListener onChildClickListener = new OnChildClickListener() {
		
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			
			long idItem = lvAdapter.getChildId(groupPosition, childPosition);
			
			String optional_id_folder = null;
			FolderSubscribtionItem groupItem = (FolderSubscribtionItem) lvAdapter.getGroup(groupPosition);
			if(groupItem != null)
				optional_id_folder = String.valueOf(groupItem.id_database);
			
			mCallbacks.onChildItemClicked(String.valueOf(idItem), optional_id_folder);
			
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
		
		
		//eListView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE	: ListView.CHOICE_MODE_NONE);//TODO comment this in
	}

	private void setActivatedPosition(int position) {/*//TODO comment this in
		if (position == ListView.INVALID_POSITION) {
			//getListView().setItemChecked(mActivatedPosition, false);
			eListView.setItemChecked(mActivatedPosition, false);
		} else {
			eListView.setItemChecked(position, true);
			//getListView().setItemChecked(position, true);
		}*/
		
		mActivatedPosition = position;
	}
		

    public void UpdateSyncButtonLayout()
    {
    	if(getActivity() != null)
    		((NewsReaderListActivity) getActivity()).UpdateButtonSyncLayout();
    }

    /**
	 * @return the eListView
	 */
	public PullToRefreshExpandableListView geteListView() {
		return eListView;
	}
    
    /*
	public void setUpdateFinishedListener(AsyncUpdateFinished listener)
	{
		asyncUpdateFinished = listener;
	}
	protected void fireUpdateFinishedClicked()
	{
		if(asyncUpdateFinished != null)
			asyncUpdateFinished.FinishedUpdate();
	}*/
}