package de.luhmer.owncloudnewsreader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.luhmer.owncloudnewsreader.ListView.PodcastArrayAdapter;
import de.luhmer.owncloudnewsreader.ListView.PodcastFeedArrayAdapter;
import de.luhmer.owncloudnewsreader.events.podcast.CollapsePodcastView;
import de.luhmer.owncloudnewsreader.events.podcast.ExpandPodcastView;
import de.luhmer.owncloudnewsreader.events.podcast.SpeedPodcast;
import de.luhmer.owncloudnewsreader.events.podcast.StartDownloadPodcast;
import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.podcast.WindPodcast;
import de.luhmer.owncloudnewsreader.model.PodcastFeedItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.services.PodcastDownloadService;
import de.luhmer.owncloudnewsreader.services.PodcastPlaybackService;
import de.luhmer.owncloudnewsreader.services.podcast.PlaybackService;
import de.luhmer.owncloudnewsreader.view.PodcastSlidingUpPanelLayout;

import static android.media.MediaMetadata.METADATA_KEY_MEDIA_ID;
import static de.luhmer.owncloudnewsreader.services.PodcastPlaybackService.CURRENT_PODCAST_MEDIA_TYPE;
import static de.luhmer.owncloudnewsreader.services.PodcastPlaybackService.PLAYBACK_SPEED_FLOAT;


/**
 * Use the {@link PodcastFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PodcastFragment extends Fragment {

    private static final String TAG = PodcastFragment.class.getCanonicalName();
    //private static UpdatePodcastStatusEvent podcast; // Retain over different instances

    private PodcastSlidingUpPanelLayout sliding_layout;
    private EventBus eventBus;
    private MediaBrowserCompat mMediaBrowser;
    private Activity mActivity;

    private long currentPositionInMillis = 0;
    private long maxPositionInMillis = 100000;

    protected @BindView(R.id.btn_playPausePodcast) ImageButton btnPlayPausePodcast;
    protected @BindView(R.id.btn_playPausePodcastSlider) ImageButton btnPlayPausePodcastSlider;
    protected @BindView(R.id.btn_nextPodcastSlider) ImageButton btnNextPodcastSlider;
    protected @BindView(R.id.btn_previousPodcastSlider) ImageButton btnPreviousPodcastSlider;
    protected @BindView(R.id.btn_podcastSpeed) ImageButton btnPlaybackSpeed;

    protected @BindView(R.id.img_feed_favicon) ImageView imgFavIcon;

    protected @BindView(R.id.tv_title) TextView tvTitle;
    protected @BindView(R.id.tv_titleSlider) TextView tvTitleSlider;

    protected @BindView(R.id.tv_from) TextView tvFrom;
    protected @BindView(R.id.tv_to) TextView tvTo;
    protected @BindView(R.id.tv_fromSlider) TextView tvFromSlider;
    protected @BindView(R.id.tv_ToSlider) TextView tvToSlider;

    protected @BindView(R.id.sb_progress) SeekBar sb_progress;
    protected @BindView(R.id.pb_progress) ProgressBar pb_progress;
    protected @BindView(R.id.pb_progress2) ProgressBar pb_progress2;

    protected @BindView(R.id.podcastFeedList) ListView /* CardGridView CardListView*/ podcastFeedList;
    protected @BindView(R.id.rlPodcast) RelativeLayout rlPodcast;
    protected @BindView(R.id.ll_podcast_header) LinearLayout rlPodcastHeader;
    protected @BindView(R.id.fl_playPausePodcastWrapper) FrameLayout playPausePodcastWrapper;
    protected @BindView(R.id.podcastTitleGrid) ListView /*CardGridView*/ podcastTitleGrid;

    protected @BindView(R.id.viewSwitcherProgress) ViewSwitcher /*CardGridView*/ viewSwitcherProgress;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PodcastFragment.
     */
    public static PodcastFragment newInstance() {
        return new PodcastFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        eventBus = EventBus.getDefault();
    }

    @Override
    public void onResume() {
        eventBus.register(this);
        super.onResume();
        //mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        mMediaBrowser = new MediaBrowserCompat(mActivity,
                new ComponentName(mActivity, PodcastPlaybackService.class),
                mConnectionCallbacks,
                null); // optional Bundle
        mMediaBrowser.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        // (see "stay in sync with the MediaSession")
        if (MediaControllerCompat.getMediaController(mActivity) != null) {
            MediaControllerCompat.getMediaController(mActivity).unregisterCallback(mediaControllerCallback);
            MediaControllerCompat.getMediaController(mActivity).unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    protected void tryOpeningPictureinPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //moveTaskToBack(false /* nonRoot */);

            if(!PiPVideoPlaybackActivity.activityIsRunning) {
                Intent intent = new Intent(getActivity(), PiPVideoPlaybackActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
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

    @OnClick(R.id.fl_playPausePodcastWrapper)
    protected void playPause() {
        eventBus.post(new TogglePlayerStateEvent());
    }

    @OnClick(R.id.btn_playPausePodcastSlider)
    protected void playPauseSlider() {
        playPause();
    }

    @OnClick(R.id.btn_nextPodcastSlider)
    protected void windForward() {
        eventBus.post(new WindPodcast(30000));

        //Toast.makeText(getActivity(), "This feature is not supported yet :(", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_previousPodcastSlider)
    protected void windBack() {
        eventBus.post(new WindPodcast(-10000));
    }

    @OnClick(R.id.btn_podcastSpeed)
    protected void openSpeedMenu() {
        showPlaybackSpeedPicker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        //Context context = new ContextThemeWrapper(getActivity(), R.style.Theme_MaterialComponents_Light_DarkActionBar);
        // clone the inflater using the ContextThemeWrapper
        //LayoutInflater localInflater = inflater.cloneInContext(context);
        // inflate using the cloned inflater, not the passed in default
        //View view = localInflater.inflate(R.layout.fragment_podcast, container, false);
        View view = inflater.inflate(R.layout.fragment_podcast, container, false);

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

        return view;
    }



    private SlidingUpPanelLayout.PanelSlideListener onPanelSlideListener = new SlidingUpPanelLayout.PanelSlideListener() {
        @Override
        public void onPanelSlide(View view, float v) { }

        @Override
        public void onPanelCollapsed(View view) {
            if(sliding_layout != null)
                sliding_layout.setDragView(rlPodcastHeader);
            viewSwitcherProgress.setDisplayedChild(0);
        }

        @Override
        public void onPanelExpanded(View view) {
            if(sliding_layout != null)
                sliding_layout.setDragView(viewSwitcherProgress);
            viewSwitcherProgress.setDisplayedChild(1);
        }

        @Override public void onPanelAnchored(View view) { }

        @Override public void onPanelHidden(View view) { }
    };


    boolean blockSeekbarUpdate = false;
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        int before;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            /*
            if(fromUser) {
                Log.v(TAG, "onProgressChanged: " + progress + "%");
                before = progress;
            }
            */
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.v(TAG, "onStartTrackingTouch");
            before = seekBar.getProgress();
            blockSeekbarUpdate = true;
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            Log.v(TAG, "onStopTrackingTouch");
            int diffInSeconds = seekBar.getProgress() - before;
            eventBus.post(new WindPodcast(diffInSeconds));
            blockSeekbarUpdate = false;
        }
    };
    // TODO SEEK DOES NOT WORK PROPERLY!!!!


    private void showPlaybackSpeedPicker() {
        final NumberPicker numberPicker = new NumberPicker(getContext());
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(PodcastPlaybackService.PLAYBACK_SPEEDS.length-1);
        numberPicker.setFormatter(i -> String.valueOf(PodcastPlaybackService.PLAYBACK_SPEEDS[i]));

        if(getActivity() instanceof PodcastFragmentActivity) {
            getCurrentPlaybackSpeed(playbackSpeed -> {
                int position = Arrays.binarySearch(PodcastPlaybackService.PLAYBACK_SPEEDS, playbackSpeed);
                numberPicker.setValue(position);
            });

        } else {
            numberPicker.setValue(3);
        }
        numberPicker.setWrapSelectorWheel(false);


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder.setTitle(getString(R.string.podcast_playback_speed_dialog_title));

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), (dialog, id) -> {
                    float speed = PodcastPlaybackService.PLAYBACK_SPEEDS[numberPicker.getValue()];
                    eventBus.post(new SpeedPodcast(speed));
                    dialog.cancel();
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialog, id) -> dialog.cancel())
                .setView(numberPicker);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


        // Code below is required to fix bug in Android (default value is not shown) (https://stackoverflow.com/a/30859583)
        try {
            Field f = NumberPicker.class.getDeclaredField("mInputText");
            f.setAccessible(true);
            EditText inputText = (EditText) f.get(numberPicker);
            inputText.setFilters(new InputFilter[0]);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private MediaControllerCompat.Callback controllerCallback =
        new MediaControllerCompat.Callback() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                Log.v(TAG, "onMetadataChanged() called with: metadata = [" + metadata + "]");
                displayMetadata(metadata);
            }

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat stateCompat) {
                Log.v(TAG, "onPlaybackStateChanged() called with: state = [" + stateCompat + "]");
                displayPlaybackState(stateCompat);
            }
        };


    private void displayMetadata(MediaMetadataCompat metadata) {
        String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String author = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        if(author != null) {
            title += " - " + author;
        }
        tvTitle.setText(title);
        tvTitleSlider.setText(title);

        String favIconUrl = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        if(favIconUrl != null) {
            Log.d(TAG, "currentPlayingPodcastReceived: " + favIconUrl);
            DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().
                    showImageOnLoading(R.drawable.default_feed_icon_light).
                    showImageForEmptyUri(R.drawable.default_feed_icon_light).
                    showImageOnFail(R.drawable.default_feed_icon_light).
                    build();
            ImageLoader.getInstance().displayImage(favIconUrl, imgFavIcon, displayImageOptions);
        }

        PlaybackService.VideoType mediaType = PlaybackService.VideoType.valueOf(metadata.getString(CURRENT_PODCAST_MEDIA_TYPE));

        if("-1".equals(metadata.getString(METADATA_KEY_MEDIA_ID))) {
            // Collapse if no podcast is loaded
            eventBus.post(new CollapsePodcastView());
        } else {
            // Expand if podcast is loaded
            eventBus.post(new ExpandPodcastView());

            if (mediaType == PlaybackService.VideoType.Video) {
                Log.v(TAG, "init regular video");
                tryOpeningPictureinPictureMode();
            }
        }

        maxPositionInMillis = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
    }

    private void displayPlaybackState(PlaybackStateCompat stateCompat) {
        boolean showPlayingButton = false;

        int state = stateCompat.getState();
        if(PlaybackStateCompat.STATE_PLAYING == state ||
                PlaybackStateCompat.STATE_BUFFERING == state ||
                PlaybackStateCompat.STATE_CONNECTING == state ||
                PlaybackStateCompat.STATE_PAUSED == state) {
            //Log.v(TAG, "State is: " + state);

            if (PlaybackStateCompat.STATE_PAUSED != state) {
                showPlayingButton = true;
            }
        }

        int drawableId = showPlayingButton ? R.drawable.ic_action_pause : R.drawable.ic_action_play;
        int contentDescriptionId = showPlayingButton ? R.string.content_desc_pause : R.string.content_desc_play;

        // If attached to context..
        if(mActivity != null) {
            btnPlayPausePodcast.setImageResource(drawableId);
            btnPlayPausePodcast.setContentDescription(getString(contentDescriptionId));
            btnPlayPausePodcastSlider.setImageResource(drawableId);
        }

        currentPositionInMillis = stateCompat.getPosition();

        updateProgressBar(state);
    }

    private void updateProgressBar(@PlaybackStateCompat.State int state) {
        int hours = (int)(currentPositionInMillis / (1000*60*60));
        int minutes = (int)(currentPositionInMillis % (1000*60*60)) / (1000*60);
        int seconds = (int) ((currentPositionInMillis % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        tvFrom.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        tvFromSlider.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        hours = (int)(maxPositionInMillis / (1000*60*60));
        minutes = (int)(maxPositionInMillis % (1000*60*60)) / (1000*60);
        seconds = (int) ((maxPositionInMillis % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        tvTo.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        tvToSlider.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        if(state == PlaybackStateCompat.STATE_CONNECTING) {
            sb_progress.setVisibility(View.INVISIBLE);
            pb_progress2.setVisibility(View.VISIBLE);

            pb_progress.setIndeterminate(true);
        } else {
            double progress = ((double) currentPositionInMillis / (double) maxPositionInMillis) * 100d;

            if(!blockSeekbarUpdate) {
                sb_progress.setVisibility(View.VISIBLE);
                pb_progress2.setVisibility(View.INVISIBLE);
                sb_progress.setProgress((int) progress);
            }

            pb_progress.setIndeterminate(false);
            pb_progress.setProgress((int) progress);
        }
    }

    // https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowser-client#customize-mediabrowser-connectioncallback
    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "onConnected() called");

                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    try {
                        // Create a MediaControllerCompat
                        MediaControllerCompat mediaController = new MediaControllerCompat(mActivity, token);

                        // Save the controller
                        MediaControllerCompat.setMediaController(mActivity, mediaController);

                        // Register a Callback to stay in sync
                        mediaController.registerCallback(controllerCallback);

                        // Display the initial state
                        MediaMetadataCompat metadata = mediaController.getMetadata();
                        PlaybackStateCompat pbState = mediaController.getPlaybackState();
                        displayMetadata(metadata);
                        displayPlaybackState(pbState);

                        // Finish building the UI
                        //buildTransportControls();
                    } catch (RemoteException e) {
                        Log.e(TAG, "Connecting to podcast service failed!", e);
                    }
                }

                @Override
                public void onConnectionSuspended() {
                    Log.d(TAG, "onConnectionSuspended() called");
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                }

                @Override
                public void onConnectionFailed() {
                    Log.e(TAG, "onConnectionFailed() called");
                    // The Service has refused our connection
                }
            };



    public void getCurrentPlaybackSpeed(final OnPlaybackSpeedCallback callback) {
        MediaControllerCompat.getMediaController(mActivity)
                .sendCommand(PLAYBACK_SPEED_FLOAT,
                        null,
                        new ResultReceiver(new Handler()) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                callback.currentPlaybackReceived(resultData.getFloat(PLAYBACK_SPEED_FLOAT));
                            }
                        });
    }


    /*
    public boolean getCurrentPlayingPodcast(final OnCurrentPlayingPodcastCallback callback) {
        if(mMediaBrowser != null && mMediaBrowser.isConnected()) {
            MediaControllerCompat.getMediaController(mActivity)
                    .sendCommand(CURRENT_PODCAST_ITEM_MEDIA_ITEM,
                            null,
                            new ResultReceiver(new Handler()) {
                                @Override
                                protected void onReceiveResult(int resultCode, Bundle resultData) {
                                    callback.currentPlayingPodcastReceived((MediaItem) resultData.getSerializable(CURRENT_PODCAST_ITEM_MEDIA_ITEM));
                                }
                            });
            return true;
        } else {
            return false;
        }
    }
    */

    private MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onSessionReady() {
            Log.d(TAG, "onSessionReady() called");
            super.onSessionReady();
        }

        @Override
        public void onSessionDestroyed() {
            Log.d(TAG, "onSessionDestroyed() called");
            super.onSessionDestroyed();
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            Log.d(TAG, "onSessionEvent() called with: event = [" + event + "], extras = [" + extras + "]");
            super.onSessionEvent(event, extras);
        }
    };


    public interface OnPlaybackSpeedCallback {
        void currentPlaybackReceived(float playbackSpeed);
    }

    /*
    public interface OnCurrentPlayingPodcastCallback {
        void currentPlayingPodcastReceived(MediaItem mediaItem);
    }*/
}
