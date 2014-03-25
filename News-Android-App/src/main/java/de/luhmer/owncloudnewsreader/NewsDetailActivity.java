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

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection.SORT_DIRECTION;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;
import de.luhmer.owncloudnewsreader.reader.IReader;
import de.luhmer.owncloudnewsreader.reader.owncloud.OwnCloud_Reader;
import de.luhmer.owncloudnewsreader.widget.WidgetProvider;

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

	PostDelayHandler pDelayHandler;

	MenuItem menuItem_Starred;
	MenuItem menuItem_Read;

    IReader _Reader;
    //ArrayList<Integer> databaseItemIds;
    DatabaseConnection dbConn;
	//public List<RssFile> rssFiles;
    Cursor cursor;

    public static final String DATABASE_IDS_OF_ITEMS = "DATABASE_IDS_OF_ITEMS";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		ThemeChooser.chooseTheme(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_detail);

		pDelayHandler = new PostDelayHandler(this);

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
		if(intent.hasExtra(NewsReaderListActivity.ITEM_ID))
			item_id = intent.getExtras().getInt(NewsReaderListActivity.ITEM_ID);
		if(intent.hasExtra(NewsReaderListActivity.TITEL))
			getSupportActionBar().setTitle(intent.getExtras().getString(NewsReaderListActivity.TITEL));
			//getActionBar().setTitle(intent.getExtras().getString(NewsReaderDetailActivity.TITEL));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		//if(intent.hasExtra(DATABASE_IDS_OF_ITEMS))
		//	databaseItemIds = intent.getIntegerArrayListExtra(DATABASE_IDS_OF_ITEMS);

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SORT_DIRECTION sDirection = SORT_DIRECTION.asc;
    	String sortDirection = mPrefs.getString(SettingsActivity.SP_SORT_ORDER, "1");
    	if(sortDirection.equals("1"))
    		sDirection = SORT_DIRECTION.desc;
		cursor = dbConn.getCurrentSelectedRssItems(sDirection);

        //If the Activity gets started from the Widget, read the item id and get the selected index in the cursor.
        if(intent.hasExtra(WidgetProvider.UID_TODO)) {
            cursor.moveToFirst();
            String rss_item_id = intent.getExtras().getString(WidgetProvider.UID_TODO);
            do {
                String current_item_id = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_RSSITEM_ID));
                if(rss_item_id.equals(current_item_id)) {
                    getSupportActionBar().setTitle(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE)));
                    break;
                }
                else
                    item_id++;
            } while(cursor.moveToNext());
        }

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		try
		{
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

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mPrefs.getBoolean(SettingsActivity.CB_NAVIGATE_WITH_VOLUME_BUTTONS_STRING, false))
		{
	        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
	        {
	        	if(currentPosition < cursor.getCount() -1)
	        	{
	        		mViewPager.setCurrentItem(currentPosition + 1, true);
	        		return true;
	        	}
	        }

	        else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP))
	        {
	        	if(currentPosition > 0)
	        	{
	        		mViewPager.setCurrentItem(currentPosition - 1, true);
	        		return true;
	        	}
	        }
		}
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			NewsDetailFragment ndf = (NewsDetailFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + currentPosition);
			if(ndf != null && ndf.webview != null)
			{
				if(ndf.webview.canGoBack())
				{
					ndf.webview.goBack();
					if(!ndf.webview.canGoBack())//RssItem
						ndf.LoadRssItemInWebView();

					return true;
				}
			}
		}

		return super.onKeyDown(keyCode, event);
    }

	private void PageChanged(int position)
	{
		StopVideoOnCurrentPage();
		currentPosition = position;
		ResumeVideoPlayersOnCurrentPage();

		//String idFeed = String.valueOf(rssFiles.get(position).getDB_Id());
		String idFeed = getIdCurrentFeed(currentPosition);

		if(!dbConn.isFeedUnreadStarred(idFeed, true))
		{
			markItemAsReadUnread(idFeed, true);

			pDelayHandler.DelayTimer();

			Log.d("PAGE CHANGED", "PAGE: " + position + " - IDFEED: " + idFeed);
		}
		else //Only in else because the function markItemAsReas updates the ActionBar items as well
			UpdateActionBarIcons();
	}

    /**
     * Returns the id of a feed. When the position is invalid, -1 is returned.
     * @param position
     * @return
     */
	public String getIdCurrentFeed(int position) {
        if(position < cursor.getCount()) {
		    cursor.moveToPosition(position);
		    String idFeed = String.valueOf(cursor.getString(0));
		    return idFeed;
        }
        return "-1";
	}

	private void ResumeVideoPlayersOnCurrentPage()
	{
		NewsDetailFragment fragment = (NewsDetailFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + currentPosition);
		if(fragment != null)  // could be null if not instantiated yet
			fragment.ResumeCurrentPage();

	}

	private void StopVideoOnCurrentPage()
	{
		NewsDetailFragment fragment = (NewsDetailFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + currentPosition);
		if(fragment != null)  // could be null if not instantiated yet
			fragment.PauseCurrentPage();
	}

	public void UpdateActionBarIcons()
	{
		String idFeed = getIdCurrentFeed(currentPosition);
        boolean isStarred = false;
        boolean isRead = false;

        if(idFeed != "-1") {
            isStarred = dbConn.isFeedUnreadStarred(idFeed, false);
            isRead = dbConn.isFeedUnreadStarred(idFeed, true);
        }

        //if(rssFiles.get(currentPosition).getStarred() && menuItem_Starred != null)
        if(isStarred && menuItem_Starred != null)
            menuItem_Starred.setIcon(getSmallVersionOfActionbarIcon(R.drawable.btn_rating_star_on_normal_holo_dark));
            //menuItem_Starred.setIcon(R.drawable.btn_rating_star_on_normal_holo_light);
        else if(menuItem_Starred != null)
            menuItem_Starred.setIcon(getSmallVersionOfActionbarIcon(R.drawable.btn_rating_star_off_normal_holo_dark));
            //menuItem_Starred.setIcon(R.drawable.btn_rating_star_off_normal_holo_light);



        if(isRead && menuItem_Read != null) {
            menuItem_Read.setIcon(R.drawable.btn_check_on_holo_dark);
            menuItem_Read.setChecked(true);
        }
        else if(menuItem_Read != null) {
            menuItem_Read.setIcon(R.drawable.btn_check_off_holo_dark);
            menuItem_Read.setChecked(false);
        }
	}

    public Drawable getSmallVersionOfActionbarIcon(int res_id) {
        Bitmap b = ((BitmapDrawable)getResources().getDrawable(res_id)).getBitmap();
        Bitmap bitmapResized = null;

        int density = getResources().getDisplayMetrics().densityDpi;
        if(density <= DisplayMetrics.DENSITY_LOW)
            bitmapResized = Bitmap.createScaledBitmap(b, 32, 32, false);
        else if(density <= DisplayMetrics.DENSITY_MEDIUM)
            bitmapResized = Bitmap.createScaledBitmap(b, 48, 48, false);
        else if(density <= DisplayMetrics.DENSITY_HIGH)
            bitmapResized = Bitmap.createScaledBitmap(b, 64, 64, false);
        else if(density <= DisplayMetrics.DENSITY_XHIGH)
            bitmapResized = Bitmap.createScaledBitmap(b, 96, 96, false);
        else
            bitmapResized = Bitmap.createScaledBitmap(b, 96, 96, false);
        //Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 32, 32, false);
        return new BitmapDrawable(bitmapResized);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.news_detail, menu);
		getSupportMenuInflater().inflate(R.menu.news_detail, menu);

		menuItem_Starred = menu.findItem(R.id.action_starred);
		menuItem_Read = menu.findItem(R.id.action_read);
        UpdateActionBarIcons();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String idFeed = getIdCurrentFeed(currentPosition);
		Cursor cursor = dbConn.getArticleByID(idFeed);

        String currentUrl = "";
        if(cursor != null)
        {
            cursor.moveToFirst();
            currentUrl = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_LINK));
        }

		switch (item.getItemId()) {
			case android.R.id.home:
				super.onBackPressed();
				break;

			case R.id.action_starred:
				//String idItem_Db = String.valueOf(rssFiles.get(currentPosition).getDB_Id());
				String idItem_Db = getIdCurrentFeed(currentPosition);
                //String idItem = String.valueOf(rssFiles.get(currentPosition).getItem_Id());
				Boolean curState = dbConn.isFeedUnreadStarred(idItem_Db, false);

				//rssFiles.get(currentPosition).setStarred(!curState);

				dbConn.updateIsStarredOfItem(idItem_Db, !curState);

				UpdateActionBarIcons();

				pDelayHandler.DelayTimer();

				List<String> idItems = new ArrayList<String>();
				cursor.moveToFirst();
				idItems.add(cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_RSSITEM_ID)));
				cursor.close();
				break;

			case R.id.action_openInBrowser:
				//String link = rssFiles.get(currentPosition).getLink();

				//if(!link.isEmpty())
				if(currentUrl.trim().length() > 0)
				{
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
					startActivity(browserIntent);
				}
				break;


			/*
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
			 */
            case R.id.action_ShareItem:

            	String title = "";
            	String content = currentUrl;

                if(cursor != null)
				    title = cursor.getString(cursor.getColumnIndex(DatabaseConnection.RSS_ITEM_TITLE));

				NewsDetailFragment fragment = (NewsDetailFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + currentPosition);
				if(fragment != null) { // could be null if not instantiated yet
					if(!fragment.webview.getUrl().equals("about:blank") && !fragment.webview.getUrl().trim().equals("")) {
						content = fragment.webview.getUrl();
						title = fragment.webview.getTitle();
					}
				}

				//content += "<br/><br/>Send via <a href=\"https://play.google.com/store/apps/details?id=de.luhmer.owncloudnewsreader\">ownCloud News Reader</a>";

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                //share.putExtra(Intent.EXTRA_SUBJECT, rssFiles.get(currentPosition).getTitle());
                //share.putExtra(Intent.EXTRA_TEXT, rssFiles.get(currentPosition).getLink());
                share.putExtra(Intent.EXTRA_SUBJECT, title);
                share.putExtra(Intent.EXTRA_TEXT, content);

                startActivity(Intent.createChooser(share, "Share Item"));
                break;

            case R.id.action_read:

            	if(cursor != null)
				{
					cursor.moveToFirst();
					String id = cursor.getString(0);
					markItemAsReadUnread(id, !menuItem_Read.isChecked());
					cursor.close();
				}

            	UpdateActionBarIcons();

            	pDelayHandler.DelayTimer();

            	break;
		}

        if(cursor != null)
            cursor.close();

		return super.onOptionsItemSelected(item);
	}


	private void markItemAsReadUnread(String item_id, boolean read) {
		dbConn.updateIsReadOfItem(item_id, read);
		UpdateActionBarIcons();
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
	//public class SectionsPagerAdapter extends FragmentStatePagerAdapter {


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
			return cursor.getCount();
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
