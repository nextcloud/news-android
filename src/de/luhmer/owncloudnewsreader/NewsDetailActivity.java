package de.luhmer.owncloudnewsreader;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;

public class NewsDetailActivity extends SherlockFragmentActivity {	
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public ViewPager mViewPager;
	private int currentPosition;
	MenuItem menuItem_Starred;
    IReader _Reader;
    ArrayList<Integer> databaseItemIds;
    DatabaseConnection dbConn;
	//public List<RssFile> rssFiles;
    
    public static final String DATABASE_IDS_OF_ITEMS = "DATABASE_IDS_OF_ITEMS";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		ThemeChooser.chooseTheme(this);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_detail);
		
		_Reader = new OwnCloud_Reader();
		dbConn = new DatabaseConnection(this);
		Intent intent = getIntent();
		
		//long subsciption_id = -1;
		//long folder_id = -1;
		int item_id = 0;
		
		//if(intent.hasExtra(NewsReaderDetailActivity.SUBSCRIPTION_ID))
		//	subsciption_id = intent.getExtras().getLong(NewsReaderDetailActivity.SUBSCRIPTION_ID);
		//if(intent.hasExtra(NewsReaderDetailActivity.FOLDER_ID))
		//	folder_id = intent.getExtras().getLong(NewsReaderDetailActivity.FOLDER_ID);		
		if(intent.hasExtra(NewsReaderDetailActivity.ITEM_ID))
			item_id = intent.getExtras().getInt(NewsReaderDetailActivity.ITEM_ID);
		if(intent.hasExtra(NewsReaderDetailActivity.TITEL))
			getSupportActionBar().setTitle(intent.getExtras().getString(NewsReaderDetailActivity.TITEL));
			//getActionBar().setTitle(intent.getExtras().getString(NewsReaderDetailActivity.TITEL));		
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		if(intent.hasExtra(DATABASE_IDS_OF_ITEMS))
			databaseItemIds = intent.getIntegerArrayListExtra(DATABASE_IDS_OF_ITEMS);
		
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);		
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
			
		//rssFiles = new ArrayList<RssFile>();
		try
		{
			/*
			Cursor cursor = null;
			if(subsciption_id != -1)
				cursor = dbConn.getAllFeedsForSubscription(String.valueOf(subsciption_id));
			else if(folder_id != -1)
				cursor = dbConn.getAllFeedsForFolder(String.valueOf(folder_id));
			
			if(cursor != null) {
				while(cursor.moveToNext())
				{
					long idDB = cursor.getLong(0);
					String title = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE));
					String link = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_LINK));
					String description = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_BODY));
					//String readString = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_READ));
					String readString = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_READ_TEMP));
					//String starredString = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_STARRED));
					String starredString = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_STARRED_TEMP));
                    String id_item = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_RSSITEM_ID));
					//long sub_id = Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID)));
                    String sub_id = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_SUBSCRIPTION_ID));
					String guid = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_GUID));
                    String guidHash = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_GUIDHASH));

					Date timestamp = null;					
					try { timestamp = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_PUBDATE)))); }
					catch(Exception ex) { ex.printStackTrace(); }
					
					Boolean read = false;
					Boolean starred = false;
					if(readString.equals("1"))
						read = true;
					if(starredString.equals("1"))
						starred = true;
					
					List<String> categories = new ArrayList<String>();
					rssFiles.add(new RssFile(idDB, id_item, title, link, description, read, sub_id, null, categories, timestamp, starred, guid, guidHash));
				}
			}
			 */
            mViewPager.setCurrentItem(item_id, true);
            PageChanged(item_id);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int pos) {
				PageChanged(pos);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		if(dbConn != null)
			dbConn.closeDatabase();
		super.onDestroy();
	}

	private void PageChanged(int position)
	{
		StopVideoOnCurrentPage();		
		currentPosition = position;		
		ResumeVideoPlayersOnCurrentPage();
		
		UpdateActionBarIcons();
		
		//String idFeed = String.valueOf(rssFiles.get(position).getDB_Id());
		String idFeed = String.valueOf(databaseItemIds.get(currentPosition));
		
		if(!dbConn.isFeedUnreadStarred(idFeed, true))
		{			
			dbConn.updateIsReadOfFeed(idFeed, true);
		
			//Cursor cur = dbConn.getArticleByID(idFeed);
			//cur.moveToFirst();
			//GoogleReaderMethods.MarkItemAsRead(true, cur, dbConn, getApplicationContext(), asyncTaskCompletedPerformTagRead);


			/*
			List<String> idItems = new ArrayList<String>();
			idItems.add(cur.getString(cur.getColumnIndex(DatabaseConnection.RSS_ITEM_RSSITEM_ID)));
			_Reader.Start_AsyncTask_PerformTagActionForSingleItem(5,
					this,
					asyncTaskCompletedPerformTagRead,
					idItems,
					FeedItemTags.TAGS.MARK_ITEM_AS_READ);
			

			cur.close();
			*/
			//dbConn.closeDatabase();
			Log.d("PAGE CHANGED", "PAGE: " + position + " - IDFEED: " + idFeed);
		}
	}
	
	private void ResumeVideoPlayersOnCurrentPage()
	{
		NewsDetailFragment fragment = (NewsDetailFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + currentPosition);
		if(fragment != null)  // could be null if not instantiated yet
			fragment.ResumeVideoPlayers();
		
	}
	
	private void StopVideoOnCurrentPage()
	{
		NewsDetailFragment fragment = (NewsDetailFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + currentPosition);
		if(fragment != null)  // could be null if not instantiated yet
			fragment.StopVideoPlayers();
	}

	public void UpdateActionBarIcons()
	{
		boolean isStarred = dbConn.isFeedUnreadStarred(String.valueOf(databaseItemIds.get(currentPosition)) , false);
		
		//if(rssFiles.get(currentPosition).getStarred() && menuItem_Starred != null)
		if(isStarred && menuItem_Starred != null)
			menuItem_Starred.setIcon(android.R.drawable.star_on);
			//menuItem_Starred.setIcon(R.drawable.btn_rating_star_on_normal_holo_light);
		else if(menuItem_Starred != null)
			menuItem_Starred.setIcon(android.R.drawable.star_off);
			//menuItem_Starred.setIcon(R.drawable.btn_rating_star_off_normal_holo_light);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.news_detail, menu);
		getSupportMenuInflater().inflate(R.menu.news_detail, menu);
		
		menuItem_Starred = menu.findItem(R.id.action_starred);
        UpdateActionBarIcons();

		return true;
	}
	
	
	@Override
	public void onBackPressed() {
		View vGroup = mViewPager.getChildAt(currentPosition);
		
		if(vGroup != null)
		{
			WebView webView = (WebView) vGroup.findViewById(R.id.webview); 
			if(webView != null)
			{
				if(webView.canGoBack())
				{
					webView.goBack();
					return;
				}
			}
		}
		super.onBackPressed();
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Cursor cursor = dbConn.getArticleByID(String.valueOf(databaseItemIds.get(currentPosition)));
		
		switch (item.getItemId()) {
			case android.R.id.home:
				super.onBackPressed();
				break;
		
			case R.id.action_starred:				
				//String idItem_Db = String.valueOf(rssFiles.get(currentPosition).getDB_Id());
				String idItem_Db = String.valueOf(databaseItemIds.get(currentPosition));
                //String idItem = String.valueOf(rssFiles.get(currentPosition).getItem_Id());
				Boolean curState = dbConn.isFeedUnreadStarred(idItem_Db, false);

				//rssFiles.get(currentPosition).setStarred(!curState);

				dbConn.updateIsStarredOfFeed(idItem_Db, !curState);
				
				UpdateActionBarIcons();
                
				List<String> idItems = new ArrayList<String>();
				cursor.moveToFirst();
				idItems.add(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_RSSITEM_ID)));
				cursor.close();
				
				/*
                if(!curState)
                    _Reader.Start_AsyncTask_PerformTagActionForSingleItem(0, this, asyncTaskCompletedPerformTagRead, idItems, FeedItemTags.TAGS.MARK_ITEM_AS_STARRED);
                else
                    _Reader.Start_AsyncTask_PerformTagActionForSingleItem(0, this, asyncTaskCompletedPerformTagRead, idItems, FeedItemTags.TAGS.MARK_ITEM_AS_UNSTARRED);
				
                */
                /*
				Cursor cur = dbConn.getFeedByID(idFeed);
				cur.moveToFirst();
				GoogleReaderMethods.MarkItemAsStarred(!curState, cur, dbConn, getApplicationContext(), asyncTaskCompletedPerformTagStarred);
				cur.close();*/
				break;
			
			case R.id.action_openInBrowser:
				//String link = rssFiles.get(currentPosition).getLink();
				String link = "";
				
				if(cursor != null)
				{
					cursor.moveToFirst();
					link = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_LINK));
					cursor.close();
				}
				
				//if(!link.isEmpty())
				if(link.trim().length() > 0)
				{
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
					startActivity(browserIntent);
				}
				break;
			
			case R.id.action_sendSourceCode:
				String description = "";
				if(cursor != null)
				{
					cursor.moveToFirst();
					description = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_BODY));
					cursor.close();
				}
				
				
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"david-dev@live.de"});
				i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_sourceCode));
				//i.putExtra(Intent.EXTRA_TEXT   , rssFiles.get(currentPosition).getDescription());
				i.putExtra(Intent.EXTRA_TEXT   , description);
				try {
				    startActivity(Intent.createChooser(i, getString(R.string.email_sendMail)));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(NewsDetailActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}				
				break;

            case R.id.action_ShareItem:
            	
            	String title = "";
            	String linkToItem = "";
				if(cursor != null)
				{
					cursor.moveToFirst();
					title = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE));
					linkToItem = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_LINK));					
					cursor.close();
				}
            	
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                //share.putExtra(Intent.EXTRA_SUBJECT, rssFiles.get(currentPosition).getTitle());
                //share.putExtra(Intent.EXTRA_TEXT, rssFiles.get(currentPosition).getLink());
                share.putExtra(Intent.EXTRA_SUBJECT, title);
                share.putExtra(Intent.EXTRA_TEXT, linkToItem);
                
                startActivity(Intent.createChooser(share, "Share Item"));
                break;
		}
		return super.onOptionsItemSelected(item);
	}


	/*
	OnAsyncTaskCompletedListener asyncTaskCompletedPerformTagRead = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
            boolean success = (Boolean) task_result;
            if(!success)
                Toast.makeText(NewsDetailActivity.this, "Error while changing the read tag..", Toast.LENGTH_LONG).show();

            Log.d("FINISHED PERFORM TAG READ ", "" + task_result);
		}
	};
	
	OnAsyncTaskCompletedListener asyncTaskCompletedPerformTagStarred = new OnAsyncTaskCompletedListener() {
		
		@Override
		public void onAsyncTaskCompleted(int task_id, Object task_result) {
			Log.d("FINISHED PERFORM TAG STARRED ", "" + task_result);			
		}
	};
	*/

	@Override
	public void finish() {
		Intent intent = new Intent();
		intent.putExtra("POS", mViewPager.getCurrentItem());
		setResult(RESULT_OK, intent);
		super.finish();
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new NewsDetailFragment();
			Bundle args = new Bundle();
			args.putInt(NewsDetailFragment.ARG_SECTION_NUMBER, position + 1);			
			fragment.setArguments(args);
			return fragment;
		}
		
		
		@Override
		public int getCount() {
			//return 2;
			return databaseItemIds.size();
			//return rssFiles.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			/*
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			*/
			return null;
		}
	}
}
