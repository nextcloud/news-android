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

package de.luhmer.owncloudnewsreader.reader;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.data.ConcreteFeedItem;
import de.luhmer.owncloudnewsreader.data.RssFile;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;

/**
 * Created by David on 24.05.13.
 */
public class InsertIntoDatabase {
    private static final String TAG = "InsertIntoDatabase";

    public static void InsertFoldersIntoDatabase(List<String[]> tags, DatabaseConnection dbConn)
    {
        //DatabaseConnection dbConn = new DatabaseConnection(activity);

        //List<String[]> tags = (List<String[]>) task_result;
        List<String> tagsAvailable = dbConn.convertCursorToStringArray(dbConn.getAllTopSubscriptions(false), 1);

        //dbConn.getDatabase().beginTransaction();
        try
        {
	        if(tags != null)
	        {
	            List<String> tagsToAdd = new ArrayList<String>();
	            for(String[] t : tags)
	            {
	                String label = t[0];
	                String label_path = t[1];
	                if(!tagsAvailable.contains(label))
	                {
	                    tagsToAdd.add(label);
	                    dbConn.insertNewFolder(label, label_path);
	                }
	            }

	            List<String> tagsToRemove = new ArrayList<String>();


	            List<String> newLabelList = new ArrayList<String>();
	            for(String[] subTag : tags)
	                newLabelList.add(subTag[0]);

	            for(String tag : tagsAvailable)
	            {
	                if(!newLabelList.contains(tag))
	                {
	                    tagsToRemove.add(tag);
	                    int result = dbConn.removeFolderByFolderLabel(tag);
	                    Log.d(TAG, "Result delete: " + result);
	                }
	            }

	            Log.d("ADD", ""+ tagsToAdd.size());
	            Log.d("REMOVE", ""+ tagsToRemove.size());
	        }
	        //dbConn.getDatabase().setTransactionSuccessful();
        } finally {
            //dbConn.getDatabase().endTransaction();
        }


        //dbConn.closeDatabase();
    }

    public static void InsertSubscriptionsIntoDatabase(ArrayList<ConcreteFeedItem> tags, DatabaseConnection dbConn)
    {
        //DatabaseConnection dbConn = new DatabaseConnection(activity);

        List<String> tagsAvailable = dbConn.convertCursorToStringArray(dbConn.getAllSubSubscriptions(), 1);

        //dbConn.getDatabase().beginTransaction();
        try
        {
	        if(tags != null)
	        {
	            for(ConcreteFeedItem tag : tags)
	            {
	                if(!tagsAvailable.contains(tag.header))
	                {
	                    String folderID_db = dbConn.getIdOfFolderByLabelPath(String.valueOf(tag.idFolder));
	                    dbConn.insertNewFeed(tag.header, folderID_db, tag.subscription_id, tag.favIcon);
	                } else {
	                	String folderID_db = dbConn.getIdOfFolderByLabelPath(String.valueOf(tag.idFolder));
	                    int result = dbConn.updateFeed(tag.header, folderID_db, tag.subscription_id, tag.favIcon);
	                    Log.d(TAG, "Affected Rows: " + result);
	                }
	            }

	            //tags.clear();

	            for(String tag : tagsAvailable)
	            {
	                boolean found = false;
	                for(int i = 0; i < tags.size(); i++)
	                {
	                    if(tags.get(i).header.contains(tag))
	                    {
	                        found = true;
	                        break;
	                    }
	                }
	                if(!found)
	                {
	                	int result = dbConn.removeTopSubscriptionItemByTag(tag);
	                    Log.d(TAG, "Remove Subscription: " + result);
	                }
	            }

	            //lvAdapter.notifyDataSetChanged();

	            //lvAdapter = new SubscriptionExpandableListAdapter(getActivity(), dbConn);
	        }
	        //dbConn.getDatabase().setTransactionSuccessful();
	    } finally {
	        //dbConn.getDatabase().endTransaction();
	    }
        //dbConn.closeDatabase();
    }


    public static void InsertFeedItemsIntoDatabase(List<RssFile> files, Activity activity)
    {
        DatabaseConnection dbConn = new DatabaseConnection(activity);

        dbConn.getDatabase().beginTransaction();
        try
        {
	        if(files != null)
	        {
	            for (RssFile rssFile : files)
	            	InsertSingleFeedItemIntoDatabase(rssFile, dbConn);
	        }
	        dbConn.getDatabase().setTransactionSuccessful();
	    } finally {
	        dbConn.getDatabase().endTransaction();
	    }

        dbConn.closeDatabase();
    }

    public static boolean InsertSingleFeedItemIntoDatabase(RssFile rssFile, DatabaseConnection dbConn)
    {
        boolean newItem = false;

        if(rssFile != null)
        {
        	Boolean isFeedAlreadyInDatabase = dbConn.doesRssItemAlreadyExsists(rssFile.getItem_Id());

            String FeedId_Db = dbConn.getRowIdBySubscriptionID(String.valueOf(rssFile.getFeedID()));
            //String IdSubscription = dbConn.getIdSubscriptionByStreamID(rssFile.getFeedID());
            if(FeedId_Db != null)
            {
                rssFile.setFeedID_Db(FeedId_Db);

                dbConn.insertNewItem(rssFile, !isFeedAlreadyInDatabase);

                newItem = !rssFile.getRead();
            }
        }
        return newItem;
    }
}
