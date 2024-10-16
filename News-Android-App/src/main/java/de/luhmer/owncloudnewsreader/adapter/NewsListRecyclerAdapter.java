package de.luhmer.owncloudnewsreader.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.LazyLoadingLinearLayoutManager;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.database.model.RssItem;
import de.luhmer.owncloudnewsreader.databinding.ProgressbarItemBinding;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemCardViewBinding;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemHeadlineBinding;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemHeadlineThumbnailBinding;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemTextBinding;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemThumbnailBinding;
import de.luhmer.owncloudnewsreader.databinding.SubscriptionDetailListItemWebLayoutBinding;
import de.luhmer.owncloudnewsreader.events.podcast.PodcastCompletedEvent;
import de.luhmer.owncloudnewsreader.helper.AsyncTaskHelper;
import de.luhmer.owncloudnewsreader.helper.FavIconHandler;
import de.luhmer.owncloudnewsreader.helper.PostDelayHandler;
import de.luhmer.owncloudnewsreader.helper.StopWatch;
import de.luhmer.owncloudnewsreader.interfaces.IPlayPausePodcastClicked;
import de.luhmer.owncloudnewsreader.model.CurrentRssViewDataHolder;

public class NewsListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "NewsListRecyclerAdapter";

    @SuppressWarnings("FieldCanBeLocal")
    private final int VIEW_ITEM = 1; // Item
    private final int VIEW_PROG = 0; // Progress
    private final FavIconHandler faviconHandler;
    private final RequestManager glide;

    private long idOfCurrentlyPlayedPodcast = -1;

    private List<RssItem> lazyList;
    private final DatabaseConnectionOrm dbConn;
    private final PostDelayHandler pDelayHandler;
    private final FragmentActivity activity;

    private int cachedPages = 1;

    private final IPlayPausePodcastClicked playPausePodcastClicked;

    private boolean loading = false;
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private final int visibleThreshold = 5;
    private final SharedPreferences mPrefs;

    private LazyLoadingLinearLayoutManager layoutManager = null;

    public NewsListRecyclerAdapter(FragmentActivity activity, RecyclerView recyclerView, IPlayPausePodcastClicked playPausePodcastClicked, PostDelayHandler postDelayHandler, SharedPreferences prefs) {
        this.activity = activity;
        this.playPausePodcastClicked = playPausePodcastClicked;
        this.mPrefs = prefs;

        pDelayHandler = postDelayHandler;

        dbConn = new DatabaseConnectionOrm(activity);
        faviconHandler = new FavIconHandler(activity);
        glide = Glide.with(activity);
        setHasStableIds(true);

        EventBus.getDefault().register(this);

        if (recyclerView.getLayoutManager() instanceof LazyLoadingLinearLayoutManager lm) {
            layoutManager = lm;

            recyclerView
                    .addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView,
                                               int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            int adapterItemCount = layoutManager.getItemCount();
                            int adapterTotalItemCount = layoutManager.getTotalItemCount();
                            int lastVisibleItem = layoutManager
                                    .findLastVisibleItemPosition();
                            if (!loading &&
                                    adapterItemCount <= (lastVisibleItem + visibleThreshold) &&
                                    adapterItemCount < adapterTotalItemCount &&
                                    adapterItemCount > 0) {
                                loading = true;

                                Log.v(TAG, "start load more task...");

                                recyclerView.post(() -> {
                                    // End has been reached
                                    // Do something
                                    try {
                                        lazyList.add(null);
                                        notifyItemInserted(lazyList.size() - 1);
                                        AsyncTaskHelper.StartAsyncTask(new LoadMoreItemsAsyncTask());
                                    } catch (UnsupportedOperationException ex) {
                                        Log.e(TAG, "error while lazy loading more items");
                                        // this can happen in case a podcast download is running and
                                        // the user tries to open the Downloaded Podcast View
                                    }
                                });
                            }
                        }
                    });
        }
    }

    public int getTotalItemCount() {
        if (this.layoutManager != null) {
            return this.layoutManager.getTotalItemCount();
        }
        return 0;
    }

    public int getCachedPages() {
        return cachedPages;
    }

    public void setTotalItemCount(int totalItemCount) {
        if (this.layoutManager != null) {
            this.layoutManager.setTotalItemCount(totalItemCount);
        }
    }

    public void setCachedPages(int cachedPages) {
        this.cachedPages = cachedPages;
    }

    /*
    // TODO right now this is not working anymore.. We need to use the MediaSession here..
    // Not sure if this is the cleanest solution though..
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
    */

    @Subscribe
    public void onEvent(PodcastCompletedEvent podcastCompletedEvent) {
        idOfCurrentlyPlayedPodcast = -1;
        notifyDataSetChanged();

        Log.v(TAG, "Updating Listview - Podcast completed");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_PROG) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ProgressbarItemBinding binding = ProgressbarItemBinding.inflate(layoutInflater, parent, false);
            return new ProgressViewHolder(binding);
        } else {
            Context context = parent.getContext();
            RssItemViewHolder viewHolder = null;
            switch (Integer.parseInt(mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0"))) {
                case 0:
                    viewHolder = new RssItemThumbnailViewHolder(
                            SubscriptionDetailListItemThumbnailBinding.inflate(LayoutInflater.from(context), parent, false),
                            faviconHandler,
                            glide,
                            mPrefs
                    );
                    break;
                case 1:
                    viewHolder = new RssItemTextViewHolder(
                            SubscriptionDetailListItemTextBinding.inflate(LayoutInflater.from(context), parent, false),
                            faviconHandler,
                            glide,
                            mPrefs
                    );
                    break;
                case 3:
                    viewHolder = new RssItemFullTextViewHolder(
                            SubscriptionDetailListItemTextBinding.inflate(LayoutInflater.from(context), parent, false),
                            faviconHandler,
                            glide,
                            mPrefs
                    );
                    break;
                case 2:
                    viewHolder = new RssItemWebViewHolder(
                            SubscriptionDetailListItemWebLayoutBinding.inflate(LayoutInflater.from(context), parent, false),
                            faviconHandler,
                            glide,
                            mPrefs
                    );
                    break;
                case 4:
                    viewHolder = new RssItemCardViewHolder(
                            SubscriptionDetailListItemCardViewBinding.inflate(LayoutInflater.from(context), parent, false),
                            faviconHandler,
                            glide,
                            mPrefs
                    );
                    break;
                case 5:
                    viewHolder = new RssItemHeadlineViewHolder(
                            SubscriptionDetailListItemHeadlineBinding.inflate(LayoutInflater.from(context), parent, false),
                            faviconHandler,
                            glide,
                            mPrefs
                    );
                    break;
                case 6:
                    viewHolder = new RssItemHeadlineThumbnailViewHolder(
                            SubscriptionDetailListItemHeadlineThumbnailBinding.inflate(LayoutInflater.from(context), parent, false),
                            faviconHandler,
                            glide,
                            mPrefs
                    );
                    break;
                default:
                    Log.e(TAG, "Unknown layout..");
            }

            RssItemViewHolder finalViewHolder = viewHolder;
            if(viewHolder.getStar() != null) {
                viewHolder.getStar().setOnClickListener(view1 -> toggleStarredStateOfItem(finalViewHolder));
            }

            viewHolder.getPlayPausePodcastWrapper().setOnClickListener(v -> {
                if (finalViewHolder.isPlaying()) {
                    playPausePodcastClicked.pausePodcast();
                } else {
                    playPausePodcastClicked.openPodcast(finalViewHolder.getRssItem());
                }
            });
            viewHolder.setClickListener((RecyclerItemClickListener) activity);
            /*
            // TODO implement option to delete cached podcasts (https://github.com/nextcloud/news-android/issues/742)
            holder.flPlayPausePodcastWrapper.setOnLongClickListener(v -> {
                // TODO check if cached..
                new AlertDialog.Builder(activity)
                        .setTitle("")
                        .setMessage("")
                        .setPositiveButton("", (dialog, which) -> {})
                        .setNegativeButton("", (dialog, which) -> {})
                        .create()
                        .show();
                return false;
            });
            */
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ProgressViewHolder) {
            ((ProgressViewHolder) viewHolder).getBinding().progressBar.setIndeterminate(true);
        } else {
            final RssItemViewHolder holder = (RssItemViewHolder) viewHolder;
            RssItem item = lazyList.get(position);
            holder.bind(item);
            holder.setStayUnread(NewsReaderListActivity.stayUnreadItems.contains(item.getId()));

            //Podcast stuff
            if (DatabaseConnectionOrm.ALLOWED_PODCASTS_TYPES.contains(item.getEnclosureMime())) {
                final boolean isPlaying = idOfCurrentlyPlayedPodcast == item.getId();
                //Enable podcast buttons in view
                holder.getPlayPausePodcastWrapper().setVisibility(View.VISIBLE);
                holder.setPlaying(isPlaying);
                holder.setDownloadPodcastProgressbar();
            } else {
                holder.getPlayPausePodcastWrapper().setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof RssItemViewHolder) {
            EventBus.getDefault().unregister(holder);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof RssItemViewHolder) {
            EventBus.getDefault().register(holder);
        }
    }

    public void changeReadStateOfItem(RssItemViewHolder viewHolder, boolean isChecked) {
        RssItem rssItem = viewHolder.getRssItem();
        if (rssItem.getRead_temp() != isChecked) { // Only perform database operations if really needed
            rssItem.setRead_temp(isChecked);
            dbConn.updateRssItem(rssItem);

            pDelayHandler.delayTimer();

            viewHolder.setReadState(isChecked);
            //notifyItemChanged(viewHolder.getAdapterPosition());

            NewsReaderListActivity.stayUnreadItems.add(rssItem.getId());
        }
    }

    public void toggleReadStateOfItem(RssItemViewHolder viewHolder) {
        RssItem rssItem = viewHolder.getRssItem();
        boolean isRead = !rssItem.getRead_temp();
        changeReadStateOfItem(viewHolder, isRead);
    }

    public void toggleStarredStateOfItem(RssItemViewHolder viewHolder) {
        RssItem rssItem = viewHolder.getRssItem();
        boolean isStarred = !rssItem.getStarred_temp();
        rssItem.setStarred_temp(isStarred);

        if (isStarred) {
            changeReadStateOfItem(viewHolder, true);
        }

        dbConn.updateRssItem(rssItem);
        pDelayHandler.delayTimer();

        viewHolder.setStarred(isStarred);
    }

    @Override
    public int getItemViewType(int position) {
        return lazyList.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public int getItemCount() {
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
        NewsReaderListActivity.stayUnreadItems.clear();

        cachedPages = 1;

        //if (this.lazyList != null) {
            //this.lazyList.close();
        //}
        //new ReloadAdapterAsyncTask().execute();

        setTotalItemCount(((Long) dbConn.getCurrentRssItemViewCount()).intValue());

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

        private final IOnRefreshFinished listener;

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
            Log.v(TAG, "Time needed (refreshing adapter): " + sw);

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

            sw.stop();
            Log.v(TAG, "Time needed (loading more): " + sw);
            return items;
        }

        @Override
        protected void onPostExecute(List<RssItem> rssItems) {
            int prevSize = lazyList.size();
            Log.d(TAG, "prevSize=" + prevSize);
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
            Log.v(TAG, "Reloaded CurrentRssView - time taken: " + sw);
            return holder;
        }

        @Override
        protected void onPostExecute(CurrentRssViewDataHolder holder) {
            lazyList = holder.rssItems;
            setTotalItemCount(holder.maxCount.intValue());
            cachedPages = 1;
            notifyDataSetChanged();
        }
    }
}
