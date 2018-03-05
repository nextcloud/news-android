package de.luhmer.owncloudnewsreader;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.luhmer.owncloudnewsreader.ListView.PodcastArrayAdapter;
import de.luhmer.owncloudnewsreader.ListView.PodcastFeedArrayAdapter;
import de.luhmer.owncloudnewsreader.events.podcast.StartDownloadPodcast;
import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.events.podcast.WindPodcast;
import de.luhmer.owncloudnewsreader.events.podcast.SpeedPodcast;
import de.luhmer.owncloudnewsreader.model.MediaItem;
import de.luhmer.owncloudnewsreader.model.PodcastFeedItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;
import de.luhmer.owncloudnewsreader.services.podcast.PlaybackService;
import de.luhmer.owncloudnewsreader.view.PodcastSlidingUpPanelLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PodcastFragment.OnFragmentInteractionListener} interface
 * to StartYoutubePlayer interaction events.
 * Use the {@link PodcastFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PodcastFragment extends Fragment {

    private static final String TAG = PodcastFragment.class.getCanonicalName();

    private UpdatePodcastStatusEvent podcast;
    private int lastDrawableId;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PodcastFragment.
     */
    public static PodcastFragment newInstance() {
        return new PodcastFragment();
    }
    public PodcastFragment() {
        // Required empty public constructor
    }

    //Your created method
    public boolean onBackPressed() //returns if the event was handled
    {
        return false;
    }


    EventBus eventBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        eventBus = EventBus.getDefault();
    }



    @Override
    public void onResume() {
        eventBus.register(this);

        super.onResume();
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);

        super.onPause();
    }

    @Subscribe
    public void onEvent(StartDownloadPodcast podcast) {
        PodcastDownloadService.startPodcastDownload(getActivity(), podcast.podcast);//, new DownloadReceiver(new Handler(), new WeakReference<ProgressBar>(holder.pbDownloadPodcast)));
    }

    @Subscribe
    public void onEvent(PodcastDownloadService.DownloadProgressUpdate downloadProgress) {
        PodcastArrayAdapter podcastArrayAdapter = (PodcastArrayAdapter) podcastTitleGrid.getAdapter();

        for(int i = 0; i < podcastTitleGrid.getCount(); i++) {
            if(podcastArrayAdapter.getItem(i).link.equals(downloadProgress.podcast.link)) {

                if(!podcastArrayAdapter.getItem(i).downloadProgress.equals(downloadProgress.podcast.downloadProgress)) { //If Progress changed
                    PodcastItem pItem = podcastArrayAdapter.getItem(i);

                    if (downloadProgress.podcast.downloadProgress == 100) {
                        pItem.downloadProgress = PodcastItem.DOWNLOAD_COMPLETED;
                        File file = new File(PodcastDownloadService.getUrlToPodcastFile(getActivity(), pItem.link, false));
                        pItem.offlineCached = file.exists();
                    } else
                        pItem.downloadProgress = downloadProgress.podcast.downloadProgress;
                    podcastTitleGrid.invalidateViews();
                }

                return;
            }
        }
    }

    long lastPodcastRssItemId = -1;
    @Subscribe
    public void onEvent(UpdatePodcastStatusEvent podcast) {
        this.podcast = podcast;

        hasTitleInCache = true;

        int drawableId = podcast.isPlaying() ? R.drawable.ic_action_pause : R.drawable.ic_action_play_arrow;
        int contentDescriptionId = podcast.isPlaying() ? R.string.content_desc_pause : R.string.content_desc_play;

        if(lastDrawableId != drawableId) {
            lastDrawableId = drawableId;
            btnPlayPausePodcast.setImageResource(drawableId);
            btnPlayPausePodcast.setContentDescription(getString(contentDescriptionId));
            btnPlayPausePodcastSlider.setImageResource(drawableId);
        }

        if(lastPodcastRssItemId != podcast.getRssItemId() && imgFavIcon != null) {
            if(loadPodcastFavIcon()) { //Returns false if PodcastItem is not found (e.g. Service is not connected to Activity yet)
                lastPodcastRssItemId = podcast.getRssItemId();
            }
        }

        int hours = (int)(podcast.getCurrent() / (1000*60*60));
        int minutes = (int)(podcast.getCurrent() % (1000*60*60)) / (1000*60);
        int seconds = (int) ((podcast.getCurrent() % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        tvFrom.setText(String.format("%02d:%02d", minutes, seconds));
        tvFromSlider.setText(String.format("%02d:%02d", minutes, seconds));

        hours = (int)( podcast.getMax() / (1000*60*60));
        minutes = (int)(podcast.getMax() % (1000*60*60)) / (1000*60);
        seconds = (int) ((podcast.getMax() % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        tvTo.setText(String.format("%02d:%02d", minutes, seconds));
        tvToSlider.setText(String.format("%02d:%02d", minutes, seconds));

        tvPlaybackSpeed.setText(String.format("%.02f", podcast.getSpeed()));

        tvTitle.setText(podcast.getTitle());
        tvTitleSlider.setText(podcast.getTitle());

        if(podcast.getStatus() == PlaybackService.Status.PREPARING) {
            sb_progress.setVisibility(View.INVISIBLE);
            pb_progress2.setVisibility(View.VISIBLE);

            pb_progress.setIndeterminate(true);
        } else {
            double progress = ((double) podcast.getCurrent() / (double) podcast.getMax()) * 100d;

            if(!blockSeekbarUpdate) {
                sb_progress.setVisibility(View.VISIBLE);
                pb_progress2.setVisibility(View.INVISIBLE);
                sb_progress.setProgress((int) progress);
            }

            pb_progress.setIndeterminate(false);
            pb_progress.setProgress((int) progress);
        }
    }

    private boolean loadPodcastFavIcon() {
        MediaItem podcastItem = ((PodcastFragmentActivity) getActivity()).getCurrentPlayingPodcast();
        if(podcastItem != null) {
            String favIconUrl = podcastItem.favIcon;
            DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().
                    showImageOnLoading(R.drawable.default_feed_icon_light).
                    showImageForEmptyUri(R.drawable.default_feed_icon_light).
                    showImageOnFail(R.drawable.default_feed_icon_light).
                    build();
            ImageLoader.getInstance().displayImage(favIconUrl, imgFavIcon, displayImageOptions);
        }
        return podcastItem != null;
    }



    @BindView(R.id.btn_playPausePodcast) ImageButton btnPlayPausePodcast;
    @BindView(R.id.btn_playPausePodcastSlider) ImageButton btnPlayPausePodcastSlider;
    @BindView(R.id.btn_nextPodcastSlider) ImageButton btnNextPodcastSlider;
    @BindView(R.id.btn_previousPodcastSlider) ImageButton btnPreviousPodcastSlider;

    @BindView(R.id.img_feed_favicon) ImageView imgFavIcon;

    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.tv_titleSlider) TextView tvTitleSlider;


    @BindView(R.id.tv_from) TextView tvFrom;
    @BindView(R.id.tv_to) TextView tvTo;
    @BindView(R.id.tv_fromSlider) TextView tvFromSlider;
    @BindView(R.id.tv_ToSlider) TextView tvToSlider;

    @BindView(R.id.sb_progress) SeekBar sb_progress;
    @BindView(R.id.pb_progress) ProgressBar pb_progress;
    @BindView(R.id.pb_progress2) ProgressBar pb_progress2;

    @Bind(R.id.tv_playbackSpeed) TextView tvPlaybackSpeed;
    @Bind(R.id.buttonSpeedMinus) TextView btnSpeedMinus;
    @Bind(R.id.buttonSpeedPlus) TextView btnSpeedPlus;

    @BindView(R.id.podcastFeedList) ListView /* CardGridView CardListView*/ podcastFeedList;
    @BindView(R.id.rlPodcast) RelativeLayout rlPodcast;
    @BindView(R.id.ll_podcast_header) LinearLayout rlPodcastHeader;
    @BindView(R.id.fl_playPausePodcastWrapper) FrameLayout playPausePodcastWrapper;
    @BindView(R.id.podcastTitleGrid) ListView /*CardGridView*/ podcastTitleGrid;

    @BindView(R.id.viewSwitcherProgress) ViewSwitcher /*CardGridView*/ viewSwitcherProgress;


    boolean hasTitleInCache = false;
    @OnClick(R.id.fl_playPausePodcastWrapper) void playPause() {
        if(!hasTitleInCache)
            Toast.makeText(getActivity(), "Please select a title first", Toast.LENGTH_SHORT).show();
        else
            eventBus.post(new TogglePlayerStateEvent());
    }

    @OnClick(R.id.btn_playPausePodcastSlider) void playPauseSlider() {
        playPause();
    }

    @OnClick(R.id.btn_nextPodcastSlider) void nextChapter() {
        eventBus.post(new WindPodcast() {{
            long position = podcast.getCurrent() + 30000;
            toPositionInPercent = ((double) position / (double) podcast.getMax()) * 100d;
        }});
        //Toast.makeText(getActivity(), "This feature is not supported yet :(", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_previousPodcastSlider) void previousChapter() {
        eventBus.post(new WindPodcast() {{
            long position = podcast.getCurrent() - 10000;
            toPositionInPercent = ((double) position / (double) podcast.getMax()) * 100d;
        }});
    }

    @OnClick(R.id.buttonSpeedMinus) void speedMinus() {
        eventBus.post(new SpeedPodcast() {{
            playbackSpeed = podcast.getSpeed() - 0.1f;
        }});
    }

    @OnClick(R.id.buttonSpeedPlus) void speedPlus() {
        eventBus.post(new SpeedPodcast() {{
            playbackSpeed = podcast.getSpeed() + 0.1f;
        }});
    }

    PodcastSlidingUpPanelLayout sliding_layout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        Context context = new ContextThemeWrapper(getActivity(), R.style.Theme_AppCompat_Light_DarkActionBar);
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(context);
        // inflate using the cloned inflater, not the passed in default
        View view = localInflater.inflate(R.layout.fragment_podcast, container, false);


        //View view = inflater.inflate(R.layout.fragment_podcast, container, false);
        ButterKnife.bind(this, view);


        if(getActivity() instanceof PodcastFragmentActivity) {
            sliding_layout = ((PodcastFragmentActivity) getActivity()).getSlidingLayout();
        }

        if(sliding_layout != null) {
            sliding_layout.setSlideableView(rlPodcast);
            sliding_layout.setDragView(rlPodcastHeader);
            //sliding_layout.setEnableDragViewTouchEvents(true);

            sliding_layout.setPanelSlideListener(onPanelSlideListener);
        }



        PodcastFeedArrayAdapter mArrayAdapter = new PodcastFeedArrayAdapter(getActivity(), new PodcastFeedItem[0]);

        if(mArrayAdapter.getCount() > 0) {
            view.findViewById(R.id.tv_no_podcasts_available).setVisibility(View.GONE);
        }

        podcastTitleGrid.setVisibility(View.GONE);
        podcastFeedList.setVisibility(View.VISIBLE);

        sb_progress.setOnSeekBarChangeListener(onSeekBarChangeListener);
        tvPlaybackSpeed.setOnEditorActionListener(onEditorActionListener);

        return view;
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    private SlidingUpPanelLayout.PanelSlideListener onPanelSlideListener = new SlidingUpPanelLayout.PanelSlideListener() {
        @Override
        public void onPanelSlide(View view, float v) {

        }

        @Override
        public void onPanelCollapsed(View view) {
            if(sliding_layout != null)
                sliding_layout.setDragView(rlPodcastHeader);
            viewSwitcherProgress.setDisplayedChild(0);

            if(getActivity() instanceof PodcastFragmentActivity)
                ((PodcastFragmentActivity)getActivity()).togglePodcastVideoViewAnimation();
        }

        @Override
        public void onPanelExpanded(View view) {
            if(sliding_layout != null)
                sliding_layout.setDragView(viewSwitcherProgress);
            viewSwitcherProgress.setDisplayedChild(1);

            if(getActivity() instanceof PodcastFragmentActivity)
                ((PodcastFragmentActivity)getActivity()).togglePodcastVideoViewAnimation();
        }

        @Override
        public void onPanelAnchored(View view) {

        }

        @Override
        public void onPanelHidden(View view) {

        }
    };

    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(final TextView textView, int i, KeyEvent keyEvent) {
            final float pbSpeed = Float.parseFloat(textView.getText().toString());
                if(hasTitleInCache) {
                    eventBus.post(new SpeedPodcast() {{
                        playbackSpeed = pbSpeed;
                    }});
                }
                Log.v(TAG, "playback speed changed: "+pbSpeed);
                return true;
        }
    };


    boolean blockSeekbarUpdate = false;
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            //Log.v(TAG, "onProgressChanged");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            blockSeekbarUpdate = true;
            Log.v(TAG, "onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            if(hasTitleInCache) {
                eventBus.post(new WindPodcast() {{
                    toPositionInPercent = seekBar.getProgress();
                }});
                blockSeekbarUpdate = false;
            }
            Log.v(TAG, "onStopTrackingTouch");
        }
    };

}
