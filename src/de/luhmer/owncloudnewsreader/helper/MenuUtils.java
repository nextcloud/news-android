package de.luhmer.owncloudnewsreader.helper;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.luhmer.owncloudnewsreader.NewsReaderDetailActivity;
import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;

public class MenuUtils {
	protected static final String TAG = "MenuUtils";

	static FragmentActivity activity;
	
	static MenuItem menuItemSettings;
	static MenuItem menuItemLogin;
	static MenuItem menuItemStartImageCaching;
	
	
	public static MenuItem menuItemUpdater;
	public static MenuItem menuItemMarkAllAsRead;
	public static MenuItem menuItemDownloadMoreItems;
	
	public static void onCreateOptionsMenu(Menu menu, MenuInflater inflater, boolean mTwoPane, FragmentActivity act) {
		inflater.inflate(R.menu.news_reader, menu);

		activity = act;
		
		menuItemSettings = menu.findItem(R.id.action_settings);
		menuItemLogin = menu.findItem(R.id.action_login);
		menuItemStartImageCaching = menu.findItem(R.id.menu_StartImageCaching);
		
		menuItemUpdater = menu.findItem(R.id.menu_update);
		menuItemMarkAllAsRead = menu.findItem(R.id.menu_markAllAsRead);
		menuItemDownloadMoreItems = menu.findItem(R.id.menu_downloadMoreItems);
		
		
		menuItemMarkAllAsRead.setEnabled(false);
		menuItemDownloadMoreItems.setEnabled(false);
		
		
		if(!mTwoPane && act instanceof NewsReaderListActivity)//On Smartphones disable this...
		{
			menuItemDownloadMoreItems.setVisible(false);
			menuItemDownloadMoreItems = null;
			menuItemMarkAllAsRead.setVisible(false);
			menuItemMarkAllAsRead = null;
		} else if(act instanceof NewsReaderDetailActivity) {
			menuItemLogin.setVisible(false);
			menuItemSettings.setVisible(false);
			menuItemStartImageCaching.setVisible(false);
			menuItemUpdater.setVisible(false);
			
			menuItemMarkAllAsRead.setEnabled(true);
			menuItemDownloadMoreItems.setEnabled(true);
		}
		
		
	}

	public static boolean onOptionsItemSelected(MenuItem item, FragmentActivity activity) {
		switch (item.getItemId()) {
			case R.id.menu_markAllAsRead:
				NewsReaderDetailFragment ndf = ((NewsReaderDetailFragment) activity.getSupportFragmentManager().findFragmentById(R.id.newsreader_detail_container));
				if(ndf != null)
				{
					DatabaseConnection dbConn = new DatabaseConnection(activity);
					try {
						dbConn.markAllItemsAsRead(ndf.getDatabaseIdsOfItems());
					} finally {
						dbConn.closeDatabase();
					}
					ndf.UpdateCursor();
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
		NewsReaderDetailActivity nda = ((NewsReaderDetailActivity) activity);
		IReader _Reader = new OwnCloud_Reader();
		_Reader.Start_AsyncTask_GetOldItems(0, activity, onAsyncTaskComplete, nda.getIdFeed(), nda.getIdFolder());
		
		Toast.makeText(activity, activity.getString(R.string.toast_GettingMoreItems), Toast.LENGTH_LONG).show();
	}
	
	static OnAsyncTaskCompletedListener onAsyncTaskComplete = new OnAsyncTaskCompletedListener() {
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {			
			NewsReaderDetailFragment ndf = ((NewsReaderDetailFragment) activity.getSupportFragmentManager().findFragmentById(R.id.newsreader_detail_container));
			if(ndf != null)
				ndf.UpdateCursor();
			
			Log.d(TAG, "Finished Download extra items..");
		}
	};
}