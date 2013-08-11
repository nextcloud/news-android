package de.luhmer.owncloudnewsreader;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.handmark.pulltorefresh.library.BlockingExpandableListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;

import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.data.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;

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
	public static IReader _Reader = null;  //AsyncTask_GetGReaderTags asyncTask_GetUnreadFeeds = null;
	
	public static String username;
	public static String password;
	//AsyncUpdateFinished asyncUpdateFinished;
	
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
			
			if(_Reader == null)
				_Reader = new OwnCloud_Reader();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private boolean isTwoPaneMode() {
		if(getActivity() != null)
			return ((NewsReaderListActivity) getActivity()).ismTwoPane();
		return false;
	}
	
	public void StartSync()
	{
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if(mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) == null)
		{
			NewsReaderListActivity nla = (NewsReaderListActivity) getActivity();
			nla.StartLoginFragment();
			
		} else {
			if (!_Reader.isSyncRunning())
	        {
				new PostDelayHandler(getActivity()).stopRunningPostDelayHandler();//Stop pending sync handler
				
				OwnCloud_Reader ocReader = (OwnCloud_Reader) _Reader;
				ocReader.Start_AsyncTask_GetVersion(Constants.TaskID_GetVersion, getActivity(), onAsyncTask_GetVersionFinished, username, password);
	        }
			else
	            _Reader.attachToRunningTask(-10, getActivity(), onAsyncTask_GetVersionFinished);
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
	
	
	OnAsyncTaskCompletedListener onAsyncTask_GetVersionFinished = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			if(isTwoPaneMode() || isAdded()) {
				if(!(task_result instanceof Exception))
				{						
					String appVersion = task_result.toString();					
					API api = API.GetRightApiForVersion(appVersion, getActivity());
					((OwnCloud_Reader) _Reader).setApi(api);
					
					_Reader.Start_AsyncTask_PerformItemStateChange(Constants.TaskID_PerformStateChange,  getActivity(), onAsyncTask_PerformTagExecute);
												
					if(eListView != null)
						eListView.getLoadingLayoutProxy().setLastUpdatedLabel(getString(R.string.pull_to_refresh_updateTags));
				}
				else 
					HandleExceptionMessages((Exception) task_result);
				
				UpdateSyncButtonLayout();
			}
		}
	};

	//Sync state of items e.g. read/unread/starred/unstarred
    OnAsyncTaskCompletedListener onAsyncTask_PerformTagExecute = new OnAsyncTaskCompletedListener() {
        @Override
        public void onAsyncTaskCompleted(int task_id, Object task_result) {
        	if(isTwoPaneMode() || isAdded()) {
	            if(task_result != null)//task result is null if there was an error
	            {	
	            	if((Boolean) task_result)
	            	{
	            		if(task_id == Constants.TaskID_PerformStateChange)
	            		{
	            			_Reader.Start_AsyncTask_GetFolder(Constants.TaskID_GetFolder,  getActivity(), onAsyncTask_GetFolder);
	            			if(eListView != null)
	                			eListView.getLoadingLayoutProxy().setLastUpdatedLabel(getString(R.string.pull_to_refresh_updateFolder));
	            		}
	            		else
	            			_Reader.setSyncRunning(true);
	            	}
	            	else
	            		UpdateSyncButtonLayout();
	            }
	            else
	            	UpdateSyncButtonLayout();
        	}
        }
    };
	
    
	OnAsyncTaskCompletedListener onAsyncTask_GetFolder = new OnAsyncTaskCompletedListener() {
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			if(isTwoPaneMode() || isAdded()) {
	            if(task_result != null)
	            	HandleExceptionMessages((Exception) task_result);
	            else {
	                _Reader.Start_AsyncTask_GetFeeds(Constants.TaskID_GetFeeds, getActivity(), onAsyncTask_GetFeed);
	                if(eListView != null)
	                	eListView.getLoadingLayoutProxy().setLastUpdatedLabel(getString(R.string.pull_to_refresh_updateFeeds));
	            }
	
	            lvAdapter.notifyDataSetChanged();
	            
	            Log.d(TAG, "onAsyncTask_GetFolder Finished");
			}
		}
	};

    public void ShowToastLong(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
	
	OnAsyncTaskCompletedListener onAsyncTask_GetFeed = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			if(isTwoPaneMode() || isAdded()) {
				if(task_result != null)
	            	HandleExceptionMessages((Exception) task_result);
				else {
	            	//dbConn.resetRssItemsDatabase();
	            	
	                _Reader.Start_AsyncTask_GetItems(Constants.TaskID_GetItems, getActivity(), onAsyncTask_GetItems, TAGS.ALL);//Recieve all unread Items
	                //_Reader.Start_AsyncTask_GetFeeds(3, getActivity(), onAsyncTask_GetFeeds, TAGS.ALL_STARRED);//Recieve all starred Items
	                
	                if(eListView != null)
	                	eListView.getLoadingLayoutProxy().setLastUpdatedLabel(getString(R.string.pull_to_refresh_updateItems));
	            }
	
	
	
	            lvAdapter.ReloadAdapter();
	            
	            Log.d(TAG, "onAsyncTask_GetFeed Finished");
	            //lvAdapter.notifyDataSetChanged();
	            //eListView.setAdapter(new SubscriptionExpandableListAdapter(getActivity(), dbConn));
				
				//new AsyncTask_GetFeeds(0,  getActivity(), onAsyncTask_GetFeeds).execute(username, password, Constants._TAG_LABEL_UNREAD);			
				//new AsyncTask_GetFeeds(0,  getActivity(), onAsyncTask_GetFeeds).execute(username, password, Constants._TAG_LABEL_STARRED);
			}
		}
	};
	
	OnAsyncTaskCompletedListener onAsyncTask_GetItems = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {			
			if(isTwoPaneMode() || isAdded()) {
				if(task_result != null)
	            	HandleExceptionMessages((Exception) task_result);
	
	            lvAdapter.notifyDataSetChanged();
	
				if(eListView != null)
	            	eListView.getLoadingLayoutProxy().setLastUpdatedLabel(null);
				
	            UpdateSyncButtonLayout();
	
	            lvAdapter.ReloadAdapter();
	            
	            NewsReaderListActivity nlActivity = (NewsReaderListActivity) getActivity();
	            if(nlActivity != null)
	            	nlActivity.UpdateItemList();
	
	            
	            Log.d(TAG, "onAsyncTask_GetItems Finished");
				//fireUpdateFinishedClicked();
			}
		}
	};
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View V = null;
		if(isTwoPaneMode() || isAdded()) {
			V = inflater.inflate(R.layout.expandable_list_layout, container, false);			
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
			
			
			//Start auto sync if enabled
			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			if(mPrefs.getBoolean(SettingsActivity.CB_SYNCONSTARTUP_STRING, false))
				StartSync();
		}
		
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