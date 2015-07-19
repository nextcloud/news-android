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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.LoginDialogFragment.LoginSuccessfullListener;
import de.luhmer.owncloudnewsreader.adapter.RecyclerItemClickListener;
import de.luhmer.owncloudnewsreader.adapter.ViewHolder;
import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.events.podcast.FeedPanelSlideEvent;
import de.luhmer.owncloudnewsreader.helper.AidlException;
import de.luhmer.owncloudnewsreader.helper.DatabaseUtils;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;
import de.luhmer.owncloudnewsreader.services.DownloadImagesService;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncService;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncServiceCallback;
import de.luhmer.owncloudnewsreader.services.OwnCloudSyncService;

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
public class NewsReaderListActivity extends PodcastFragmentActivity implements
		 NewsReaderListFragment.Callbacks,RecyclerItemClickListener,SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = NewsReaderListActivity.class.getCanonicalName();

	public static final String FOLDER_ID = "FOLDER_ID";
	public static final String FEED_ID = "FEED_ID";
	public static final String ITEM_ID = "ITEM_ID";
	public static final String TITEL = "TITEL";

	private static MenuItem menuItemUpdater;
	private static MenuItem menuItemDownloadMoreItems;
	private static IReader _Reader;

    @InjectView(R.id.toolbar) Toolbar toolbar;

	private ServiceConnection mConnection = null;

	@Optional @InjectView(R.id.drawer_layout) protected DrawerLayout drawerLayout;

	private ActionBarDrawerToggle drawerToggle;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeChooser.chooseTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newsreader);

        ButterKnife.inject(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

		initAccountManager();

		//Init config --> if nothing is configured start the login dialog.
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) == null)
        	StartLoginFragment(NewsReaderListActivity.this);


		Bundle args = new Bundle();
		String userName = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String url = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null);
		args.putString("accountName", String.format("%s\n%s",userName,url));
		NewsReaderListFragment newsReaderListFragment = new NewsReaderListFragment();
		newsReaderListFragment.setArguments(args);
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
        				.replace(R.id.left_drawer, newsReaderListFragment)
                   		.commit();

		if(drawerLayout != null) {
			drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.empty_view_content, R.string.empty_view_content) {
				@Override
				public void onDrawerClosed(View drawerView) {
					super.onDrawerClosed(drawerView);
					togglePodcastVideoViewAnimation();

					syncState();
					EventBus.getDefault().post(new FeedPanelSlideEvent(false));
				}

				@Override
				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					togglePodcastVideoViewAnimation();
					reloadCountNumbersOfSlidingPaneAdapter();

					syncState();
				}
			};

			drawerLayout.setDrawerListener(drawerToggle);
		}
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		if(drawerToggle != null)
			drawerToggle.syncState();

        if(savedInstanceState == null)//When the app starts (no orientation change)
        {
        	StartDetailFragment(SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue(), true, null, true);
        }

        //AppRater.app_launched(this);
        //AppRater.rateNow(this);

		UpdateButtonLayout();
    }

	View.OnClickListener mSnackbarListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			//Toast.makeText(getActivity(), "button 1 pressed", 3000).show();

			NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
			if(ndf != null) {
				//ndf.reloadAdapterFromScratch();
				ndf.UpdateCurrentRssView(NewsReaderListActivity.this, true);
			}
		}
	};

	private static final String ID_FEED_STRING = "ID_FEED_STRING";
	private static final String IS_FOLDER_BOOLEAN = "IS_FOLDER_BOOLEAN";
	private static final String OPTIONAL_FOLDER_ID ="OPTIONAL_FOLDER_ID";


	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
        safeInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	/**
	 * Check if the account is in the Android Account Manager. If not it will be added automatically
	 */
	private void initAccountManager() {
		AccountManager mAccountManager = AccountManager.get(this);

		boolean isAccountThere = false;
		Account[] accounts = mAccountManager.getAccounts();
		for (Account account : accounts) {
			if (account.type.intern().equals(AccountGeneral.ACCOUNT_TYPE)) {
				isAccountThere = true;
			}
		}

		//If the account is not in the Android Account Manager
		if(!isAccountThere) {
			//Then add the new account
			Account account = new Account(getString(R.string.app_name), AccountGeneral.ACCOUNT_TYPE);
			mAccountManager.addAccountExplicitly(account, "", new Bundle());

			SyncIntervalSelectorActivity.SetAccountSyncInterval(this);
		}
	}


    private void safeInstanceState(Bundle outState) {
        NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
        if(ndf != null) {
            outState.putLong(OPTIONAL_FOLDER_ID, ndf.getIdFeed() == null ? ndf.getIdFolder() : ndf.getIdFeed());
            outState.putBoolean(IS_FOLDER_BOOLEAN, ndf.getIdFeed() == null);
            outState.putLong(ID_FEED_STRING, ndf.getIdFeed() != null ? ndf.getIdFeed() : ndf.getIdFolder());
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState.containsKey(ID_FEED_STRING) &&
                savedInstanceState.containsKey(IS_FOLDER_BOOLEAN) &&
                savedInstanceState.containsKey(OPTIONAL_FOLDER_ID)) {


            StartDetailFragment(savedInstanceState.getLong(OPTIONAL_FOLDER_ID),
					savedInstanceState.getBoolean(IS_FOLDER_BOOLEAN),
					savedInstanceState.getLong(ID_FEED_STRING),
					false);
		}
    }

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if(savedInstanceState != null) {
			restoreInstanceState(savedInstanceState);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if(drawerToggle != null)
			drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(drawerToggle != null)
			drawerToggle.onConfigurationChanged(newConfig);
	}

	public void reloadCountNumbersOfSlidingPaneAdapter() {
        NewsReaderListFragment nlf = getSlidingListFragment();
        if(nlf != null) {
            nlf.ListViewNotifyDataSetChanged();
        }
    }

	@Override
	protected void onStart() {
		Intent serviceIntent = new Intent(this, OwnCloudSyncService.class);
		mConnection = generateServiceConnection();
		if(!isMyServiceRunning(OwnCloudSyncService.class)) {
			startService(serviceIntent);
		}
		bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
		super.onStart();
	}

	@Override
	protected void onStop() {
		if(_ownCloudSyncService != null) {
			try {
				_ownCloudSyncService.unregisterCallback(callback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		unbindService(mConnection);
		mConnection = null;
		super.onStop();
	}

	private ServiceConnection generateServiceConnection() {
		return new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder binder) {
				_ownCloudSyncService = IOwnCloudSyncService.Stub.asInterface(binder);
				try {
					_ownCloudSyncService.registerCallback(callback);

					//Start auto sync if enabled
					SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(NewsReaderListActivity.this);
					if(mPrefs.getBoolean(SettingsActivity.CB_SYNCONSTARTUP_STRING, false))
						startSync();

						UpdateButtonLayout();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				try {
					_ownCloudSyncService.unregisterCallback(callback);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	IOwnCloudSyncService _ownCloudSyncService;
	private IOwnCloudSyncServiceCallback callback = new IOwnCloudSyncServiceCallback.Stub() {
		private void UpdateButtonLayoutWithHandler() {
			Handler refresh = new Handler(Looper.getMainLooper());
			refresh.post(new Runnable() {
				public void run() {
					UpdateButtonLayout();;
				}
			});
		}

		@Override
		public void throwException(AidlException ex) throws RemoteException {
			Toast.makeText(NewsReaderListActivity.this,ex.getmException().getLocalizedMessage(),Toast.LENGTH_LONG).show();

			UpdateButtonLayoutWithHandler();
		}

		@Override
		public void startedSync(String sync_type) throws RemoteException {
			UpdateButtonLayoutWithHandler();
		}

		@Override
		public void finishedSync(String sync_type) throws RemoteException {
			UpdateButtonLayoutWithHandler();

			Constants.SYNC_TYPES st = Constants.SYNC_TYPES.valueOf(sync_type);

			switch(st) {
				case SYNC_TYPE__GET_API:
					break;
				case SYNC_TYPE__ITEM_STATES:
					break;
				case SYNC_TYPE__FOLDER:
					break;
				case SYNC_TYPE__FEEDS:
					break;
				case SYNC_TYPE__ITEMS:

					Log.d(TAG, "finished sync");
					Handler refresh = new Handler(Looper.getMainLooper());
					refresh.post(new Runnable() {
						public void run() {
							NewsReaderListFragment newsReaderListFragment = getSlidingListFragment();
							newsReaderListFragment.ReloadAdapter();
							UpdateItemList();
							UpdatePodcastView();

							SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(NewsReaderListActivity.this);
							int newItemsCount = mPrefs.getInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 0);
							if(newItemsCount > 0) {
								Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout),
										getResources().getQuantityString(R.plurals.message_bar_new_articles_available,newItemsCount,newItemsCount),
										Snackbar.LENGTH_LONG);
								snackbar.setAction(getString(R.string.message_bar_reload), mSnackbarListener);
								snackbar.setActionTextColor(getResources().getColor(R.color.accent_material_dark));
								// Setting android:TextColor to #000 in the light theme results in black on black
								// text on the Snackbar, set the text back to white,
								// TODO: find a cleaner way to do this
								TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
								textView.setTextColor(Color.WHITE);
								snackbar.show();
							}
						}
					});
					break;
			}
		}

	};

	@Override
	protected void onResume() {
		ThemeChooser.chooseTheme(this);

		reloadCountNumbersOfSlidingPaneAdapter();

		invalidateOptionsMenu();

		super.onResume();
	}

	@Override
	public void onRefresh() {
		startSync();
	}

	/**
	 * Callback method from {@link NewsReaderListFragment.Callbacks} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public void onTopItemClicked(long idFeed, boolean isFolder, Long optional_folder_id) {
		if(drawerLayout != null)
			drawerLayout.closeDrawer(GravityCompat.START);

		StartDetailFragment(idFeed, isFolder, optional_folder_id, true);
	}

	@Override
	public void onChildItemClicked(long idFeed, Long optional_folder_id) {
		if(drawerLayout != null)
			drawerLayout.closeDrawer(GravityCompat.START);

		//StartDetailFragment(idSubscription, false, optional_folder_id);
		StartDetailFragment(idFeed, false, optional_folder_id, true);
	}

	private NewsReaderDetailFragment StartDetailFragment(long id, Boolean folder, Long optional_folder_id, boolean updateListView)
	{
		if(menuItemDownloadMoreItems != null) {
			menuItemDownloadMoreItems.setEnabled(true);
		}

		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getApplicationContext());

		Long feedId = null;
		Long folderId;
		String titel = null;

		if(!folder)
		{
			feedId = id;
			folderId = optional_folder_id;
			titel = dbConn.getFeedById(id).getFeedTitle();
		}
		else
		{
			folderId = id;
			int idFolder = (int) id;
			if(idFolder >= 0)
				titel = dbConn.getFolderById(id).getLabel();
			else if(idFolder == -10)
				titel = getString(R.string.allUnreadFeeds);
			else if(idFolder == -11)
				titel = getString(R.string.starredFeeds);
		}

		NewsReaderDetailFragment fragment = getNewsReaderDetailFragment();
		fragment.setData(feedId, folderId, titel, updateListView);
		return fragment;
	}


    public void UpdateItemList()
    {
        try {
            NewsReaderDetailFragment nrD = getNewsReaderDetailFragment();
            if (nrD != null)
                nrD.getRecyclerView().getAdapter().notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    void startSync()
    {
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		if(mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) == null)
			StartLoginFragment(this);
		else {
			try {
				if (!_ownCloudSyncService.isSyncRunning())
				{
					new PostDelayHandler(this).stopRunningPostDelayHandler();//Stop pending sync handler

					Bundle accBundle = new Bundle();
					accBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
					AccountManager mAccountManager = AccountManager.get(this);
					Account[] accounts = mAccountManager.getAccounts();
					for(Account acc : accounts)
						if(acc.type.equals(AccountGeneral.ACCOUNT_TYPE))
							ContentResolver.requestSync(acc, AccountGeneral.ACCOUNT_TYPE, accBundle);
					//http://stackoverflow.com/questions/5253858/why-does-contentresolver-requestsync-not-trigger-a-sync
				} else {
					UpdateButtonLayout();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    }

	public void UpdateButtonLayout()
    {
		try {
			NewsReaderListFragment newsReaderListFragment = getSlidingListFragment();
			NewsReaderDetailFragment newsReaderDetailFragment = getNewsReaderDetailFragment();

			if(_ownCloudSyncService != null) {
				IOwnCloudSyncService _Reader = _ownCloudSyncService;

				boolean isSyncRunning = _Reader.isSyncRunning();

				newsReaderListFragment.setRefreshing(isSyncRunning);
				newsReaderDetailFragment.swipeRefresh.setRefreshing(isSyncRunning);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.news_reader, menu);

		menuItemUpdater = menu.findItem(R.id.menu_update);
		menuItemDownloadMoreItems = menu.findItem(R.id.menu_downloadMoreItems);

		menuItemDownloadMoreItems.setEnabled(false);

		NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
		if(ndf != null)
			ndf.UpdateMenuItemsState();

        UpdateButtonLayout();

		return true;
	}

	public MenuItem getMenuItemDownloadMoreItems() {
		return menuItemDownloadMoreItems;
	}

	@Override
	public void onBackPressed() {
        if(handlePodcastBackPressed());
		if(drawerLayout != null) {
			if (drawerLayout.isDrawerOpen(GravityCompat.START))
				super.onBackPressed();
			else
				drawerLayout.openDrawer(GravityCompat.START);
		}
	}

	private static final int RESULT_SETTINGS = 15642;
    private static final int RESULT_ADD_NEW_FEED = 15643;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(drawerToggle != null && drawerToggle.onOptionsItemSelected(item))
			return true;
		switch (item.getItemId()) {

			case android.R.id.home:
				if(handlePodcastBackPressed());
				return true;

			case R.id.action_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivityForResult(intent, RESULT_SETTINGS);
				return true;

			case R.id.menu_update:
				startSync();
				break;

			case R.id.action_login:
				StartLoginFragment(NewsReaderListActivity.this);
				break;

			case R.id.action_add_new_feed:
				Intent newFeedIntent = new Intent(this, NewFeedActivity.class);
				startActivityForResult(newFeedIntent, RESULT_ADD_NEW_FEED);
				break;

			case R.id.menu_StartImageCaching:
				DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);

				long highestItemId = dbConn.getLowestRssItemIdUnread();
				Intent service = new Intent(this, DownloadImagesService.class);
				service.putExtra(DownloadImagesService.LAST_ITEM_ID, highestItemId);
				startService(service);

				break;

			case R.id.menu_CreateDatabaseDump:
				DatabaseUtils.CopyDatabaseToSdCard(this);

				new AlertDialog.Builder(this)
						.setMessage("Created dump at: " + DatabaseUtils.GetPath(this))
						.setNeutralButton(getString(android.R.string.ok), null)
						.show();
				break;

			case R.id.menu_About_Changelog:
				DialogFragment dialog = new VersionInfoDialogFragment();
				dialog.show(getSupportFragmentManager(), "VersionChangelogDialogFragment");
				return true;

			case R.id.menu_markAllAsRead:
				NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
				if(ndf != null)
				{
					DatabaseConnectionOrm dbConn2 = new DatabaseConnectionOrm(this);
					dbConn2.markAllItemsAsReadForCurrentView();

					reloadCountNumbersOfSlidingPaneAdapter();
					ndf.UpdateCurrentRssView(this, false);
				}
				return true;

			case R.id.menu_downloadMoreItems:
				DownloadMoreItems();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private void DownloadMoreItems()
	{
		String username = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("edt_username", "");
		String password = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("edt_password", "");

		if(username != null) {
			_Reader = new OwnCloud_Reader();
			((OwnCloud_Reader)_Reader).Start_AsyncTask_GetVersion(Constants.TaskID_GetVersion, this, onAsyncTaskGetVersionFinished, username, password);

			Toast.makeText(this, getString(R.string.toast_GettingMoreItems), Toast.LENGTH_SHORT).show();
		}
	}

	OnAsyncTaskCompletedListener onAsyncTaskGetVersionFinished = new OnAsyncTaskCompletedListener() {

		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			if(_Reader != null) {
				String appVersion = task_result.toString();
				API api = API.GetRightApiForVersion(appVersion, NewsReaderListActivity.this);
				((OwnCloud_Reader) _Reader).setApi(api);

				NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
				_Reader.Start_AsyncTask_GetOldItems(Constants.TaskID_GetItems, NewsReaderListActivity.this, onAsyncTaskComplete, ndf.getIdFeed(), ndf.getIdFolder());
			}
		}
	};

	OnAsyncTaskCompletedListener onAsyncTaskComplete = new OnAsyncTaskCompletedListener() {
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
			if(ndf != null)
				ndf.UpdateCurrentRssView(NewsReaderListActivity.this, true);

			Log.v(TAG, "Finished Download extra items..");
		}
	};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            UpdateListView();

			getSlidingListFragment().ListViewNotifyDataSetChanged();
        }

        if(requestCode == RESULT_SETTINGS)
        {
			getSlidingListFragment().ReloadAdapter();

			String oldLayout = data.getStringExtra(SettingsActivity.SP_FEED_LIST_LAYOUT);
			String newLayout = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.SP_FEED_LIST_LAYOUT,"0");

            if(ThemeChooser.ThemeRequiresRestartOfUI(this) || !newLayout.equals(oldLayout)) {
                finish();
                startActivity(getIntent());
            }
        } else if(requestCode == RESULT_ADD_NEW_FEED) {
            if(data != null) {
                boolean val = data.getBooleanExtra(NewFeedActivity.ADD_NEW_SUCCESS, false);
                if (val)
                    startSync();
            }
        }
    }

	private NewsReaderListFragment getSlidingListFragment() {
		return ((NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer));
	}

	private NewsReaderDetailFragment getNewsReaderDetailFragment() {
		 return (NewsReaderDetailFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
	}

    public static void StartLoginFragment(final FragmentActivity activity)
    {
	   	LoginDialogFragment dialog = LoginDialogFragment.getInstance();
	   	dialog.setActivity(activity);
	   	dialog.setListener(new LoginSuccessfullListener() {

            @Override
            public void LoginSucceeded() {
                ((NewsReaderListActivity) activity).startSync();
            }
        });
	    dialog.show(activity.getSupportFragmentManager(), "NoticeDialogFragment");
    }


	private void UpdateListView()
    {
        getNewsReaderDetailFragment().notifyDataSetChangedOnAdapter();
    }
	@Override
	public void onClick(ViewHolder vh, int position) {

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (mPrefs.getBoolean(SettingsActivity.CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING, false)) {
            String currentUrl = vh.toString();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
            startActivity(browserIntent);

            vh.setReadState(true);
		} else {
			Intent intentNewsDetailAct = new Intent(this, NewsDetailActivity.class);

			intentNewsDetailAct.putExtra(NewsReaderListActivity.ITEM_ID, position);
			intentNewsDetailAct.putExtra(NewsReaderListActivity.TITEL, getNewsReaderDetailFragment().getTitel());
			startActivityForResult(intentNewsDetailAct, Activity.RESULT_CANCELED);
		}
	}
}
