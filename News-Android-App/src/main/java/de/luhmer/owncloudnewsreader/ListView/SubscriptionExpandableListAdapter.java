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

package de.luhmer.owncloudnewsreader.ListView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.ViewUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
import de.luhmer.owncloudnewsreader.model.AbstractItem;
import de.luhmer.owncloudnewsreader.model.ConcreteFeedItem;
import de.luhmer.owncloudnewsreader.model.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.model.Tuple;

import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_STARRED_ITEMS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ITEMS_WITHOUT_FOLDER;

public class SubscriptionExpandableListAdapter extends BaseExpandableListAdapter {
    private final String TAG = getClass().getCanonicalName();

	private Context mContext;
    private DatabaseConnectionOrm dbConn;

    ListView listView;

    ExpListTextClicked eListTextClickHandler;

    private ArrayList<AbstractItem> mCategoriesArrayList;
    private SparseArray<ArrayList<ConcreteFeedItem>> mItemsArrayList;
	private boolean showOnlyUnread = false;

    public enum SPECIAL_FOLDERS  {
        ALL_UNREAD_ITEMS(-10), ALL_STARRED_ITEMS(-11), ALL_ITEMS(-12), ITEMS_WITHOUT_FOLDER(-22);

        private int id;
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

    private int mTextColorLightTheme;
    private FavIconHandler favIconHandler;

    LayoutInflater inflater;

    public SubscriptionExpandableListAdapter(Context mContext, DatabaseConnectionOrm dbConn, ListView listView)
    {
        favIconHandler = new FavIconHandler(mContext);

        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	this.mContext = mContext;
    	this.dbConn = dbConn;

        mTextColorLightTheme = ContextCompat.getColor(mContext, R.color.slider_listview_text_color_light_theme);

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
            convertView = inflater.inflate(R.layout.subscription_list_sub_item, view, true);
            viewHolder = new ChildHolder(convertView);
            convertView.setTag(viewHolder);
        }


        if(item != null)
        {
	        String headerText = (item.header != null) ? item.header : "";
	        viewHolder.tV_HeaderText.setText(headerText);


            String unreadCount;
            if(item.idFolder == ALL_STARRED_ITEMS.getValue()) {
                unreadCount = starredCountFeeds.get((int) item.id_database);
            } else {
                unreadCount = unreadCountFeeds.get((int) item.id_database);
            }

            if(unreadCount != null)
                viewHolder.tV_UnreadCount.setText(unreadCount);
            else
                viewHolder.tV_UnreadCount.setText("");

            favIconHandler.loadFavIconForFeed(item.favIcon, viewHolder.imgView_FavIcon);
        }
        else
        {
	        viewHolder.tV_HeaderText.setText(mContext.getString(R.string.login_dialog_text_something_went_wrong));
	        viewHolder.tV_UnreadCount.setText("");
	        viewHolder.imgView_FavIcon.setImageDrawable(null);
        }

        return convertView;
	}

	static class ChildHolder {
        @Bind(R.id.list_item_layout) View listItemLayout;
        @Bind(R.id.summary) TextView tV_HeaderText;
        @Bind(R.id.tv_unreadCount) TextView tV_UnreadCount;
        @Bind(R.id.iVFavicon) ImageView imgView_FavIcon;

        public ChildHolder(View view) {
            ButterKnife.bind(this, view);
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
	public View getGroupView(final int groupPosition, final boolean isExpanded,
			View convertView, ViewGroup parent) {

		GroupHolder viewHolder;
        final AbstractItem group = (AbstractItem) getGroup(groupPosition);

        if (convertView == null) {
            LinearLayout view = new LinearLayout(mContext);
            convertView = inflater.inflate(R.layout.subscription_list_item, view, true);
            viewHolder = new GroupHolder(convertView);
            view.setTag(viewHolder);
        } else {
        	viewHolder = (GroupHolder) convertView.getTag();
        }

        viewHolder.txt_Summary.setText(group.header);
        viewHolder.listItemLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                long idFeed = group.id_database;
                boolean skipFireEvent = false;

                if (group instanceof ConcreteFeedItem) {
                    fireListTextClicked(idFeed, false, (long) ITEMS_WITHOUT_FOLDER.getValue());
                    skipFireEvent = true;
                }

                if (!skipFireEvent)
                    fireListTextClicked(idFeed, true, ((FolderSubscribtionItem) group).idFolder);
            }
        });

        viewHolder.listItemLayout.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                long idFeed = group.id_database;

                if (group instanceof ConcreteFeedItem) {
                    fireListTextLongClicked(idFeed, false, (long) ITEMS_WITHOUT_FOLDER.getValue());
                } else {
                    fireListTextLongClicked(idFeed, true, ((FolderSubscribtionItem) group).idFolder);
                }
                return true; //consume event
            }
        });


        viewHolder.txt_UnreadCount.setText("");
        boolean skipGetUnread = false;
        if(group.idFolder != null && group.idFolder == ITEMS_WITHOUT_FOLDER.getValue()) {
            String unreadCount = unreadCountFeeds.get((int) group.id_database);
            if(unreadCount != null) {
                viewHolder.txt_UnreadCount.setText(unreadCount);
            }

            skipGetUnread = true;
        }

        if(!skipGetUnread) {
            String unreadCount = unreadCountFolders.get((int) group.id_database);
            if(unreadCount != null)
                viewHolder.txt_UnreadCount.setText(unreadCount);
        }


        int rotation = 0;
        int contentDescriptionId = R.string.content_desc_none;


        if(group.idFolder != null)
        {
            viewHolder.imgView.setVisibility(View.GONE);
	        if(group.idFolder == ITEMS_WITHOUT_FOLDER.getValue())
	        {
                ConcreteFeedItem concreteFeedItem = ((ConcreteFeedItem) group);
                favIconHandler.loadFavIconForFeed(concreteFeedItem.favIcon, viewHolder.faviconView);
	        }
        } else {
        	if(group.id_database == ALL_STARRED_ITEMS.getValue()) {
                viewHolder.imgView.setVisibility(View.GONE);
        		viewHolder.faviconView.setVisibility(View.VISIBLE);
                rotation = 0;
                viewHolder.faviconView.setImageResource(getBtn_rating_star_off_normal_holo_light());
        	} else if (getChildrenCount( groupPosition ) == 0 ) {
	        	viewHolder.imgView.setVisibility(View.GONE);
                viewHolder.faviconView.setVisibility(View.INVISIBLE);
	        } else {
	        	viewHolder.imgView.setVisibility(View.VISIBLE);
                viewHolder.faviconView.setVisibility(View.INVISIBLE);
                viewHolder.imgView.setImageResource(R.drawable.ic_action_expand_less);

	        	if(isExpanded) {
                    rotation = 180;
                    contentDescriptionId = R.string.content_desc_collapse;
	        	} else {
                    if (ViewUtils.isLayoutRtl(listView)) {
                        rotation = -90; // mirror for rtl layout
                    } else {
                        rotation = 90;
                    }
                    contentDescriptionId = R.string.content_desc_expand;
                }
        
                viewHolder.imgView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isExpanded)
                            ((ExpandableListView)listView).collapseGroup(groupPosition);
                        else
                            ((ExpandableListView)listView).expandGroup(groupPosition);
                    }
                });
	        }
        }

        viewHolder.imgView.setRotation(rotation);
        viewHolder.imgView.setContentDescription(viewHolder.imgView.getContext().getString(contentDescriptionId));

        return convertView;
	}





    private Integer btn_rating_star_off_normal_holo_light;

    private int getBtn_rating_star_off_normal_holo_light() {
        if(btn_rating_star_off_normal_holo_light == null) {
            if(ThemeChooser.isDarkTheme(mContext)) {
                btn_rating_star_off_normal_holo_light = R.drawable.ic_action_star_border_dark;
            } else {
                btn_rating_star_off_normal_holo_light = R.drawable.ic_action_star_border_light;
            }
        }
        return btn_rating_star_off_normal_holo_light;
    }

	static class GroupHolder
	{
        @Bind(R.id.list_item_layout) View listItemLayout;
        @Bind(R.id.summary) TextView txt_Summary;
        @Bind(R.id.tV_feedsCount) TextView txt_UnreadCount;
        @Bind(R.id.img_View_expandable_indicator) ImageButton imgView;
        @Bind(R.id.img_view_favicon) ImageView faviconView;

        public GroupHolder(View view) {
            ButterKnife.bind(this, view);
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


    SparseArray<String> starredCountFeeds;
    SparseArray<String> unreadCountFolders;
    SparseArray<String> unreadCountFeeds;
    SparseArray<String> urlsToFavIcons;


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
                    } else {
                        //Log.v(TAG, "Keep.. " + unreadCountFoldersTemp.get(((Long) item.id_database).intValue()));
                    }
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

            notifyCountDataSetChanged(unreadCountFoldersTemp, unreadCountFeedsTemp, urlsToFavIconsTemp, starredCountFeedsTemp);
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
            //return ReloadAdapter();

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
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
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
            mItemsArrayListAsync.append(parent_id, new ArrayList<ConcreteFeedItem>());

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
    public void notifyCountDataSetChanged(SparseArray<String> unreadCountFolders, SparseArray<String> unreadCountFeeds, SparseArray<String> urlsToFavIcons, SparseArray<String> starredCountFeeds) {
        this.unreadCountFolders = unreadCountFolders;
        this.unreadCountFeeds = unreadCountFeeds;
        this.urlsToFavIcons = urlsToFavIcons;
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
