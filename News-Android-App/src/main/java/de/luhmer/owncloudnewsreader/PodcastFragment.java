package de.luhmer.owncloudnewsreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.SherlockFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.ListView.PodcastArrayAdapter;
import de.luhmer.owncloudnewsreader.ListView.PodcastFeedArrayAdapter;
import de.luhmer.owncloudnewsreader.database.DatabaseConnection;
import de.luhmer.owncloudnewsreader.events.podcast.AudioPodcastClicked;
import de.luhmer.owncloudnewsreader.events.podcast.OpenPodcastEvent;
import de.luhmer.owncloudnewsreader.events.podcast.PodcastFeedClicked;
import de.luhmer.owncloudnewsreader.events.podcast.StartDownloadPodcast;
import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.events.podcast.WindPodcast;
import de.luhmer.owncloudnewsreader.model.PodcastFeedItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;
import de.luhmer.owncloudnewsreader.services.PodcastPlaybackService;
import de.luhmer.owncloudnewsreader.view.PodcastSlidingUpPanelLayout;


/**
 * A simple {@link SherlockFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PodcastFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PodcastFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PodcastFragment extends SherlockFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "PodcastFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PodcastFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PodcastFragment newInstance(String param1, String param2) {
        PodcastFragment fragment = new PodcastFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public PodcastFragment() {
        // Required empty public constructor
    }

    //Your created method
    public boolean onBackPressed() //returns if the event was handled
    {
        if(audioPodcasts != null) { //We are in the AudioPodcast view
            audioPodcasts = null;
            podcastTitleGrid.setVisibility(View.GONE);
            podcastFeedList.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }


    EventBus eventBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        eventBus = EventBus.getDefault();

        // when initialize
        //getActivity().registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);

        getActivity().startService(new Intent(getActivity(), PodcastPlaybackService.class));
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

    public void onEventMainThread(StartDownloadPodcast podcast) {
        PodcastDownloadService.startPodcastDownload(getActivity(), podcast.podcast);//, new DownloadReceiver(new Handler(), new WeakReference<ProgressBar>(holder.pbDownloadPodcast)));
    }

    /*
    private class DownloadReceiver extends ResultReceiver {
        WeakReference<ProgressBar> progressBar;

        public DownloadReceiver(Handler handler, WeakReference<ProgressBar> progressBar) {
            super(handler);

            this.progressBar = progressBar;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == PodcastDownloadService.UPDATE_PROGRESS) {

                if(progressBar.get() != null) {
                    int progress = resultData.getInt("progress");
                    progressBar.get().setIndeterminate(false);
                    progressBar.get().setProgress(progress);
                    if (progress == 100) {
                        progressBar.get().setVisibility(View.GONE);
                    }
                }
            }
        }
    }
    */

    public static String[] VIDEO_FORMATS = { "youtube" };

    public void onEventMainThread(AudioPodcastClicked podcast) {
        final PodcastItem audioPodcast = audioPodcasts.get(podcast.position);

        tvTitle.setText(audioPodcast.title);

        boolean isVideo = Arrays.asList(VIDEO_FORMATS).contains(audioPodcast.mimeType);

        if(audioPodcast.mimeType.equals("youtube") && !audioPodcast.offlineCached)
            Toast.makeText(getActivity(), "Cannot stream from youtube. Please download the video first.", Toast.LENGTH_SHORT).show();
        else {
            File file = new File(PodcastDownloadService.getUrlToPodcastFile(getActivity(), audioPodcast.link, false));
            if(file.exists())
                audioPodcast.link = file.getAbsolutePath();
            else if(!audioPodcast.offlineCached)
                Toast.makeText(getActivity(), "Starting podcast.. please wait", Toast.LENGTH_SHORT).show(); //Only show if we need to stream the file

            eventBus.post(new OpenPodcastEvent(audioPodcast.link, audioPodcast.title, isVideo));
        }
    }

    public void onEventMainThread(PodcastFeedClicked podcast) {
        DatabaseConnection dbConn = new DatabaseConnection(getActivity());
        audioPodcasts = dbConn.getListOfAudioPodcastsForFeed(getActivity(), feedsWithAudioPodcasts.get(podcast.position).itemId);


        for(int i = 0; i < audioPodcasts.size(); i++) {
            PodcastItem podcastItem = audioPodcasts.get(i);

            File podcastFile = new File(PodcastDownloadService.getUrlToPodcastFile(getActivity(), podcastItem.link, false));
            File podcastFileCache = new File(PodcastDownloadService.getUrlToPodcastFile(getActivity(), podcastItem.link, false) + ".download");
            if(podcastFile.exists())
                podcastItem.downloadProgress = PodcastItem.DOWNLOAD_COMPLETED;
            else if(podcastFileCache.exists())
                podcastItem.downloadProgress = 0;
            else
                podcastItem.downloadProgress = PodcastItem.DOWNLOAD_NOT_STARTED;
        }

        PodcastArrayAdapter mArrayAdapter = new PodcastArrayAdapter(getActivity(), audioPodcasts.toArray(new PodcastItem[audioPodcasts.size()]));
        if (podcastTitleGrid != null) {
            podcastTitleGrid.setAdapter(mArrayAdapter);
        }

        podcastTitleGrid.setVisibility(View.VISIBLE);
        podcastFeedList.setVisibility(View.GONE);


        //eventBus.post(new OpenAudioPodcastEvent(FileUtils.getPathPodcasts(getActivity()) + "/Foxes.mp4", "Test Video"));
        //eventBus.post(new OpenPodcastEvent(FileUtils.getPathPodcasts(getActivity()) + "/Aneta.mp4", "Test Video", true));

        //PodcastDownloadService.startPodcastDownload(getActivity(), new PodcastItem("5", "Blaa", "http://www.youtube.com/v/wtLJPvx7-ys?version=3&f=playlists&app=youtube_gdata", "youtube"));
    }


    public void onEventMainThread(PodcastDownloadService.DownloadProgressUpdate downloadProgress) {
        PodcastArrayAdapter podcastArrayAdapter = (PodcastArrayAdapter) podcastTitleGrid.getAdapter();

        for(int i = 0; i < podcastTitleGrid.getCount(); i++) {
            if(podcastArrayAdapter.getItem(i).link.equals(downloadProgress.podcast.link)) {

                if(podcastArrayAdapter.getItem(i).downloadProgress != downloadProgress.podcast.downloadProgress) { //If Progress changed
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
                /*
                View v = podcastTitleGrid.getChildAt(i -
                        podcastTitleGrid.getFirstVisiblePosition());
                ((ProgressBar)v.findViewById(R.id.pbDownloadPodcast)).setProgress(downloadProgress.podcast.downloadProgress);

                //podcastArrayAdapter.notifyDataSetChanged();
                return;
                */
            }
        }
    }

    /*
    private String downloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    private IntentFilter downloadCompleteIntentFilter = new IntentFilter(downloadCompleteIntentName);
    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((ArrayAdapter) podcastTitleGrid.getAdapter()).notifyDataSetChanged();
        }
    };
    */


    int lastDrawableId;
    public void onEventMainThread(UpdatePodcastStatusEvent podcast) {

        hasTitleInCache = true;

        int drawableId = podcast.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        int drawableIdDarkDesign = podcast.isPlaying() ? R.drawable.av_pause : R.drawable.av_play;

        if(lastDrawableId != drawableId) {
            lastDrawableId = drawableId;
            btnPlayPausePodcast.setBackgroundResource(drawableId);
            btnPlayPausePodcastSlider.setBackgroundResource(drawableIdDarkDesign);
        }

        int hours = (int)( podcast.getCurrent() / (1000*60*60));
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

        tvTitle.setText(podcast.getTitle());
        tvTitleSlider.setText(podcast.getTitle());

        if(podcast.isPreparingFile()) {
            if(!blockSeekbarUpdate)
                sb_progress.setIndeterminate(true);

            pb_progress.setIndeterminate(true);
        } else {
            double progress = ((double) podcast.getCurrent() / (double) podcast.getMax()) * 100d;

            if(!blockSeekbarUpdate) {
                sb_progress.setIndeterminate(false);
                sb_progress.setProgress((int) progress);
            }

            pb_progress.setIndeterminate(false);
            pb_progress.setProgress((int) progress);
        }
    }

    List<PodcastItem> audioPodcasts;
    List<PodcastFeedItem> feedsWithAudioPodcasts;

    @InjectView(R.id.btn_playPausePodcast) ImageButton btnPlayPausePodcast;
    @InjectView(R.id.btn_playPausePodcastSlider) ImageButton btnPlayPausePodcastSlider;
    @InjectView(R.id.btn_nextPodcastSlider) ImageButton btnNextPodcastSlider;
    @InjectView(R.id.btn_previousPodcastSlider) ImageButton btnPreviousPodcastSlider;


    @InjectView(R.id.tv_title) TextView tvTitle;
    @InjectView(R.id.tv_titleSlider) TextView tvTitleSlider;


    @InjectView(R.id.tv_from) TextView tvFrom;
    @InjectView(R.id.tv_to) TextView tvTo;
    @InjectView(R.id.tv_fromSlider) TextView tvFromSlider;
    @InjectView(R.id.tv_ToSlider) TextView tvToSlider;

    @InjectView(R.id.sb_progress) SeekBar sb_progress;
    @InjectView(R.id.pb_progress) ProgressBar pb_progress;

    @InjectView(R.id.podcastFeedList) ListView /* CardGridView CardListView*/ podcastFeedList;
    @InjectView(R.id.rlPodcast) RelativeLayout rlPodcast;
    @InjectView(R.id.ll_podcast_header) LinearLayout rlPodcastHeader;
    @InjectView(R.id.fl_playPausePodcastWrapper) FrameLayout playPausePodcastWrapper;
    @InjectView(R.id.podcastTitleGrid) ListView /*CardGridView*/ podcastTitleGrid;

    @InjectView(R.id.viewSwitcherProgress) ViewSwitcher /*CardGridView*/ viewSwitcherProgress;


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
        Toast.makeText(getActivity(), "This feature is not supported yet :(", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_previousPodcastSlider) void previousChapter() {
        Toast.makeText(getActivity(), "This feature is not supported yet :(", Toast.LENGTH_SHORT).show();
    }

    PodcastSlidingUpPanelLayout sliding_layout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // create ContextThemeWrapper from the original Activity Context with the custom theme
        Context context = new ContextThemeWrapper(getActivity(), R.style.Theme_Sherlock_Light_DarkActionBar);
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(context);
        // inflate using the cloned inflater, not the passed in default
        View view = localInflater.inflate(R.layout.fragment_podcast, container, false);


        //View view = inflater.inflate(R.layout.fragment_podcast, container, false);
        ButterKnife.inject(this, view);


        if(getActivity() instanceof PodcastSherlockFragmentActivity) {
            sliding_layout = ((PodcastSherlockFragmentActivity) getActivity()).getSlidingLayout();
        }

        if(sliding_layout != null) {
            sliding_layout.setSlideableView(rlPodcast);
            sliding_layout.setDragView(rlPodcastHeader);
            //sliding_layout.setEnableDragViewTouchEvents(true);

            sliding_layout.setPanelSlideListener(onPanelSlideListener);
        }




        DatabaseConnection dbConn = new DatabaseConnection(getActivity());
        feedsWithAudioPodcasts = dbConn.getListOfFeedsWithAudioPodcasts();
        PodcastFeedArrayAdapter mArrayAdapter = new PodcastFeedArrayAdapter(getActivity(), feedsWithAudioPodcasts.toArray(new PodcastFeedItem[feedsWithAudioPodcasts.size()]));
        if (podcastFeedList != null) {
            podcastFeedList.setAdapter(mArrayAdapter);
        }

        if(mArrayAdapter.getCount() > 0) {
            view.findViewById(R.id.tv_no_podcasts_available).setVisibility(View.GONE);
        }

        podcastTitleGrid.setVisibility(View.GONE);
        podcastFeedList.setVisibility(View.VISIBLE);

        sb_progress.setOnSeekBarChangeListener(onSeekBarChangeListener);

        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
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

            if(getActivity() instanceof PodcastSherlockFragmentActivity)
                ((PodcastSherlockFragmentActivity)getActivity()).togglePodcastVideoViewAnimation();
        }

        @Override
        public void onPanelExpanded(View view) {
            if(sliding_layout != null)
                sliding_layout.setDragView(viewSwitcherProgress);
            viewSwitcherProgress.setDisplayedChild(1);

            if(getActivity() instanceof PodcastSherlockFragmentActivity)
                ((PodcastSherlockFragmentActivity)getActivity()).togglePodcastVideoViewAnimation();
        }

        @Override
        public void onPanelAnchored(View view) {

        }

        @Override
        public void onPanelHidden(View view) {

        }
    };

    boolean blockSeekbarUpdate = false;
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            //Log.d(TAG, "onProgressChanged");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            blockSeekbarUpdate = true;
            Log.d(TAG, "onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            if(hasTitleInCache) {
                eventBus.post(new WindPodcast() {{
                    toPositionInPercent = seekBar.getProgress();
                }});
                blockSeekbarUpdate = false;
            }
            Log.d(TAG, "onStopTrackingTouch");
        }
    };

}
