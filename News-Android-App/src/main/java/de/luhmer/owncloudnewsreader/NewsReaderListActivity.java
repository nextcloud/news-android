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
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SlidingPaneLayout.PanelSlideListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.LoginDialogFragment.LoginSuccessfullListener;
import de.luhmer.owncloudnewsreader.adapter.NewsListArrayAdapter;
import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.events.podcast.FeedPanelSlideEvent;
import de.luhmer.owncloudnewsreader.helper.DatabaseUtils;
import de.luhmer.owncloudnewsreader.helper.ImageHandler;
import de.luhmer.owncloudnewsreader.helper.ShowcaseDimHelper;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.OnAsyncTaskCompletedListener;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;
import de.luhmer.owncloudnewsreader.services.DownloadImagesService;
import de.luhmer.owncloudnewsreader.services.IOwnCloudSyncService;

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
		 NewsReaderListFragment.Callbacks {

	private static final String TAG = NewsReaderListActivity.class.getCanonicalName();

	public static final String FOLDER_ID = "FOLDER_ID";
	public static final String FEED_ID = "FEED_ID";
	public static final String ITEM_ID = "ITEM_ID";
	public static final String TITEL = "TITEL";
	private static MenuItem menuItemUpdater;
	private static MenuItem menuItemDownloadMoreItems;
	private static IReader _Reader;

    @InjectView(R.id.toolbar) Toolbar toolbar;
	@InjectView(R.id.sliding_pane) SlidingPaneLayout mSlidingLayout;

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


        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
        				.replace(R.id.left_drawer, new NewsReaderListFragment())
                   		.commit();

        mSlidingLayout.setParallaxDistance(280);
        mSlidingLayout.setSliderFadeColor(getResources().getColor(android.R.color.transparent));
        mSlidingLayout.setPanelSlideListener(panelSlideListener);
        mSlidingLayout.openPane();

        if(savedInstanceState == null)//When the app starts (no orientation change)
        {
        	startDetailFHolder = new StartDetailFragmentHolder(SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue(), true, null, true);
        	StartDetailFragmentNow();
        }

        ImageHandler.createNoMediaFile(this);
        //AppRater.app_launched(this);
        //AppRater.rateNow(this);

		UpdateButtonLayout();
    }


    public void showShowCaseViewForView(final View dimView, View targetView, String title, String text) {
        ShowcaseDimHelper.dimView(dimView);

        ViewTarget target = new ViewTarget(targetView);
        ShowcaseView sv = new ShowcaseView.Builder(this)
                .setTarget(target)
                .setContentTitle(title)
                .setContentText(text)
                .hideOnTouchOutside()
                .build();

        sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {
            @Override
            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                ShowcaseDimHelper.undoDimView(dimView);
            }

            @Override
            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

            }

            @Override
            public void onShowcaseViewShow(ShowcaseView showcaseView) {

            }
        });

        sv.show();
    }


    private static final String SLIDING_PANE_OPEN_BOOLEAN = "SLIDING_PANE_OPEN_BOOLEAN";
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
        safeInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

    public NewsReaderDetailFragment getNewsReaderDetailFragment() {
        return ((NewsReaderDetailFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame));
    }



	private PanelSlideListener panelSlideListener = new PanelSlideListener() {
		@Override
		public void onPanelSlide(View arg0, float arg1) {
		}

		@Override
		public void onPanelOpened(View arg0) {
			togglePodcastVideoViewAnimation();

			reloadCountNumbersOfSlidingPaneAdapter();

			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
			getSupportActionBar().setHomeButtonEnabled(false);

			menuItemUpdater.setVisible(false);
		}

		@Override
		public void onPanelClosed(View arg0) {
			togglePodcastVideoViewAnimation();

			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);

			menuItemUpdater.setVisible(true);

			StartDetailFragmentNow();

			EventBus.getDefault().post(new FeedPanelSlideEvent(false));
		}
	};


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
        outState.putBoolean(SLIDING_PANE_OPEN_BOOLEAN, mSlidingLayout.isOpen());

        NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
        if(ndf != null) {
            View v = ndf.getListView().getChildAt(0);
            int top = (v == null) ? 0 : v.getTop();

            outState.putInt(FIRST_VISIBLE_DETAIL_ITEM_STRING, ndf.getListView().getFirstVisiblePosition());
            outState.putInt(FIRST_VISIBLE_DETAIL_ITEM_MARGIN_TOP_STRING, top);
            outState.putLong(OPTIONAL_FOLDER_ID, ndf.getIdFeed() == null ? ndf.getIdFolder() : ndf.getIdFeed());
            outState.putBoolean(IS_FOLDER_BOOLEAN, ndf.getIdFeed() == null);
            outState.putLong(ID_FEED_STRING, ndf.getIdFeed() != null ? ndf.getIdFeed() : ndf.getIdFolder());
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        boolean isOpen = savedInstanceState.getBoolean(SLIDING_PANE_OPEN_BOOLEAN);
        getSupportActionBar().setDisplayHomeAsUpEnabled(!isOpen);
        getSupportActionBar().setHomeButtonEnabled(!isOpen);


        if(savedInstanceState.containsKey(FIRST_VISIBLE_DETAIL_ITEM_STRING) &&
                savedInstanceState.containsKey(ID_FEED_STRING) &&
                savedInstanceState.containsKey(IS_FOLDER_BOOLEAN) &&
                savedInstanceState.containsKey(OPTIONAL_FOLDER_ID)) {


            startDetailFHolder = new StartDetailFragmentHolder(savedInstanceState.getLong(OPTIONAL_FOLDER_ID),
                    savedInstanceState.getBoolean(IS_FOLDER_BOOLEAN),
                    savedInstanceState.getLong(ID_FEED_STRING),
                    false);

            NewsReaderDetailFragment ndf = StartDetailFragmentNow();
            if(ndf != null) {
                ndf.setActivatedPosition(savedInstanceState.getInt(FIRST_VISIBLE_DETAIL_ITEM_STRING));
                ndf.setMarginFromTop(savedInstanceState.getInt(FIRST_VISIBLE_DETAIL_ITEM_MARGIN_TOP_STRING));
            }
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

	public void reloadCountNumbersOfSlidingPaneAdapter() {
        NewsReaderListFragment nlf = getSlidingListFragment();
        if(nlf != null) {
            nlf.ListViewNotifyDataSetChanged();
        }
    }



	@Override
	protected void onResume() {
		ThemeChooser.chooseTheme(this);

		reloadCountNumbersOfSlidingPaneAdapter();

		invalidateOptionsMenu();

		super.onResume();
	}

	public boolean shouldDrawerStayOpen() {
        return getResources().getBoolean(R.bool.two_pane);
    }

	private StartDetailFragmentHolder startDetailFHolder = null;

	private class StartDetailFragmentHolder {
		long idFeed;
		boolean isFolder;
        Long optional_folder_id;
        boolean updateListView;

		public StartDetailFragmentHolder(long idFeed, boolean isFolder, Long optional_folder_id, boolean updateListView) {
			this.idFeed = idFeed;
			this.isFolder = isFolder;
			this.optional_folder_id = optional_folder_id;
            this.updateListView = updateListView;
		}

		public NewsReaderDetailFragment StartDetailFragment() {
			return NewsReaderListActivity.this.StartDetailFragment(idFeed, isFolder, optional_folder_id, updateListView);
		}
	}

	/**
	 * Callback method from {@link NewsReaderListFragment.Callbacks} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public void onTopItemClicked(long idFeed, boolean isFolder, Long optional_folder_id) {
		if(!shouldDrawerStayOpen())
			mSlidingLayout.closePane();

		startDetailFHolder = new StartDetailFragmentHolder(idFeed, isFolder, optional_folder_id, true);

		if(shouldDrawerStayOpen())
			StartDetailFragmentNow();
	}

	@Override
	public void onChildItemClicked(long idFeed, Long optional_folder_id) {
		if(!shouldDrawerStayOpen())
			mSlidingLayout.closePane();

		//StartDetailFragment(idSubscription, false, optional_folder_id);
		startDetailFHolder = new StartDetailFragmentHolder(idFeed, false, optional_folder_id, true);
		if(shouldDrawerStayOpen())
			StartDetailFragmentNow();
	}

	private NewsReaderDetailFragment StartDetailFragment(long id, Boolean folder, Long optional_folder_id, boolean updateListView)
	{
		if(menuItemDownloadMoreItems != null) {
			menuItemDownloadMoreItems.setEnabled(true);
		}

		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getApplicationContext());


		Intent intent = new Intent();

		if(!folder)
		{
			intent.putExtra(FEED_ID, id);
			intent.putExtra(FOLDER_ID, optional_folder_id);
			intent.putExtra(TITEL, dbConn.getFeedById(id).getFeedTitle());
		}
		else
		{
			intent.putExtra(FOLDER_ID, id);
			int idFolder = (int) id;
			if(idFolder >= 0)
				intent.putExtra(TITEL, dbConn.getFolderById(id).getLabel());
			else if(idFolder == -10)
				intent.putExtra(TITEL, getString(R.string.allUnreadFeeds));
			else if(idFolder == -11)
				intent.putExtra(TITEL, getString(R.string.starredFeeds));
		}

		Bundle arguments = intent.getExtras();

		NewsReaderDetailFragment fragment = new NewsReaderDetailFragment();
        fragment.setUpdateListViewOnStartUp(updateListView);

		fragment.setArguments(arguments);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment)
				.commit();

		return fragment;
	}


    public void UpdateItemList()
    {
        try {
            NewsReaderDetailFragment nrD = getDetailFragment();
            if (nrD != null)
                ((NewsListArrayAdapter) nrD.getListAdapter()).notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    void startSync()
    {
		getSlidingListFragment().StartSync();
    }

	public void UpdateButtonLayout()
    {
        if(menuItemUpdater != null)
        {
            try {
                NewsReaderListFragment ndf = getSlidingListFragment();
                SwipeRefreshLayout pullToRefreshView = null;

				if(ndf != null) //If the Fragment is not instantiated yet.
					pullToRefreshView = ndf.mPullToRefreshLayout;

                if(ndf._ownCloudSyncService != null) {
                    IOwnCloudSyncService _Reader = ndf._ownCloudSyncService;

					if(_Reader.isSyncRunning())
					{
						menuItemUpdater.setActionView(R.layout.inderterminate_progress);

					    if(pullToRefreshView != null && !pullToRefreshView.isRefreshing()) {
					    	pullToRefreshView.setRefreshing(true);
                            Log.v(TAG, "Start ptr refreshing");
                        }
					}
					else
					{
					    menuItemUpdater.setActionView(null);

					    if(pullToRefreshView != null) {
                            pullToRefreshView.setRefreshing(false);
                            Log.v(TAG, "Finished ptr refreshing");
                        }
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
		getMenuInflater().inflate(R.menu.news_reader, menu);

		menuItemUpdater = menu.findItem(R.id.menu_update);
		menuItemDownloadMoreItems = menu.findItem(R.id.menu_downloadMoreItems);

		menuItemDownloadMoreItems.setEnabled(false);

		NewsReaderDetailFragment ndf = getDetailFragment();
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
        else if(mSlidingLayout.isOpen())
			super.onBackPressed();
		else
			mSlidingLayout.openPane();
	}

	private static final int RESULT_SETTINGS = 15642;
    private static final int RESULT_ADD_NEW_FEED = 15643;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case android.R.id.home:
				if(handlePodcastBackPressed());
				else if(!mSlidingLayout.isOpen())
					mSlidingLayout.openPane();
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
				NewsReaderDetailFragment ndf = getDetailFragment();
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

				NewsReaderDetailFragment ndf = getDetailFragment();
				_Reader.Start_AsyncTask_GetOldItems(Constants.TaskID_GetItems, NewsReaderListActivity.this, onAsyncTaskComplete, ndf.getIdFeed(), ndf.getIdFolder());
			}
		}
	};

	OnAsyncTaskCompletedListener onAsyncTaskComplete = new OnAsyncTaskCompletedListener() {
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			NewsReaderDetailFragment ndf = getDetailFragment();
			if(ndf != null)
				ndf.UpdateCurrentRssView(NewsReaderListActivity.this, true);

			Log.v(TAG, "Finished Download extra items..");
		}
	};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            UpdateListView(this);

			getSlidingListFragment().ListViewNotifyDataSetChanged();
        }

        if(requestCode == RESULT_SETTINGS)
        {
			getSlidingListFragment().ReloadAdapter();

            if(ThemeChooser.ThemeRequiresRestartOfUI(this)) {
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

	private NewsReaderDetailFragment getDetailFragment() {
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


	public static void UpdateListView(FragmentActivity act)
    {
        ((NewsReaderDetailFragment) act.getSupportFragmentManager().findFragmentById(R.id.content_frame)).notifyDataSetChangedOnAdapter();
    }
}
