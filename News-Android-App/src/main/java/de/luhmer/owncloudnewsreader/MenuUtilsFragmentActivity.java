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

package de.luhmer.owncloudnewsreader;

import android.annotation.TargetApi;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;

public class MenuUtilsFragmentActivity extends PodcastFragmentActivity {

    protected static final String TAG = "MenuUtils";

    static FragmentActivity activity;

    static MenuItem menuItemSettings;
    static MenuItem menuItemLogin;
    static MenuItem menuItemStartImageCaching;


    private static MenuItem menuItemUpdater;
    private static MenuItem menuItemDownloadMoreItems;

    static IReader _Reader;

    /**
     * @return the menuItemUpdater
     */
    public static MenuItem getMenuItemUpdater() {
        return menuItemUpdater;
    }


    /**
     * @return the menuItemDownloadMoreItems
     */
    public static MenuItem getMenuItemDownloadMoreItems() {
        return menuItemDownloadMoreItems;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
        super.onResume();
    }


    public static void onCreateOptionsMenu(Menu menu, MenuInflater inflater, FragmentActivity act) {
        inflater.inflate(R.menu.news_reader, menu);
        activity = act;

        menuItemSettings = menu.findItem(R.id.action_settings);
        menuItemLogin = menu.findItem(R.id.action_login);
        menuItemStartImageCaching = menu.findItem(R.id.menu_StartImageCaching);

        menuItemUpdater = menu.findItem(R.id.menu_update);
        //menuItemMarkAllAsRead = menu.findItem(R.id.menu_markAllAsRead);
        menuItemDownloadMoreItems = menu.findItem(R.id.menu_downloadMoreItems);


        //menuItemMarkAllAsRead.setEnabled(false);
        menuItemDownloadMoreItems.setEnabled(false);

        NewsReaderDetailFragment ndf = ((NewsReaderDetailFragment) activity.getSupportFragmentManager().findFragmentById(R.id.content_frame));
        if(ndf != null)
            ndf.UpdateMenuItemsState();
    }

    public static boolean onOptionsItemSelected(MenuItem item, FragmentActivity activity) {
        switch (item.getItemId()) {
            case R.id.menu_About_Changelog:
                DialogFragment dialog = new VersionInfoDialogFragment();
                dialog.show(activity.getSupportFragmentManager(), "VersionChangelogDialogFragment");
                return true;

            case R.id.menu_markAllAsRead:
                NewsReaderDetailFragment ndf = ((NewsReaderDetailFragment) activity.getSupportFragmentManager().findFragmentById(R.id.content_frame));
                if(ndf != null)
                {
                    DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(activity);
                    //dbConn.markAllItemsAsReadForCurrentView();

                    for(int i = 0; i < ndf.getListAdapter().getCount(); i++) {
                        RssItem rssItem = (RssItem) ndf.getListAdapter().getItem(i);
                        rssItem.setRead_temp(true);
                        dbConn.updateRssItem(rssItem);
                    }

                    ndf.notifyDataSetChangedOnAdapter();

                    //If tablet view is enabled update the listview as well
                    if(activity instanceof NewsReaderListActivity)
                        ((NewsReaderListActivity) activity).updateAdapter();

                }
                return true;

            case R.id.menu_downloadMoreItems:
                DownloadMoreItems();
                return true;
        }
        return false;
    }

    private static void DownloadMoreItems()
    {
        String username = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString("edt_username", "");
        String password = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString("edt_password", "");

        if(username != null) {
            _Reader = new OwnCloud_Reader();
            ((OwnCloud_Reader)_Reader).Start_AsyncTask_GetVersion(Constants.TaskID_GetVersion, activity, onAsyncTaskGetVersionFinished, username, password);

            Toast.makeText(activity, activity.getString(R.string.toast_GettingMoreItems), Toast.LENGTH_SHORT).show();
        }
    }

    static OnAsyncTaskCompletedListener onAsyncTaskGetVersionFinished = new OnAsyncTaskCompletedListener() {

        @Override
        public void onAsyncTaskCompleted(int task_id, Object task_result) {
            if(_Reader != null) {
                String appVersion = task_result.toString();
                API api = API.GetRightApiForVersion(appVersion, activity);
                ((OwnCloud_Reader) _Reader).setApi(api);

                NewsReaderDetailFragment ndf = ((NewsReaderDetailFragment) activity.getSupportFragmentManager().findFragmentById(R.id.content_frame));
                _Reader.Start_AsyncTask_GetOldItems(Constants.TaskID_GetItems, activity, onAsyncTaskComplete, ndf.getIdFeed(), ndf.getIdFolder());
            }
        }
    };

    static OnAsyncTaskCompletedListener onAsyncTaskComplete = new OnAsyncTaskCompletedListener() {
        @Override
        public void onAsyncTaskCompleted(int task_id, Object task_result) {
            NewsReaderDetailFragment ndf = ((NewsReaderDetailFragment) activity.getSupportFragmentManager().findFragmentById(R.id.content_frame));
            if(ndf != null)
                ndf.UpdateCurrentRssView(activity, true);

            Log.d(TAG, "Finished Download extra items..");
        }
    };
}