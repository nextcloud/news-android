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

package de.luhmer.owncloudnewsreader.reader;

import android.util.Log;

import java.util.List;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.helper.FavIconUtils;

public class InsertIntoDatabase {
    private static final String TAG = "InsertRssItemIntoDb";

    public static void InsertFoldersIntoDatabase(List<Folder> folderList, DatabaseConnectionOrm dbConn)
    {
        dbConn.deleteOldAndInsertNewFolders(folderList);

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

    public static void InsertFeedsIntoDatabase(List<Feed> feeds, DatabaseConnectionOrm dbConn)
    {
        List<Feed> oldFeeds = dbConn.getListOfFeeds();

        if(feeds != null)
        {
            dbConn.insertNewFeed(feeds);

            for(Feed oldFeed : oldFeeds)
            {
                boolean found = false;
                for(Feed newFeed : feeds)
                {
                    if(oldFeed.getId() == newFeed.getId()) {
                        found = true;

                        // Set the avg color after sync again.
                        newFeed.setAvgColour(oldFeed.getAvgColour());
                        // Set the notification channel after sync again
                        newFeed.setNotificationChannel(oldFeed.getNotificationChannel());
                        newFeed.setOpenIn(oldFeed.getOpenIn());

                        // fix favicon url
                        newFeed.setFaviconUrl(FavIconUtils.fixFavIconUrl(newFeed.getFaviconUrl()));

                        dbConn.updateFeed(newFeed);
                        break;
                    }
                }
                if(!found)
                {
                    dbConn.removeFeedById(oldFeed.getId());
                    Log.v(TAG, "Remove Subscription: " + oldFeed.getFeedTitle());
                }
            }
        }
    }
}
