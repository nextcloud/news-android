package de.luhmer.owncloudnewsreader.ListView;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.FontHelper;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;

public class SubscriptionExpandableListAdapter extends BaseExpandableListAdapter {

	private Context mContext;
    private DatabaseConnection dbConn;

    ExpListTextClicked eListTextClickHandler;
    
    private ArrayList<FolderSubscribtionItem> mCategoriesArrayList;
    private SparseArray<SparseArray<ConcreteFeedItem>> mItemsArrayList;
	private boolean showOnlyUnread = false;

	public static final String ALL_UNREAD_ITEMS = "-10";
	public static final String ALL_STARRED_ITEMS = "-11";
	public static final String ITEMS_WITHOUT_FOLDER = "-22";
	

    public SubscriptionExpandableListAdapter(Context mContext, DatabaseConnection dbConn)
    {
    	this.mContext = mContext;
    	this.dbConn = dbConn;

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
        } else {
            view = (LinearLayout) convertView;
        }

        if(item != null)
        {
	        TextView textTV = (TextView) view.findViewById(R.id.summary);
	        String headerText = (item.header != null) ? item.header : "";        		
	        textTV.setText(headerText);
	
	        boolean execludeStarredItems = (item.folder_id.equals(ALL_STARRED_ITEMS)) ? false : true;
	        
	        TextView tV_UnreadCount = (TextView) view.findViewById(R.id.tv_unreadCount);
	        if(tV_UnreadCount.getTag() == null)//TODO Work on this here... 
	        	tV_UnreadCount.setText("");
	        SetUnreadCountForFeed(tV_UnreadCount, String.valueOf(item.id_database), execludeStarredItems);	
	        
	        ImageView imgView = (ImageView) view.findViewById(R.id.iVFavicon);
	        GetFavIconForFeed(item.favIcon, imgView);
        }
        else
        {
        	TextView textTV = (TextView) view.findViewById(R.id.summary);
	        textTV.setText(mContext.getString(R.string.login_dialog_text_something_went_wrong));
	        
	        TextView tV_UnreadCount = (TextView) view.findViewById(R.id.tv_unreadCount);	        
	        tV_UnreadCount.setText("0");
	        
	        ImageView imgView = (ImageView) view.findViewById(R.id.iVFavicon);
	        imgView.setImageDrawable(null);
        }

        return view;        
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
            
            ImageView indicatorImage = (ImageView) view.findViewById(R.id.img_View_expandable_indicator);
            TextView txt_Summary = (TextView) view.findViewById(R.id.summary);
            TextView textFeedCount = (TextView) view.findViewById(R.id.tV_feedsCount);
            
            viewHolder = new GroupHolder();
            viewHolder.imgView = indicatorImage;
            viewHolder.txt_Summary = txt_Summary;
            viewHolder.txt_UnreadCount = textFeedCount;
                         
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
        
        if(group.idFolder != null)
        {
	        if(group.idFolder.equals(ITEMS_WITHOUT_FOLDER))
	        {
	        	Cursor cursor = dbConn.getFeedByDbID(String.valueOf(group.id_database));
	        	if(cursor != null)
	        	{
	        		if(cursor.getCount() > 0)
	        		{
			        	cursor.moveToFirst();
			        	String favIconURL = cursor.getString(cursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_FAVICON_URL));			        	
			        	GetFavIconForFeed(favIconURL, viewHolder.imgView);
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
        		viewHolder.imgView.setImageResource(R.drawable.star);
        	} else if (getChildrenCount( groupPosition ) == 0 ) {
	        	viewHolder.imgView.setVisibility( View.INVISIBLE );
	        	//viewHolder.imgView.setImageDrawable(null);
	        } 
	        else {
	        	viewHolder.imgView.setVisibility( View.VISIBLE );
	        	if(ThemeChooser.isDarkTheme(mContext))
	        		viewHolder.imgView.setImageResource( isExpanded ? R.drawable.ic_find_previous_holo_dark : R.drawable.ic_find_next_holo_dark);
	        	else
	        		viewHolder.imgView.setImageResource( isExpanded ? R.drawable.ic_find_previous_holo_light : R.drawable.ic_find_next_holo_light);
	        }
	        
        }
        
        //view.setTag(group.id_database);
        
        return view;
	}
	
	
	private void SetUnreadCountForFeed(TextView textView, String idDatabase, boolean execludeStarredItems)
	{
		IGetTextForTextViewAsyncTask iGetter = new UnreadFeedCount(mContext, idDatabase, execludeStarredItems);
		new FillTextForTextViewAsyncTask(textView, iGetter).execute((Void) null);
	}
	
	private void SetUnreadCountForFolder(TextView textView, String idDatabase, boolean execludeStarredItems)
	{
		IGetTextForTextViewAsyncTask iGetter = new UnreadFolderCount(mContext, idDatabase);
		new FillTextForTextViewAsyncTask(textView, iGetter).execute((Void) null);
	}
	
	private void GetFavIconForFeed(String favIconURL, ImageView imgView)
	{
		try
		{
			if(favIconURL != null)
	    	{
				
				if(favIconURL.trim().length() > 0)
				{	
		    		FavIconHandler.GetImageAsync(imgView, favIconURL, mContext);
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