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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.apache.commons.lang3.time.StopWatch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.dao.query.LazyList;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.adapter.DividerItemDecoration;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.ViewHolder;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm.SORT_DIRECTION;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;

/**
 * A fragment representing a single NewsReader detail screen. This fragment is
 * either contained in a {@link NewsReaderListActivity} in two-pane mode (on
 * tablets) or a {@link NewsReaderListActivity} on handsets.
 */
public class NewsReaderDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	protected final String TAG = getClass().getCanonicalName();


	//private boolean DialogShowedToMarkLastItemsAsRead = false;

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

    private boolean reloadCursorOnStartUp = false;

	//private static ArrayList<Integer> databaseIdsOfItems;
    private static final String LAYOUT_MANAGER_STATE = "LAYOUT_MANAGER_STATE";

    @InjectView(R.id.pb_loading) ProgressBar pbLoading;
    @InjectView(R.id.tv_no_items_available) View tvNoItemsAvailable;
    @InjectView(R.id.list) RecyclerView recyclerView;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NewsReaderDetailFragment() {
		//databaseIdsOfItems = new ArrayList<Integer>();
	}

    public void setUpdateListViewOnStartUp(boolean reloadCursorOnStartUp) {
        this.reloadCursorOnStartUp = reloadCursorOnStartUp;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		}
	}

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if(mPrefs.getBoolean(SettingsActivity.CB_MARK_AS_READ_WHILE_SCROLLING_STRING, false)) {
            recyclerView.addOnScrollListener(ListScrollListener);
        }

        if(reloadCursorOnStartUp)
            UpdateCurrentRssView(getActivity(), true);
        else
            UpdateCurrentRssView(getActivity(), false);

        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    private RecyclerView.OnScrollListener ListScrollListener = new RecyclerView.OnScrollListener() {
            //CheckBox lastViewedArticleCheckbox = null;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if(dy == 0 || recyclerView.getChildCount() <= 0)
                return;

            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
            int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

            for(int i=firstVisibleItem;i<=lastVisibleItem;i++) {
                ViewHolder vh = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(i);
                if(vh != null && !vh.shouldStayUnread()) {
                    vh.setReadState(true);
                }
            }
        }
    };

    public void UpdateMenuItemsState()
	{
        NewsReaderListActivity nla = (NewsReaderListActivity)getActivity();
		if(nla.getMenuItemDownloadMoreItems() != null)
		{
			if(idFolder != null && idFolder == SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue()) {
                nla.getMenuItemDownloadMoreItems().setEnabled(false);
            } else {
                nla.getMenuItemDownloadMoreItems().setEnabled(true);
            }
		}
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(titel);
    }

    public void onEventMainThread(PodcastDownloadService.DownloadProgressUpdate downloadProgress) {
        NewsListRecyclerAdapter nca = (NewsListRecyclerAdapter) recyclerView.getAdapter();
        if(nca != null) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            NewsListRecyclerAdapter.downloadProgressList.put((int) downloadProgress.podcast.itemId, downloadProgress.podcast.downloadProgress);

            RssItem currentRssItem;
            for (int i = linearLayoutManager.findFirstVisibleItemPosition(); i < linearLayoutManager.findLastVisibleItemPosition(); i++) {
                currentRssItem = nca.getItem(i);
                if (currentRssItem.getId().equals(downloadProgress.podcast.itemId)) {
                    int position = i - linearLayoutManager.findFirstVisibleItemPosition();
                    nca.setDownloadPodcastProgressbar(linearLayoutManager.getChildAt(position), currentRssItem);
                    break;
                }
            }
        }
    }

	public void notifyDataSetChangedOnAdapter()
	{
        NewsListRecyclerAdapter nca = (NewsListRecyclerAdapter) recyclerView.getAdapter();
        if(nca != null)
            nca.notifyDataSetChanged();
	}

    /**
     * Updates the current RSS-View
     * @param context
     */
    public void UpdateCurrentRssView(Context context, boolean refreshCurrentRssView) {
        Log.v(TAG, "UpdateCurrentRssView");
        new UpdateCurrentRssViewTask(context, refreshCurrentRssView).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public LinearLayoutManager getLayoutManager() {
        if(recyclerView == null) return null;
        return (LinearLayoutManager) recyclerView.getLayoutManager();
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
            recyclerView.setVisibility(View.GONE);
            tvNoItemsAvailable.setVisibility(View.GONE);

            sortDirection = getSortDirection(context);

            super.onPreExecute();
        }

        @Override
        protected LazyList<RssItem> doInBackground(Void... urls) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

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

            setUpdateListViewOnStartUp(false);//Always reset this variable here. Otherwise the list will be cleared when the activity is restarted

            LazyList<RssItem> list = dbConn.getCurrentRssItemView(sortDirection);

            stopWatch.stop();
            Log.v(TAG, "Reloaded CurrentRssView - time taken: " + stopWatch.toString());

            return list;
        }

        @Override
        protected void onPostExecute(LazyList<RssItem> rssItemLazyList) {
            try
            {
                // TODO: is this necessary for RecyclerView?
                // Block children layout for now
                //BlockingListView bView = ((BlockingListView) getListView());
                //bView.setBlockLayoutChildren(true);

                NewsListRecyclerAdapter nra = ((NewsListRecyclerAdapter) recyclerView.getAdapter());
                if(nra != null) {
                    nra.setLazyList(rssItemLazyList);
                } else {
                    nra = new NewsListRecyclerAdapter(getActivity(), rssItemLazyList, (PodcastFragmentActivity) getActivity());
                    recyclerView.setAdapter(nra);
                }

                pbLoading.setVisibility(View.GONE);
                if(nra.getItemCount() <= 0) {
                    recyclerView.setVisibility(View.GONE);
                    tvNoItemsAvailable.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvNoItemsAvailable.setVisibility(View.GONE);
                }

                // TODO: see above: bView.setBlockLayoutChildren(false);
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
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        return rootView;
	}

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null)
            recyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LAYOUT_MANAGER_STATE, getLayoutManager().onSaveInstanceState());
    }
}
