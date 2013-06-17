package de.luhmer.owncloudnewsreader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.cursor.NewsListCursorAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;

/**
 * A fragment representing a single NewsReader detail screen. This fragment is
 * either contained in a {@link NewsReaderListActivity} in two-pane mode (on
 * tablets) or a {@link NewsReaderDetailActivity} on handsets.
 */
public class NewsReaderDetailFragment extends ListFragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	DatabaseConnection dbConn;
	NewsListCursorAdapter lvAdapter;
	String idSubscription;
	String idFolder;
	String titel;
	
	ArrayList<Integer> databaseIdsOfItems;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NewsReaderDetailFragment() {
		databaseIdsOfItems = new ArrayList<Integer>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			
			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
					
		}*/
		
		if (getArguments().containsKey(NewsReaderDetailActivity.SUBSCRIPTION_ID)) {
			idSubscription = getArguments().getString(NewsReaderDetailActivity.SUBSCRIPTION_ID);
		}
		if (getArguments().containsKey(NewsReaderDetailActivity.TITEL)) {
			titel = getArguments().getString(NewsReaderDetailActivity.TITEL);
		}
		if (getArguments().containsKey(NewsReaderDetailActivity.FOLDER_ID)) {
			idFolder = getArguments().getString(NewsReaderDetailActivity.FOLDER_ID);
		}
		
		dbConn = new DatabaseConnection(getActivity());
				
		//lvAdapter = new Subscription_ListViewAdapter(this);
		UpdateCursor();
	}
	
	@Override
	public void onDestroy() {
		if(lvAdapter != null)
			lvAdapter.CloseDatabaseConnection();
		if(dbConn != null)
			dbConn.closeDatabase();
		super.onDestroy();
	}

	public void UpdateCursor()
	{
		try
		{
			Cursor cursor = getRightCusor(idFolder);
			
			databaseIdsOfItems.clear();
			if(cursor != null)
				while(cursor.moveToNext())
					databaseIdsOfItems.add(cursor.getInt(0));
			
			
			if(lvAdapter == null)
			{			
				lvAdapter = new NewsListCursorAdapter(getActivity(), cursor);
				setListAdapter(lvAdapter);
			}
			else
				lvAdapter.changeCursor(cursor);
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

    public Cursor getRightCusor(String ID_FOLDER)
    {
    	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    	boolean onlyUnreadItems = mPrefs.getBoolean(SettingsActivity.CB_SHOWONLYUNREAD_STRING, false);
    	boolean onlyStarredItems = false;
    	if(ID_FOLDER != null)
    		if(ID_FOLDER.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS))
    			onlyStarredItems = true;
    		
        if(idSubscription != null)
        {
            return dbConn.getAllItemsForFeed(idSubscription, onlyUnreadItems, onlyStarredItems);
        }
        else if(idFolder != null)
        {
        	if(idFolder.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS))
        		onlyUnreadItems = false;
            return dbConn.getAllItemsForFolder(idFolder, onlyUnreadItems);
        }
        return null;
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_newsreader_detail, container, false);		
		return rootView;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
				
		Intent intentNewsDetailAct = new Intent(getActivity(), NewsDetailActivity.class);
		//if(idSubscription != null)
		//	intentNewsDetailAct.putExtra(NewsReaderDetailActivity.SUBSCRIPTION_ID, Long.valueOf(idSubscription));
		//else if(idFolder != null)
		//	intentNewsDetailAct.putExtra(NewsReaderDetailActivity.FOLDER_ID, Long.valueOf(idFolder));		
		
		//intentNewsDetailAct.putIntegerArrayListExtra(NewsDetailActivity.DATABASE_IDS_OF_ITEMS, databaseIdsOfItems);
		//Integer[] databaseIdsOfItemsArray = databaseIdsOfItems.toArray(new Integer[databaseIdsOfItems.size()]);
		intentNewsDetailAct.putIntegerArrayListExtra(NewsDetailActivity.DATABASE_IDS_OF_ITEMS, databaseIdsOfItems);
		
		intentNewsDetailAct.putExtra(NewsReaderDetailActivity.ITEM_ID, position);
		intentNewsDetailAct.putExtra(NewsReaderDetailActivity.TITEL, titel);
		startActivityForResult(intentNewsDetailAct, Activity.RESULT_CANCELED);
		
		super.onListItemClick(l, v, position, id);
	}

	public ArrayList<Integer> getDatabaseIdsOfItems() {
		return databaseIdsOfItems;
	}

}
