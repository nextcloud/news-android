package de.luhmer.owncloudnewsreader.ListView;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
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
import de.luhmer.owncloudnewsreader.data.AbstractItem;
import de.luhmer.owncloudnewsreader.data.ConcreteSubscribtionItem;
import de.luhmer.owncloudnewsreader.data.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.BitmapDownloaderTask;
import de.luhmer.owncloudnewsreader.helper.DownloadImagesFromWeb;
import de.luhmer.owncloudnewsreader.interfaces.ExpListTextClicked;

public class SubscriptionExpandableListAdapter extends BaseExpandableListAdapter {

	private Context mContext;
    private DatabaseConnection dbConn;

    ExpListTextClicked eListTextClickHandler;
    
    private ArrayList<FolderSubscribtionItem> mCategoriesArrayList;
    private SparseArray<SparseArray<ConcreteSubscribtionItem>> mItemsArrayList;
	
    public SubscriptionExpandableListAdapter(Context mContext, DatabaseConnection dbConn)
    {
    	this.mContext = mContext;
    	this.dbConn = dbConn;

    	ReloadAdapter();
    }
    
    private void AddEverythingInCursorToSubscriptions(Cursor itemsCursor)
    {
    	while (itemsCursor.moveToNext()) {
    		String header = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.FOLDER_LABEL));
            String id_folder = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.FOLDER_LABEL_ID));
    		long id = itemsCursor.getLong(0);
    		mCategoriesArrayList.add(new FolderSubscribtionItem(header, id_folder, id));
    	}
        itemsCursor.close();
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public Object getChild(int groupPosition, int childPosition) {		
		int parent_id = (int)getGroupId(groupPosition);
        //Check if we are not in our current group now, or the current cached items are wrong - MUST BE RECACHED
        //if(mItemsArrayList.isEmpty() /*|| ((ConcreteSubscribtionItem)mItemsArrayList.get(0)).parent_id != parent_id */){
		if(mItemsArrayList.indexOfKey(groupPosition) < 0 /*|| (mItemsArrayList.get(groupPosition).size() <= childPosition)*/ /*|| ((ConcreteSubscribtionItem)mItemsArrayList.get(0)).parent_id != parent_id */){
			mItemsArrayList.append(groupPosition, new SparseArray<ConcreteSubscribtionItem>());
            Cursor itemsCursor = dbConn.getAllSub_SubscriptionForSubscription(String.valueOf(parent_id));
            itemsCursor.requery();
            //mItemsArrayList.clear();  
            int childPosTemp = childPosition;
            if (itemsCursor.moveToFirst())
                do {
                    long id_database = itemsCursor.getLong(0);
                    String name = itemsCursor.getString(1);
                    String subscription_id = itemsCursor.getString(2);
                    String urlFavicon = itemsCursor.getString(itemsCursor.getColumnIndex(DatabaseConnection.SUBSCRIPTION_FAVICON_URL));
                    ConcreteSubscribtionItem newItem = new ConcreteSubscribtionItem(name, String.valueOf(parent_id), subscription_id, urlFavicon, id_database);
                    mItemsArrayList.get(groupPosition).put(childPosTemp, newItem);
                    childPosTemp++;
                } while (itemsCursor.moveToNext());
            itemsCursor.close();            
        }                
        return mItemsArrayList.get(groupPosition).get(childPosition);		
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return ((ConcreteSubscribtionItem)(getChild(groupPosition, childPosition))).id_database;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		LinearLayout view;
		
        ConcreteSubscribtionItem item = (ConcreteSubscribtionItem)getChild(groupPosition, childPosition);

        if (convertView == null) {
            view = new LinearLayout(mContext);
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(inflater);
            vi.inflate(R.layout.subscription_list_sub_item, view, true);
            view.setTag(item.id_database);
        } else {
            view = (LinearLayout) convertView;
        }

        TextView textTV = (TextView) view.findViewById(R.id.summary);  
        textTV.setText(item.header);

        if(item.favIcon != null)
        {
            ImageView imgView = (ImageView) view.findViewById(R.id.iVFavicon);
            //imgView.setImageDrawable(DownloadImagesFromWeb.LoadImageFromWebOperations(item.favIcon));
            BitmapDownloaderTask task = new BitmapDownloaderTask(imgView);
            task.execute(item.favIcon);
        }


        return view;        
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int count = 0;
        if(mItemsArrayList.indexOfKey(groupPosition) < 0){
        	
            Cursor itemsCursor = dbConn.getAllSub_SubscriptionForSubscription(String.valueOf(getGroupId(groupPosition)));
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

        FolderSubscribtionItem group = (FolderSubscribtionItem)getGroup(groupPosition);
        
        
        if (convertView == null) {
            view = new LinearLayout(mContext);
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(inflater);
            vi.inflate(R.layout.subscription_list_item, view, true);
            
            
            TextView tView = (TextView) view.findViewById(R.id.summary);
            
            tView.setClickable(true);
            
            //tView.setTag(group.id);
            tView.setOnClickListener(new OnClickListener() {
				
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
					
					String val = String.valueOf(((View) v.getParent().getParent()).getTag());
					fireListTextClicked(val, mContext);
					
					//ExpandableListView eListView = null;
					//eListView.getSelectedItem()
				}
			});
            
            //view.setTag(group.id);
        } else {
            view = (LinearLayout) convertView;
        }

        view.setTag(group.id_database);
        
        TextView textTV = (TextView) view.findViewById(R.id.summary);        
        textTV.setText(group.headerFolder);
        
        TextView textFeedCount = (TextView) view.findViewById(R.id.tV_feedsCount);
        String unreadCountText = dbConn.getCountUnreadFeedsForFolder(String.valueOf(group.id_database)) + "/" + dbConn.getCountFeedsForFolder(String.valueOf(group.id_database));
        textFeedCount.setText(unreadCountText);

        return view;
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
        mCategoriesArrayList = new ArrayList<FolderSubscribtionItem>();
        mCategoriesArrayList.add(new FolderSubscribtionItem(mContext.getString(R.string.allUnreadFeeds), null, -10));
        mCategoriesArrayList.add(new FolderSubscribtionItem(mContext.getString(R.string.starredFeeds), null, -11));
        //mCategoriesArrayList.add(new FolderSubscribtionItem(mContext.getString(R.string.starredFeeds), -11, null, null));

        AddEverythingInCursorToSubscriptions(dbConn.getAllTopSubscriptions());
        //AddEverythingInCursorToSubscriptions(dbConn.getAllTopSubscriptionsWithUnreadFeeds());
        mItemsArrayList = new SparseArray<SparseArray<ConcreteSubscribtionItem>>();

        this.notifyDataSetChanged();
    }

	
	public void setHandlerListener(ExpListTextClicked listener)
	{
		eListTextClickHandler = listener;
	}
	protected void fireListTextClicked(String idSubscription, Context context)
	{
		if(eListTextClickHandler != null)
			eListTextClickHandler.onTextClicked(idSubscription, context);
	}
}