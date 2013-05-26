package de.luhmer.owncloudnewsreader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.data.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.data.RssFile;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.interfaces.AsyncUpdateFinished;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
import de.luhmer.owncloudnewsreader.reader.AsyncTask_Reader;
import de.luhmer.owncloudnewsreader.reader.FeedItemTags.TAGS;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.GoogleReaderApi.AsyncTask_GetFeeds;
import de.luhmer.owncloudnewsreader.reader.GoogleReaderApi.AsyncTask_GetGReaderTags;
import de.luhmer.owncloudnewsreader.reader.GoogleReaderApi.AsyncTask_GetSubReaderTags;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;
import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;

/**
 * A list fragment representing a list of NewsReader. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link NewsReaderDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class NewsReaderListFragment extends Fragment implements OnCreateContextMenuListener /*, 
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
		public void onChildItemClicked(String idSubscription);
		public void onTopItemClicked(String idSubscription);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sExpListCallbacks = new Callbacks() {
		@Override
		public void onChildItemClicked(String idSubscription) {			
		}

		@Override
		public void onTopItemClicked(String idSubscription) {			
		}
	};

	DatabaseConnection dbConn;
	SubscriptionExpandableListAdapter lvAdapter;
	ExpandableListView eListView;
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
			
			username = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("edt_username", "");
			password = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("edt_password", "");
			
			//dbConn.resetRssItemsDatabase();
			
			lvAdapter = new SubscriptionExpandableListAdapter(getActivity(), dbConn);
			lvAdapter.setHandlerListener(expListTextClickedListener);
			
			if(_Reader == null)
				_Reader = new OwnCloud_Reader();
			
			//_Reader.Start_AsyncTask_GetFeeds(2, getActivity(), null);
			
						
			/*
			new AsyncTask_GetGReaderTags(1, getActivity(), onAsyncTask_GetTopReaderTags).execute(username, password);			
			new AsyncTask_GetSubReaderTags(1, getActivity(), onAsyncTask_GetSubReaderTags).execute(username, password);*/
			
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	
	
	public void StartSync()
	{
		if (!_Reader.isSyncRunning())
			_Reader.Start_AsyncTask_GetFolder(0,  getActivity(), onAsyncTask_GetTopReaderTags);
		else
	    	_Reader.attachToRunningTask(0, getActivity(), onAsyncTask_GetTopReaderTags);
	}
	
	OnAsyncTaskCompletedListener onAsyncTask_GetTopReaderTags = new OnAsyncTaskCompletedListener() {

		
		@SuppressWarnings("unchecked")
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			Log.d(TAG, "onAsyncTask_GetTopReaderTags started: " + task_id);

            if(task_result != null)
            {
                if(task_result instanceof HttpHostConnectException)
                    ShowToastLong("Cannot connect to the Host !");
                else if(task_result instanceof HttpResponseException)
                {
                    HttpResponseException responseException = (HttpResponseException) task_result;
                    //if(responseException.getStatusCode() == 401)
                    //    ShowToastLong("Authentication failed");
                    //else
                    ShowToastLong(responseException.getLocalizedMessage());
                }
                else
                    ShowToastLong(((Exception)task_result).getLocalizedMessage());

                UpdateSyncButtonLayout();
            }
            else
                _Reader.Start_AsyncTask_GetSubFolder(1, getActivity(), onAsyncTask_GetSubReaderTags);

            lvAdapter.notifyDataSetChanged();
            Log.d(TAG, "onAsyncTask_GetTopReaderTags Finished");
		}
	};

    public void ShowToastLong(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
	
	OnAsyncTaskCompletedListener onAsyncTask_GetSubReaderTags = new OnAsyncTaskCompletedListener() {
		
		@SuppressWarnings("unchecked")
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {

			Log.d(TAG, "onAsyncTask_GetSubReaderTags Finished");

            if(task_result != null)
            {
                ShowToastLong(((Exception)task_result).getLocalizedMessage());
                UpdateSyncButtonLayout();
            }
            else
            {
                _Reader.Start_AsyncTask_GetFeeds(2, getActivity(), onAsyncTask_GetFeeds, TAGS.ALL_UNREAD);//Recieve all unread Items
                _Reader.Start_AsyncTask_GetFeeds(2, getActivity(), onAsyncTask_GetFeeds, TAGS.ALL_STARRED);//Recieve all starred Items
            }



            lvAdapter.ReloadAdapter();
            //lvAdapter.notifyDataSetChanged();
            //eListView.setAdapter(new SubscriptionExpandableListAdapter(getActivity(), dbConn));
			
			//new AsyncTask_GetFeeds(0,  getActivity(), onAsyncTask_GetFeeds).execute(username, password, Constants._TAG_LABEL_UNREAD);			
			//new AsyncTask_GetFeeds(0,  getActivity(), onAsyncTask_GetFeeds).execute(username, password, Constants._TAG_LABEL_STARRED);
		}
	};
	
	OnAsyncTaskCompletedListener onAsyncTask_GetFeeds = new OnAsyncTaskCompletedListener() {
		
		@SuppressWarnings("unchecked")
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {

            if(task_result != null)
                ShowToastLong(((Exception)task_result).getLocalizedMessage());

            lvAdapter.notifyDataSetChanged();
            //Activity act = getActivity();
            //lvAdapter = new SubscriptionExpandableListAdapter(act, dbConn);

			Log.d(TAG, "onAsyncTask_GetFeeds Finished");

            UpdateSyncButtonLayout();

            NewsReaderListActivity nlActivity = (NewsReaderListActivity) getActivity();
            nlActivity.UpdateItemList();

			//fireUpdateFinishedClicked();
		}
	};
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View V = null;
		try
		{	
			V = inflater.inflate(R.layout.expandable_list_layout, container, false);			
			eListView = (ExpandableListView) V.findViewById(R.id.expandableListView);
			eListView.setOnChildClickListener(onChildClickListener);
			eListView.setSmoothScrollbarEnabled(true);			
			
			View empty = inflater.inflate(R.layout.subscription_list_item_empty, null, false);
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
			eListView.setAdapter(lvAdapter);
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
		public void onTextClicked(String idSubscription, Context context) {
			mCallbacks.onTopItemClicked(idSubscription);
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
			mCallbacks.onChildItemClicked(String.valueOf(idItem));
			
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
		
		//getListView().setChoiceMode(
		eListView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE	: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			//getListView().setItemChecked(mActivatedPosition, false);
			eListView.setItemChecked(mActivatedPosition, false);
		} else {
			eListView.setItemChecked(position, true);
			//getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
		

    public void UpdateSyncButtonLayout()
    {
        ((NewsReaderListActivity) getActivity()).UpdateButtonSync();
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


/*
			dbConn.insertNewSubscription("Ungelesene Artikel");
			dbConn.insertNewSubscription("Markierte Artikel");			
			dbConn.insertNewSubscription("Android");
			dbConn.insertNewSubscription("Apple");
			dbConn.insertNewSubscription("Bugtracker");
			dbConn.insertNewSubscription("Development");
			dbConn.insertNewSubscription("Linux");
			dbConn.insertNewSubscription("Software");
			dbConn.insertNewSubscription("Owncloud");
			dbConn.insertNewSubscription("Other");
			
			dbConn.insertNewSub_Subscription("4droid", dbConn.getIdOfSubscription("Android"));
			dbConn.insertNewSub_Subscription("GIGA Rss Feed", dbConn.getIdOfSubscription("Android"));
			
			dbConn.insertNewSub_Subscription("macnews.de", dbConn.getIdOfSubscription("Apple"));
			dbConn.insertNewSub_Subscription("MACNOTES.DE", dbConn.getIdOfSubscription("Apple"));
			
			dbConn.insertNewSub_Subscription("FS Bugtracker", dbConn.getIdOfSubscription("Bugtracker"));
			
			dbConn.insertNewSub_Subscription("Code 2 Learn", dbConn.getIdOfSubscription("Development"));
			dbConn.insertNewSub_Subscription("Coding Horror", dbConn.getIdOfSubscription("Development"));
			
			dbConn.insertNewSub_Subscription("Canoncial", dbConn.getIdOfSubscription("Linux"));
			
			dbConn.insertNewSub_Subscription("iTek Zone", dbConn.getIdOfSubscription("Software"));
			
			dbConn.insertNewSub_Subscription("CNET", dbConn.getIdOfSubscription("Other"));
			
			dbConn.insertNewSub_Subscription("ownCloud.org", dbConn.getIdOfSubscription("Owncloud"));			
			
			dbConn.insertNewFeed("Samsung Game Pad pops up on Galaxy S4 site, outs Note 3?",
									"http://news.cnet.com/8301-17938_105-57574766-1/samsung-game-pad-pops-up-on-galaxy-s4-site-outs-note-3/?part\u003drss\u0026subj\u003dnews\u0026tag\u003d2547-1_3-0-20",
									"The gaming controller works with devices up to 6.3 inches, perhaps suggesting Samsung is planning a bigger phablet, or should we be calling it a \"tabone?\"",
									"0",
									dbConn.getIdOfSubscription("CNET"));
			
			*/

