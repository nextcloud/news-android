package de.luhmer.owncloudnewsreader.reader;

import android.app.Activity;
import android.util.Log;
import de.luhmer.owncloudnewsreader.data.ConcreteSubscribtionItem;
import de.luhmer.owncloudnewsreader.data.FolderSubscribtionItem;
import de.luhmer.owncloudnewsreader.data.RssFile;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David on 24.05.13.
 */
public class InsertIntoDatabase {
    private static final String TAG = "InsertIntoDatabase";

    public static void InsertFoldersIntoDatabase(List<String[]> tags, Activity activity)
    {
        DatabaseConnection dbConn = new DatabaseConnection(activity);

        //List<String[]> tags = (List<String[]>) task_result;
        List<String> tagsAvailable = dbConn.convertCursorToStringArray(dbConn.getAllTopSubscriptions(), 1);

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

        dbConn.closeDatabase();
    }

    public static void InsertSubscriptionsIntoDatabase(ArrayList<ConcreteSubscribtionItem> tags, Activity activity)
    {
        DatabaseConnection dbConn = new DatabaseConnection(activity);

        List<String> tagsAvailable = dbConn.convertCursorToStringArray(dbConn.getAllSubSubscriptions(), 1);

        if(tags != null)
        {
            for(ConcreteSubscribtionItem tag : tags)
            {
                if(!tagsAvailable.contains(tag.header))
                {
                    String folderID_db = dbConn.getIdOfFolderByLabelPath(String.valueOf(tag.folder_id));
                    dbConn.insertNewSub_Subscription(tag.header, folderID_db, tag.subscription_id, tag.favIcon);
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
                    dbConn.removeTopSubscriptionItemByTag(tag);
            }

            //lvAdapter.notifyDataSetChanged();

            //lvAdapter = new SubscriptionExpandableListAdapter(getActivity(), dbConn);
        }

        dbConn.closeDatabase();
    }


    public static void InsertFeedItemsIntoDatabase(List<RssFile> files, Activity activity)
    {
        DatabaseConnection dbConn = new DatabaseConnection(activity);

        if(files != null)
        {
            Boolean skipReset = false;
            if(files.size() > 0)
            {
                if(files.get(0).getStarred())
                    skipReset = true;
                if(!skipReset)
                    dbConn.resetRssItemsDatabase();
            }

            for (RssFile rssFile : files) {
                String FeedId_Db = dbConn.getRowIdBySubscriptionID(String.valueOf(rssFile.getFeedID()));
                //String IdSubscription = dbConn.getIdSubscriptionByStreamID(rssFile.getFeedID());
                if(FeedId_Db != null)
                {
                    rssFile.setFeedID_Db(FeedId_Db);
                    //if(!dbConn.doesRssItemAlreadyExsists(rssFile.getFeedID()))
                    dbConn.insertNewFeed(rssFile.getTitle(),
                            rssFile.getLink(),
                            rssFile.getDescription(),
                            rssFile.getRead(),
                            String.valueOf(rssFile.getFeedID_Db()),
                            rssFile.getItem_Id(),
                            rssFile.getDate(),
                            rssFile.getStarred(),
                            rssFile.getGuid(),
                            rssFile.getGuidHash());
                }
            }
        }

        dbConn.closeDatabase();
    }
}
