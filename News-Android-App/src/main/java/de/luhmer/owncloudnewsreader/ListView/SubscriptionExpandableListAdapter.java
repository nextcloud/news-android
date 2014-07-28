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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.FileUtils;
import de.luhmer.owncloudnewsreader.helper.FontHelper;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;
import de.luhmer.owncloudnewsreader.model.AbstractItem;
import de.luhmer.owncloudnewsreader.model.ConcreteFeedItem;
import de.luhmer.owncloudnewsreader.model.FolderSubscribtionItem;

import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_STARRED_ITEMS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS;
import static de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ITEMS_WITHOUT_FOLDER;

public class SubscriptionExpandableListAdapter extends BaseExpandableListAdapter {
    //private static final String TAG = "SubscriptionExpandableListAdapter";

	private Context mContext;
    private DatabaseConnectionOrm dbConn;
    FontHelper fHelper;

    ListView listView;

    ExpListTextClicked eListTextClickHandler;

    private ArrayList<AbstractItem> mCategoriesArrayList;
    private SparseArray<SparseArray<ConcreteFeedItem>> mItemsArrayList;
	private boolean showOnlyUnread = false;

    public static enum SPECIAL_FOLDERS  {
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

    int mTextColorLightTheme;

    LayoutInflater inflater;
    private final String favIconPath;
    boolean mIsTwoPane;
    public static boolean isTwoPane(Context context) {
        return context.getResources().getBoolean(R.bool.two_pane);
        //return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public SubscriptionExpandableListAdapter(Context mContext, DatabaseConnectionOrm dbConn, ListView listView)
    {
        mIsTwoPane = isTwoPane(mContext);

        //Picasso.with(mContext).setDebugging(true);

        this.favIconPath = FileUtils.getPathFavIcons(mContext);
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	this.mContext = mContext;
    	this.dbConn = dbConn;

        mTextColorLightTheme = mContext.getResources().getColor(R.color.slider_listview_text_color_light_theme);

    	//int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    	//Use 1/8 of the available memory for this memory cache
    	//int cachSize = maxMemory / 8;
    	//favIconCache = new BitmapDrawableLruCache(cachSize);

        fHelper = new FontHelper(mContext);

        unreadCountFeeds = new SparseArray<String>();
        unreadCountFolders = new SparseArray<String>();
        starredCountFeeds = new SparseArray<String>();

        mCategoriesArrayList = new ArrayList<AbstractItem>();
        mItemsArrayList = new SparseArray<SparseArray<ConcreteFeedItem>>();

        this.listView = listView;
    }

    /*
    private void AddEverythingInCursorFolderToSubscriptions(Cursor itemsCursor, ArrayList<AbstractItem> dest)
    {
    	while (itemsCursor.moveToNext()) {
    		String header = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.FOLDER_LABEL));
            //String id_folder = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.FOLDER_LABEL_ID));
    		long id = itemsCursor.getLong(0);
            dest.add(new FolderSubscribtionItem(header, null, id, null));
    	}
        itemsCursor.close();
    }

    private void AddEverythingInCursorFeedsToSubscriptions(Cursor itemsCursor, ArrayList<AbstractItem> dest)
    {
    	while (itemsCursor.moveToNext()) {
    		String header = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_HEADERTEXT));
            //String id_folder = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_ID));
    		long id = itemsCursor.getLong(0);
            String subscriptionId = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_ID));
            String favIconUrl = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_FAVICON_URL));
            dest.add(new ConcreteFeedItem(header, ITEMS_WITHOUT_FOLDER.getValueString(), subscriptionId, favIconUrl, id));
    	}
        itemsCursor.close();
    }
    */

	@SuppressWarnings("deprecation")
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
            viewHolder = new ChildHolder(convertView, mContext);
            convertView.setTag(viewHolder);

            if(!ThemeChooser.isDarkTheme(mContext)) {
                viewHolder.tV_HeaderText.setTextColor(mTextColorLightTheme);
                viewHolder.tV_UnreadCount.setTextColor(mTextColorLightTheme);
            }
        }


        if(item != null)
        {
	        String headerText = (item.header != null) ? item.header : "";
	        viewHolder.tV_HeaderText.setText(headerText);


            String unreadCount = null;
            if(item.idFolder == ALL_STARRED_ITEMS.getValue()) {
                unreadCount = starredCountFeeds.get((int) item.id_database);
            } else {
                unreadCount = unreadCountFeeds.get((int) item.id_database);
            }

            if(unreadCount != null)
                viewHolder.tV_UnreadCount.setText(unreadCount);
            else
                viewHolder.tV_UnreadCount.setText("");
            /*
            else {
                viewHolder.tV_UnreadCount.setText("0");
                //mItemsArrayList.get(groupPosition).remove(childPosition-1);
                //this.notifyDataSetChanged();
            }*/
            /*
            else {
                boolean excludeStarredItems = (item.idFolder.equals(ALL_STARRED_ITEMS)) ? false : true;
                SetUnreadCountForFeed(viewHolder.tV_UnreadCount, String.valueOf(item.id_database), excludeStarredItems);
            }*/

            loadFavIconForFeed(item.favIcon, viewHolder.imgView_FavIcon);
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
        @InjectView(R.id.summary) TextView tV_HeaderText;
        @InjectView(R.id.tv_unreadCount) TextView tV_UnreadCount;
        @InjectView(R.id.iVFavicon) ImageView imgView_FavIcon;

        public ChildHolder(View view, Context mContext) {
            ButterKnife.inject(this, view);

            FontHelper fHelper = new FontHelper(mContext);
            fHelper.setFontForAllChildren(view, fHelper.getFont());
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		GroupHolder viewHolder;
        final AbstractItem group = (AbstractItem) getGroup(groupPosition);

        if (convertView == null) {
            LinearLayout view = new LinearLayout(mContext);
            convertView = inflater.inflate(R.layout.subscription_list_item, view, true);
            viewHolder = new GroupHolder(convertView, mContext);
            view.setTag(viewHolder);

            if(!ThemeChooser.isDarkTheme(mContext)) {
                viewHolder.txt_UnreadCount.setTextColor(mTextColorLightTheme);
                viewHolder.txt_Summary.setTextColor(mTextColorLightTheme);
            }
        } else {
        	viewHolder = (GroupHolder) convertView.getTag();
        }


        viewHolder.txt_Summary.setText(group.header);
        viewHolder.txt_Summary.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				long idFeed = group.id_database;
				boolean skipFireEvent = false;

                if(group instanceof ConcreteFeedItem) {
                    fireListTextClicked(idFeed, mContext, false, (long) ITEMS_WITHOUT_FOLDER.getValue());
                    skipFireEvent = true;
				}

				if(!skipFireEvent)
					fireListTextClicked(idFeed, mContext, true, ((FolderSubscribtionItem) group).idFolder);
			}
		});


        viewHolder.txt_UnreadCount.setText("");
        boolean skipGetUnread = false;
        if(group.idFolder != null && group.idFolder == ITEMS_WITHOUT_FOLDER.getValue()) {
        //if(group instanceof ConcreteFeedItem) {

            String unreadCount = unreadCountFeeds.get((int) group.id_database);
            if(unreadCount != null)
                viewHolder.txt_UnreadCount.setText(unreadCount);
            /*
            else {
                //SetUnreadCountForFeed(viewHolder.txt_UnreadCount, String.valueOf(group.id_database), true);
                //Log.d(TAG, "Fetching unread count manually... " + group.headerFolder);
            }
             */

            skipGetUnread = true;
        }

        if(!skipGetUnread) {
            String unreadCount = unreadCountFolders.get((int) group.id_database);
            if(unreadCount != null)
                viewHolder.txt_UnreadCount.setText(unreadCount);
            /*
            else {
                //viewHolder.txt_UnreadCount.setText("-1");
                SetUnreadCountForFolder(viewHolder.txt_UnreadCount, String.valueOf(group.id_database));
                //Log.d(TAG, "Fetching unread count manually... " + group.headerFolder);
            }
            */
        }



        //viewHolder.txt_UnreadCount.setText(group.unreadCount);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	viewHolder.imgView.setRotation(0);


        if(group.idFolder != null)
        {
	        if(group.idFolder == ITEMS_WITHOUT_FOLDER.getValue())
	        {
                ConcreteFeedItem concreteFeedItem = ((ConcreteFeedItem) group);
                loadFavIconForFeed(concreteFeedItem.favIcon, viewHolder.imgView);
	        }
        } else {
        	if(group.id_database == ALL_STARRED_ITEMS.getValue()) {
        		viewHolder.imgView.setVisibility(View.VISIBLE);
        		//viewHolder.imgView.setImageResource(R.drawable.btn_rating_star_off_normal_holo_light);
                viewHolder.imgView.setImageDrawable(getBtn_rating_star_off_normal_holo_light(mContext));
        	} else if (getChildrenCount( groupPosition ) == 0 ) {
	        	viewHolder.imgView.setVisibility(View.INVISIBLE);
	        } else {
	        	viewHolder.imgView.setVisibility(View.VISIBLE);
	        	//viewHolder.imgView.setImageResource(R.drawable.ic_find_next_holo);
                viewHolder.imgView.setImageDrawable(getic_find_next_holo(mContext));

	        	if(isExpanded) {
	        		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
	        			viewHolder.imgView.setRotation(-90);
	        		else
                        viewHolder.imgView.setImageDrawable(getic_find_previous_holo(mContext));
	        			//viewHolder.imgView.setImageResource(R.drawable.ic_find_previous_holo);
	        	}
	        }
        }

        return convertView;
	}

    private void loadFavIconForFeed(String favIconUrl, ImageView imgView) {

        File cacheFile = ImageHandler.getFullPathOfCacheFileSafe(favIconUrl, favIconPath);
        if(cacheFile != null && cacheFile.exists()) {
            Picasso.with(mContext)
                    .load(cacheFile)
                    .placeholder(FavIconHandler.getResourceIdForRightDefaultFeedIcon(mContext))
                    .into(imgView, null);
        } else {
            Picasso.with(mContext)
                    .load(favIconUrl)
                    .placeholder(FavIconHandler.getResourceIdForRightDefaultFeedIcon(mContext))
                    .into(imgView, null);
        }
    }



    Drawable ic_find_next_holo;
    Drawable ic_find_previous_holo;
    Drawable btn_rating_star_off_normal_holo_light;

    private Drawable getBtn_rating_star_off_normal_holo_light(Context context) {
        if(btn_rating_star_off_normal_holo_light == null)
            btn_rating_star_off_normal_holo_light = context.getResources().getDrawable(R.drawable.btn_rating_star_off_normal_holo_light);
        return btn_rating_star_off_normal_holo_light;
    }

    private Drawable getic_find_next_holo(Context context) {
        if(ic_find_next_holo == null) {
            if(ThemeChooser.isDarkTheme(mContext))
                ic_find_next_holo = context.getResources().getDrawable(R.drawable.ic_find_next_holo_dark);
            else
                ic_find_next_holo = context.getResources().getDrawable(R.drawable.ic_find_next_holo_light);
        }
        return ic_find_next_holo;
    }

    private Drawable getic_find_previous_holo(Context context) {
        if(ic_find_previous_holo == null) {
            if(ThemeChooser.isDarkTheme(mContext))
                ic_find_previous_holo = context.getResources().getDrawable(R.drawable.ic_find_previous_holo_dark);
            else
                ic_find_previous_holo = context.getResources().getDrawable(R.drawable.ic_find_previous_holo_light);
        }
        return ic_find_previous_holo;
    }



    /*
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void SetUnreadCountForFeed(TextView textView, String idDatabase, boolean execludeStarredItems)
	{
		IGetTextForTextViewAsyncTask iGetter = new UnreadFeedCount(mContext, idDatabase, execludeStarredItems);
        FillTextForTextViewHelper.FillTextForTextView(textView, iGetter, !mIsTwoPane);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void SetUnreadCountForFolder(TextView textView, String idDatabase)
	{
		IGetTextForTextViewAsyncTask iGetter = new UnreadFolderCount(mContext, idDatabase);
        FillTextForTextViewHelper.FillTextForTextView(textView, iGetter, !mIsTwoPane);
	}
    */




	static class GroupHolder
	{
        @InjectView(R.id.summary) TextView txt_Summary;
        @InjectView(R.id.tV_feedsCount) TextView txt_UnreadCount;
        @InjectView(R.id.img_View_expandable_indicator) ImageView imgView;

        public GroupHolder(View view, Context mContext) {
            ButterKnife.inject(this, view);

            txt_Summary.setClickable(true);

            FontHelper fHelper = new FontHelper(mContext);
            fHelper.setFontForAllChildren(view, fHelper.getFont());
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


    ArrayList<AbstractItem> mCategoriesArrayListAsync;
    SparseArray<SparseArray<ConcreteFeedItem>> mItemsArrayListAsync;
    public void ReloadAdapter()
    {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        showOnlyUnread = mPrefs.getBoolean(SettingsActivity.CB_SHOWONLYUNREAD_STRING, false);

        mCategoriesArrayListAsync = new ArrayList<AbstractItem>();
        mCategoriesArrayListAsync.add(new FolderSubscribtionItem(mContext.getString(R.string.allUnreadFeeds), null, ALL_UNREAD_ITEMS.getValue()));
        mCategoriesArrayListAsync.add(new FolderSubscribtionItem(mContext.getString(R.string.starredFeeds), null, ALL_STARRED_ITEMS.getValue()));

        List<Folder> folderList;
        if(showOnlyUnread)
            folderList = dbConn.getListOfFoldersWithUnreadItems();
        else
            folderList = dbConn.getListOfFolders();


        for(Folder folder : folderList)
            mCategoriesArrayListAsync.add(new FolderSubscribtionItem(folder.getLabel(), null, folder.getId()));

        for(Feed feed : dbConn.getListOfFeedsWithoutFolders(showOnlyUnread))
            mCategoriesArrayListAsync.add(new ConcreteFeedItem(feed.getFeedTitle(), (long) ITEMS_WITHOUT_FOLDER.getValue(), feed.getId(), feed.getFaviconUrl(), feed.getId()));

        //AddEverythingInCursorToSubscriptions(dbConn.getAllTopSubscriptionsWithUnreadFeeds());
        mItemsArrayListAsync = new SparseArray<SparseArray<ConcreteFeedItem>>();

        for(int groupPosition = 0; groupPosition < mCategoriesArrayListAsync.size(); groupPosition++) {
            //int parent_id = (int)getGroupId(groupPosition);
            int parent_id = (int) mCategoriesArrayListAsync.get(groupPosition).id_database;
            mItemsArrayListAsync.append(parent_id, new SparseArray<ConcreteFeedItem>());

            int childPosTemp = 0;

            List<Feed> feedItemList = null;

            if(parent_id == ALL_UNREAD_ITEMS.getValue()) {
                feedItemList = dbConn.getAllFeedsWithUnreadRssItems();
            } else if(parent_id == ALL_STARRED_ITEMS.getValue()) {
                feedItemList = dbConn.getAllFeedsWithStarredRssItems();
            } else {
                for(Folder folder : folderList) {//Find the current selected folder
                    if (folder.getId() == parent_id) {//Current item
                        feedItemList = dbConn.getAllFeedsWithUnreadRssItemsForFolder(folder.getId(), showOnlyUnread);// folder.getFeedList();
                        break;
                    }
                }
            }

            if(feedItemList != null) {
                for (Feed feed : feedItemList) {
                    ConcreteFeedItem newItem = new ConcreteFeedItem(feed.getFeedTitle(), (long) parent_id, feed.getId(), feed.getFaviconUrl(), feed.getId());
                    mItemsArrayListAsync.get(parent_id).put(childPosTemp, newItem);
                    childPosTemp++;
                }
            }
        }
    }


    SparseArray<String> starredCountFeeds;
    SparseArray<String> unreadCountFolders;
    SparseArray<String> unreadCountFeeds;
    SparseArray<String> urlsToFavIcons;

    @Deprecated
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
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
            unreadCountFoldersTemp = dbConn.getUnreadItemCountForFolder();
            unreadCountFeedsTemp = dbConn.getUnreadItemCountForFeed();
            starredCountFeedsTemp = dbConn.getStarredItemCountForFeed();

            /*
            SparseArray<Integer>[] values = dbConn.getUnreadItemCount();
            unreadCountFoldersTemp = values[0];
            unreadCountFeedsTemp = values[1];
            */
            urlsToFavIconsTemp = dbConn.getUrlsToFavIcons();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            notifyCountDataSetChanged(unreadCountFoldersTemp, unreadCountFeedsTemp, urlsToFavIconsTemp, starredCountFeedsTemp);
            super.onPostExecute(aVoid);
        }
    }

    public void ReloadAdapterAsync(View progressBar) {
        new ReloadAdapterAsyncTask(progressBar).execute((Void) null);
    }

    private class ReloadAdapterAsyncTask extends AsyncTask<Void, Void, Void> {

        View progressBar;

        public ReloadAdapterAsyncTask(View progressBar) {
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ReloadAdapter();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            notifyReloadAdapterDataChanged();

            progressBar.setVisibility(View.GONE);

            super.onPostExecute(aVoid);
        }
    }


    public void notifyReloadAdapterDataChanged()
    {
        mCategoriesArrayList = mCategoriesArrayListAsync;
        mCategoriesArrayListAsync = null;

        mItemsArrayList = mItemsArrayListAsync;
        mItemsArrayListAsync = null;

        notifyDataSetChanged();

        NotifyDataSetChangedAsync();
    }

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
	protected void fireListTextClicked(long idFeed, Context context, boolean isFolder, Long optional_folder_id)
	{
		if(eListTextClickHandler != null)
			eListTextClickHandler.onTextClicked(idFeed, context, isFolder, optional_folder_id);
	}
}