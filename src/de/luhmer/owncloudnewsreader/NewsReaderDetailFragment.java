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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
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
import de.luhmer.owncloudnewsreader.cursor.SimpleCursorLoader;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection.SORT_DIRECTION;
import de.luhmer.owncloudnewsreader.helper.MenuUtilsSherlockFragmentActivity;

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

	//private DatabaseConnection dbConn;
	
	//private boolean DialogShowedToMarkLastItemsAsRead = false; 
	
	/*
	private NewsListCursorAdapter lvAdapter;
	
	public NewsListCursorAdapter getLvAdapter() {
		return lvAdapter;
	}*/

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
		
		//setRetainInstance(true);
				
		//dbConn = new DatabaseConnection(getActivity());
		
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
				
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setTitle(titel);
			
			UpdateMenuItemsState();//Is called on Tablets and Smartphones but on Smartphones the menuItemDownloadMoreItems is null. So it will be ignored
			
			//getListView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			
			//lvAdapter = null;
			NewsListCursorAdapter lvAdapter  = new NewsListCursorAdapter(getActivity(), null, this);
			setListAdapter(lvAdapter);
			
			getActivity().getSupportLoaderManager().destroyLoader(0);
			UpdateCursor();
		}
	}
	
	
	public void UpdateMenuItemsState()
	{	
		if(MenuUtilsSherlockFragmentActivity.getMenuItemDownloadMoreItems() != null)
		{
			if(idFolder != null) {
				if(idFolder.equals(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS))
					MenuUtilsSherlockFragmentActivity.getMenuItemDownloadMoreItems().setEnabled(false);
				else
					MenuUtilsSherlockFragmentActivity.getMenuItemDownloadMoreItems().setEnabled(true);
			} else
				MenuUtilsSherlockFragmentActivity.getMenuItemDownloadMoreItems().setEnabled(false);
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
		//if(lvAdapter != null)
		//	lvAdapter.CloseDatabaseConnection();
		//if(lvAdapter != null)
		//	lvAdapter.CloseDatabaseConnection();
		//if(dbConn != null)
		//	dbConn.closeDatabase();
		super.onDestroy();
	}

	public void UpdateCursor()
	{
		try
		{	
			LoaderManager loader = getActivity().getSupportLoaderManager();
			loader.initLoader(0, null, new LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {
					return new NewsDetailCursorLoader(getActivity(), idFolder, idFeed);
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
					NewsListCursorAdapter nca = (NewsListCursorAdapter) getListAdapter();
					if(nca != null)
						nca.swapCursor(cursor);
					//((NewsListCursorAdapter) getListAdapter()).changeCursor(cursor);
				}

				@Override
				public void onLoaderReset(Loader<Cursor> loader) {
					NewsListCursorAdapter nca = (NewsListCursorAdapter) getListAdapter();
					if(nca != null)
						((NewsListCursorAdapter) getListAdapter()).swapCursor(null);
				}
			});
			
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void notifyDataSetChangedOnAdapter()
	{
		NewsListCursorAdapter nca = (NewsListCursorAdapter) getListAdapter();
		if(nca != null)
			((NewsListCursorAdapter) getListAdapter()).notifyDataSetChanged();
	}

    public static Cursor getRightCusor(Context context, String idFolder, String idFeed)
    {
    	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    	boolean onlyUnreadItems = mPrefs.getBoolean(SettingsActivity.CB_SHOWONLYUNREAD_STRING, false);
    	boolean onlyStarredItems = false;
    	if(idFolder != null)
    		if(idFolder.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS))
    			onlyStarredItems = true;
    	
    	SORT_DIRECTION sDirection = SORT_DIRECTION.asc;
    	String sortDirection = mPrefs.getString(SettingsActivity.SP_SORT_ORDER, "desc");
    	if(sortDirection.equals(SORT_DIRECTION.desc.toString()))
    		sDirection = SORT_DIRECTION.desc;
    		
    	DatabaseConnection dbConn = new DatabaseConnection(context);
    	String sqlSelectStatement = null;
    	if(idFeed != null)
    		sqlSelectStatement = dbConn.getAllItemsIdsForFeedSQL(idFeed, onlyUnreadItems, onlyStarredItems, sDirection);
        else if(idFolder != null)
        {
        	if(idFolder.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS))
        		onlyUnreadItems = false;
        	sqlSelectStatement = dbConn.getAllItemsIdsForFolderSQL(idFolder, onlyUnreadItems, sDirection);
        }
    	if(sqlSelectStatement != null) {    		
    		dbConn.insertIntoRssCurrentViewTable(sqlSelectStatement);
    	}
    	
    	
    	return dbConn.getCurrentSelectedRssItems(sDirection);
    	/*
        if(idFeed != null)
            return dbConn.getAllItemsForFeed(idFeed, onlyUnreadItems, onlyStarredItems, sDirection);
        else if(idFolder != null)
        {
        	if(idFolder.equals(SubscriptionExpandableListAdapter.ALL_STARRED_ITEMS))
        		onlyUnreadItems = false;
            return dbConn.getAllItemsForFolder(idFolder, onlyUnreadItems, sDirection);
        }
        return null;
        */
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
	
	public static class NewsDetailCursorLoader extends SimpleCursorLoader {
		String idFolder;
		String idFeed;
		
	    public NewsDetailCursorLoader(Context context, String idFolder, String idFeed) {
	        super(context);	        
	        this.idFolder = idFolder;
	        this.idFeed = idFeed;
	    }

	    @Override 
	    public Cursor loadInBackground() {
	        return getRightCusor(getContext(), idFolder, idFeed);
	    }
	}
	
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
