package de.luhmer.owncloudnewsreader;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import de.luhmer.owncloudnewsreader.cursor.NewsListCursorAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection.SORT_DIRECTION;
import de.luhmer.owncloudnewsreader.helper.MenuUtilsSherlockFragmentActivity;

/**
 * A fragment representing a single NewsReader detail screen. This fragment is
 * either contained in a {@link NewsReaderListActivity} in two-pane mode (on
 * tablets) or a {@link NewsReaderDetailActivity} on handsets.
 */
public class NewsReaderDetailFragment extends SherlockListFragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	protected static final String TAG = "NewsReaderDetailFragment";

	private DatabaseConnection dbConn;
	
	private boolean DialogShowedToMarkLastItemsAsRead = false; 
	
	private NewsListCursorAdapter lvAdapter;
	/**
	 * @return the lvAdapter
	 */
	public NewsListCursorAdapter getLvAdapter() {
		return lvAdapter;
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
		
		if (getArguments().containsKey(NewsReaderDetailActivity.SUBSCRIPTION_ID)) {
			idFeed = getArguments().getString(NewsReaderDetailActivity.SUBSCRIPTION_ID);
		}
		if (getArguments().containsKey(NewsReaderDetailActivity.TITEL)) {
			titel = getArguments().getString(NewsReaderDetailActivity.TITEL);
		}
		if (getArguments().containsKey(NewsReaderDetailActivity.FOLDER_ID)) {
			idFolder = getArguments().getString(NewsReaderDetailActivity.FOLDER_ID);
		}
		
		dbConn = new DatabaseConnection(getActivity());
			
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().setTitle(titel);
		
		UpdateMenuItemsState();//Is called on Tablets and Smartphones but on Smartphones the menuItemDownloadMoreItems is null. So it will be ignored
		
		//getListView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		
		UpdateCursor();
	}
	
	@SuppressWarnings("static-access")
	public void UpdateMenuItemsState()
	{
		MenuUtilsSherlockFragmentActivity mActivity = ((MenuUtilsSherlockFragmentActivity) getActivity());
		
		if(mActivity.getMenuItemDownloadMoreItems() != null)
		{
			if(idFolder.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS))
				mActivity.getMenuItemDownloadMoreItems().setEnabled(false);
			else
				mActivity.getMenuItemDownloadMoreItems().setEnabled(true);
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
	            	
	            	//Log.d(TAG, "Scroll: " + scrollState);
	            	/*
	            	if(AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == scrollState)
	            	{
	            		
	            	}*/
	            }
	
	            
	            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, int totalItemCount) {
	            	RobotoCheckBox cb = getCheckBoxAtPosition(0, view);
	            	NewsListCursorAdapter.ChangeCheckBoxState(cb, true, getActivity());
        			
	            	if(((firstVisibleItem + visibleItemCount) == totalItemCount) && !DialogShowedToMarkLastItemsAsRead ){
	            		
	            		DialogShowedToMarkLastItemsAsRead = true;
	            		
	            		boolean needQuestion = false;
	            		for (int i = firstVisibleItem + 1; i < firstVisibleItem + visibleItemCount; i++) {
	            			cb = getCheckBoxAtPosition(i - firstVisibleItem, view);
	            			if(!cb.isChecked())
	            			{
	            				needQuestion = true;
	            				break;
	            			}
	            		}
	            		
	            		if(needQuestion)
	            			new AlertDialog.Builder(getActivity())
        						.setTitle("Alle als gelesen markieren ?")
        						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										for (int i = firstVisibleItem + 1; i < firstVisibleItem + visibleItemCount; i++) {
											RobotoCheckBox cb = getCheckBoxAtPosition(i - firstVisibleItem, view);
					            			NewsListCursorAdapter.ChangeCheckBoxState(cb, true, getActivity());
					            		}
									}
								})
								.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,int id) {
										// if this button is clicked, just close
										// the dialog box and do nothing
										dialog.cancel();
									}
								})
								.create()
								.show();
	            	}	            	
	            }
	            
	            /*
	            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	                for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
	                	
	                	if(lastItemPosition < firstVisibleItem)
	                	{
	                		lastItemPosition = firstVisibleItem;
	                		
	                		CheckBox cb = (CheckBox) view.findViewById(R.id.cb_lv_item_read);
	                		if(!cb.isChecked())
	                			cb.setChecked(true);
	                		
	                		//dbConn.
	                	}
	                	
	                    //Cursor cursor = (Cursor)view.getItemAtPosition(i);
	                    //long id = cursor.getLong(cursor.getColumnIndex(AlertsContract._ID));
	                    //String type = cursor.getString(cursor.getColumnIndex(AlertsContract.TYPE));
	                    //Log.d("VIEWED", "This is viewed "+ type + " id: " + id);
	                    //Log.d("VIEWED", "This is viewed "+ firstVisibleItem + " id: ");
	
	                    // here I can get the id and mark the item read
	                }
	            }*/
	            
	            
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
				//setEmptyListView();
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
