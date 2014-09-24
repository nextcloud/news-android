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

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.database.model.RssItem;

/**
 * Created by David on 24.05.13.
 */
public class InsertIntoDatabase {
    private static final String TAG = "InsertIntoDatabase";

    public static void InsertFoldersIntoDatabase(List<Folder> folderList, DatabaseConnectionOrm dbConn)
    {
        List<Feed> feeds = dbConn.getListOfFeeds();

        List<String> tagsAvailable = new ArrayList<String>(feeds.size());
        for(int i = 0; i < feeds.size(); i++)
            tagsAvailable.add(feeds.get(i).getFeedTitle());


        //dbConn.getDatabase().beginTransaction();
        try
        {
	        if(folderList != null)
	        {
                int addedCount = 0;
                int removedCount = 0;

	            for(Folder folder : folderList)
	            {
	                if(!tagsAvailable.contains(folder.getLabel()))
	                {
                        addedCount++;
                        dbConn.insertNewFolder(folder);
                    }
	            }


	            //List<String> newLabelList = new ArrayList<String>();
	            /*
                for(String[] subTag : tags)
	                newLabelList.add(subTag[0]);
	                */

                /*
	            for(String tag : tagsAvailable)
	            {
	                if(!newLabelList.contains(tag))
	                {
	                    int result = dbConn.removeFolderByFolderLabel(tag);//TODO this line is needed!!!!
	                    Log.d(TAG, "Result delete: " + result);
	                }
	            }
                */

	            Log.d("ADD", ""+ addedCount);
	            Log.d("REMOVE", ""+ removedCount++);
	        }
	        //dbConn.getDatabase().setTransactionSuccessful();
        } finally {
            //dbConn.getDatabase().endTransaction();
        }


        //dbConn.closeDatabase();
    }

    public static void InsertFeedsIntoDatabase(ArrayList<Feed> newFeeds, DatabaseConnectionOrm dbConn)
    {
        List<Feed> oldFeeds = dbConn.getListOfFeeds();

        try
        {
	        if(newFeeds != null)
	        {
                for(Feed feed : newFeeds)
                    dbConn.insertNewFeed(feed);

	            for(Feed feed : oldFeeds)
	            {
	                boolean found = false;
	                for(int i = 0; i < newFeeds.size(); i++)
	                {
	                    if(newFeeds.get(i).getFeedTitle().equals(feed.getFeedTitle()))
	                    {
                            //Set the avg color after sync again.
                            feed.setAvgColour(oldFeeds.get(i).getAvgColour());
                            dbConn.updateFeed(feed);

	                        found = true;
	                        break;
	                    }
	                }
	                if(!found)
	                {
	                	dbConn.removeFeedById(feed.getId());
	                    Log.d(TAG, "Remove Subscription: " + feed.getFeedTitle());
	                }
	            }
	        }
	        //dbConn.getDatabase().setTransactionSuccessful();
	    } finally {
	        //dbConn.getDatabase().endTransaction();
	    }
        //dbConn.closeDatabase();
    }


    public static void InsertRssItemsIntoDatabase(List<RssItem> files, Activity activity)
    {
        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(activity);

        if(files != null) {
            dbConn.insertNewItems(files.toArray(new RssItem[files.size()]));
        }

        /*
        dbConn.getDatabase().beginTransaction();
        try
        {
	        if(files != null)
	        {
	            for (RssItem rssFile : files)
	            	InsertSingleFeedItemIntoDatabase(rssFile, dbConn);
	        }
	        dbConn.getDatabase().setTransactionSuccessful();
	    } finally {
	        dbConn.getDatabase().endTransaction();
	    }

        dbConn.closeDatabase();
        */
    }

    public static boolean InsertSingleFeedItemIntoDatabase(RssItem rssFile, DatabaseConnectionOrm dbConn)
    {
        boolean newItem = false;

        if(rssFile != null)
        {
        	//Boolean isFeedAlreadyInDatabase = dbConn.doesRssItemAlreadyExsists(rssFile.getId());

            dbConn.insertNewItems(rssFile);
            newItem = !rssFile.getRead();
        }
        return newItem;
    }
}
