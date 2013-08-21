package de.luhmer.owncloudnewsreader;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.devspark.robototextview.widget.RobotoCheckBox;

import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.cursor.IOnStayUnread;
import de.luhmer.owncloudnewsreader.cursor.NewsListCursorAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection.SORT_DIRECTION;
import de.luhmer.owncloudnewsreader.helper.MenuUtilsSherlockFragmentActivity;
import de.luhmer.owncloudnewsreader.helper.NewsListCursorAdapterHolder;

/**
 * A fragment representing a single NewsReader detail screen. This fragment is
 * either contained in a {@link NewsReaderListActivity} in two-pane mode (on
 * tablets) or a {@link NewsReaderDetailActivity} on handsets.
 */
public class NewsReaderDetailFragment extends SherlockListFragment implements IOnStayUnread {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	protected static final String TAG = "NewsReaderDetailFragment";

	private DatabaseConnection dbConn;
	
	//private boolean DialogShowedToMarkLastItemsAsRead = false; 
	
	//private static NewsListCursorAdapter lvAdapter;
	private NewsListCursorAdapterHolder lvAdapterHolder;
	
	/**
	 * @return the lvAdapterHolder
	 */
	public NewsListCursorAdapterHolder getLvAdapterHolder() {
		return lvAdapterHolder;
	}

	String idFeed;
	/**
	 * @return the idFeed
	 */
	public String getIdFeed() {
		return idFeed;
	}

	String idFolder;
	/**
	 * @return the idFolder
	 */
	public String getIdFolder() {
		return idFolder;
	}

	String titel;
	int lastItemPosition;
	
	//private static ArrayList<Integer> databaseIdsOfItems;
	ArrayList<RobotoCheckBox> stayUnreadCheckboxes;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NewsReaderDetailFragment() {
		//databaseIdsOfItems = new ArrayList<Integer>();
		stayUnreadCheckboxes = new ArrayList<RobotoCheckBox>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
				
		
		if(getArguments() != null) {
			if (getArguments().containsKey(NewsReaderListActivity.SUBSCRIPTION_ID)) {
				idFeed = getArguments().getString(NewsReaderListActivity.SUBSCRIPTION_ID);
			}
			if (getArguments().containsKey(NewsReaderListActivity.TITEL)) {
				titel = getArguments().getString(NewsReaderListActivity.TITEL);
			}
			if (getArguments().containsKey(NewsReaderListActivity.FOLDER_ID)) {
				idFolder = getArguments().getString(NewsReaderListActivity.FOLDER_ID);
			}
			
			dbConn = new DatabaseConnection(getActivity());
				
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setTitle(titel);
			
			UpdateMenuItemsState();//Is called on Tablets and Smartphones but on Smartphones the menuItemDownloadMoreItems is null. So it will be ignored
			
			//getListView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			
			//lvAdapter = null;
			lvAdapterHolder.setLvAdapter(null);
			
			UpdateCursor();
		}
	}
	
	@SuppressWarnings("static-access")
	public void UpdateMenuItemsState()
	{
		MenuUtilsSherlockFragmentActivity mActivity = ((MenuUtilsSherlockFragmentActivity) getActivity());
		
		if(mActivity.getMenuItemDownloadMoreItems() != null)
		{
			if(idFolder != null) {
				if(idFolder.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS))
					mActivity.getMenuItemDownloadMoreItems().setEnabled(false);
				else
					mActivity.getMenuItemDownloadMoreItems().setEnabled(true);
			} else
				mActivity.getMenuItemDownloadMoreItems().setEnabled(false);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if(mPrefs.getBoolean(SettingsActivity.CB_MARK_AS_READ_WHILE_SCROLLING_STRING, false))
		{		
			getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
				
	            public void onScrollStateChanged(AbsListView view, int scrollState) {
	            
	            }
	
	            RobotoCheckBox lastViewedArticleCheckbox = null;
	            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, int totalItemCount) {
	            	
	            	if(lastViewedArticleCheckbox == null)
	            		lastViewedArticleCheckbox = getCheckBoxAtPosition(0, view); 
	            		            	
	            	RobotoCheckBox cb = getCheckBoxAtPosition(0, view);
	            	if(lastViewedArticleCheckbox != cb) {
	            		if(!stayUnreadCheckboxes.contains(lastViewedArticleCheckbox));
	            			NewsListCursorAdapter.ChangeCheckBoxState(lastViewedArticleCheckbox, true, getActivity());
	            		
	            		lastViewedArticleCheckbox = cb;
	            	}
	            }
	        });
		}
		
		super.onViewCreated(view, savedInstanceState);
	}
	
	
	
	private RobotoCheckBox getCheckBoxAtPosition(int pos, AbsListView viewLV)
	{
		ListView lv = (ListView) viewLV;
		View view = (View) lv.getChildAt(pos);
		if(view != null)
			return (RobotoCheckBox) view.findViewById(R.id.cb_lv_item_read);
		else
			return null;
	}
	
	@Override
	public void onResume() {
		//setEmptyListView();
		
		lastItemPosition = -1;
		super.onResume();
	}

	@Override
	public void onDestroy() {
		if(lvAdapterHolder.getLvAdapter() != null)
			lvAdapterHolder.getLvAdapter().CloseDatabaseConnection();
		//if(lvAdapter != null)
		//	lvAdapter.CloseDatabaseConnection();
		if(dbConn != null)
			dbConn.closeDatabase();
		super.onDestroy();
	}

	public void UpdateCursor()
	{
		try
		{
			Cursor cursor = getRightCusor(idFolder);
			
			/*
			databaseIdsOfItems.clear();
			if(cursor != null)
				while(cursor.moveToNext())
					databaseIdsOfItems.add(cursor.getInt(0));
			*/
			
			if(lvAdapterHolder.getLvAdapter() == null)
			{			
				lvAdapterHolder.setLvAdapter(new NewsListCursorAdapter(getActivity(), cursor, this));
				setListAdapter(lvAdapterHolder.getLvAdapter());
			}
			else
				lvAdapterHolder.getLvAdapter().changeCursor(cursor);
			
			/*
			if(lvAdapter == null)
			{			
				lvAdapter = new NewsListCursorAdapter(getActivity(), cursor, this);
				//setEmptyListView();
				setListAdapter(lvAdapter);
			}
			else
				lvAdapter.changeCursor(cursor);
				*/
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
    	
    	SORT_DIRECTION sDirection = SORT_DIRECTION.asc;
    	String sortDirection = mPrefs.getString(SettingsActivity.SP_SORT_ORDER, "desc");
    	if(sortDirection.equals(SORT_DIRECTION.desc.toString()))
    		sDirection = SORT_DIRECTION.desc;
    		
    	
        if(idFeed != null)
            return dbConn.getAllItemsForFeed(idFeed, onlyUnreadItems, onlyStarredItems, sDirection);
        else if(idFolder != null)
        {
        	if(idFolder.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS))
        		onlyUnreadItems = false;
            return dbConn.getAllItemsForFolder(idFolder, onlyUnreadItems, sDirection);
        }
        return null;
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_newsreader_detail, container, false);
		return rootView;
	}

	/*
	private void setEmptyListView() {
		LayoutInflater inflator=getActivity().getLayoutInflater();
        View emptyView = inflator.inflate(R.layout.subscription_detail_list_item_empty, (ViewGroup)getView());
        
        FontHelper fHelper = new FontHelper(getActivity());
        fHelper.setFontForAllChildren(emptyView, fHelper.getFont());
        
        ListView lv = getListView();
        if(lv != null)
        	lv.setEmptyView(emptyView);
	}
	*/
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
				
		Intent intentNewsDetailAct = new Intent(getActivity(), NewsDetailActivity.class);		
		//intentNewsDetailAct.putIntegerArrayListExtra(NewsDetailActivity.DATABASE_IDS_OF_ITEMS, databaseIdsOfItems);
		
		intentNewsDetailAct.putExtra(NewsReaderListActivity.ITEM_ID, position);
		intentNewsDetailAct.putExtra(NewsReaderListActivity.TITEL, titel);
		startActivityForResult(intentNewsDetailAct, Activity.RESULT_CANCELED);
		
		super.onListItemClick(l, v, position, id);
	}

	/*
	public static ArrayList<Integer> getDatabaseIdsOfItems() {
		return databaseIdsOfItems;
	}*/

	@Override
	public void stayUnread(RobotoCheckBox cb) {
		stayUnreadCheckboxes.add(cb);
	}
}
