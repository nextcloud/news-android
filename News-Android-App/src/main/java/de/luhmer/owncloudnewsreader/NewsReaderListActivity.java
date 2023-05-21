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

package de.luhmer.owncloudnewsreader;

import static androidx.annotation.VisibleForTesting.PROTECTED;
import static de.luhmer.owncloudnewsreader.LoginDialogActivity.RESULT_LOGIN;
import static de.luhmer.owncloudnewsreader.LoginDialogActivity.ShowAlertDialog;
import static de.luhmer.owncloudnewsreader.SettingsActivity.PREF_SERVER_SETTINGS;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;
import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountPermissionNotGrantedException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.exceptions.SSOException;
import com.nextcloud.android.sso.exceptions.TokenMismatchException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import de.luhmer.owncloudnewsreader.ListView.SubscriptionExpandableListAdapter;
import de.luhmer.owncloudnewsreader.adapter.NewsListRecyclerAdapter;
import de.luhmer.owncloudnewsreader.adapter.RecyclerItemClickListener;
import de.luhmer.owncloudnewsreader.adapter.RssItemViewHolder;
import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.Feed;
import de.luhmer.owncloudnewsreader.database.model.Folder;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.databinding.ActivityNewsreaderBinding;
import de.luhmer.owncloudnewsreader.helper.DatabaseUtilsKt;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.model.OcsUser;
import de.luhmer.owncloudnewsreader.reader.nextcloud.RssItemObservable;
import de.luhmer.owncloudnewsreader.services.DownloadImagesService;
import de.luhmer.owncloudnewsreader.services.DownloadWebPageService;
import de.luhmer.owncloudnewsreader.services.OwnCloudSyncService;
import de.luhmer.owncloudnewsreader.services.events.SyncFailedEvent;
import de.luhmer.owncloudnewsreader.services.events.SyncFinishedEvent;
import de.luhmer.owncloudnewsreader.services.events.SyncStartedEvent;
import de.luhmer.owncloudnewsreader.ssl.OkHttpSSLClient;
import de.luhmer.owncloudnewsreader.view.PodcastSlidingUpPanelLayout;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

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

	public static final String ITEM_ID = "ITEM_ID";
	public static final String TITLE = "TITLE";

    public static HashSet<Long> stayUnreadItems = new HashSet<>();

	private MenuItem menuItemDownloadMoreItems;

	@VisibleForTesting(otherwise = PROTECTED)
	public ActivityNewsreaderBinding binding;

	//private ServiceConnection mConnection = null;

	private ActionBarDrawerToggle drawerToggle;
	private SearchView mSearchView;
	private String mSearchString;
	private static final String SEARCH_KEY = "SEARCH_KEY";

	private PublishSubject<String> searchPublishSubject;
	private static final int REQUEST_CODE_PERMISSION_DOWNLOAD_WEB_ARCHIVE = 1;
	private static final int REQUEST_CODE_PERMISSION_NOTIFICATIONS = 2;

	private static final String ID_FEED_STRING = "ID_FEED_STRING";
	private static final String IS_FOLDER_BOOLEAN = "IS_FOLDER_BOOLEAN";
	private static final String OPTIONAL_FOLDER_ID = "OPTIONAL_FOLDER_ID";
	private static final String LIST_ADAPTER_TOTAL_COUNT = "LIST_ADAPTER_TOTAL_COUNT";
	private static final String LIST_ADAPTER_PAGE_COUNT = "LIST_ADAPTER_PAGE_COUNT";

	@Inject
	@Named("sharedPreferencesFileName")
	String sharedPreferencesFileName;


	private final View.OnClickListener mSnackbarListener = view -> {
		//Toast.makeText(getActivity(), "button 1 pressed", 3000).show();
		updateCurrentRssView();
	};

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (drawerToggle != null) {
			drawerToggle.syncState();
		}

		// Fragments are not ready when calling the method below in onCreate()
		updateButtonLayout();

		// Start auto sync if enabled (and user is logged in)
		if (isUserLoggedIn() && mPrefs.getBoolean(SettingsActivity.CB_SYNCONSTARTUP_STRING, true)) {
			startSync();
		}
	}

	private boolean isUserLoggedIn() {
		return (mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) != null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		((NewsReaderApplication) getApplication()).getAppComponent().injectActivity(this);

		SharedPreferences defaultValueSp = getSharedPreferences(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);
		if (!defaultValueSp.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
			PreferenceManager.setDefaultValues(this, sharedPreferencesFileName, Context.MODE_PRIVATE, R.xml.pref_data_sync, true);
			PreferenceManager.setDefaultValues(this, sharedPreferencesFileName, Context.MODE_PRIVATE, R.xml.pref_display, true);
			PreferenceManager.setDefaultValues(this, sharedPreferencesFileName, Context.MODE_PRIVATE, R.xml.pref_general, true);
		}

		super.onCreate(savedInstanceState);

		binding = ActivityNewsreaderBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setSupportActionBar(binding.toolbarLayout.toolbar);

		initAccountManager();

		checkNotificationPermissions();

		binding.toolbarLayout.avatar.setVisibility(View.VISIBLE);
		binding.toolbarLayout.avatar.setOnClickListener((v) -> startActivityForResult(new Intent(this, LoginDialogActivity.class), RESULT_LOGIN));

		// Init config --> if nothing is configured start the login dialog.
		if (!isUserLoggedIn()) {
			startLoginActivity();
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

		if (binding.drawerLayout != null) {
			drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbarLayout.toolbar, R.string.news_list_drawer_text, R.string.news_list_drawer_text) {
				@Override
				public void onDrawerClosed(View drawerView) {
					super.onDrawerClosed(drawerView);

					syncState();
				}

				@Override
				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					reloadCountNumbersOfSlidingPaneAdapter();

					syncState();
				}
			};

			binding.drawerLayout.addDrawerListener(drawerToggle);

			adjustEdgeSizeOfDrawer();
		}
		setSupportActionBar(binding.toolbarLayout.toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
		if (drawerToggle != null) {
			drawerToggle.syncState();
		}

		//AppRater.app_launched(this);
		//AppRater.rateNow(this);

		if (savedInstanceState == null) { //When the app starts (no orientation change)
			updateDetailFragment(SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue(), true, null, true);
		}
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		restoreInstanceState(savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		saveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	private void saveInstanceState(Bundle outState) {
		NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
		if (ndf != null) {
			outState.putLong(OPTIONAL_FOLDER_ID, ndf.getIdFolder());
			outState.putBoolean(IS_FOLDER_BOOLEAN, ndf.getIdFeed() == null);
			outState.putLong(ID_FEED_STRING, ndf.getIdFeed() != null ? ndf.getIdFeed() : ndf.getIdFolder());

			NewsListRecyclerAdapter adapter = (NewsListRecyclerAdapter) ndf.getRecyclerView().getAdapter();
			if (adapter != null) {
				outState.putInt(LIST_ADAPTER_TOTAL_COUNT, adapter.getTotalItemCount());
				outState.putInt(LIST_ADAPTER_PAGE_COUNT, adapter.getCachedPages());
			}
		}
		if (mSearchView != null) {
			mSearchString = mSearchView.getQuery().toString();
			outState.putString(SEARCH_KEY, mSearchString);
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(ID_FEED_STRING) &&
                savedInstanceState.containsKey(IS_FOLDER_BOOLEAN) &&
                savedInstanceState.containsKey(OPTIONAL_FOLDER_ID)) {

			NewsListRecyclerAdapter adapter = new NewsListRecyclerAdapter(this, getNewsReaderDetailFragment().binding.list, this, mPostDelayHandler, mPrefs);

			adapter.setTotalItemCount(savedInstanceState.getInt(LIST_ADAPTER_TOTAL_COUNT));
			adapter.setCachedPages(savedInstanceState.getInt(LIST_ADAPTER_PAGE_COUNT));

			getNewsReaderDetailFragment()
					.getRecyclerView()
					.setAdapter(adapter);

			updateDetailFragment(savedInstanceState.getLong(ID_FEED_STRING),
					savedInstanceState.getBoolean(IS_FOLDER_BOOLEAN),
					savedInstanceState.getLong(OPTIONAL_FOLDER_ID),
					false);
		}
		mSearchString = savedInstanceState.getString(SEARCH_KEY, null);
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (drawerToggle != null) {
			drawerToggle.onConfigurationChanged(newConfig);
		}
	}

	/**
	 * This method increases the "pull to open drawer" area by three.
	 * This method should be called only once!
	 */
	private void adjustEdgeSizeOfDrawer() {
		try {
			// increase the size of the drag margin to prevent starting a star swipe when
			// trying to open the drawer.
			Field mDragger = Objects.requireNonNull(binding.drawerLayout).getClass().getDeclaredField("mLeftDragger");
			mDragger.setAccessible(true);
			ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(binding.drawerLayout);
			Field mEdgeSize = Objects.requireNonNull(draggerObj).getClass().getDeclaredField("mEdgeSize");
			mEdgeSize.setAccessible(true);
			int edge = mEdgeSize.getInt(draggerObj);
			mEdgeSize.setInt(draggerObj, edge * 3);
		} catch (Exception e) {
			Log.e(TAG, "Setting edge width of drawer failed..", e);
		}
	}

	public int getEdgeSizeOfDrawer() {
		try {
			Field mDragger = Objects.requireNonNull(binding.drawerLayout).getClass().getDeclaredField("mLeftDragger");
			mDragger.setAccessible(true);
			ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(binding.drawerLayout);
			Field mEdgeSize = Objects.requireNonNull(draggerObj).getClass().getDeclaredField("mEdgeSize");
			mEdgeSize.setAccessible(true);
			return mEdgeSize.getInt(draggerObj);
		} catch (Exception e) {
			Log.e(TAG, "Failed to get edge size of drawer", e);
		}
		return 0;
	}


	/**
	 * Check if the account is in the Android Account Manager. If not it will be added automatically
	 */
	private void initAccountManager() {
		AccountManager mAccountManager = AccountManager.get(this);

		boolean isAccountThere = false;
		Account[] accounts = mAccountManager.getAccounts();
		String accountType = AccountGeneral.getAccountType(this);
		for (Account account : accounts) {
			if (account.type.intern().equals(accountType)) {
				isAccountThere = true;
			}
		}

		//If the account is not in the Android Account Manager
		if (!isAccountThere) {
			//Then add the new account
			Account account = new Account(getString(R.string.app_name), accountType);

			try {
				mAccountManager.addAccountExplicitly(account, "", new Bundle());

				SettingsFragment.setAccountSyncInterval(this, getResources().getInteger(R.integer.default_sync_minutes));
			} catch (SecurityException exception) {
				// not sure if this error can still occur.. it showed up a few versions ago.. so we'll
				// keep it here just to be safe
				new AlertDialog.Builder(this)
						.setTitle("Failed to add account")
						.setMessage("If you installed this app previously from anywhere else than the Google Play Store (e.g. F-Droid), please make sure to uninstall it first.")
						.setPositiveButton(android.R.string.ok, (dialog, which) -> {
							dialog.dismiss();
						})
						.setIcon(android.R.drawable.ic_dialog_alert)
						.show();
			}
		}
	}

	public void checkNotificationPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_PERMISSION_NOTIFICATIONS);
		}
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
			ndf.updateCurrentRssView();
		}
	}

	public void switchToAllUnreadItemsFolder() {
		updateDetailFragment(SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue(), true, null, true);
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SyncFailedEvent event) {
        Throwable exception = event.getCause();

        // If SSOException is wrapped inside another exception, we extract that SSOException
        if(exception.getCause() != null && exception.getCause() instanceof SSOException) {
            exception = exception.getCause();
        }

        if(exception instanceof SSOException){
            if(exception instanceof NextcloudHttpRequestFailedException && ((NextcloudHttpRequestFailedException) exception).getStatusCode() == 302) {
                ShowAlertDialog(
                        getString(R.string.login_dialog_title_error),
                        getString(R.string.login_dialog_text_news_app_not_installed_on_server,
                                "https://github.com/nextcloud/news/blob/master/docs/install.md#installing-from-the-app-store"),
                        this);
            } else if (exception instanceof TokenMismatchException) {
				Toast.makeText(NewsReaderListActivity.this, "Token out of sync. Please reauthenticate", Toast.LENGTH_LONG).show();
				try {
					SingleAccountHelper.reauthenticateCurrentAccount(this);
				} catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException | NextcloudFilesAppNotSupportedException e) {
					UiExceptionManager.showDialogForException(this, e);
				} catch (NextcloudFilesAppAccountPermissionNotGrantedException e) {
					// Unable to reauthenticate account just like that..
					startLoginActivity();
				}
				//StartLoginFragment(this);
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
		Log.d(TAG, "onEventMainThread - SyncStartedEvent");
		updateButtonLayout();
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SyncFinishedEvent event) {
		Log.d(TAG, "onEventMainThread - SyncFinishedEvent");
		updateButtonLayout();
		syncFinishedHandler();
	}

	/**
	 * @return true if new items count was greater than 0
	 */
	private boolean syncFinishedHandler() {
		NewsReaderListFragment newsReaderListFragment = getSlidingListFragment();
		newsReaderListFragment.reloadAdapter();
		UpdateItemList();
		updatePodcastView();
		updateDetailFragmentTitle();

		if(mApi.getNewsAPI() != null) {
            getSlidingListFragment().startAsyncTaskGetUserInfo();
        }

		int newItemsCount = mPrefs.getInt(Constants.LAST_UPDATE_NEW_ITEMS_COUNT_STRING, 0);

		if (newItemsCount > 0) {
			int firstVisiblePosition = getNewsReaderDetailFragment().getFirstVisibleScrollPosition();

			// Only show the update snackbar if scrollposition is not top.
			// 0 if scrolled all the way up
			// 1 if no items are visible right now (e.g. first sync)
			if (firstVisiblePosition == 0 || firstVisiblePosition == -1) {
				updateCurrentRssView();
			} else {
				showSnackbar(newItemsCount);
			}
			return true;
		} else {
			// update rss view even if no new items are available
			// If the user just finished reading some articles (e.g. all unread items) - he most
			// likely wants  the read articles to be removed when the sync is finished
			updateCurrentRssView();
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
	protected PodcastSlidingUpPanelLayout getPodcastSlidingUpPanelLayout() {
		return binding.slidingLayout;
	}

	@Override
	public void onRefresh() {
		startSync();
	}

	private void showSnackbar(int newItemsCount) {
		Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout),
				getResources().getQuantityString(R.plurals.message_bar_new_articles_available, newItemsCount, newItemsCount),
				Snackbar.LENGTH_LONG);
		snackbar.setAction(getString(R.string.message_bar_reload), mSnackbarListener);
		//snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.accent_material_dark));
		// Setting android:TextColor to #000 in the light theme results in black on black
		// text on the Snackbar, set the text back to white,
		//TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
		//textView.setTextColor(Color.WHITE);
		snackbar.show();
	}

	/**
	 * Callback method from {@link NewsReaderListFragment.Callbacks} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public void onTopItemClicked(long idFeed, boolean isFolder, Long optional_folder_id) {
		if (binding.drawerLayout != null)
			binding.drawerLayout.closeDrawer(GravityCompat.START);

		updateDetailFragment(idFeed, isFolder, optional_folder_id, true);
	}

	@Override
	public void onChildItemClicked(long idFeed, Long optional_folder_id) {
		if (binding.drawerLayout != null)
			binding.drawerLayout.closeDrawer(GravityCompat.START);

		updateDetailFragment(idFeed, false, optional_folder_id, true);
	}

	@Override
	public void onTopItemLongClicked(long idFeed, boolean isFolder) {
		startDialogFragment(idFeed, isFolder);
	}

	@Override
	public void onUserInfoUpdated(OcsUser userInfo) {
		final Drawable placeHolder = getDrawable(R.drawable.ic_baseline_account_circle_24);

		if (userInfo.getId() != null) {
			String mOc_root_path = mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null);
			String avatarUrl = mOc_root_path + "/index.php/avatar/" + Uri.encode(userInfo.getId()) + "/64";

			Glide.with(this)
					.load(avatarUrl)
					.diskCacheStrategy(DiskCacheStrategy.DATA)
					.placeholder(placeHolder)
					.error(placeHolder)
					.circleCrop()
					.into(binding.toolbarLayout.avatar);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				binding.toolbarLayout.avatar.setTooltipText(userInfo.getDisplayName());
			}
		}
	}

	@Override
	public void onCreateFolderClicked() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("add_folder_dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		AddFolderDialogFragment fragment = AddFolderDialogFragment.newInstance();
		fragment.setActivity(this);
		fragment.show(ft, "add_folder_dialog");
	}

	@Override
	public void onChildItemLongClicked(long idFeed) {
		startDialogFragment(idFeed, false);
	}

	private void startDialogFragment(long id, Boolean isFolder) {
		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getApplicationContext());

		if (!isFolder) {
			String titel = dbConn.getFeedById(id).getFeedTitle();
			String iconurl = dbConn.getFeedById(id).getFaviconUrl();
			String feedurl = dbConn.getFeedById(id).getLink();

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag("news_reader_list_dialog");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			NewsReaderListDialogFragment fragment = NewsReaderListDialogFragment.newInstance(id, titel, iconurl, feedurl);
			fragment.setActivity(this);
			fragment.show(ft, "news_reader_list_dialog");
		} else {
			Folder folder = dbConn.getFolderById(id);
			if (folder == null) {
				Log.e(TAG, "cannot find folder with id: " + id);
				return;
			}
			String label = folder.getLabel();

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag("folder_options_dialog");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			FolderOptionsDialogFragment fragment = FolderOptionsDialogFragment.newInstance(id, label);
			fragment.setActivity(this);
			fragment.show(ft, "folder_options_dialog");
		}
	}


    private NewsReaderDetailFragment updateDetailFragment(long id, Boolean folder, Long optional_folder_id, boolean updateListView) {
        if(menuItemDownloadMoreItems != null) {
            menuItemDownloadMoreItems.setEnabled(true);
        }

        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getApplicationContext());

        Long feedId = null;
        Long folderId;
        String title = null;

        if(!folder) {
            feedId = id;
            folderId = optional_folder_id;
            title = dbConn.getFeedById(id).getFeedTitle();
        } else {
            folderId = id;
            int idFolder = (int) id;
            if(idFolder >= 0) {
                title = dbConn.getFolderById(id).getLabel();
            } else if(idFolder == -10) {
                title = getString(R.string.allUnreadFeeds);
            } else if(idFolder == -11) {
                title = getString(R.string.starredFeeds);
            }
        }

        NewsReaderDetailFragment fragment = getNewsReaderDetailFragment();
        fragment.setData(feedId, folderId, title, updateListView);
        return fragment;
    }

	private void updateDetailFragmentTitle() {
		NewsReaderDetailFragment fragment = getNewsReaderDetailFragment();
		Long id = fragment.getIdFeed() == null ? fragment.getIdFolder() : fragment.getIdFeed();
		if (id == null) {
			return;
		}

		DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(getApplicationContext());

		String title = null;
		boolean isFolder = fragment.getIdFolder() == null;

		if (isFolder) {
			int idFolder = id.intValue();
			if (idFolder >= 0) {
				Folder folder = dbConn.getFolderById(id);
				if (folder == null) {
					return;
				}
				title = folder.getLabel();
			} else if (idFolder == -10) {
				title = getString(R.string.allUnreadFeeds);
			} else if (idFolder == -11) {
				title = getString(R.string.starredFeeds);
			}
		} else {
			Feed feed = dbConn.getFeedById(id);
			if (feed == null) {
				return;
			}
			title = feed.getFeedTitle();
		}

		fragment.setTitle(title);
	}


    public void UpdateItemList() {
		try {
			NewsReaderDetailFragment nrD = getNewsReaderDetailFragment();
			if (nrD != null && nrD.getRecyclerView() != null) {
				nrD.getRecyclerView().getAdapter().notifyDataSetChanged();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
        }
    }


    public void startSync()
    {
		if (!isUserLoggedIn()) {
			startLoginActivity();
		} else {
			if (!OwnCloudSyncService.isSyncRunning()) {
				mPostDelayHandler.stopRunningPostDelayHandler(); //Stop pending sync handler

				Bundle accBundle = new Bundle();
				accBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
				AccountManager mAccountManager = AccountManager.get(this);
				Account[] accounts = mAccountManager.getAccounts();
				for (Account acc : accounts) {
					String accountType = AccountGeneral.getAccountType(this);
					if (acc.type.equals(accountType)) {
                        ContentResolver.requestSync(acc, accountType, accBundle);
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
			newsReaderDetailFragment.binding.swipeRefresh.setRefreshing(isSyncRunning);
		}
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.news_reader, menu);

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
                //onQueryTextChange(""); // Reset search
                mSearchView.setQuery("", true);
                clearSearchViewFocus();
                return true;
            }
        });

		mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		mSearchView.setIconifiedByDefault(false);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                clearSearchViewFocus();
            }
        });

        NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
        if(ndf != null) {
            ndf.updateMenuItemsState();
        }

        updateButtonLayout();

        // focus the SearchView (if search view was active before orientation change)
        if (mSearchString != null && !mSearchString.isEmpty()) {
            searchItem.expandActionView();
            mSearchView.setQuery(mSearchString, true);
            mSearchView.clearFocus();
        }

        return true;
	}

	public MenuItem getMenuItemDownloadMoreItems() {
		return menuItemDownloadMoreItems;
	}

	@Override
	public void onBackPressed() {
        if(!handlePodcastBackPressed()) {
			if (binding.drawerLayout != null) {
				if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
					super.onBackPressed();
				else
					binding.drawerLayout.openDrawer(GravityCompat.START);
			} else {
				super.onBackPressed();
			}
		}
	}

	public static final int RESULT_SETTINGS = 15642;
    public static final int RESULT_ADD_NEW_FEED = 15643;

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item))
			return true;

		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			if (handlePodcastBackPressed())
				return true;
		} else if (itemId == R.id.menu_update) {
			startSync();
		} else if (itemId == R.id.menu_StartImageCaching) {
			final DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);

			long highestItemId = dbConn.getLowestRssItemIdUnread();


			Intent data = new Intent();
			data.putExtra(DownloadImagesService.LAST_ITEM_ID, highestItemId);
			data.putExtra(DownloadImagesService.DOWNLOAD_MODE_STRING, DownloadImagesService.DownloadMode.PICTURES_ONLY);
			DownloadImagesService.enqueueWork(this, data);
		} else if (itemId == R.id.menu_CreateDatabaseDump) {
			DatabaseUtilsKt.copyDatabaseToSdCard(this);

			new AlertDialog.Builder(this)
					.setMessage("Created dump at: " + DatabaseUtilsKt.getPath(this))
					.setNeutralButton(getString(android.R.string.ok), null)
					.show();
		} else if (itemId == R.id.menu_markAllAsRead) {
			NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();
			if (ndf != null) {
				DatabaseConnectionOrm dbConn2 = new DatabaseConnectionOrm(this);
				dbConn2.markAllItemsAsReadForCurrentView();

				reloadCountNumbersOfSlidingPaneAdapter();
				ndf.refreshCurrentRssView();
			}
			return true;
		} else if (itemId == R.id.menu_downloadMoreItems) {
			DownloadMoreItems();
			return true;
		} else if (itemId == R.id.menu_search) {
			mSearchView.setIconified(false);
			mSearchView.setFocusable(true);
			mSearchView.requestFocusFromTouch();
			return true;
		} else if (itemId == R.id.menu_download_web_archive) {
			checkAndStartDownloadWebPagesForOfflineReadingPermission();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void checkAndStartDownloadWebPagesForOfflineReadingPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("Permission error","You have permission");
                startDownloadWebPagesForOfflineReading();
            } else {
                Log.e("Permission error","Asking for permission");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.FOREGROUND_SERVICE}, REQUEST_CODE_PERMISSION_DOWNLOAD_WEB_ARCHIVE);
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

	private void DownloadMoreItems() {
		final NewsReaderDetailFragment ndf = getNewsReaderDetailFragment();

		// Folder is selected.. download more items for all feeds in this folder
		if(ndf.getIdFeed() == null) {
			Long idFolder = ndf.getIdFolder();

			List<Integer> specialFolders = Arrays.asList(
					SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS.getValue(),
					SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_STARRED_ITEMS.getValue(),
					SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_ITEMS.getValue()
			);
			// if a special folder is selected, we can start the sync
			if (specialFolders.contains(idFolder.intValue())) {
				startSync();
			} else {
				// Otherwise load more items for that particular folder and all its feeds
				DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(this);
				for (Feed feed : dbConn.getFolderById(idFolder).getFeedList()) {
					downloadMoreItemsForFeed(feed.getId());
				}
			}
		} else {
			// Single feed is selected.. download more items
			downloadMoreItemsForFeed(ndf.getIdFeed());
		}

		Toast.makeText(this, getString(R.string.toast_GettingMoreItems), Toast.LENGTH_SHORT).show();
	}

	@SuppressLint("CheckResult")
	private void downloadMoreItemsForFeed(final Long feedId) {
		Completable.fromAction(new Action() {
			@Override
			public void run() throws Exception {
				DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(NewsReaderListActivity.this);
				RssItem rssItem = dbConn.getLowestRssItemIdByFeed(feedId);
				long offset = Long.MAX_VALUE;
				if(rssItem != null) {
					offset = rssItem.getId();
				}
				int type = 0; // the type of the query (Feed: 0, Folder: 1, Starred: 2, All: 3)

				List<RssItem> buffer = mApi.getNewsAPI().items(100, offset, type, feedId, true, false).execute().body();
				RssItemObservable.performDatabaseBatchInsert(dbConn, buffer);
			}
		})
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(() -> {
					updateCurrentRssView();
					Log.v(TAG, "Finished Download extra items..");
				}, throwable -> {
					throwable.printStackTrace();
					Throwable e = OkHttpSSLClient.HandleExceptions(throwable);
					Toast.makeText(NewsReaderListActivity.this, getString(R.string.login_dialog_text_something_went_wrong) + " - " + e.getMessage(), Toast.LENGTH_SHORT).show();
				});
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            UpdateListView();
            getSlidingListFragment().ListViewNotifyDataSetChanged();
        }

		if (requestCode == RESULT_LOGIN) {
			Intent intent = new Intent();
			intent.putExtra(PREF_SERVER_SETTINGS, true);
			setResult(RESULT_OK, intent);
		}

        if(requestCode == RESULT_SETTINGS) {
        	// Extra is set if user entered/modified server settings
        	if (data == null || data.getBooleanExtra(PREF_SERVER_SETTINGS,false)) {
				resetUiAndStartSync();
			} else {
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
			}
        } else if(requestCode == RESULT_ADD_NEW_FEED) {
            if(data != null) {
                boolean val = data.getBooleanExtra(NewFeedActivity.ADD_NEW_SUCCESS, false);
                if (val) {
                    startSync();
                }
            }
        } else if(requestCode == RESULT_LOGIN) {
            resetUiAndStartSync();
        }


        try {
            AccountImporter.onActivityResult(requestCode, resultCode, data, this, account -> {
                Log.d(TAG, "accountAccessGranted() called with: account = [" + account + "]");
                mApi.initApi(new NextcloudAPI.ApiConnectedListener() {
                    @Override
                    public void onConnected() {
                        Log.d(TAG, "onConnected() called");
                    }

                    @Override
                    public void onError(Exception ex) {
                        Log.e(TAG, "onError() called with:", ex);
                    }
                });

            });
        } catch (AccountImportCancelledException ignored) {
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
        String oldListLayout = data.getStringExtra(SettingsActivity.RI_FEED_LIST_LAYOUT);
        String newListLayout = mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0");
        boolean themeChanged = !newListLayout.equals(oldListLayout);
        boolean cacheWasCleared = data.hasExtra(SettingsActivity.RI_CACHE_CLEARED);

        Log.d(TAG, "themeChanged: " + themeChanged + " cacheWasCleared: " + cacheWasCleared);

        if (ThemeChooser.themeRequiresRestartOfUI() || themeChanged) {
            NewsReaderListActivity.this.recreate();
        } else if (cacheWasCleared) {
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

    public void startLoginActivity() {
        Intent loginIntent = new Intent(this, LoginDialogActivity.class);
        startActivityForResult(loginIntent, RESULT_LOGIN);
    }

    private void resetUiAndStartSync() {
		NewsReaderListFragment nrlf = getSlidingListFragment();
		if (nrlf != null) {
			nrlf.reloadAdapter();
			updateCurrentRssView();
			startSync();
			nrlf.bindUserInfoToUI();
		} else {
			Log.e(TAG, "resetUiAndStartSync - NewsReaderListFragment is not available");
		}
	}

	private void UpdateListView() {
		getNewsReaderDetailFragment().notifyDataSetChangedOnAdapter();
	}

	@Override
	public void onClick(RssItemViewHolder vh, int position) {

		if (mPrefs.getBoolean(SettingsActivity.CB_SKIP_DETAILVIEW_AND_OPEN_BROWSER_DIRECTLY_STRING, false)) {
			String currentUrl = vh.getRssItem().getLink();

			//Choose Browser based on user settings
			//modified copy from NewsDetailFragment.java:loadUrl(String url)
			int selectedBrowser = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_DISPLAY_BROWSER, "0"));
			if (selectedBrowser == 0) { // Custom Tabs
				CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder()
						.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
						.setShowTitle(true)
						.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
						.setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right)
						.addDefaultShareMenuItem();
				builder.build().launchUrl(this, Uri.parse(currentUrl));
			} else { //External browser
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
				startActivity(browserIntent);
			}

			((NewsListRecyclerAdapter) getNewsReaderDetailFragment().getRecyclerView().getAdapter()).changeReadStateOfItem(vh, true);
		} else {
			Intent intentNewsDetailAct = new Intent(this, NewsDetailActivity.class);

			intentNewsDetailAct.putExtra(NewsReaderListActivity.ITEM_ID, position);
			intentNewsDetailAct.putExtra(NewsReaderListActivity.TITLE, getNewsReaderDetailFragment().getTitle());
			startActivityForResult(intentNewsDetailAct, Activity.RESULT_CANCELED);
		}
	}

	@Override
	public boolean onLongClick(RssItemViewHolder vh, int position) {
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
					.map(s -> getNewsReaderDetailFragment().performSearch(s))
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribeWith(getNewsReaderDetailFragment().searchResultObserver);

        }
        searchPublishSubject.onNext(newText);
        return true;
    }

    public void clearSearchViewFocus() {
        mSearchView.clearFocus();
    }
}
