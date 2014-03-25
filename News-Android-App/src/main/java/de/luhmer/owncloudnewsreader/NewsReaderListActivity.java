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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SlidingPaneLayout.PanelSlideListener;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.luhmer.owncloudnewsreader.ListView.BlockingExpandableListView;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.LoginDialogFragment.LoginSuccessfullListener;
import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;
import de.luhmer.owncloudnewsreader.cursor.NewsListCursorAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.MenuUtilsSherlockFragmentActivity;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.services.DownloadImagesService;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncService;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;

/**
 * An activity representing a list of NewsReader. This activity has different
 * presentations for handset and tablet-size devices.
 * The activity makes heavy use of fragments. The list of items is a
 * {@link NewsReaderListFragment} and the item details (if present) is a
 * {@link NewsReaderDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link NewsReaderListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class NewsReaderListActivity extends MenuUtilsSherlockFragmentActivity implements
		 NewsReaderListFragment.Callbacks {

	private SlidingPaneLayout mSlidingLayout;

	static final String TAG = "NewsReaderListActivity";
	//ActionBarDrawerToggle drawerToggle;
	//DrawerLayout drawerLayout;

	public static final String FOLDER_ID = "FOLDER_ID";
	public static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
	public static final String ITEM_ID = "ITEM_ID";
	public static final String TITEL = "TITEL";


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		ThemeChooser.chooseTheme(this);

		//setTheme(R.style.Theme_Sherlock);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newsreader);


		AccountManager mAccountManager = AccountManager.get(this);

		boolean isAccountThere = false;
		//Remove all accounts first
		Account[] accounts = mAccountManager.getAccounts();
	    for (int index = 0; index < accounts.length; index++) {
	    	if (accounts[index].type.intern() == AccountGeneral.ACCOUNT_TYPE) {
	    		//mAccountManager.removeAccount(accounts[index], null, null);
	    		isAccountThere = true;
	    	}
	    }

	    if(!isAccountThere) {
		    //Then add the new account
	    	Account account = new Account(getString(R.string.app_name), AccountGeneral.ACCOUNT_TYPE);
	    	mAccountManager.addAccountExplicitly(account, "", new Bundle());
	    	//ContentResolver.setSyncAutomatically(account, getString(R.string.authorities), true);
			//ContentResolver.setIsSyncable(account, getString(R.string.authorities), 1);
	    }

		//Init config --> if nothing is configured start the login dialog.
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) == null)
        	StartLoginFragment(NewsReaderListActivity.this);


        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
        				.replace(R.id.left_drawer, new NewsReaderListFragment())
                   		.commit();


        mSlidingLayout = (SlidingPaneLayout) findViewById(R.id.sliding_pane);

        mSlidingLayout.setParallaxDistance(280);
        mSlidingLayout.setSliderFadeColor(getResources().getColor(android.R.color.transparent));

        mSlidingLayout.setPanelSlideListener(new PanelSlideListener() {

			@Override
			public void onPanelSlide(View arg0, float arg1) {
			}

			@Override
			public void onPanelOpened(View arg0) {
				updateAdapter();

                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);

                getMenuItemUpdater().setVisible(false);
			}

			@Override
			public void onPanelClosed(View arg0) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);

                getMenuItemUpdater().setVisible(true);

				StartDetailFragmentNow();
			}
		});
        mSlidingLayout.openPane();

        /*
		// Get a reference of the DrawerLayout
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		*/

		// Set a listener to be notified of drawer events.
		//drawerLayout.setDrawerListener(drawerToggle);


        //if(mPrefs.getBoolean(SettingsActivity.CB_SYNCONSTARTUP_STRING, false))
		//	startSync();

        /*
		if(!shouldDrawerStayOpen()) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}*/

        if(savedInstanceState == null)//When the app starts (no orientation change)
        {
        	startDetailFHolder = new StartDetailFragmentHolder(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS, true, null, true);
        	StartDetailFragmentNow();
        }

        //AppRater.app_launched(this);
        //AppRater.rateNow(this);

        //onTopItemClicked(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS, true, null);
    }

	private static final String FIRST_VISIBLE_DETAIL_ITEM_STRING = "FIRST_VISIBLE_DETAIL_ITEM_STRING";
	private static final String FIRST_VISIBLE_DETAIL_ITEM_MARGIN_TOP_STRING = "FIRST_VISIBLE_DETAIL_ITEM_MARGIN_TOP_STRING";
	private static final String ID_FEED_STRING = "ID_FEED_STRING";
	private static final String IS_FOLDER_BOOLEAN = "IS_FOLDER_BOOLEAN";
	private static final String OPTIONAL_FOLDER_ID ="OPTIONAL_FOLDER_ID";

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {

		NewsReaderDetailFragment ndf = ((NewsReaderDetailFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame));
		if(ndf != null) {
			View v = ndf.getListView().getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();

			outState.putInt(FIRST_VISIBLE_DETAIL_ITEM_STRING, ndf.getListView().getFirstVisiblePosition());
			outState.putInt(FIRST_VISIBLE_DETAIL_ITEM_MARGIN_TOP_STRING, top);
			outState.putString(OPTIONAL_FOLDER_ID, ndf.getIdFeed() == null ? ndf.getIdFolder() : ndf.getIdFeed());
			outState.putBoolean(IS_FOLDER_BOOLEAN, ndf.getIdFeed() == null ? true : false);
			outState.putString(ID_FEED_STRING, ndf.getIdFeed() != null ? ndf.getIdFolder() : ndf.getIdFeed());
		}

		super.onSaveInstanceState(outState);
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if(savedInstanceState != null) {
			if(savedInstanceState.containsKey(FIRST_VISIBLE_DETAIL_ITEM_STRING) &&
					savedInstanceState.containsKey(ID_FEED_STRING) &&
					savedInstanceState.containsKey(IS_FOLDER_BOOLEAN) &&
					savedInstanceState.containsKey(OPTIONAL_FOLDER_ID)) {


				startDetailFHolder = new StartDetailFragmentHolder(savedInstanceState.getString(OPTIONAL_FOLDER_ID),
																	savedInstanceState.getBoolean(IS_FOLDER_BOOLEAN),
																	savedInstanceState.getString(ID_FEED_STRING),
                                                                    false);

	        	NewsReaderDetailFragment ndf = StartDetailFragmentNow();
	        	if(ndf != null) {
	        		ndf.setActivatedPosition(savedInstanceState.getInt(FIRST_VISIBLE_DETAIL_ITEM_STRING));
	        		ndf.setMarginFromTop(savedInstanceState.getInt(FIRST_VISIBLE_DETAIL_ITEM_MARGIN_TOP_STRING));
	        	}
			}
		}
		super.onRestoreInstanceState(savedInstanceState);
	}


    public boolean isSlidingPaneOpen() {
        return mSlidingLayout.isOpen();
    }

	private NewsReaderDetailFragment StartDetailFragmentNow() {
		NewsReaderDetailFragment nrdf = null;
		if(startDetailFHolder != null) {
			nrdf = startDetailFHolder.StartDetailFragment();
			startDetailFHolder = null;
		}
		return nrdf;
	}

	public void updateAdapter() {
		NewsReaderListFragment nlf = ((NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer));
		if(nlf != null)
		{
			// Block children layout for now
			BlockingExpandableListView bView = ((BlockingExpandableListView) nlf.eListView);

			int firstVisPos = bView.getFirstVisiblePosition();
			View firstVisView = bView.getChildAt(0);
			int top = firstVisView != null ? firstVisView.getTop() : 0;

			// Number of items added before the first visible item
			int itemsAddedBeforeFirstVisible = 0;

			bView.setBlockLayoutChildren(true);
			nlf.lvAdapter.notifyDataSetChanged();
			bView.setBlockLayoutChildren(false);

			// Call setSelectionFromTop to change the ListView position
			if(bView.getCount() >= firstVisPos + itemsAddedBeforeFirstVisible)
				bView.setSelectionFromTop(firstVisPos + itemsAddedBeforeFirstVisible, top);
		}
	}

	@Override
	protected void onResume() {
		ThemeChooser.chooseTheme(this);

		updateAdapter();

		super.onResume();
	}

	public boolean shouldDrawerStayOpen() {
		if(getResources().getBoolean(R.bool.two_pane))
			return true;
		return false;
	}

	private StartDetailFragmentHolder startDetailFHolder = null;

	private class StartDetailFragmentHolder {
		String idSubscription;
		boolean isFolder;
		String optional_folder_id;
        boolean updateListView;

		public StartDetailFragmentHolder(String idSubscription, boolean isFolder, String optional_folder_id, boolean updateListView) {
			this.idSubscription = idSubscription;
			this.isFolder = isFolder;
			this.optional_folder_id = optional_folder_id;
            this.updateListView = updateListView;
		}

		public NewsReaderDetailFragment StartDetailFragment() {
			return NewsReaderListActivity.this.StartDetailFragment(idSubscription, isFolder, optional_folder_id, updateListView);
		}
	}

	/**
	 * Callback method from {@link NewsReaderListFragment.Callbacks} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public void onTopItemClicked(String idSubscription, boolean isFolder, String optional_folder_id) {
		if(!shouldDrawerStayOpen())
			mSlidingLayout.closePane();

		startDetailFHolder = new StartDetailFragmentHolder(idSubscription, isFolder, optional_folder_id, true);

		if(shouldDrawerStayOpen())
			StartDetailFragmentNow();
	}

	@Override
	public void onChildItemClicked(String idSubscription, String optional_folder_id) {
		if(!shouldDrawerStayOpen())
			mSlidingLayout.closePane();

		//StartDetailFragment(idSubscription, false, optional_folder_id);
		startDetailFHolder = new StartDetailFragmentHolder(idSubscription, false, optional_folder_id, true);
		if(shouldDrawerStayOpen())
			StartDetailFragmentNow();
	}

	private NewsReaderDetailFragment StartDetailFragment(String id, Boolean folder, String optional_folder_id, boolean UpdateListView)
	{
		if(super.getMenuItemDownloadMoreItems() != null)
			super.getMenuItemDownloadMoreItems().setEnabled(true);

		DatabaseConnection dbConn = new DatabaseConnection(getApplicationContext());


		Intent intent = new Intent();

		if(!folder)
		{
			intent.putExtra(SUBSCRIPTION_ID, id);
			intent.putExtra(FOLDER_ID, optional_folder_id);
			intent.putExtra(TITEL, dbConn.getTitleOfSubscriptionByRowID(id));
		}
		else
		{
			intent.putExtra(FOLDER_ID, id);
			int idFolder = Integer.valueOf(id);
			if(idFolder >= 0)
				intent.putExtra(TITEL, dbConn.getTitleOfFolderByID(id));
			else if(idFolder == -10)
				intent.putExtra(TITEL, getString(R.string.allUnreadFeeds));
			else if(idFolder == -11)
				intent.putExtra(TITEL, getString(R.string.starredFeeds));
		}

		Bundle arguments = intent.getExtras();

		NewsReaderDetailFragment fragment = new NewsReaderDetailFragment();
        fragment.setUpdateListViewOnStartUp(UpdateListView);

		fragment.setArguments(arguments);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment)
				.commit();

		dbConn.closeDatabase();

		return fragment;
	}


    public void UpdateItemList()
    {
        NewsReaderDetailFragment nrD = (NewsReaderDetailFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(nrD != null)
            ((NewsListCursorAdapter)nrD.getListAdapter()).notifyDataSetChanged();
            //nrD.UpdateCursor();
    }


    void startSync()
    {
		//menuItemUpdater.setActionView(R.layout.inderterminate_progress);
		((NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer)).StartSync();
    }

	public void UpdateButtonSyncLayout()
    {
        if(super.getMenuItemUpdater() != null)
        {
            //IReader _Reader = ((NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer))._Reader;


            try {
                NewsReaderListFragment ndf = (NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer);
            	IOwnCloudSyncService _Reader = ndf._ownCloudSyncService;
                PullToRefreshLayout pullToRefreshView = ndf.mPullToRefreshLayout;

                if(_Reader != null) {
					if(_Reader.isSyncRunning())
					{
						super.getMenuItemUpdater().setActionView(R.layout.inderterminate_progress);
					    if(pullToRefreshView != null && !pullToRefreshView.isRefreshing()) {
					    	pullToRefreshView.setRefreshing(true);
                        }
					}
					else
					{
						super.getMenuItemUpdater().setActionView(null);
					    if(pullToRefreshView != null)
					    	pullToRefreshView.setRefreshComplete();
					}
                }
			} catch (RemoteException e) {
				e.printStackTrace();
			}
        }
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.news_reader, menu);
		//getSupportMenuInflater().inflate(R.menu.news_reader, menu);


		super.onCreateOptionsMenu(menu, getSupportMenuInflater(), this);

        UpdateButtonSyncLayout();

		return true;
	}

	@Override
	public void onBackPressed() {
		if(mSlidingLayout.isOpen())
			super.onBackPressed();
			//mSlidingLayout.closePane();
		else
			mSlidingLayout.openPane();
	}

	private static final int RESULT_SETTINGS = 15642;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = super.onOptionsItemSelected(item, this);

		if(!handled)
		{
			switch (item.getItemId()) {

				case android.R.id.home:
					if(!mSlidingLayout.isOpen())
						mSlidingLayout.openPane();
					return true;

				case R.id.action_settings:
					Intent intent = new Intent(this, SettingsActivity.class);
				    //intent.putExtra(EXTRA_MESSAGE, message);
				    startActivityForResult(intent, RESULT_SETTINGS);
					return true;

                case R.id.action_sync_settings:
                    String[] authorities = { "de.luhmer.owncloudnewsreader" };
                    Intent intentSyncSettings = new Intent(Settings.ACTION_SYNC_SETTINGS);
                    intentSyncSettings.putExtra(Settings.EXTRA_AUTHORITIES, authorities);
                    startActivity(intentSyncSettings);
                    break;

				case R.id.menu_update:
					//menuItemUpdater = item.setActionView(R.layout.inderterminate_progress);
					startSync();
					break;

				case R.id.action_login:
					StartLoginFragment(NewsReaderListActivity.this);
					break;

				case R.id.menu_StartImageCaching:
					DatabaseConnection dbConn = new DatabaseConnection(this);
			    	try {
			    		long highestItemId = dbConn.getLowestItemIdUnread();
			    		Intent service = new Intent(this, DownloadImagesService.class);
			        	service.putExtra(DownloadImagesService.LAST_ITEM_ID, highestItemId);
			    		startService(service);
			    	} finally {
			    		dbConn.closeDatabase();
			    	}
					break;
			}
		}
		return super.onOptionsItemSelected(item);
	}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if (requestCode == 1) {
        if(resultCode == RESULT_OK){
            int pos = data.getIntExtra("POS", 0);
            UpdateListViewAndScrollToPos(this, pos);

            ((NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer)).lvAdapter.notifyDataSetChanged();
        }
        else if(requestCode == RESULT_SETTINGS)
        {
        	((NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer)).lvAdapter.ReloadAdapter();
        	((NewsReaderDetailFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame)).UpdateCursor();
        }
    }


    public static void StartLoginFragment(final SherlockFragmentActivity activity)
    {
	   	LoginDialogFragment dialog = new LoginDialogFragment();
	   	dialog.setmActivity(activity);
	   	dialog.setListener(new LoginSuccessfullListener() {

			@Override
			public void LoginSucceeded() {
				((NewsReaderListActivity) activity).startSync();
			}
		});
	    dialog.show(activity.getSupportFragmentManager(), "NoticeDialogFragment");
    }

    /*
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//if (requestCode == 1) {
			if(resultCode == RESULT_OK){
				int pos = data.getIntExtra("POS", 0);
				UpdateListViewAndScrollToPos(this, pos);
			}
			if (resultCode == RESULT_CANCELED) {
				//Write your code on no result return
			}
		//}
	}*/


    //@TargetApi(Build.VERSION_CODES.FROYO)
	public static void UpdateListViewAndScrollToPos(FragmentActivity act, int pos)
    {
        ((NewsReaderDetailFragment) act.getSupportFragmentManager().findFragmentById(R.id.content_frame)).notifyDataSetChangedOnAdapter();
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
        	//((NewsReaderDetailFragment) act.getSupportFragmentManager().findFragmentById(R.id.newsreader_detail_container)).getListView().smoothScrollToPosition(pos);
        //else

        //Is not used any longer
        //((NewsReaderDetailFragment) act.getSupportFragmentManager().findFragmentById(R.id.content_frame)).getListView().setSelection(pos);
    }
}
