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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm.SORT_DIRECTION;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.model.TTSItem;
import de.luhmer.owncloudnewsreader.widget.WidgetProvider;

public class NewsDetailActivity extends PodcastFragmentActivity {

	private static final String TAG = NewsDetailActivity.class.getCanonicalName();
	/**
	 * The {@link PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;
    protected @BindView(R.id.toolbar) Toolbar toolbar;
	protected @BindView(R.id.progressIndicator) ProgressBar progressIndicator;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	private int currentPosition;

	private MenuItem menuItem_PlayPodcast;
	private MenuItem menuItem_Starred;
	private MenuItem menuItem_Read;

	private DatabaseConnectionOrm dbConn;
	public List<RssItem> rssItems;

	protected @Inject SharedPreferences mPrefs;

	//public static final String DATABASE_IDS_OF_ITEMS = "DATABASE_IDS_OF_ITEMS";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        ((NewsReaderApplication) getApplication()).getAppComponent().injectActivity(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_detail);

        ButterKnife.bind(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

		dbConn = new DatabaseConnectionOrm(this);
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


        rssItems = dbConn.getCurrentRssItemView(-1);

        //If the Activity gets started from the Widget, read the item id and get the selected index in the cursor.
        if(intent.hasExtra(WidgetProvider.RSS_ITEM_ID)) {
            long rss_item_id = intent.getExtras().getLong(WidgetProvider.RSS_ITEM_ID);
            for(RssItem rssItem : rssItems) {
                if(rss_item_id == rssItem.getId()) {
                    getSupportActionBar().setTitle(rssItem.getTitle());
                    break;
                }
                else
                    item_id++;
            }
        }

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		progressIndicator.setMax(mSectionsPagerAdapter.getCount());

		// Set up the ViewPager with the sections adapter.
		mViewPager = findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);


        try {
            mViewPager.setCurrentItem(item_id, true);
            pageChanged(item_id);
		} catch(Exception ex) {
			ex.printStackTrace();
		}

        mViewPager.addOnPageChangeListener(onPageChangeListener);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int pos) {
            pageChanged(pos);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    public static SORT_DIRECTION getSortDirectionFromSettings(SharedPreferences prefs) {
        SORT_DIRECTION sDirection = SORT_DIRECTION.asc;
        String sortDirection = prefs.getString(SettingsActivity.SP_SORT_ORDER, "1");
        if (sortDirection.equals("1"))
            sDirection = SORT_DIRECTION.desc;
        return sDirection;
    }

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(mPrefs.getBoolean(SettingsActivity.CB_NAVIGATE_WITH_VOLUME_BUTTONS_STRING, false))
		{
	        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
	        {
	        	if(currentPosition < rssItems.size()-1)
	        	{
	        		mViewPager.setCurrentItem(currentPosition + 1, true);
	        	}
				// capture event to avoid volume change at end of feed
				return true;
	        }

	        else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP))
	        {
	        	if(currentPosition > 0)
	        	{
	        		mViewPager.setCurrentItem(currentPosition - 1, true);
	        	}
				// capture event to avoid volume change at beginning of feed
				return true;
	        }
		}
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			NewsDetailFragment ndf = getNewsDetailFragmentAtPosition(currentPosition);//(NewsDetailFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + currentPosition);

			if(ndf != null && ndf.canNavigateBack()) {
				ndf.navigateBack();
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
    }

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
			// capture event to suppress android system sound
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	private void pageChanged(int position)
	{
		StopVideoOnCurrentPage();
		currentPosition = position;
		ResumeVideoPlayersOnCurrentPage();
		progressIndicator.setProgress(position + 1);

        getSupportActionBar().setTitle(rssItems.get(position).getTitle());

		if(!rssItems.get(position).getRead_temp())
		{
			markItemAsReadUnread(rssItems.get(position), true);

			mPostDelayHandler.delayTimer();

			Log.v("PAGE CHANGED", "PAGE: " + position + " - IDFEED: " + rssItems.get(position).getId());
		}
		else { //Only in else because the function markItemAsReas updates the ActionBar items as well
            UpdateActionBarIcons();
        }
	}


    private NewsDetailFragment getNewsDetailFragmentAtPosition(int position) {
		if(mSectionsPagerAdapter.items.get(position) != null)
			return mSectionsPagerAdapter.items.get(position).get();
		return null;
    }

	private void ResumeVideoPlayersOnCurrentPage()
	{
		NewsDetailFragment fragment = getNewsDetailFragmentAtPosition(currentPosition);
		if(fragment != null)  // could be null if not instantiated yet
			fragment.resumeCurrentPage();

	}

	private void StopVideoOnCurrentPage()
	{
        NewsDetailFragment fragment = getNewsDetailFragmentAtPosition(currentPosition);
		if(fragment != null)  // could be null if not instantiated yet
			fragment.pauseCurrentPage();
	}

	public void UpdateActionBarIcons()
	{
        RssItem rssItem = rssItems.get(currentPosition);

        boolean isStarred = rssItem.getStarred_temp();
        boolean isRead = rssItem.getRead_temp();


        PodcastItem podcastItem =  DatabaseConnectionOrm.ParsePodcastItemFromRssItem(this, rssItem);
        boolean podcastAvailable = !"".equals(podcastItem.link);


        if(menuItem_PlayPodcast != null)
            menuItem_PlayPodcast.setVisible(podcastAvailable);


        if(isStarred && menuItem_Starred != null)
            menuItem_Starred.setIcon(R.drawable.ic_action_star_dark);
        else if(menuItem_Starred != null)
            menuItem_Starred.setIcon(R.drawable.ic_action_star_border_dark);



        if(isRead && menuItem_Read != null) {
            menuItem_Read.setIcon(R.drawable.ic_check_box_white);
            menuItem_Read.setChecked(true);
        }
        else if(menuItem_Read != null) {
            menuItem_Read.setIcon(R.drawable.ic_check_box_outline_blank_white);
            menuItem_Read.setChecked(false);
        }
    }


    @Override
    public void onBackPressed() {
        if(!handlePodcastBackPressed())
            super.onBackPressed();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.news_detail, menu);

		menuItem_Starred = menu.findItem(R.id.action_starred);
		menuItem_Read = menu.findItem(R.id.action_read);
        menuItem_PlayPodcast = menu.findItem(R.id.action_playPodcast);

		Set<String> selections = mPrefs.getStringSet("sp_news_detail_actionbar_icons", new HashSet<String>());
		String[] selected = selections.toArray(new String[] {});
		for(String selection : selected) {
            switch(selection) {
                case "open_in_browser":
                    menu.findItem(R.id.action_openInBrowser).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    break;
                case "share":
                    menu.findItem(R.id.action_ShareItem).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    break;
                case "podcast":
                    menu.findItem(R.id.action_playPodcast).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    break;
                //case "tts":
                //    menu.findItem(R.id.action_tts).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                //    break;
            }
		}

        UpdateActionBarIcons();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		RssItem rssItem = rssItems.get(currentPosition);

		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;

            case R.id.action_read:
                markItemAsReadUnread(rssItem, !menuItem_Read.isChecked());
                UpdateActionBarIcons();
                mPostDelayHandler.delayTimer();
                break;

            case R.id.action_starred:
                toggleRssItemStarredState();
                break;

            case R.id.action_openInBrowser:
                NewsDetailFragment newsDetailFragment = getNewsDetailFragmentAtPosition(currentPosition);
                String link = "about:blank";

                if(newsDetailFragment != null && newsDetailFragment.mWebView != null) {
                    link = newsDetailFragment.mWebView.getUrl();
                }

                if("about:blank".equals(link)) {
                    link = rssItem.getLink();
                }

				if(link.length() > 0)
				{
					//if(isChromeDefaultBrowser() && mCustomTabsSupported) {
					if(isChromeDefaultBrowser()) {
						//CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(mCustomTabsSession);
						CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
						builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
						builder.setShowTitle(true);
						builder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left);
						builder.setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right);
						builder.build().launchUrl(this, Uri.parse(link));
					} else {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
						startActivity(browserIntent);
					}
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

            case R.id.action_playPodcast:
                openPodcast(rssItem);
                break;

            case R.id.action_tts:
                TTSItem ttsItem = new TTSItem(rssItem.getId(), rssItem.getAuthor(), rssItem.getTitle(), rssItem.getTitle() + "\n\n " + Html.fromHtml(rssItem.getBody()).toString(), rssItem.getFeed().getFaviconUrl());
				openMediaItem(ttsItem);
                break;

            case R.id.action_ShareItem:

            	String title = rssItem.getTitle();
            	String content = rssItem.getLink();

                NewsDetailFragment fragment = getNewsDetailFragmentAtPosition(currentPosition);
				if(fragment != null) { // could be null if not instantiated yet
					if(!fragment.mWebView.getUrl().equals("about:blank") && !fragment.mWebView.getUrl().trim().equals("")) {
						content = fragment.mWebView.getUrl();
						title = fragment.mWebView.getTitle();
					}
				}

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                //share.putExtra(Intent.EXTRA_SUBJECT, rssFiles.get(currentPosition).getTitle());
                //share.putExtra(Intent.EXTRA_TEXT, rssFiles.get(currentPosition).getLink());
                share.putExtra(Intent.EXTRA_SUBJECT, title);
                share.putExtra(Intent.EXTRA_TEXT, content);

                startActivity(Intent.createChooser(share, "Share Item"));
                break;


		}

		return super.onOptionsItemSelected(item);
	}

	public void toggleRssItemStarredState() {
        RssItem rssItem = rssItems.get(currentPosition);
		Boolean curState = rssItem.getStarred_temp();
		rssItem.setStarred_temp(!curState);
		dbConn.updateRssItem(rssItem);

		UpdateActionBarIcons();

		mPostDelayHandler.delayTimer();
	}

	private boolean isChromeDefaultBrowser() {
		Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
		ResolveInfo resolveInfo = getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);

        Log.v(TAG, "Default Browser is: " + resolveInfo.loadLabel(getPackageManager()).toString());
		return (resolveInfo.loadLabel(getPackageManager()).toString().contains("Chrome"));
	}

	private void markItemAsReadUnread(RssItem item, boolean read) {
        item.setRead_temp(read);
		dbConn.updateRssItem(item);
		UpdateActionBarIcons();
	}

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
	//public class SectionsPagerAdapter extends FragmentPagerAdapter {
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		SparseArray<WeakReference<NewsDetailFragment>> items = new SparseArray<>();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);

			if(fm.getFragments() != null) {
				for (Fragment fragment : fm.getFragments()) {
					if (fragment instanceof NewsDetailFragment) {
						int id = ((NewsDetailFragment) fragment).getSectionNumber();
						items.put(id, new WeakReference<>((NewsDetailFragment) fragment));
					}
				}
			}
		}

		@Override
		public Fragment getItem(int position) {
			NewsDetailFragment fragment = null;

			if(items.get(position) != null) {
				fragment = items.get(position).get();
			}

			if(fragment == null) {
				fragment = new NewsDetailFragment();
				Bundle args = new Bundle();
				args.putInt(NewsDetailFragment.ARG_SECTION_NUMBER, position);
				fragment.setArguments(args);
				items.put(position, new WeakReference<>(fragment));
			}

			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			items.remove(position);

			super.destroyItem(container, position, object);
		}

		@Override
		public int getCount() {
			//return cursor.getCount();
            return rssItems.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return null;
		}
	}

	protected void setBackgroundColorOfViewPager(int backgroundColor) {
		this.mViewPager.setBackgroundColor(backgroundColor);
	}
}
