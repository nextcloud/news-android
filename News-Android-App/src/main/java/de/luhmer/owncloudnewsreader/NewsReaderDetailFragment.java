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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.apache.commons.lang3.time.StopWatch;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.adapter.DividerItemDecoration;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.ViewHolder;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm.SORT_DIRECTION;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.database.model.RssItemDao;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;

/**
 * A fragment representing a single NewsReader detail screen. This fragment is
 * either contained in a {@link NewsReaderListActivity} in two-pane mode (on
 * tablets) or a {@link NewsReaderListActivity} on handsets.
 */
public class NewsReaderDetailFragment extends Fragment {

	protected final String TAG = getClass().getCanonicalName();

	private Long idFeed;

    private Drawable markAsReadDrawable;
    private Drawable starredDrawable;
    private int accentColor;
    private Parcelable layoutManagerSavedState;


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

    private int onResumeCount = 0;
    private static final String LAYOUT_MANAGER_STATE = "LAYOUT_MANAGER_STATE";
    private boolean mMarkAsReadWhileScrollingEnabled;

    @Bind(R.id.pb_loading) ProgressBar pbLoading;
    @Bind(R.id.tv_no_items_available) View tvNoItemsAvailable;
    @Bind(R.id.list) RecyclerView recyclerView;
    @Bind(R.id.swipeRefresh) SwipeRefreshLayout swipeRefresh;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public NewsReaderDetailFragment() {
	}


    public void setData(Long idFeed, Long idFolder, String titel, boolean updateListView) {
        Log.v(TAG, "Creating new itstance");

        this.idFeed = idFeed;
        this.idFolder = idFolder;
        this.titel = titel;
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(titel);

        if(updateListView)
            UpdateCurrentRssView(getActivity());
        else
            RefreshCurrentRssView();
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume called!");

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mMarkAsReadWhileScrollingEnabled = mPrefs.getBoolean(SettingsActivity.CB_MARK_AS_READ_WHILE_SCROLLING_STRING, false);

        //When the fragment is instantiated by the xml file, onResume will be called twice
        if(onResumeCount >= 2) {
            RefreshCurrentRssView();
        }
        onResumeCount++;

        super.onResume();
    }

    private void handleMarkAsReadScrollEvent() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
        int visibleItemCount = lastVisibleItem - firstVisibleItem;
        int totalItemCount = recyclerView.getAdapter().getItemCount();

        NewsListRecyclerAdapter adapter = (NewsListRecyclerAdapter) recyclerView.getAdapter();

        //Set the item at top to read
        ViewHolder vh = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(firstVisibleItem);
        if (vh != null && !vh.shouldStayUnread()) {
            adapter.ChangeReadStateOfItem(vh, true);
        }



        //Check if Listview is scrolled to bottom
        if (lastVisibleItem == (totalItemCount-1) &&
                visibleItemCount != 0 && //Check if list is empty
                recyclerView.getChildAt(visibleItemCount).getBottom() <= recyclerView.getHeight()) {
            for (int i = firstVisibleItem; i <= lastVisibleItem; i++) {
                RecyclerView.ViewHolder vhTemp = recyclerView.findViewHolderForLayoutPosition(i);
                if(vhTemp instanceof ViewHolder) { //Check for ViewHolder instance because of ProgressViewHolder
                    vh = (ViewHolder) vhTemp;
                    if (vh != null && !vh.shouldStayUnread()) {
                        adapter.ChangeReadStateOfItem(vh, true);
                    }
                }
            }
        }
    }

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

	public void notifyDataSetChangedOnAdapter()
	{
        NewsListRecyclerAdapter nca = (NewsListRecyclerAdapter) recyclerView.getAdapter();
        if(nca != null)
            nca.notifyDataSetChanged();
	}

    /**
     * Refreshes the current RSS-View
     */
    public void RefreshCurrentRssView() {
        Log.v(TAG, "RefreshCurrentRssView");
        NewsListRecyclerAdapter nra = ((NewsListRecyclerAdapter) recyclerView.getAdapter());

        if(nra != null) {
            nra.refreshAdapterDataAsync(new NewsListRecyclerAdapter.IOnRefreshFinished() {
                @Override
                public void OnRefreshFinished() {
                    pbLoading.setVisibility(View.GONE);

                    if (layoutManagerSavedState != null) {
                        recyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
                        layoutManagerSavedState = null;
                    }
                }
            });
        }
    }

    /**
     * Updates the current RSS-View
     * @param context
     */
    public void UpdateCurrentRssView(Context context) {
        Log.v(TAG, "UpdateCurrentRssView");
        AsyncTaskHelper.StartAsyncTask(new UpdateCurrentRssViewTask(context));
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public LinearLayoutManager getLayoutManager() {
        if(recyclerView == null) return null;
        return (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    private class UpdateCurrentRssViewTask extends AsyncTask<Void, Void, List<RssItem>> {

        private Context context;
        private SORT_DIRECTION sortDirection;

        public UpdateCurrentRssViewTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            pbLoading.setVisibility(View.VISIBLE);
            tvNoItemsAvailable.setVisibility(View.GONE);

            sortDirection = getSortDirection(context);

            super.onPreExecute();
        }

        @Override
        protected List<RssItem> doInBackground(Void... urls) {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);

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
                int index = sqlSelectStatement.indexOf("ORDER BY");
                if(index == -1) {
                    index = sqlSelectStatement.length();
                }
                sqlSelectStatement = new StringBuilder(sqlSelectStatement).insert(index, " GROUP BY " + RssItemDao.Properties.Fingerprint.columnName + " ").toString();
                dbConn.insertIntoRssCurrentViewTable(sqlSelectStatement);
            }

            StopWatch sw = new StopWatch();
            sw.start();

            List<RssItem> items = dbConn.getCurrentRssItemView(0);

            sw.stop();
            Log.v(TAG, "Time needed (init loading): " + sw.toString());

            return items;
        }

        @Override
        protected void onPostExecute(List<RssItem> rssItem) {
            try
            {
                NewsListRecyclerAdapter nra = ((NewsListRecyclerAdapter) recyclerView.getAdapter());
                if(nra == null) {
                    nra = new NewsListRecyclerAdapter(getActivity(), recyclerView, (PodcastFragmentActivity) getActivity());

                    recyclerView.setAdapter(nra);
                }
                nra.updateAdapterData(rssItem);

                pbLoading.setVisibility(View.GONE);
                if(nra.getItemCount() <= 0) {
                    tvNoItemsAvailable.setVisibility(View.VISIBLE);
                } else {
                    tvNoItemsAvailable.setVisibility(View.GONE);
                }

                recyclerView.scrollToPosition(0);

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
        ButterKnife.bind(this, rootView);

		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new NewsReaderItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        swipeRefresh.setColorSchemeColors(accentColor);
        swipeRefresh.setOnRefreshListener((SwipeRefreshLayout.OnRefreshListener) getActivity());

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetectorCompat detector = new GestureDetectorCompat(getActivity(), new RecyclerViewOnGestureListener());

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                detector.onTouchEvent(e);
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });


        return rootView;
	}

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(mMarkAsReadWhileScrollingEnabled && (e2.getY() - e1.getY()) < 0) { // (distance < 0) => scroll up
                handleMarkAsReadScrollEvent();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }


    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs,new int[]{R.attr.markasreadDrawable, R.attr.starredDrawable, R.attr.colorAccent});
        markAsReadDrawable = a.getDrawable(0);
        starredDrawable = a.getDrawable(1);
        int color = Constants.IsNextCloud(getContext()) ? R.color.nextcloudBlueLight : R.color.owncloudBlueLight;
        accentColor = a.getColor(2, ContextCompat.getColor(context, color));
        a.recycle();
    }

    // TODO: somehow always cancel item out animation
    private class NewsReaderItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {
        public NewsReaderItemTouchHelperCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return 0.25f;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
            final NewsListRecyclerAdapter adapter = (NewsListRecyclerAdapter) recyclerView.getAdapter();

            if(direction == ItemTouchHelper.LEFT) {
                adapter.toggleReadStateOfItem((ViewHolder) viewHolder);
            } else if(direction == ItemTouchHelper.RIGHT) {
                adapter.toggleStarredStateOfItem((ViewHolder) viewHolder);
                //adapter.toggleReadStateOfItem((ViewHolder) viewHolder);
            }
            // Hack to reset view, see https://code.google.com/p/android/issues/detail?id=175798
            recyclerView.removeView(viewHolder.itemView);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            // swipeRefresh cancels swiping left/right when accidentally moving in the y direction;
            swipeRefresh.setEnabled(!isCurrentlyActive);
            if(isCurrentlyActive) {
                Rect viewRect = new Rect();
                viewHolder.itemView.getDrawingRect(viewRect);
                float fractionMoved = Math.abs(dX/viewHolder.itemView.getMeasuredWidth());
                Drawable drawable;
                if(dX < 0) {
                    drawable = markAsReadDrawable;
                    viewRect.left = (int) dX + viewRect.right;
                } else {
                    drawable = starredDrawable;
                    viewRect.right = (int) dX - viewRect.left;
                }

                if(fractionMoved > getSwipeThreshold(viewHolder))
                    drawable.setState(new int[]{android.R.attr.state_above_anchor});
                else
                    drawable.setState(new int[]{-android.R.attr.state_above_anchor});

                viewRect.offset(0,viewHolder.itemView.getTop());
                drawable.setBounds(viewRect);
                drawable.draw(c);
            }
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if(savedInstanceState != null)
            layoutManagerSavedState = savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE);
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(LAYOUT_MANAGER_STATE, getLayoutManager().onSaveInstanceState());
    }


    public int getFirstVisibleScrollPosition() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager)recyclerView.getLayoutManager());
        return layoutManager.findFirstVisibleItemPosition();
    }


}
