package de.luhmer.owncloudnewsreader.helper;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.luhmer.owncloudnewsreader.Constants;
import de.luhmer.owncloudnewsreader.NewsReaderDetailFragment;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.VersionInfoDialogFragment;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;

public class MenuUtilsSherlockFragmentActivity extends SherlockFragmentActivity {
		
	protected static final String TAG = "MenuUtils";

	static FragmentActivity activity;
	
	static MenuItem menuItemSettings;
	static MenuItem menuItemLogin;
	static MenuItem menuItemStartImageCaching;
	
	
	private static MenuItem menuItemUpdater;
	private static MenuItem menuItemMarkAllAsRead;
	private static MenuItem menuItemDownloadMoreItems;
	
	static IReader _Reader;
	
	/**
	 * @return the menuItemUpdater
	 */
	public static MenuItem getMenuItemUpdater() {
		return menuItemUpdater;
	}

	/**
	 * @return the menuItemMarkAllAsRead
	 */
	public static MenuItem getMenuItemMarkAllAsRead() {
		return menuItemMarkAllAsRead;
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
				
		NewsReaderDetailFragment ndf = ((NewsReaderDetailFragment) activity.getSupportFragmentManager().findFragmentById(R.id.content_frame));
		if(ndf != null)
			ndf.UpdateMenuItemsState();
	}

	public static boolean onOptionsItemSelected(MenuItem item, FragmentActivity activity) {
		switch (item.getItemId()) {				
			case R.id.menu_About_Changelog:
				SherlockDialogFragment dialog = new VersionInfoDialogFragment();
		        dialog.show(activity.getSupportFragmentManager(), "VersionChangelogDialogFragment");
				return true;
		
			case R.id.menu_markAllAsRead:
				NewsReaderDetailFragment ndf = ((NewsReaderDetailFragment) activity.getSupportFragmentManager().findFragmentById(R.id.content_frame));
				if(ndf != null)
				{
					/*
					for(int i = 0; i < ndf.getListView().getChildCount(); i++)
					{
						 View view = ndf.getListView().getChildAt(i);
						 CheckBox cb = (CheckBox) view.findViewById(R.id.cb_lv_item_read);
						 if(!cb.isChecked())
							 cb.setChecked(true);
					}
					*/
					
					DatabaseConnection dbConn = new DatabaseConnection(activity);
					try {
						//dbConn.markAllItemsAsRead(ndf.getDatabaseIdsOfItems());
						List<Integer> items = new ArrayList<Integer>();
						
						NewsListCursorAdapterHolder ncla = ((NewsListCursorAdapterHolder)activity.getApplication());
						Cursor cursor = ncla.getLvAdapter().getCursor();
						cursor.moveToFirst();
						do {
							items.add(Integer.parseInt(cursor.getString(0)));
						} while (cursor.moveToNext());
						dbConn.markAllItemsAsRead(items);
						
					} finally {
						dbConn.closeDatabase();
					}
					ndf.UpdateCursor();
					
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
		DatabaseConnection dbConn = new DatabaseConnection(activity);
		int count = dbConn.getCountFeedsForFolder(SubscriptionExpandableListAdapter.ALL_ITEMS, false);
		if(count >= Constants.maxItemsCount)
		{
			String text = activity.getString(R.string.max_items_count_reached);
			text = text.replace("XX", "" + Constants.maxItemsCount);
			new AlertDialog.Builder(activity)
					.setTitle(activity.getString(R.string.empty_view_header))
					.setMessage(text)
					.setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							
						}
					  })
					.create()
					.show();
			//Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
		}
		else
		{		
			String username = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString("edt_username", "");			
			String password = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString("edt_password", "");
			
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
				ndf.UpdateCursor();
			
			Log.d(TAG, "Finished Download extra items..");
		}
	};
}