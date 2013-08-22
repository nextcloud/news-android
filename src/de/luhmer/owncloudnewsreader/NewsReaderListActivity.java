package de.luhmer.owncloudnewsreader;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SlidingPaneLayout.PanelSlideListener;
import android.util.DisplayMetrics;
import android.view.View;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.BlockingExpandableListView;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;

import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.MenuUtilsSherlockFragmentActivity;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.services.DownloadImagesService;

/**
 * An activity representing a list of NewsReader. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link NewsReaderDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
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
	
	//IabHelper mHelper;
	static final String TAG = "NewsReaderListActivity";
	ActionBarDrawerToggle drawerToggle;
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
		
		//DatabaseUtils.CopyDatabaseToSdCard(this);
		
        /*
		((NewsReaderListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.newsreader_list)).setUpdateFinishedListener(updateFinished);
        */
		
		/*
		AppUpdater au = new AppUpdater(this, false);
        au.UpdateApp();
        */
        
		/*
        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, Constants.getBase64EncodedPublicKey());
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
        	   public void onIabSetupFinished(IabResult result) {
        	      if (!result.isSuccess()) {
					// Oh noes, there was a problem.
        	         Log.d(TAG, "Problem setting up In-app Billing: " + result);
        	      }  
        	   }
        	});
        */

		//Init config --> if nothing is configured start the login dialog.
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPrefs.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null) == null)
        	StartLoginFragment();
        
        /*
        NewsReaderListFragment nrlFrag = new NewsReaderListFragment();
        
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                       .replace(R.id.left_drawer, nrlFrag)
                       .commit();
        */
        
       
        mSlidingLayout = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
        mSlidingLayout.setParallaxDistance(280);        
        mSlidingLayout.setPanelSlideListener(new PanelSlideListener() {
			
			@Override
			public void onPanelSlide(View arg0, float arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPanelOpened(View arg0) {
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				getSupportActionBar().setHomeButtonEnabled(true);
			}
			
			@Override
			public void onPanelClosed(View arg0) {
				getSupportActionBar().setDisplayHomeAsUpEnabled(false);
				getSupportActionBar().setHomeButtonEnabled(false);
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
		
        if(shouldDrawerStayOpen()) {
        	//LinearLayout ll_expListView = (LinearLayout) findViewById(R.id.expandableListView_LinearLayoutWrapper);
        	//ll_expListView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        }
        
		//if(!shouldDrawerStayOpen()) {
		//	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//	getSupportActionBar().setHomeButtonEnabled(true);
		//}
        
        //onTopItemClicked(SubscriptionExpandableListAdapter.ALL_UNREAD_ITEMS, true, null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
        //ThemeChooser.ChangeBackgroundOfSlider(this);
        //TODO 
        /*
    	drawerLayout.openDrawer(Gravity.LEFT);
    	
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
        */
    }


	public void updateAdapter() {
		NewsReaderListFragment nlf = ((NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer));
		if(nlf != null)
		{				
			// Block children layout for now
			PullToRefreshExpandableListView ptrel = ((PullToRefreshExpandableListView)nlf.eListView);
			BlockingExpandableListView bView = ((BlockingExpandableListView) ptrel.getRefreshableView());
			
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
				
		/*
		if(shouldDrawerStayOpen()) {
			//TODO
			drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
			drawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));
		}
		*/
		
		updateAdapter();
		
		super.onResume();
	}

	private boolean shouldDrawerStayOpen() {
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			double x = Math.pow(dm.widthPixels/dm.xdpi,2);
			double y = Math.pow(dm.heightPixels/dm.ydpi,2);
			double screenInches = Math.sqrt(x+y);
			
			if(screenInches >= 6) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onDestroy() {
		//this.unregisterReceiver()
		super.onDestroy();
	   /*
	   try
	   {
		   if (mHelper != null)
			   mHelper.dispose();
		   mHelper = null;
	   }
	   catch(Exception ex)
	   {
		   ex.printStackTrace();
	   }*/
	}


	/**
	 * Callback method from {@link NewsReaderListFragment.Callbacks} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public void onTopItemClicked(String idSubscription, boolean isFolder, String optional_folder_id) {
		//if(!shouldDrawerStayOpen())
		//	drawerLayout.closeDrawer(Gravity.LEFT);
		if(!shouldDrawerStayOpen())
			mSlidingLayout.closePane();
		StartDetailFragment(idSubscription, isFolder, optional_folder_id);		
	}

	@Override
	public void onChildItemClicked(String idSubscription, String optional_folder_id) {
		//if(!shouldDrawerStayOpen())
		//	drawerLayout.closeDrawer(Gravity.LEFT);
		if(!shouldDrawerStayOpen())
			mSlidingLayout.closePane();
		StartDetailFragment(idSubscription, false, optional_folder_id);
	}
		
	private void StartDetailFragment(String id, Boolean folder, String optional_folder_id)
	{
		if(super.getMenuItemMarkAllAsRead() != null)
			super.getMenuItemMarkAllAsRead().setEnabled(true);
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
		fragment.setArguments(arguments);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment)
				.commit();
		
		
		/*
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = detailIntent.getExtras();
			
			//arguments.putString(NewsReaderDetailFragment.ARG_ITEM_ID, id);
			
			//getApplicationContext().startActivity(detailIntent);
			
			NewsReaderDetailFragment fragment = new NewsReaderDetailFragment();			
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.newsreader_detail_container, fragment)
					.commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.			
			startActivity(detailIntent);
		}
		*/
		dbConn.closeDatabase();
	}


    public void UpdateItemList()
    {
        NewsReaderDetailFragment nrD = (NewsReaderDetailFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(nrD != null)
            nrD.UpdateCursor();
    }


    void startSync()
    {
		//menuItemUpdater.setActionView(R.layout.inderterminate_progress);
		((NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer)).StartSync();		
    }

    @SuppressWarnings("static-access")
	public void UpdateButtonSyncLayout()
    {
        if(super.getMenuItemUpdater() != null)
        {
            IReader _Reader = ((NewsReaderListFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer))._Reader;
            PullToRefreshExpandableListView pullToRefreshView = (PullToRefreshExpandableListView) findViewById(R.id.expandableListView);
            if(_Reader.isSyncRunning())   
            {
            	super.getMenuItemUpdater().setActionView(R.layout.inderterminate_progress);
                if(pullToRefreshView != null)
                	pullToRefreshView.setRefreshing(true);
                
            }
            else
            {
            	super.getMenuItemUpdater().setActionView(null);
                if(pullToRefreshView != null)
                	pullToRefreshView.onRefreshComplete();
            }
        }
    }
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.news_reader, menu);
		//getSupportMenuInflater().inflate(R.menu.news_reader, menu);
		
		
		super.onCreateOptionsMenu(menu, getSupportMenuInflater(), true, this);
		
        UpdateButtonSyncLayout();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = super.onOptionsItemSelected(item, this);
		        
		if(!handled)
		{		
			switch (item.getItemId()) {
			
			case android.R.id.home:
				if(mSlidingLayout.isOpen())
					mSlidingLayout.closePane();
				else
					mSlidingLayout.openPane();
				//if (drawerLayout.isDrawerOpen(Gravity.LEFT))
	            //	drawerLayout.closeDrawer(Gravity.LEFT);
	            //else
	            //	drawerLayout.openDrawer(Gravity.LEFT);
				break;
			
				case R.id.action_settings:
					Intent intent = new Intent(this, SettingsActivity.class);		    
				    //intent.putExtra(EXTRA_MESSAGE, message);
				    startActivityForResult(intent, RESULT_SETTINGS);
					return true;
					
				case R.id.menu_update:
					//menuItemUpdater = item.setActionView(R.layout.inderterminate_progress);
					startSync();
					break;
					
				case R.id.action_login:
					StartLoginFragment();
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

	private static final int RESULT_SETTINGS = 15642;


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


    public void StartLoginFragment()
    {    	
	   	SherlockDialogFragment dialog = new LoginDialogFragment();
	    dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
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
        ((NewsReaderDetailFragment) act.getSupportFragmentManager().findFragmentById(R.id.content_frame)).getLvAdapter().notifyDataSetChanged();
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
        	//((NewsReaderDetailFragment) act.getSupportFragmentManager().findFragmentById(R.id.newsreader_detail_container)).getListView().smoothScrollToPosition(pos);
        //else
        	((NewsReaderDetailFragment) act.getSupportFragmentManager().findFragmentById(R.id.content_frame)).getListView().setSelection(pos);
        
    }
}
