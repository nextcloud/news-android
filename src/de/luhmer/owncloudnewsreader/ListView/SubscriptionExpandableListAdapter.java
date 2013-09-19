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

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.TextView;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.async_tasks.FillTextForTextViewAsyncTask;
import de.luhmer.owncloudnewsreader.async_tasks.IGetTextForTextViewAsyncTask;
import de.luhmer.owncloudnewsreader.data.AbstractItem;
import de.luhmer.owncloudnewsreader.data.ConcreteFeedItem;
import de.luhmer.owncloudnewsreader.data.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.BitmapDrawableLruCache;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.FontHelper;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;

public class SubscriptionExpandableListAdapter extends BaseExpandableListAdapter {

	BitmapDrawableLruCache favIconCache = null;
	
	private Context mContext;
    private DatabaseConnection dbConn;

    ExpListTextClicked eListTextClickHandler;
    
    private ArrayList<FolderSubscribtionItem> mCategoriesArrayList;
    private SparseArray<SparseArray<ConcreteFeedItem>> mItemsArrayList;
	private boolean showOnlyUnread = false;
	
	public static final String ALL_UNREAD_ITEMS = "-10";
	public static final String ALL_STARRED_ITEMS = "-11";
	public static final String ALL_ITEMS = "-12";
	public static final String ITEMS_WITHOUT_FOLDER = "-22";
	//private static final String TAG = "SubscriptionExpandableListAdapter";
	

    public SubscriptionExpandableListAdapter(Context mContext, DatabaseConnection dbConn)
    {
    	this.mContext = mContext;
    	this.dbConn = dbConn;

    	int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    	//Use 1/8 of the available memory for this memory cache
    	int cachSize = maxMemory / 8;
    	favIconCache = new BitmapDrawableLruCache(cachSize);
    	
    	ReloadAdapter();
    }
    
    private void AddEverythingInCursorFolderToSubscriptions(Cursor itemsCursor)
    {
    	while (itemsCursor.moveToNext()) {
    		String header = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.FOLDER_LABEL));
            //String id_folder = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.FOLDER_LABEL_ID));
    		long id = itemsCursor.getLong(0);
    		mCategoriesArrayList.add(new FolderSubscribtionItem(header, null, id));
    	}
        itemsCursor.close();
    }
    
    private void AddEverythingInCursorFeedsToSubscriptions(Cursor itemsCursor)
    {
    	while (itemsCursor.moveToNext()) {
    		String header = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_HEADERTEXT));
            //String id_folder = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_ID));
    		long id = itemsCursor.getLong(0);
    		mCategoriesArrayList.add(new FolderSubscribtionItem(header, ITEMS_WITHOUT_FOLDER, id));
    	}
        itemsCursor.close();
    }
    
    /*
    private String getUnreadTextFolder(String id_db_folder, boolean onlyUnread)
    {
    	//return dbConn.getCountFeedsForFolder(id_db_folder, onlyUnread) + "/" + dbConn.getCountFeedsForFolder(id_db_folder, !onlyUnread);
    	return convertCountIntToString(dbConn.getCountFeedsForFolder(id_db_folder, onlyUnread));
    }
    
    private String getUnreadTextItems(String id_db_item, boolean onlyUnread, boolean execludeStarredItems)
    {
    	//return dbConn.getCountItemsForSubscription(id_db_item, onlyUnread, execludeStarredItems) + "/" + dbConn.getCountItemsForSubscription(id_db_item, !onlyUnread, execludeStarredItems);
    	return convertCountIntToString(dbConn.getCountItemsForSubscription(id_db_item, onlyUnread, execludeStarredItems));
    }*/
    
    /*
    private String convertCountIntToString(int value)
    {
    	if(value > 0)
    		return String.valueOf(value);
    	else
    		return "";
    	
    }*/
	
	@SuppressWarnings("deprecation")
	@Override
	public Object getChild(int groupPosition, int childPosition) {		
		int parent_id = (int)getGroupId(groupPosition);
        //Check if we are not in our current group now, or the current cached items are wrong - MUST BE RECACHED
        //if(mItemsArrayList.isEmpty() /*|| ((ConcreteSubscribtionItem)mItemsArrayList.get(0)).parent_id != parent_id */){
		if(mItemsArrayList.indexOfKey(groupPosition) < 0 /*|| (mItemsArrayList.get(groupPosition).size() <= childPosition)*/ /*|| ((ConcreteSubscribtionItem)mItemsArrayList.get(0)).parent_id != parent_id */){
			mItemsArrayList.append(groupPosition, new SparseArray<ConcreteFeedItem>());
            Cursor itemsCursor = dbConn.getAllSubscriptionForFolder(String.valueOf(parent_id), showOnlyUnread);
            itemsCursor.requery();
            //mItemsArrayList.clear();  
            int childPosTemp = childPosition;
            if (itemsCursor.moveToFirst())
                do {
                    long id_database = itemsCursor.getLong(0);
                    String name = itemsCursor.getString(1);
                    String subscription_id = itemsCursor.getString(2);
                    String urlFavicon = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_FAVICON_URL));
                    ConcreteFeedItem newItem = new ConcreteFeedItem(name, String.valueOf(parent_id), subscription_id, urlFavicon, id_database);
                    mItemsArrayList.get(groupPosition).put(childPosTemp, newItem);
                    childPosTemp++;
                } while (itemsCursor.moveToNext());
            itemsCursor.close();            
        }                
        return mItemsArrayList.get(groupPosition).get(childPosition);		
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return ((ConcreteFeedItem)(getChild(groupPosition, childPosition))).id_database;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		LinearLayout view;		
		ChildHolder viewHolder;
		ConcreteFeedItem item = (ConcreteFeedItem)getChild(groupPosition, childPosition);

		
		if (convertView == null) {   
			view = new LinearLayout(mContext);
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(inflater);
            vi.inflate(R.layout.subscription_list_sub_item, view, true);  
            if(item != null)
            	view.setTag(item.id_database);
            
            FontHelper fHelper = new FontHelper(mContext);
            fHelper.setFontForAllChildren(view, fHelper.getFont());
            
            viewHolder = new ChildHolder();
            viewHolder.tV_HeaderText = (TextView) view.findViewById(R.id.summary);
            viewHolder.tV_UnreadCount = (TextView) view.findViewById(R.id.tv_unreadCount); 
            viewHolder.imgView_FavIcon = (ImageView) view.findViewById(R.id.iVFavicon);
            
            view.setTag(viewHolder);            
            convertView = view;
            
        } else {
            view = (LinearLayout) convertView;
        	viewHolder = (ChildHolder) convertView.getTag();
        }
		
        if(item != null)
        {    
	        String headerText = (item.header != null) ? item.header : "";        		
	        viewHolder.tV_HeaderText.setText(headerText);
	
	        boolean execludeStarredItems = (item.folder_id.equals(ALL_STARRED_ITEMS)) ? false : true;	        
	        SetUnreadCountForFeed(viewHolder.tV_UnreadCount, String.valueOf(item.id_database), execludeStarredItems);	        
	        GetFavIconForFeed(item.favIcon, viewHolder.imgView_FavIcon, String.valueOf(item.id_database));
        }
        else
        {        	
	        viewHolder.tV_HeaderText.setText(mContext.getString(R.string.login_dialog_text_something_went_wrong));
	        viewHolder.tV_UnreadCount.setText("0");	        
	        viewHolder.imgView_FavIcon.setImageDrawable(null);
        }

        return view;        
	}

	static class ChildHolder {
	    public TextView tV_HeaderText;
	    public TextView tV_UnreadCount;
	    public ImageView imgView_FavIcon;
	  }
	
	@Override
	public int getChildrenCount(int groupPosition) {
		int count;
        if(mItemsArrayList.indexOfKey(groupPosition) < 0){
            Cursor itemsCursor = dbConn.getAllSubscriptionForFolder(String.valueOf(getGroupId(groupPosition)), showOnlyUnread);        	
            count = itemsCursor.getCount();
            itemsCursor.close();
        }
        else
            count = mItemsArrayList.get(groupPosition).size();
        return count;
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("CutPasteId")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		LinearLayout view;
		//View view;
		GroupHolder viewHolder;
		
        final FolderSubscribtionItem group = (FolderSubscribtionItem)getGroup(groupPosition);        
                 
        if (convertView == null) {   
            view = new LinearLayout(mContext);
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(inflater);
            vi.inflate(R.layout.subscription_list_item, view, true);
            
            FontHelper fHelper = new FontHelper(mContext);
            fHelper.setFontForAllChildren(view, fHelper.getFont());
            
            viewHolder = new GroupHolder();
            viewHolder.imgView = (ImageView) view.findViewById(R.id.img_View_expandable_indicator);
            viewHolder.txt_Summary = (TextView) view.findViewById(R.id.summary);
            viewHolder.txt_UnreadCount = (TextView) view.findViewById(R.id.tV_feedsCount);
                         
            viewHolder.txt_Summary.setClickable(true);
            
            view.setTag(viewHolder);
            //view = convertView;
            convertView = view;
            
        } else {
            view = (LinearLayout) convertView;
        	viewHolder = (GroupHolder) convertView.getTag();
        }

        
        viewHolder.txt_Summary.setText(group.headerFolder);
        viewHolder.txt_Summary.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//v.setPressed(true);
				//v.setSelected(true);
				//v.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_blue_light));
				//NewsReaderActivity.StartSubscriptionActivity((View) v.getParent().getParent(), mContext);					
				//v.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
				//group.id;
				//ExpandableListView exListView = ((ExpandableListView) v.getParent().getParent().getParent());
				//exListView.setEmptyView(tView);
				//exListView.getSelectedPosition();
				
				//String val = String.valueOf(((View) v.getParent().getParent()).getTag());
				String val = String.valueOf(group.id_database /*((LinearLayout) v.getParent()).getChildAt(1).getTag()*/);
				boolean skipFireEvent = false;
				if(group.idFolder != null)
				{
					if(group.idFolder.equals(ITEMS_WITHOUT_FOLDER))
					{
						fireListTextClicked(val, mContext, false, group.idFolder);
						skipFireEvent = true;
					}
				}
				
				if(!skipFireEvent)
					fireListTextClicked(val, mContext, true, group.idFolder);
			}
		});
        /*
        if(viewHolder.txt_UnreadCount.getText() == "")
        {
	        String unreadCountText = null;
	        boolean skipGetUnread = false;
	        if(group.idFolder != null)
	        {
	        	if(group.idFolder.equals(ITEMS_WITHOUT_FOLDER))
	        	{	
	            	unreadCountText = dbConn.getCountItemsForSubscription(String.valueOf(group.id_database), true, true) + "/" + dbConn.getCountItemsForSubscription(String.valueOf(group.id_database), false, true);
	            	skipGetUnread = true;
	        	}
	        }
	        if(!skipGetUnread)
	        	unreadCountText = dbConn.getCountFeedsForFolder(String.valueOf(group.id_database), true) + "/" + dbConn.getCountFeedsForFolder(String.valueOf(group.id_database), false);        
	                
	        viewHolder.txt_UnreadCount.setText(unreadCountText);
        }*/
        
        boolean skipGetUnread = false;
        if(group.idFolder != null)
        {
        	if(group.idFolder.equals(ITEMS_WITHOUT_FOLDER))
        	{	
            	//unreadCountText = getUnreadTextItems(String.valueOf(group.id_database), true, true);
            	SetUnreadCountForFeed(viewHolder.txt_UnreadCount, String.valueOf(group.id_database), true);        		
            	skipGetUnread = true;
        	}
        }
        if(!skipGetUnread)
        	SetUnreadCountForFolder(viewHolder.txt_UnreadCount, String.valueOf(group.id_database), true);
        	//unreadCountText = getUnreadTextFolder(String.valueOf(group.id_database), true);
      
                
        viewHolder.txt_UnreadCount.setText("");      
        
        
        //viewHolder.txt_UnreadCount.setText(group.unreadCount);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        	viewHolder.imgView.setRotation(0);
        
        if(group.idFolder != null)
        {
	        if(group.idFolder.equals(ITEMS_WITHOUT_FOLDER))
	        {
	        	Cursor cursor = dbConn.getFeedByDbID(String.valueOf(group.id_database));
	        	if(cursor != null)
	        	{
	        		if(cursor.getCount() > 0)
	        		{
	        			viewHolder.imgView.setImageDrawable(null);
	        			
			        	cursor.moveToFirst();
			        	String favIconURL = cursor.getString(cursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_FAVICON_URL));			        	
			        	GetFavIconForFeed(favIconURL, viewHolder.imgView, String.valueOf(group.id_database));
	        		}
	        	}
	        	cursor.close();
	        }
        }
        else
        {
        	if(String.valueOf(group.id_database).equals(ALL_STARRED_ITEMS))
        	{
        		viewHolder.imgView.setVisibility( View.VISIBLE );
        		//viewHolder.imgView.setImageResource(R.drawable.star);
        		viewHolder.imgView.setImageResource(R.drawable.btn_rating_star_off_normal_holo_light);
        	} else if (getChildrenCount( groupPosition ) == 0 ) {
	        	viewHolder.imgView.setVisibility( View.INVISIBLE );
	        	//viewHolder.imgView.setImageDrawable(null);
	        } 
	        else {
	        	viewHolder.imgView.setVisibility( View.VISIBLE );
	        	//if(ThemeChooser.isDarkTheme(mContext))
	        	//	viewHolder.imgView.setImageResource(isExpanded ? R.drawable.ic_find_previous_holo_dark : R.drawable.ic_find_next_holo_dark);
	        	//else
	        		//viewHolder.imgView.setImageResource(isExpanded ? R.drawable.ic_find_previous_holo_light : R.drawable.ic_find_next_holo_light);
	        	viewHolder.imgView.setImageResource(R.drawable.ic_find_next_holo_dark);
	        	
	        	if(isExpanded)
	        	{
	        		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
	        			viewHolder.imgView.setRotation(-90);
	        		else
	        			viewHolder.imgView.setImageResource(R.drawable.ic_find_previous_holo_dark);
	        	}
	        }
        }
        
        //view.setTag(group.id_database);
        
        return view;
	}
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)//TODO check this
	private void SetUnreadCountForFeed(TextView textView, String idDatabase, boolean execludeStarredItems)
	{
		IGetTextForTextViewAsyncTask iGetter = new UnreadFeedCount(mContext, idDatabase, execludeStarredItems);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			// Execute in parallel
			new FillTextForTextViewAsyncTask(textView, iGetter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ((Void) null));
		else
			new FillTextForTextViewAsyncTask(textView, iGetter).execute((Void) null);
	}
		
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void SetUnreadCountForFolder(TextView textView, String idDatabase, boolean execludeStarredItems)
	{
		IGetTextForTextViewAsyncTask iGetter = new UnreadFolderCount(mContext, idDatabase);
				
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			// Execute in parallel
			new FillTextForTextViewAsyncTask(textView, iGetter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ((Void) null));
		else
			new FillTextForTextViewAsyncTask(textView, iGetter).execute((Void) null);
			
	}
	
	private void GetFavIconForFeed(String favIconURL, ImageView imgView, String feedID)
	{
		try
		{
			if(favIconURL != null)
	    	{
				if(favIconURL.trim().length() > 0)
				{	
		    		new FavIconHandler().GetImageAsync(imgView, favIconURL, mContext, feedID, favIconCache);
		    		//new FavIconHandler.GetImageFromWebAsyncTask(favIconURL, mContext, imgView).execute((Void)null);
				}
				else
					imgView.setImageResource(FavIconHandler.getResourceIdForRightDefaultFeedIcon(mContext));
	    	}
			else
				imgView.setImageResource(FavIconHandler.getResourceIdForRightDefaultFeedIcon(mContext));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	static class GroupHolder
	{
		TextView txt_Summary;
		TextView txt_UnreadCount;
		ImageView imgView;
	}
	

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	/*
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		//used to make the notifyDataSetChanged() method work
		super.registerDataSetObserver(observer);
	}*/

    public void ReloadAdapter()
    {
    	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    	showOnlyUnread = mPrefs.getBoolean(SettingsActivity.CB_SHOWONLYUNREAD_STRING, false);
    	
        mCategoriesArrayList = new ArrayList<FolderSubscribtionItem>();
        mCategoriesArrayList.add(new FolderSubscribtionItem(mContext.getString(R.string.allUnreadFeeds), null, Long.valueOf(ALL_UNREAD_ITEMS)));
        mCategoriesArrayList.add(new FolderSubscribtionItem(mContext.getString(R.string.starredFeeds), null, Long.valueOf(ALL_STARRED_ITEMS)));
        
        //mCategoriesArrayList.add(new FolderSubscribtionItem(mContext.getString(R.string.starredFeeds), -11, null, null));

        AddEverythingInCursorFolderToSubscriptions(dbConn.getAllTopSubscriptions(showOnlyUnread));
        AddEverythingInCursorFeedsToSubscriptions(dbConn.getAllTopSubscriptionsWithoutFolder(showOnlyUnread));

        //AddEverythingInCursorToSubscriptions(dbConn.getAllTopSubscriptionsWithUnreadFeeds());
        mItemsArrayList = new SparseArray<SparseArray<ConcreteFeedItem>>();

        this.notifyDataSetChanged();
    }

	
	public void setHandlerListener(ExpListTextClicked listener)
	{
		eListTextClickHandler = listener;
	}
	protected void fireListTextClicked(String idSubscription, Context context, boolean isFolder, String optional_folder_id)
	{
		if(eListTextClickHandler != null)
			eListTextClickHandler.onTextClicked(idSubscription, context, isFolder, optional_folder_id);
	}
}