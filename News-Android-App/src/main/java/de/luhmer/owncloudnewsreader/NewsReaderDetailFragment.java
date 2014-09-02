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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoCheckBox;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.LazyList;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.ListView.BlockingListView;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.adapter.NewsListArrayAdapter;
import de.luhmer.owncloudnewsreader.cursor.IOnStayUnread;
import de.luhmer.owncloudnewsreader.cursor.NewsListCursorAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection.SORT_DIRECTION;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;

/**
 * A fragment representing a single NewsReader detail screen. This fragment is
 * either contained in a {@link NewsReaderListActivity} in two-pane mode (on
 * tablets) or a {@link NewsReaderListActivity} on handsets.
 */
public class NewsReaderDetailFragment extends ListFragment implements IOnStayUnread {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	protected static final String TAG = "NewsReaderDetailFragment";


	//private boolean DialogShowedToMarkLastItemsAsRead = false;

	/*
	private NewsListCursorAdapter lvAdapter;

	public NewsListCursorAdapter getLvAdapter() {
		return lvAdapter;
	}*/

	Long idFeed;
	/**
	 * @return the idFeed
	 */
	public Long getIdFeed() {
		return idFeed;
	}

	Long idFolder;
	/**
	 * @return the idFolder
	 */
	public Long getIdFolder() {
		return idFolder;
	}

	String titel;

	/**
	 * @return the titel
	 */
	public String getTitel() {
		return titel;
	}

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;
	private int marginFromTop = ListView.INVALID_POSITION;

    private boolean reloadCursorOnStartUp = false;

	//private static ArrayList<Integer> databaseIdsOfItems;
	ArrayList<RobotoCheckBox> stayUnreadCheckboxes;

    @InjectView(R.id.pb_loading) ProgressBar pbLoading;
    @InjectView(R.id.tv_no_items_available) TextView tvNoItemsAvailable;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NewsReaderDetailFragment() {
		//databaseIdsOfItems = new ArrayList<Integer>();
		stayUnreadCheckboxes = new ArrayList<RobotoCheckBox>();
	}

    public void setUpdateListViewOnStartUp(boolean reloadCursorOnStartUp) {
        this.reloadCursorOnStartUp = reloadCursorOnStartUp;
    }

	public void setActivatedPosition(int position) {
		mActivatedPosition = position;
	}
	public void setMarginFromTop(int margin) {
		marginFromTop = margin;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//setRetainInstance(true);

		//dbConn = new DatabaseConnection(getActivity());

		if(getArguments() != null) {
			if (getArguments().containsKey(NewsReaderListActivity.FEED_ID)) {
				idFeed = getArguments().getLong(NewsReaderListActivity.FEED_ID);
			}
			if (getArguments().containsKey(NewsReaderListActivity.TITEL)) {
				titel = getArguments().getString(NewsReaderListActivity.TITEL);
			}
			if (getArguments().containsKey(NewsReaderListActivity.FOLDER_ID)) {
				idFolder = getArguments().getLong(NewsReaderListActivity.FOLDER_ID);
			}

			UpdateMenuItemsState();//Is called on Tablets and Smartphones but on Smartphones the menuItemDownloadMoreItems is null. So it will be ignored

			//getListView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);

			//lvAdapter = null;

			//getActivity().getSupportLoaderManager().destroyLoader(0);
		}
	}

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);

        //Reload data. Use case: download was finished while app was not in foreground
        notifyDataSetChangedOnAdapter();

        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    public void UpdateMenuItemsState()
	{
		if(MenuUtilsFragmentActivity.getMenuItemDownloadMoreItems() != null)
		{
			if(idFolder != null) {
				if(idFolder == SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue())
					MenuUtilsFragmentActivity.getMenuItemDownloadMoreItems().setEnabled(false);
				else
					MenuUtilsFragmentActivity.getMenuItemDownloadMoreItems().setEnabled(true);
			} else
				MenuUtilsFragmentActivity.getMenuItemDownloadMoreItems().setEnabled(false);
		}
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(titel);

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
	            		if(! (lastViewedArticleCheckbox.isChecked() && stayUnreadCheckboxes.contains(lastViewedArticleCheckbox)));
	            			NewsListCursorAdapter.ChangeCheckBoxState(lastViewedArticleCheckbox, true, getActivity());

	            		lastViewedArticleCheckbox = cb;
	            	}
	            }
	        });
		}

        if(reloadCursorOnStartUp)
            UpdateCurrentRssView(getActivity(), true);
        else
            UpdateCurrentRssView(getActivity(), false);

        //UpdateCursor();
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
	public void onDestroy() {
		//if(lvAdapter != null)
		//	lvAdapter.CloseDatabaseConnection();
		//if(lvAdapter != null)
		//	lvAdapter.CloseDatabaseConnection();
		//if(dbConn != null)
		//	dbConn.closeDatabase();
		super.onDestroy();
	}


    public void onEventMainThread(PodcastDownloadService.DownloadProgressUpdate downloadProgress) {
        NewsListArrayAdapter nca = (NewsListArrayAdapter) getListAdapter();
        if(nca != null) {
            nca.downloadProgressList.put((int) downloadProgress.podcast.itemId, downloadProgress.podcast.downloadProgress);

            RssItem currentRssItem;
            for (int i = getListView().getFirstVisiblePosition(); i < getListView().getLastVisiblePosition(); i++) {
                currentRssItem = (RssItem) getListAdapter().getItem(i);
                if (currentRssItem.getId().equals(downloadProgress.podcast.itemId)) {
                    int position = i - getListView().getFirstVisiblePosition();
                    nca.setDownloadPodcastProgressbar(getListView().getChildAt(position), currentRssItem);
                    break;
                }
            }
        }
    }

	public void notifyDataSetChangedOnAdapter()
	{
        NewsListArrayAdapter nca = (NewsListArrayAdapter) getListAdapter();
        if(nca != null)
            nca.notifyDataSetChanged();

		//NewsListCursorAdapter nca = (NewsListCursorAdapter) getListAdapter();
		//if(nca != null)
			//((NewsListCursorAdapter) getListAdapter()).notifyDataSetChanged();
	}

    /**
     * Updates the current RSS-View
     * @param context
     */
    public void UpdateCurrentRssView(Context context, boolean refreshCurrentRssView) {
        new UpdateCurrentRssViewTask(context, refreshCurrentRssView).execute((Void) null);//TODO api level 11--> new method
    }

    private class UpdateCurrentRssViewTask extends AsyncTask<Void, Void, LazyList<RssItem>> {

        Context context;
        SORT_DIRECTION sortDirection;
        boolean refreshCurrentRssView;

        public UpdateCurrentRssViewTask(Context context, boolean refreshCurrentRssView) {
            this.context = context;
            this.refreshCurrentRssView = refreshCurrentRssView;
        }

        @Override
        protected void onPreExecute() {
            pbLoading.setVisibility(View.VISIBLE);
            getListView().setVisibility(View.GONE);
            tvNoItemsAvailable.setVisibility(View.GONE);

            sortDirection = getSortDirection(context);

            super.onPreExecute();
        }

        @Override
        protected LazyList<RssItem> doInBackground(Void... urls) {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);

            if(refreshCurrentRssView) {
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean onlyUnreadItems = mPrefs.getBoolean(SettingsActivity.CB_SHOWONLYUNREAD_STRING, false);
                boolean onlyStarredItems = false;
                if (idFolder != null)
                    if (idFolder == SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_STARRED_ITEMS.getValue())
                        onlyStarredItems = true;

                String sqlSelectStatement = null;
                if (idFeed != null)
                    sqlSelectStatement = dbConn.getAllItemsIdsForFeedSQL(idFeed, onlyUnreadItems, onlyStarredItems, sortDirection);
                else if (idFolder != null) {
                    if (idFolder == SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_STARRED_ITEMS.getValue())
                        onlyUnreadItems = false;
                    sqlSelectStatement = dbConn.getAllItemsIdsForFolderSQL(idFolder, onlyUnreadItems, sortDirection);
                }
                if (sqlSelectStatement != null) {
                    dbConn.insertIntoRssCurrentViewTable(sqlSelectStatement);
                }
            }
            return dbConn.getCurrentRssItemView(sortDirection);
        }

        @Override
        protected void onPostExecute(LazyList<RssItem> rssItemLazyList) {
            try
            {
                // Block children layout for now
                BlockingListView bView = ((BlockingListView) getListView());

                bView.setBlockLayoutChildren(true);

                NewsListArrayAdapter lvAdapter  = new NewsListArrayAdapter(getActivity(), rssItemLazyList, NewsReaderDetailFragment.this, (PodcastFragmentActivity) getActivity());
                setListAdapter(lvAdapter);

                pbLoading.setVisibility(View.GONE);
                if(lvAdapter.getCount() <= 0) {
                    getListView().setVisibility(View.GONE);
                    tvNoItemsAvailable.setVisibility(View.VISIBLE);
                } else {
                    getListView().setVisibility(View.VISIBLE);
                    tvNoItemsAvailable.setVisibility(View.GONE);
                }

                try {
                    if(mActivatedPosition != ListView.INVALID_POSITION && marginFromTop != ListView.INVALID_POSITION)
                        getListView().setSelectionFromTop(mActivatedPosition, marginFromTop);
                    else if(mActivatedPosition != ListView.INVALID_POSITION)
                        getListView().setSelection(mActivatedPosition);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }

                bView.setBlockLayoutChildren(false);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }


    public static SORT_DIRECTION getSortDirection(Context context) {
        return NewsDetailActivity.getSortDirectionFromSettings(context);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_newsreader_detail, container, false);
        ButterKnife.inject(this, rootView);
		return rootView;
	}


    /*
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
	        return getRightCusor(getContext());
	    }
	}
    */

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(mPrefs.getBoolean(SettingsActivity.CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING, false)) {
            String currentUrl = ((NewsListArrayAdapter) getListAdapter()).getItem(position).getLink();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
            startActivity(browserIntent);

            ((RobotoCheckBox) v.findViewById(R.id.cb_lv_item_read)).setChecked(true);
        } else {
            Intent intentNewsDetailAct = new Intent(getActivity(), NewsDetailActivity.class);

            intentNewsDetailAct.putExtra(NewsReaderListActivity.ITEM_ID, position);
            intentNewsDetailAct.putExtra(NewsReaderListActivity.TITEL, titel);
            startActivityForResult(intentNewsDetailAct, Activity.RESULT_CANCELED);
        }
		super.onListItemClick(l, v, position, id);
	}


	@Override
	public void stayUnread(RobotoCheckBox cb) {
		stayUnreadCheckboxes.add(cb);
	}
}
