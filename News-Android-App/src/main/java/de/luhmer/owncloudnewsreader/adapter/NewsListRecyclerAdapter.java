package de.luhmer.owncloudnewsreader.adapter;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.events.podcast.PodcastCompletedEvent;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.StopWatch;
import de.luhmer.owncloudnewsreader.interfaces.IPlayPausePodcastClicked;
import de.luhmer.owncloudnewsreader.model.CurrentRssViewDataHolder;

public class NewsListRecyclerAdapter extends RecyclerView.Adapter {
    private static final String TAG = "NewsListRecyclerAdapter";

    private final int VIEW_ITEM = 1; // Item
    private final int VIEW_PROG = 0; // Progress

    private long idOfCurrentlyPlayedPodcast = -1;

    private List<RssItem> lazyList;
    private DatabaseConnectionOrm dbConn;
    private PostDelayHandler pDelayHandler;
    private FragmentActivity activity;
    private HashSet<Long> stayUnreadItems = new HashSet<>();

    private int totalItemCount = 0;
    private int cachedPages = 1;

    public int getTotalItemCount() {
        return totalItemCount;
    }

    public int getCachedPages() {
        return cachedPages;
    }

    public void setTotalItemCount(int totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    public void setCachedPages(int cachedPages) {
        this.cachedPages = cachedPages;
    }

    private IPlayPausePodcastClicked playPausePodcastClicked;

    private boolean loading = false;
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleThreshold = 5;

    public NewsListRecyclerAdapter(FragmentActivity activity, RecyclerView recyclerView, IPlayPausePodcastClicked playPausePodcastClicked, PostDelayHandler postDelayHandler) {
        this.activity = activity;
        this.playPausePodcastClicked = playPausePodcastClicked;

        pDelayHandler = postDelayHandler;

        dbConn = new DatabaseConnectionOrm(activity);
        setHasStableIds(true);

        EventBus.getDefault().register(this);

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager =
                    (LinearLayoutManager) recyclerView.getLayoutManager();


            recyclerView
                    .addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView,
                                               int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            int adapterTotalItemCount = linearLayoutManager.getItemCount();
                            int lastVisibleItem = linearLayoutManager
                                    .findLastVisibleItemPosition();
                            if (!loading &&
                                    adapterTotalItemCount <= (lastVisibleItem + visibleThreshold) &&
                                    adapterTotalItemCount < totalItemCount) {
                                loading = true;

                                Log.v(TAG, "start load more task...");

                                recyclerView.post(new Runnable() {
                                    public void run() {
                                        // End has been reached
                                        // Do something
                                        lazyList.add(null);
                                        notifyItemInserted(lazyList.size() - 1);

                                        AsyncTaskHelper.StartAsyncTask(new LoadMoreItemsAsyncTask());
                                    }
                                });
                            }
                        }
                    });
        }
    }

    @Subscribe
    public void onEvent(UpdatePodcastStatusEvent podcast) {
        if (podcast.isPlaying()) {
            if (podcast.getRssItemId() != idOfCurrentlyPlayedPodcast) {
                idOfCurrentlyPlayedPodcast = podcast.getRssItemId();
                notifyDataSetChanged();

                Log.v(TAG, "Updating Listview - Podcast started");
            }
        } else if (idOfCurrentlyPlayedPodcast != -1) {
            idOfCurrentlyPlayedPodcast = -1;
            notifyDataSetChanged();

            Log.v(TAG, "Updating Listview - Podcast paused");
        }
    }

    @Subscribe
    public void onEvent(PodcastCompletedEvent podcastCompletedEvent) {
        idOfCurrentlyPlayedPodcast = -1;
        notifyDataSetChanged();

        Log.v(TAG, "Updating Listview - Podcast completed");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_PROG) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progressbar_item, parent, false);

            return new ProgressViewHolder(v);
        } else {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
            Integer layout = 0;
            switch (Integer.parseInt(mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0"))) {
                case 0:
                    layout = R.layout.subscription_detail_list_item_thumbnail;
                    break;
                case 1:
                    layout = R.layout.subscription_detail_list_item_text;
                    break;
                case 3:
                    layout = R.layout.subscription_detail_list_item_text;
                    break;
                case 2:
                    layout = R.layout.subscription_detail_list_item_web_layout;
                    break;
                case 4:
                    layout = R.layout.subscription_detail_list_item_card_view;
                    break;
                case 5:
                    layout = R.layout.subscription_detail_list_item_headline;
                    break;
                default:
                    Log.e(TAG, "Unknown layout..");
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

            final ViewHolder holder = new ViewHolder(view);

            holder.starImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleStarredStateOfItem(holder);
                }
            });

            holder.setClickListener((RecyclerItemClickListener) activity);

            holder.flPlayPausePodcastWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.isPlaying()) {
                        playPausePodcastClicked.pausePodcast();
                    } else {
                        playPausePodcastClicked.openPodcast(holder.getRssItem());
                    }
                }
            });

            return holder;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof ProgressViewHolder) {
            ((ProgressViewHolder) viewHolder).progressBar.setIndeterminate(true);
        } else {
            final ViewHolder holder = (ViewHolder) viewHolder;
            RssItem item = lazyList.get(position);
            holder.setRssItem(item);
            holder.setStayUnread(stayUnreadItems.contains(item.getId()));

            //Podcast stuff
            if (DatabaseConnectionOrm.ALLOWED_PODCASTS_TYPES.contains(item.getEnclosureMime())) {
                final boolean isPlaying = idOfCurrentlyPlayedPodcast == item.getId();
                //Enable podcast buttons in view
                holder.flPlayPausePodcastWrapper.setVisibility(View.VISIBLE);
                holder.setPlaying(isPlaying);
                holder.setDownloadPodcastProgressbar();
            } else {
                holder.flPlayPausePodcastWrapper.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if(holder instanceof ViewHolder)
            EventBus.getDefault().unregister(holder);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if(holder instanceof ViewHolder)
            EventBus.getDefault().register(holder);
    }

    public void changeReadStateOfItem(ViewHolder viewHolder, boolean isChecked) {
        RssItem rssItem = viewHolder.getRssItem();
        if (rssItem.getRead_temp() != isChecked) { //Only perform database operations if really needed
            rssItem.setRead_temp(isChecked);
            dbConn.updateRssItem(rssItem);

            pDelayHandler.delayTimer();

            viewHolder.setReadState(isChecked);
            //notifyItemChanged(viewHolder.getAdapterPosition());

            stayUnreadItems.add(rssItem.getId());
        }
    }

    public void toggleReadStateOfItem(ViewHolder viewHolder) {
        RssItem rssItem = viewHolder.getRssItem();
        boolean isRead = !rssItem.getRead_temp();
        changeReadStateOfItem(viewHolder, isRead);
    }

    public void toggleStarredStateOfItem(ViewHolder viewHolder) {
        RssItem rssItem = viewHolder.getRssItem();
        boolean isStarred = !rssItem.getStarred_temp();
        rssItem.setStarred_temp(isStarred);
        if (isStarred) {
            changeReadStateOfItem(viewHolder, true);
        } else {
            dbConn.updateRssItem(rssItem);
            pDelayHandler.delayTimer();
        }
        viewHolder.setStarred(isStarred);
    }

    @Override
    public int getItemViewType(int position) {
        return lazyList.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public int getItemCount() {
        //return totalItemCount;
        return lazyList != null ? lazyList.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        if (lazyList != null) {
            RssItem item = lazyList.get(position);
            return item != null ? item.getId() : 0;
        }
        return 0;
    }



    private List<RssItem> refreshAdapterData() {
        List<RssItem> rssItems = new ArrayList<>();
        DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(activity);
        for(int i = 0; i < cachedPages; i++) {
            rssItems.addAll(dbConn.getCurrentRssItemView(i));
        }
        return rssItems;
    }



    public void updateAdapterData(List<RssItem> rssItems) {
        stayUnreadItems.clear();

        cachedPages = 1;

        if (this.lazyList != null) {
            //this.lazyList.close();
        }
        //new ReloadAdapterAsyncTask().execute();

        totalItemCount = ((Long) dbConn.getCurrentRssItemViewCount()).intValue();

        lazyList = rssItems;
        notifyDataSetChanged();

        loading = false;
    }

    public interface IOnRefreshFinished {
        void OnRefreshFinished();
    }

    public void refreshAdapterDataAsync(IOnRefreshFinished listener) {
        AsyncTaskHelper.StartAsyncTask(new RefreshDataAsyncTask(listener));
    }

    private class RefreshDataAsyncTask extends AsyncTask<Void, Void, List<RssItem>> {

        private IOnRefreshFinished listener;

        public RefreshDataAsyncTask(IOnRefreshFinished listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            loading = true;

            super.onPreExecute();
        }

        @Override
        protected List<RssItem> doInBackground(Void... params) {
            StopWatch sw = new StopWatch();
            sw.start();

            List<RssItem> rssItems = refreshAdapterData();

            sw.stop();
            Log.v(TAG, "Time needed (refreshing adapter): " + sw.toString());

            return rssItems;
        }

        @Override
        protected void onPostExecute(List<RssItem> rssItems) {
            lazyList = rssItems;
            notifyDataSetChanged();

            loading = false;

            listener.OnRefreshFinished();

            super.onPostExecute(rssItems);
        }
    }


    private class LoadMoreItemsAsyncTask extends AsyncTask<Void, Void, List<RssItem>> {
        @Override
        protected List<RssItem> doInBackground(Void... params) {
            StopWatch sw = new StopWatch();
            sw.start();

            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(activity);
            List<RssItem> items = dbConn.getCurrentRssItemView(cachedPages++);

            /*
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            sw.stop();
            Log.v(TAG, "Time needed (loading more): " + sw.toString());

            return items;
        }

        @Override
        protected void onPostExecute(List<RssItem> rssItems) {
            int prevSize = lazyList.size();
            lazyList.remove(prevSize - 1);
            lazyList.addAll(rssItems);

            notifyItemRangeInserted(prevSize, rssItems.size());

            loading = false;

            super.onPostExecute(rssItems);
        }
    }

    private class ReloadAdapterAsyncTask extends AsyncTask<Void, Void, CurrentRssViewDataHolder> {

        @Override
        protected CurrentRssViewDataHolder doInBackground(Void... params) {
            StopWatch sw = new StopWatch();
            sw.start();

            List<RssItem> list = dbConn.getCurrentRssItemView(0);

            CurrentRssViewDataHolder holder = new CurrentRssViewDataHolder();
            holder.maxCount = dbConn.getCurrentRssItemViewCount();
            holder.rssItems = list;

            sw.stop();
            Log.v(TAG, "Reloaded CurrentRssView - time taken: " + sw.toString());
            return holder;
        }

        @Override
        protected void onPostExecute(CurrentRssViewDataHolder holder) {
            lazyList = holder.rssItems;
            totalItemCount = holder.maxCount.intValue();
            cachedPages = 1;
            notifyDataSetChanged();
        }
    }
}