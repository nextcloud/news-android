/*
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

package de.luhmer.owncloudnewsreader.ListView;

import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_STARRED_ITEMS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ITEMS_WITHOUT_FOLDER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionListItemBinding;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionListSubItemBinding;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.StopWatch;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
import de.luhmer.owncloudnewsreader.model.AbstractItem;
import de.luhmer.owncloudnewsreader.model.ConcreteFeedItem;
import de.luhmer.owncloudnewsreader.model.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.model.Tuple;

public class SubscriptionExpandableListAdapter extends BaseExpandableListAdapter {
    private final String TAG = getClass().getCanonicalName();

    private final Context mContext;
    private final DatabaseConnectionOrm dbConn;

    private final ListView listView;

    private ExpListTextClicked eListTextClickHandler;

    private final FavIconHandler favIconHandler;

    private ArrayList<AbstractItem> mCategoriesArrayList;
    private SparseArray<ArrayList<ConcreteFeedItem>> mItemsArrayList;
    private boolean showOnlyUnread = false;

    private SparseArray<String> starredCountFeeds;
    private SparseArray<String> unreadCountFolders;
    private SparseArray<String> unreadCountFeeds;

    private final SharedPreferences mPrefs;

    public enum SPECIAL_FOLDERS  {
        ALL_UNREAD_ITEMS(-10), ALL_STARRED_ITEMS(-11), ALL_ITEMS(-12), ITEMS_WITHOUT_FOLDER(-22);

        private final int id;
        SPECIAL_FOLDERS(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }


        public String getValueString() {
            return String.valueOf(id);
        }

        @Override
        public String toString() {
            return getValueString();
        }
    }

    public SubscriptionExpandableListAdapter(Context mContext, DatabaseConnectionOrm dbConn, ListView listView, SharedPreferences prefs) {
        this.favIconHandler = new FavIconHandler(mContext);
        this.mPrefs = prefs;

    	this.mContext = mContext;
    	this.dbConn = dbConn;

        unreadCountFeeds = new SparseArray<>();
        unreadCountFolders = new SparseArray<>();
        starredCountFeeds = new SparseArray<>();

        mCategoriesArrayList = new ArrayList<>();
        mItemsArrayList = new SparseArray<>();

        this.listView = listView;
    }

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		int parent_id = (int)getGroupId(groupPosition);
        return mItemsArrayList.get(parent_id).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return ((ConcreteFeedItem)(getChild(groupPosition, childPosition))).id_database;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final ConcreteFeedItem item = (ConcreteFeedItem)getChild(groupPosition, childPosition);
        final ChildHolder viewHolder;

        if (convertView != null) {
            viewHolder = (ChildHolder) convertView.getTag();
        } else {
            LinearLayout view = new LinearLayout(mContext);
            SubscriptionListSubItemBinding binding = SubscriptionListSubItemBinding.inflate(LayoutInflater.from(mContext), view, true);
            convertView = binding.getRoot();
            viewHolder = new ChildHolder(binding);
            convertView.setTag(viewHolder);
        }


        if(item != null)
        {
	        String headerText = (item.header != null) ? item.header : "";
	        viewHolder.binding.summary.setText(headerText);


            String unreadCount;
            if(item.idFolder == ALL_STARRED_ITEMS.getValue()) {
                unreadCount = starredCountFeeds.get((int) item.id_database);
            } else {
                unreadCount = unreadCountFeeds.get((int) item.id_database);
            }

            if(unreadCount != null)
                viewHolder.binding.tvUnreadCount.setText(unreadCount);
            else
                viewHolder.binding.tvUnreadCount.setText("");

            favIconHandler.loadFavIconForFeed(item.favIcon, viewHolder.binding.iVFavicon);
        }
        else
        {
	        viewHolder.binding.summary.setText(mContext.getString(R.string.login_dialog_text_something_went_wrong));
	        viewHolder.binding.tvUnreadCount.setText("");
	        viewHolder.binding.iVFavicon.setImageDrawable(null);
        }

        return convertView;
	}

	static class ChildHolder {
        @NonNull final SubscriptionListSubItemBinding binding;

        public ChildHolder(@NonNull SubscriptionListSubItemBinding binding) {
            this.binding = binding;
        }
	  }

	@Override
	public int getChildrenCount(int groupPosition) {
        int parent_id = (int)getGroupId(groupPosition);
        return (mItemsArrayList.get(parent_id) != null) ? mItemsArrayList.get(parent_id).size() : 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mCategoriesArrayList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mCategoriesArrayList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return ((AbstractItem)getGroup(groupPosition)).id_database;
	}

    private enum GroupViewType { FOLDER, FEED }

    @Override
    public int getGroupType(int groupPosition) {
        AbstractItem ai = mCategoriesArrayList.get(groupPosition);

        if(ai instanceof FolderSubscribtionItem)
            return GroupViewType.FOLDER.ordinal();
        else
            return GroupViewType.FEED.ordinal();
    }

    @Override
    public int getGroupTypeCount() {
        return GroupViewType.values().length;
    }

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {

        GroupHolder viewHolder;
        final AbstractItem group = (AbstractItem) getGroup(groupPosition);

        if (convertView == null) {
            SubscriptionListItemBinding binding = SubscriptionListItemBinding.inflate(LayoutInflater.from(mContext), new LinearLayout(mContext), true);
            viewHolder = new GroupHolder(binding);
            convertView = binding.getRoot();
            binding.getRoot().setTag(viewHolder);
        } else {
        	viewHolder = (GroupHolder) convertView.getTag();
        }

        viewHolder.binding.summary.setText(group.header);
        viewHolder.binding.listItemLayout.setOnClickListener(v -> {

            long idFeed = group.id_database;
            boolean skipFireEvent = false;

            if (group instanceof ConcreteFeedItem) {
                fireListTextClicked(idFeed, false, (long) ITEMS_WITHOUT_FOLDER.getValue());
                skipFireEvent = true;
            }

            if (!skipFireEvent)
                fireListTextClicked(idFeed, true, ((FolderSubscribtionItem) group).idFolder);
        });

        viewHolder.binding.listItemLayout.setOnLongClickListener(v -> {

            long idFeed = group.id_database;

            if (group instanceof ConcreteFeedItem) {
                fireListTextLongClicked(idFeed, false, (long) ITEMS_WITHOUT_FOLDER.getValue());
            } else {
                fireListTextLongClicked(idFeed, true, ((FolderSubscribtionItem) group).idFolder);
            }
            return true; //consume event
        });


        viewHolder.binding.tVFeedsCount.setText("");
        boolean skipGetUnread = false;
        if(group.idFolder != null && group.idFolder == ITEMS_WITHOUT_FOLDER.getValue()) {
            String unreadCount = unreadCountFeeds.get((int) group.id_database);
            if(unreadCount != null) {
                viewHolder.binding.tVFeedsCount.setText(unreadCount);
            }

            skipGetUnread = true;
        }

        if(!skipGetUnread) {
            String unreadCount = unreadCountFolders.get((int) group.id_database);
            if(unreadCount != null)
                viewHolder.binding.tVFeedsCount.setText(unreadCount);
        }


        int rotation = 0;
        int contentDescriptionId = R.string.content_desc_none;


        if(group.idFolder != null)
        {
            viewHolder.binding.imgViewExpandableIndicator.setVisibility(View.GONE);
	        if(group.idFolder == ITEMS_WITHOUT_FOLDER.getValue())
	        {
                ConcreteFeedItem concreteFeedItem = ((ConcreteFeedItem) group);
                favIconHandler.loadFavIconForFeed(concreteFeedItem.favIcon, viewHolder.binding.imgViewFavicon);
	        }
        } else {
        	if(group.id_database == ALL_STARRED_ITEMS.getValue()) {
                viewHolder.binding.imgViewExpandableIndicator.setVisibility(View.GONE);
        		viewHolder.binding.imgViewFavicon.setVisibility(View.VISIBLE);
                rotation = 0;
                viewHolder.binding.imgViewFavicon.setImageResource(R.drawable.ic_star_border_24dp_theme_aware);
        	} else if (getChildrenCount( groupPosition ) == 0 ) {
	        	viewHolder.binding.imgViewExpandableIndicator.setVisibility(View.GONE);
                viewHolder.binding.imgViewFavicon.setVisibility(View.INVISIBLE);
	        } else {
	        	viewHolder.binding.imgViewExpandableIndicator.setVisibility(View.VISIBLE);
                viewHolder.binding.imgViewFavicon.setVisibility(View.INVISIBLE);
                viewHolder.binding.imgViewExpandableIndicator.setImageResource(R.drawable.ic_action_expand_less_24);

	        	if(isExpanded) {
                    rotation = 180;
                    contentDescriptionId = R.string.content_desc_collapse;
	        	} else {
                    if (ViewCompat.getLayoutDirection(listView) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                        rotation = -90; // mirror for rtl layout
                    } else {
                        rotation = 90;
                    }
                    contentDescriptionId = R.string.content_desc_expand;
                }
        
                viewHolder.binding.imgViewExpandableIndicator.setOnClickListener(v -> {
                    if(isExpanded)
                        ((ExpandableListView)listView).collapseGroup(groupPosition);
                    else
                        ((ExpandableListView)listView).expandGroup(groupPosition);
                });
            }
        }

        viewHolder.binding.imgViewExpandableIndicator.setRotation(rotation);
        viewHolder.binding.imgViewExpandableIndicator.setContentDescription(viewHolder.binding.imgViewExpandableIndicator.getContext().getString(contentDescriptionId));

        return convertView;
    }


    static class GroupHolder {
        @NonNull final SubscriptionListItemBinding binding;

        public GroupHolder(@NonNull SubscriptionListItemBinding binding) {
            this.binding = binding;
        }
    }


    @Override
    public boolean hasStableIds() {
		return false;
	}

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

    public void NotifyDataSetChangedAsync() {
        new NotifyDataSetChangedAsyncTask().execute((Void) null);
    }


    private class NotifyDataSetChangedAsyncTask extends AsyncTask<Void, Void, Void> {
        SparseArray<String> starredCountFeedsTemp;
        SparseArray<String> unreadCountFoldersTemp;
        SparseArray<String> unreadCountFeedsTemp;
        SparseArray<String> urlsToFavIconsTemp;

        @Override
        protected Void doInBackground(Void... voids) {
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();

            SparseArray<String>[] temp = dbConn.getUnreadItemCountFeedFolder();

            unreadCountFoldersTemp = temp[0];// dbConn.getUnreadItemCountForFolder();
            unreadCountFeedsTemp = temp[1]; // dbConn.getUnreadItemCountForFeed();

            starredCountFeedsTemp = dbConn.getStarredItemCount();
            urlsToFavIconsTemp = dbConn.getUrlsToFavIcons();

            stopwatch.stop();
            Log.v(TAG, "Fetched folder/feed counts in " + stopwatch.toString());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(showOnlyUnread) {
                for (int i = 0; i < mCategoriesArrayList.size(); i++) {
                    AbstractItem item = mCategoriesArrayList.get(i);

                    if(item instanceof FolderSubscribtionItem &&
                            unreadCountFoldersTemp.get(((Long) item.id_database).intValue()) == null) {
                        Log.v(TAG, "Remove folder item!!!");
                        mCategoriesArrayList.remove(i);
                        i--;
                    } else if(item instanceof ConcreteFeedItem &&
                            unreadCountFeedsTemp.get(((Long) item.id_database).intValue()) == null) {
                        Log.v(TAG, "Remove feed item!!!");
                        mCategoriesArrayList.remove(i);
                        i--;
                    } /* else {
                        Log.v(TAG, "Keep.. " + unreadCountFoldersTemp.get(((Long) item.id_database).intValue()));
                    } */
                }

                for (int i = 0; i < mItemsArrayList.size(); i++) {
                    ArrayList<ConcreteFeedItem> item = mItemsArrayList.valueAt(i);
                    for (int x = 0; x < item.size(); x++) {
                        if (unreadCountFeedsTemp.get((int) item.get(x).id_database) == null) {
                            item.remove(x);
                            x--;
                            Log.v(TAG, "Remove sub feed!!");
                        }
                    }
                }
            }

            notifyCountDataSetChanged(unreadCountFoldersTemp, unreadCountFeedsTemp, starredCountFeedsTemp);
            super.onPostExecute(aVoid);
        }
    }

    public void ReloadAdapterAsync() {
        new ReloadAdapterAsyncTask().execute((Void) null);
    }

    private class ReloadAdapterAsyncTask extends AsyncTask<Void, Void, Tuple<ArrayList<AbstractItem>, SparseArray<ArrayList<ConcreteFeedItem>>>> {

        public ReloadAdapterAsyncTask() {

        }

        @Override
        protected Tuple<ArrayList<AbstractItem>, SparseArray<ArrayList<ConcreteFeedItem>>> doInBackground(Void... voids) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            Tuple<ArrayList<AbstractItem>, SparseArray<ArrayList<ConcreteFeedItem>>> ad = ReloadAdapter();
            //return reloadAdapter();

            stopWatch.stop();
            Log.v(TAG, "Reload Adapter - time taken: " + stopWatch.toString());

            return ad;
        }

        @Override
        protected void onPostExecute(Tuple<ArrayList<AbstractItem>, SparseArray<ArrayList<ConcreteFeedItem>>> arrayListSparseArrayTuple) {
            mCategoriesArrayList = arrayListSparseArrayTuple.key;
            mItemsArrayList = arrayListSparseArrayTuple.value;

            notifyDataSetChanged();

            NotifyDataSetChangedAsync();

            super.onPostExecute(arrayListSparseArrayTuple);
        }

    }

    public Tuple<ArrayList<AbstractItem>, SparseArray<ArrayList<ConcreteFeedItem>>> ReloadAdapter()
    {
        showOnlyUnread = mPrefs.getBoolean(SettingsActivity.CB_SHOWONLYUNREAD_STRING, false);

        ArrayList<AbstractItem> mCategoriesArrayListAsync = new ArrayList<>();
        mCategoriesArrayListAsync.add(new FolderSubscribtionItem(mContext.getString(R.string.allUnreadFeeds), null, ALL_UNREAD_ITEMS.getValue()));
        mCategoriesArrayListAsync.add(new FolderSubscribtionItem(mContext.getString(R.string.starredFeeds), null, ALL_STARRED_ITEMS.getValue()));


        StopWatch sw = new StopWatch();
        sw.start();

        List<Folder> folderList;
        //if(showOnlyUnread) {
        //    folderList = dbConn.getListOfFoldersWithUnreadItems();
        //} else {
            folderList = dbConn.getListOfFolders();
        //}

        sw.stop();
        Log.v(TAG, "Time needed (fetch folder list): " + sw.toString());


        for(Folder folder : folderList) {
            mCategoriesArrayListAsync.add(new FolderSubscribtionItem(folder.getLabel(), null, folder.getId()));
        }

        for(Feed feed : dbConn.getListOfFeedsWithoutFolders(showOnlyUnread)) {
            mCategoriesArrayListAsync.add(new ConcreteFeedItem(feed.getFeedTitle(), (long) ITEMS_WITHOUT_FOLDER.getValue(), feed.getId(), feed.getFaviconUrl(), feed.getId()));
        }

        SparseArray<ArrayList<ConcreteFeedItem>> mItemsArrayListAsync = new SparseArray<>();

        for(int groupPosition = 0; groupPosition < mCategoriesArrayListAsync.size(); groupPosition++) {
            //int parent_id = (int)getGroupId(groupPosition);
            int parent_id = (int) mCategoriesArrayListAsync.get(groupPosition).id_database;
            mItemsArrayListAsync.append(parent_id, new ArrayList<>());

            List<Feed> feedItemList = null;

            if(parent_id == ALL_UNREAD_ITEMS.getValue()) {
                feedItemList = dbConn.getAllFeedsWithUnreadRssItems();
            } else if(parent_id == ALL_STARRED_ITEMS.getValue()) {
                feedItemList = dbConn.getAllFeedsWithStarredRssItems();
            } else {
                for(Folder folder : folderList) {//Find the current selected folder
                    if (folder.getId() == parent_id) {//Current item
                        feedItemList = dbConn.getAllFeedsWithUnreadRssItemsForFolder(folder.getId());
                        break;
                    }
                }
            }

            if(feedItemList != null) {
                for (Feed feed : feedItemList) {
                    ConcreteFeedItem newItem = new ConcreteFeedItem(feed.getFeedTitle(), (long) parent_id, feed.getId(), feed.getFaviconUrl(), feed.getId());
                    mItemsArrayListAsync.get(parent_id).add(newItem);
                }
            }
        }

        return new Tuple<>(mCategoriesArrayListAsync, mItemsArrayListAsync);
    }


    @SuppressLint("NewApi") // wrongly reports setSelectionFromTop is only available in lollipop
    public void notifyCountDataSetChanged(SparseArray<String> unreadCountFolders, SparseArray<String> unreadCountFeeds, SparseArray<String> starredCountFeeds) {
        this.unreadCountFolders = unreadCountFolders;
        this.unreadCountFeeds = unreadCountFeeds;
        this.starredCountFeeds = starredCountFeeds;

        BlockingExpandableListView bView = (BlockingExpandableListView) listView;

        int firstVisPos = bView.getFirstVisiblePosition();
        View firstVisView = bView.getChildAt(0);
        int top = firstVisView != null ? firstVisView.getTop() : 0;

        // Number of items added before the first visible item
        int itemsAddedBeforeFirstVisible = 0;

        bView.setBlockLayoutChildren(true);
        notifyDataSetChanged();
        bView.setBlockLayoutChildren(false);

        // Call setSelectionFromTop to change the ListView position
        if(bView.getCount() >= firstVisPos + itemsAddedBeforeFirstVisible)
            bView.setSelectionFromTop(firstVisPos + itemsAddedBeforeFirstVisible, top);
    }


    public void setHandlerListener(ExpListTextClicked listener)
	{
		eListTextClickHandler = listener;
	}
	protected void fireListTextClicked(long idFeed, boolean isFolder, Long optional_folder_id)
	{
        if(eListTextClickHandler != null)
			eListTextClickHandler.onTextClicked(idFeed, isFolder, optional_folder_id);
	}
    protected void fireListTextLongClicked(long idFeed, boolean isFolder, Long optional_folder_id)
    {
        if(eListTextClickHandler != null)
            eListTextClickHandler.onTextLongClicked(idFeed, isFolder, optional_folder_id);
    }
}
