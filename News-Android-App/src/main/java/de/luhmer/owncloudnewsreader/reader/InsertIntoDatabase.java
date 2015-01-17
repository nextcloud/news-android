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
        dbConn.deleteOldAndInsertNewFolders(folderList.toArray(new Folder[folderList.size()]));

        /*
        List<Feed> feeds = dbConn.getListOfFeeds();

        List<String> tagsAvailable = new ArrayList<String>(feeds.size());
        for(int i = 0; i < feeds.size(); i++)
            tagsAvailable.add(feeds.get(i).getFeedTitle());


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

            Log.d("ADD", ""+ addedCount);
            Log.d("REMOVE", ""+ removedCount++);
        }
    */
    }

    public static void InsertFeedsIntoDatabase(ArrayList<Feed> newFeeds, DatabaseConnectionOrm dbConn)
    {
        List<Feed> oldFeeds = dbConn.getListOfFeeds();

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
    }
}
