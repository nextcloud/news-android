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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.SSOException;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.LoginDialogFragment.LoginSuccessfulListener;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.RecyclerItemClickListener;
import de.luhmer.owncloudnewsreader.adapter.ViewHolder;
import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.events.podcast.FeedPanelSlideEvent;
import de.luhmer.owncloudnewsreader.helper.DatabaseUtils;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.reader.nextcloud.RssItemObservable;
import de.luhmer.owncloudnewsreader.services.DownloadImagesService;
import de.luhmer.owncloudnewsreader.services.DownloadWebPageService;
import de.luhmer.owncloudnewsreader.services.OwnCloudSyncService;
import de.luhmer.owncloudnewsreader.services.events.SyncFailedEvent;
import de.luhmer.owncloudnewsreader.services.events.SyncFinishedEvent;
import de.luhmer.owncloudnewsreader.services.events.SyncStartedEvent;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static de.luhmer.owncloudnewsreader.LoginDialogFragment.ShowAlertDialog;

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
		 NewsReaderListFragment.Callbacks, RecyclerItemClickListener, SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {

	private static final String TAG = NewsReaderListActivity.class.getCanonicalName();

	public static final String FOLDER_ID = "FOLDER_ID";
	public static final String FEED_ID = "FEED_ID";
	public static final String ITEM_ID = "ITEM_ID";
	public static final String TITEL = "TITEL";

	private static MenuItem menuItemUpdater;
	private static MenuItem menuItemDownloadMoreItems;

	//private Date mLastSyncDate = new Date(0);
	private boolean mSyncOnStartupPerformed = false;

	protected @BindView(R.id.toolbar) Toolbar toolbar;

	//private ServiceConnection mConnection = null;

	@VisibleForTesting @Nullable @BindView(R.id.drawer_layout) public DrawerLayout drawerLayout;

	private ActionBarDrawerToggle drawerToggle;
	private SearchView searchView;

    private PublishSubject<String> searchPublishSubject;
    private static final int REQUEST_CODE_PERMISSION_DOWNLOAD_WEB_ARCHIVE = 1;

    private static final String ID_FEED_STRING = "ID_FEED_STRING";
    private static final String IS_FOLDER_BOOLEAN = "IS_FOLDER_BOOLEAN";
    private static final String OPTIONAL_FOLDER_ID = "OPTIONAL_FOLDER_ID";
    private static final String LIST_ADAPTER_TOTAL_COUNT = "LIST_ADAPTER_TOTAL_COUNT";
    private static final String LIST_ADAPTER_PAGE_COUNT = "LIST_ADAPTER_PAGE_COUNT";


    @Override
	protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences defaultValueSp = getSharedPreferences(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);
        if(!defaultValueSp.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
            PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, true);
            PreferenceManager.setDefaultValues(this, R.xml.pref_display, true);
            PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
            PreferenceManager.setDefaultValues(this, R.xml.pref_notification, true);
        }

        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newsreader);

		ButterKnife.bind(this);

		if (toolbar != null) {
			setSupportActionBar(toolbar);
		}

		initAccountManager();

		//Init config --> if nothing is configured start the login dialog.
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) == null) {
			StartLoginFragment(NewsReaderListActivity.this);
		}


		Bundle args = new Bundle();
		String userName = mPrefs.getString(SettingsActivity.EDT_USERNAME_STRING, null);
		String url = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null);
		args.putString("accountName", String.format("%s\n%s", userName, url));
		NewsReaderListFragment newsReaderListFragment = new NewsReaderListFragment();
		newsReaderListFragment.setArguments(args);
		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.left_drawer, newsReaderListFragment)
				.commit();

		if (drawerLayout != null) {
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

					showTapLogoToSyncShowcaseView();
				}
			};

			drawerLayout.addDrawerListener(drawerToggle);

            adjustEdgeSizeOfDrawer();
		}
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		if (drawerToggle != null) {
			drawerToggle.syncState();
		}

		if (savedInstanceState == null) {//When the app starts (no orientation change)
			startDetailFragment(SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue(), true, null, true);
		}

		//AppRater.app_launched(this);
		//AppRater.rateNow(this);

        //Start auto sync if enabled
        if (mPrefs.getBoolean(SettingsActivity.CB_SYNCONSTARTUP_STRING, false)) {
            startSync();
        }

		updateButtonLayout();
	}

    /**
     * This method increases the "pull to open drawer" area by three.
     * This method should be called only once!
     */
	private void adjustEdgeSizeOfDrawer() {
        try {
            // increase the size of the drag margin to prevent starting a star swipe when
            // trying to open the drawer.
            Field mDragger = drawerLayout.getClass().getDeclaredField("mLeftDragger");
            mDragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(drawerLayout);
            Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            int edge = mEdgeSize.getInt(draggerObj);
            mEdgeSize.setInt(draggerObj, edge * 3);
        } catch (Exception e) {
            Log.e(TAG, "Setting edge width of drawer failed..", e);
        }
	}

    public int getEdgeSizeOfDrawer() {
        try {
            Field mDragger = drawerLayout.getClass().getDeclaredField("mLeftDragger");
            mDragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(drawerLayout);
            Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            return mEdgeSize.getInt(draggerObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get edge size of drawer", e);
        }
        return 0;
    }


    private void showTapLogoToSyncShowcaseView() {
		getSlidingListFragment().showTapLogoToSyncShowcaseView();
	}

	private View.OnClickListener mSnackbarListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			//Toast.makeText(getActivity(), "button 1 pressed", 3000).show();
			updateCurrentRssView();
		}
	};


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
		if (!isAccountThere) {
			//Then add the new account
			Account account = new Account(getString(R.string.app_name), AccountGeneral.ACCOUNT_TYPE);
			mAccountManager.addAccountExplicitly(account, "", new Bundle());

			SyncIntervalSelectorActivity.SetAccountSyncInterval(this);
		}
	}


	private void safeInstanceState(Bundle outState) {
		NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
		if (ndf != null) {
			outState.putLong(OPTIONAL_FOLDER_ID, ndf.getIdFeed() == null ? ndf.getIdFolder() : ndf.getIdFeed());
			outState.putBoolean(IS_FOLDER_BOOLEAN, ndf.getIdFeed() == null);
			outState.putLong(ID_FEED_STRING, ndf.getIdFeed() != null ? ndf.getIdFeed() : ndf.getIdFolder());

			NewsListRecyclerAdapter adapter = (NewsListRecyclerAdapter) ndf.getRecyclerView().getAdapter();
			if (adapter != null) {
				outState.putInt(LIST_ADAPTER_TOTAL_COUNT, adapter.getTotalItemCount());
				outState.putInt(LIST_ADAPTER_PAGE_COUNT, adapter.getCachedPages());
			}
		}
	}

	private void restoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(ID_FEED_STRING) &&
				savedInstanceState.containsKey(IS_FOLDER_BOOLEAN) &&
				savedInstanceState.containsKey(OPTIONAL_FOLDER_ID)) {


			NewsListRecyclerAdapter adapter = new NewsListRecyclerAdapter(this, getNewsReaderDetailFragment().recyclerView, this);

			adapter.setTotalItemCount(savedInstanceState.getInt(LIST_ADAPTER_TOTAL_COUNT));
			adapter.setCachedPages(savedInstanceState.getInt(LIST_ADAPTER_PAGE_COUNT));

			getNewsReaderDetailFragment()
					.getRecyclerView()
					.setAdapter(adapter);

			startDetailFragment(savedInstanceState.getLong(OPTIONAL_FOLDER_ID),
					savedInstanceState.getBoolean(IS_FOLDER_BOOLEAN),
					savedInstanceState.getLong(ID_FEED_STRING),
					false);
		}
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		restoreInstanceState(savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (drawerToggle != null)
			drawerToggle.syncState();

		boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
		if (tabletSize) {
			showTapLogoToSyncShowcaseView();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (drawerToggle != null)
			drawerToggle.onConfigurationChanged(newConfig);
	}

	public void reloadCountNumbersOfSlidingPaneAdapter() {
		NewsReaderListFragment nlf = getSlidingListFragment();
		if (nlf != null) {
			nlf.ListViewNotifyDataSetChanged();
		}
	}

	protected void updateCurrentRssView() {
		NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
		if (ndf != null) {
			//ndf.reloadAdapterFromScratch();
			ndf.updateCurrentRssView(NewsReaderListActivity.this);
		}
	}

	public void switchToAllUnreadItemsFolder() {
		startDetailFragment(SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue(), true, null, true);
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SyncFailedEvent event) {
	    Throwable exception = event.exception();
		if(exception instanceof SSOException){
			if(exception instanceof NextcloudHttpRequestFailedException && ((NextcloudHttpRequestFailedException) exception).getStatusCode() == 302) {
				ShowAlertDialog(
						getString(R.string.login_dialog_title_error),
						getString(R.string.login_dialog_text_news_app_not_installed_on_server,
								"https://github.com/nextcloud/news/blob/master/docs/install.md#installing-from-the-app-store"),
						this);
			} else {
				UiExceptionManager.showDialogForException(this, (SSOException) exception);
				//UiExceptionManager.showNotificationForException(this, (SSOException) exception);
			}
		} else {
            Toast.makeText(NewsReaderListActivity.this, exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        updateButtonLayout();
        syncFinishedHandler();
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SyncStartedEvent event) {
        updateButtonLayout();
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SyncFinishedEvent event) {
        updateButtonLayout();
        syncFinishedHandler();
	}

	/**
	 * @return true if new items count was greater than 0
	 */
	private boolean syncFinishedHandler() {
		ShowcaseConfig config = new ShowcaseConfig();
		config.setDelay(300); // half second between each showcase view
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "SWIPE_LEFT_RIGHT_AND_PTR");
		sequence.setConfig(config);
		sequence.addSequenceItem(getNewsReaderDetailFragment().pbLoading,
                "Pull-to-Refresh to sync with server", "GOT IT");
		sequence.addSequenceItem(getNewsReaderDetailFragment().pbLoading,
                "Swipe Left/Right to mark article as read", "GOT IT");
		sequence.start();

		NewsReaderListFragment newsReaderListFragment = getSlidingListFragment();
		newsReaderListFragment.reloadAdapter();
		UpdateItemList();
		updatePodcastView();

		getSlidingListFragment().startAsyncTaskGetUserInfo();

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(NewsReaderListActivity.this);
		int newItemsCount = mPrefs.getInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 0);

		if (newItemsCount > 0) {
			int firstVisiblePosition = getNewsReaderDetailFragment().getFirstVisibleScrollPosition();

			//Only show the update snackbar if scrollposition is not top.
			// 0 if scrolled all the way up
			// 1 if no items are visible right now (e.g. first sync)
			if (firstVisiblePosition == 0 || firstVisiblePosition == -1) {
				updateCurrentRssView();
			} else {
				Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout),
						getResources().getQuantityString(R.plurals.message_bar_new_articles_available, newItemsCount, newItemsCount),
						Snackbar.LENGTH_LONG);
				snackbar.setAction(getString(R.string.message_bar_reload), mSnackbarListener);
				snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.accent_material_dark));
				// Setting android:TextColor to #000 in the light theme results in black on black
				// text on the Snackbar, set the text back to white,
				// TODO: find a cleaner way to do this
				TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
				textView.setTextColor(Color.WHITE);
				snackbar.show();
			}
			return true;
		}
		return false;
	}


	@Override
	protected void onResume() {
		NewsReaderListFragment newsReaderListFragment = getSlidingListFragment();
		if (newsReaderListFragment != null) {
            newsReaderListFragment.reloadAdapter();
			newsReaderListFragment.bindUserInfoToUI();
		}
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
		if (drawerLayout != null)
			drawerLayout.closeDrawer(GravityCompat.START);

		startDetailFragment(idFeed, isFolder, optional_folder_id, true);
	}

	@Override
	public void onChildItemClicked(long idFeed, Long optional_folder_id) {
		if (drawerLayout != null)
			drawerLayout.closeDrawer(GravityCompat.START);

		startDetailFragment(idFeed, false, optional_folder_id, true);
	}

	@Override
	public void onTopItemLongClicked(long idFeed, boolean isFolder) {
		startDialogFragment(idFeed, isFolder);
	}

	@Override
	public void onChildItemLongClicked(long idFeed) {
		startDialogFragment(idFeed, false);
	}


	private void startDialogFragment(long idFeed, Boolean isFolder) {
		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getApplicationContext());

		if (isFolder) {
            /*
			if(idFeed >= 0) {
				//currently no actions for folders
				//String titel = dbConn.getFolderById(idFeed).getLabel();
			}*/
		} else {
			String titel = dbConn.getFeedById(idFeed).getFeedTitle();
			String iconurl = dbConn.getFeedById(idFeed).getFaviconUrl();
			String feedurl = dbConn.getFeedById(idFeed).getLink();

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag("news_reader_list_dialog");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			NewsReaderListDialogFragment fragment = NewsReaderListDialogFragment.newInstance(idFeed, titel, iconurl, feedurl);
			fragment.setActivity(this);
			fragment.show(ft, "news_reader_list_dialog");
		}
	}


	private NewsReaderDetailFragment startDetailFragment(long id, Boolean folder, Long optional_folder_id, boolean updateListView)
	{
		if(menuItemDownloadMoreItems != null) {
			menuItemDownloadMoreItems.setEnabled(true);
		}

		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getApplicationContext());

		Long feedId = null;
		Long folderId;
		String title = null;

		if(!folder)
		{
			feedId = id;
			folderId = optional_folder_id;
			title = dbConn.getFeedById(id).getFeedTitle();
		}
		else
		{
			folderId = id;
			int idFolder = (int) id;
			if(idFolder >= 0)
				title = dbConn.getFolderById(id).getLabel();
			else if(idFolder == -10)
				title = getString(R.string.allUnreadFeeds);
			else if(idFolder == -11)
				title = getString(R.string.starredFeeds);

		}

		NewsReaderDetailFragment fragment = getNewsReaderDetailFragment();
		fragment.setData(feedId, folderId, title, updateListView);
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


    public void startSync()
    {
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		if(mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) == null) {
			StartLoginFragment(this);
		} else {
			if (!OwnCloudSyncService.isSyncRunning()) {
				new PostDelayHandler(this).stopRunningPostDelayHandler();//Stop pending sync handler

				Bundle accBundle = new Bundle();
				accBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
				AccountManager mAccountManager = AccountManager.get(this);
				Account[] accounts = mAccountManager.getAccounts();
				for(Account acc : accounts) {
                    if (acc.type.equals(AccountGeneral.ACCOUNT_TYPE)) {
                        ContentResolver.requestSync(acc, AccountGeneral.ACCOUNT_TYPE, accBundle);
                    }
                }
				//http://stackoverflow.com/questions/5253858/why-does-contentresolver-requestsync-not-trigger-a-sync
			} else {
				updateButtonLayout();
			}
		}
    }

	public void updateButtonLayout()
    {
		NewsReaderListFragment newsReaderListFragment = getSlidingListFragment();
		NewsReaderDetailFragment newsReaderDetailFragment = getNewsReaderDetailFragment();

		if(newsReaderListFragment != null && newsReaderDetailFragment != null) {
			boolean isSyncRunning = OwnCloudSyncService.isSyncRunning();
			newsReaderListFragment.setRefreshing(isSyncRunning);
			newsReaderDetailFragment.swipeRefresh.setRefreshing(isSyncRunning);
		}
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.news_reader, menu);

		menuItemUpdater = menu.findItem(R.id.menu_update);
		menuItemDownloadMoreItems = menu.findItem(R.id.menu_downloadMoreItems);

		menuItemDownloadMoreItems.setEnabled(false);

		MenuItem searchItem = menu.findItem(R.id.menu_search);

		//Set expand listener to close keyboard
		searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				clearSearchViewFocus();
				return true;
			}
		});

		searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		searchView.setIconifiedByDefault(false);
		searchView.setOnQueryTextListener(this);
		searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    clearSearchViewFocus();
                }
            }
        });

		NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
		if(ndf != null)
			ndf.updateMenuItemsState();

        updateButtonLayout();

		return true;
	}

	public MenuItem getMenuItemDownloadMoreItems() {
		return menuItemDownloadMoreItems;
	}

	@Override
	public void onBackPressed() {
        if(!handlePodcastBackPressed()) {
			if (drawerLayout != null) {
				if (drawerLayout.isDrawerOpen(GravityCompat.START))
					super.onBackPressed();
				else
					drawerLayout.openDrawer(GravityCompat.START);
			} else {
				super.onBackPressed();
			}
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
				if(handlePodcastBackPressed())
					return true;
				break;

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
				final DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);

				long highestItemId = dbConn.getLowestRssItemIdUnread();


				Intent data = new Intent();
				data.putExtra(DownloadImagesService.LAST_ITEM_ID, highestItemId);
				data.putExtra(DownloadImagesService.DOWNLOAD_MODE_STRING, DownloadImagesService.DownloadMode.PICTURES_ONLY);
				DownloadImagesService.enqueueWork(this, data);

				break;

			case R.id.menu_CreateDatabaseDump:
				DatabaseUtils.CopyDatabaseToSdCard(this);

				new AlertDialog.Builder(this)
						.setMessage("Created dump at: " + DatabaseUtils.GetPath(this))
						.setNeutralButton(getString(android.R.string.ok), null)
						.show();
				break;

			case R.id.menu_markAllAsRead:
				NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
				if(ndf != null) {
					DatabaseConnectionOrm dbConn2 = new DatabaseConnectionOrm(this);
					dbConn2.markAllItemsAsReadForCurrentView();

					reloadCountNumbersOfSlidingPaneAdapter();
					ndf.refreshCurrentRssView();
				}
				return true;

			case R.id.menu_downloadMoreItems:
				DownloadMoreItems();
				return true;

			case R.id.menu_search:
				searchView.setIconified(false);
				searchView.setFocusable(true);
				searchView.requestFocusFromTouch();
                return true;

            case R.id.menu_download_web_archive:
                checkAndStartDownloadWebPagesForOfflineReadingPermission();
                return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void checkAndStartDownloadWebPagesForOfflineReadingPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("Permission error","You have permission");
                startDownloadWebPagesForOfflineReading();
            } else {
                Log.e("Permission error","Asking for permission");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.FOREGROUND_SERVICE}, REQUEST_CODE_PERMISSION_DOWNLOAD_WEB_ARCHIVE);
                return;
            }
        } else { //you dont need to worry about these stuff below api level 23
            Log.v("Permission error","You already have the permission");
            startDownloadWebPagesForOfflineReading();
        }
    }

	private void startDownloadWebPagesForOfflineReading() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, DownloadWebPageService.class));
        } else {
            startService(new Intent(this, DownloadWebPageService.class));
        }
    }

	private void DownloadMoreItems()
	{
		String username = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("edt_username", null);

		if(username != null) {
			final NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();

			// Folder is selected.. download more items for all feeds in this folder
			if(ndf.getIdFeed() == null) {
				Long idFolder = ndf.getIdFolder();
				DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);
				for(Feed feed : dbConn.getFolderById(idFolder).getFeedList()) {
					downloadMoreItemsForFeed(feed.getId());
				}
			} else {
				// Single feed is selected.. download more items
				downloadMoreItemsForFeed(ndf.getIdFeed());
			}

			Toast.makeText(this, getString(R.string.toast_GettingMoreItems), Toast.LENGTH_SHORT).show();
		}
	}

	private void downloadMoreItemsForFeed(final Long feedId) {
		Completable.fromAction(new Action() {
			@Override
			public void run() throws Exception {
				DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(NewsReaderListActivity.this);
				RssItem rssItem = dbConn.getLowestRssItemIdByFeed(feedId);
				long offset = rssItem.getId();
				long id = rssItem.getFeedId();
				int type = 0; // the type of the query (Feed: 0, Folder: 1, Starred: 2, All: 3)

				List<RssItem> buffer = mApi.getAPI().items(100, offset, type, id, true, false).execute().body();
				RssItemObservable.performDatabaseBatchInsert(dbConn, buffer);
			}
		})
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action() {
					@Override
					public void run() throws Exception {
						updateCurrentRssView();
						Log.v(TAG, "Finished Download extra items..");
					}
				}, new Consumer<Throwable>() {
					@Override
					public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
						throwable.printStackTrace();
						Throwable e = OkHttpSSLClient.HandleExceptions(throwable);
						Toast.makeText(NewsReaderListActivity.this, getString(R.string.login_dialog_text_something_went_wrong) + " - " + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				})
				.dispose();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == RESULT_OK) {
			UpdateListView();
			getSlidingListFragment().ListViewNotifyDataSetChanged();
        }

        if(requestCode == RESULT_SETTINGS)
        {
            //Update settings of image Loader
            mApi.initApi(new NextcloudAPI.ApiConnectedListener() {
                @Override
                public void onConnected() {
                    ensureCorrectTheme(data);
                }

                @Override
                public void onError(Exception ex) {
                    ensureCorrectTheme(data);
                    ex.printStackTrace();
                }
            });

        } else if(requestCode == RESULT_ADD_NEW_FEED) {
            if(data != null) {
                boolean val = data.getBooleanExtra(NewFeedActivity.ADD_NEW_SUCCESS, false);
                if (val) {
                    startSync();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(requestCode == REQUEST_CODE_PERMISSION_DOWNLOAD_WEB_ARCHIVE) {
                startDownloadWebPagesForOfflineReading();
            } else {
                Log.d(TAG, "No action defined here yet..");
            }
        }
    }

    private void ensureCorrectTheme(Intent data) {
	    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String oldListLayout = data.getStringExtra(SettingsActivity.SP_FEED_LIST_LAYOUT);
        String newListLayout = mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT,"0");

        if (ThemeChooser.getInstance(NewsReaderListActivity.this).themeRequiresRestartOfUI(NewsReaderListActivity.this) || !newListLayout.equals(oldListLayout)) {
            NewsReaderListActivity.this.recreate();
        } else if (data.hasExtra(SettingsActivity.CACHE_CLEARED)) {
            resetUiAndStartSync();
        }
    }

    @VisibleForTesting
	public NewsReaderListFragment getSlidingListFragment() {
		return ((NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer));
	}

    @VisibleForTesting
    public NewsReaderDetailFragment getNewsReaderDetailFragment() {
		 return (NewsReaderDetailFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
	}

    public static void StartLoginFragment(final FragmentActivity activity)
    {
	   	LoginDialogFragment dialog = LoginDialogFragment.getInstance();
	   	dialog.setActivity(activity);
	   	dialog.setListener(new LoginSuccessfulListener() {
            @Override
            public void loginSucceeded() {
                ((NewsReaderListActivity) activity).resetUiAndStartSync();
			}
		});
	    dialog.show(activity.getSupportFragmentManager(), "NoticeDialogFragment");
    }

    private void resetUiAndStartSync() {
        getSlidingListFragment().reloadAdapter();
        updateCurrentRssView();
        startSync();
        getSlidingListFragment().bindUserInfoToUI();
    }

	private void UpdateListView()
    {
        getNewsReaderDetailFragment().notifyDataSetChangedOnAdapter();
    }

    @Override
	public void onClick(ViewHolder vh, int position) {

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (mPrefs.getBoolean(SettingsActivity.CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING, false)) {
            String currentUrl = vh.getRssItem().getLink();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
            startActivity(browserIntent);

            ((NewsListRecyclerAdapter) getNewsReaderDetailFragment().getRecyclerView().getAdapter()).changeReadStateOfItem(vh, true);
		} else {
			Intent intentNewsDetailAct = new Intent(this, NewsDetailActivity.class);

			intentNewsDetailAct.putExtra(NewsReaderListActivity.ITEM_ID, position);
			intentNewsDetailAct.putExtra(NewsReaderListActivity.TITEL, getNewsReaderDetailFragment().getTitel());
			startActivityForResult(intentNewsDetailAct, Activity.RESULT_CANCELED);
		}
	}

	@Override
	public boolean onLongClick(ViewHolder vh, int position) {
        RssItem rssItem = vh.getRssItem();
        DialogFragment newFragment =
                NewsDetailImageDialogFragment.newInstanceUrl(rssItem.getTitle(), rssItem.getLink());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("menu_fragment_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        newFragment.show(ft, "menu_fragment_dialog");
        return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		clearSearchViewFocus();
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
        if (searchPublishSubject == null) {
            searchPublishSubject = PublishSubject.create();
            searchPublishSubject
                    .debounce(400, TimeUnit.MILLISECONDS)
                    .distinctUntilChanged()
                    .map(new Function<String, List<RssItem>>() {

                        @Override
                        public List<RssItem> apply(String s) throws Exception {
                            return getNewsReaderDetailFragment().performSearch(s);
                        }

                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(getNewsReaderDetailFragment().SearchResultObserver)
                    .isDisposed();

        }
        searchPublishSubject.onNext(newText);
        return true;
    }

    public void clearSearchViewFocus() {
        searchView.clearFocus();
    }
}
