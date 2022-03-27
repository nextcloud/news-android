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
import android.widget.NumberPicker;
import android.widget.SeekBar;

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
import de.luhmer.owncloudnewsreader.ListView.PodcastArrayAdapter;
import de.luhmer.owncloudnewsreader.ListView.PodcastFeedArrayAdapter;
import de.luhmer.owncloudnewsreader.databinding.FragmentPodcastBinding;
import de.luhmer.owncloudnewsreader.events.podcast.CollapsePodcastView;
import de.luhmer.owncloudnewsreader.events.podcast.ExitPlayback;
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

    protected FragmentPodcastBinding binding;

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
        PodcastArrayAdapter podcastArrayAdapter = (PodcastArrayAdapter) binding.podcastTitleGrid.getAdapter();

        for(int i = 0; i < binding.podcastTitleGrid.getCount(); i++) {
            if(podcastArrayAdapter.getItem(i).link.equals(downloadProgress.podcast.link)) {

                if(!podcastArrayAdapter.getItem(i).downloadProgress.equals(downloadProgress.podcast.downloadProgress)) { //If Progress changed
                    PodcastItem pItem = podcastArrayAdapter.getItem(i);

                    if (downloadProgress.podcast.downloadProgress == 100) {
                        pItem.downloadProgress = PodcastItem.DOWNLOAD_COMPLETED;
                        File file = new File(PodcastDownloadService.getUrlToPodcastFile(getActivity(), pItem.link, false));
                        pItem.offlineCached = file.exists();
                    } else
                        pItem.downloadProgress = downloadProgress.podcast.downloadProgress;
                    binding.podcastTitleGrid.invalidateViews();
                }

                return;
            }
        }
    }

    protected void playPause() {
        eventBus.post(new TogglePlayerStateEvent());
    }

    protected void playPauseSlider() {
        playPause();
    }

    protected void windForward() {
        eventBus.post(new WindPodcast(30000));

        //Toast.makeText(getActivity(), "This feature is not supported yet :(", Toast.LENGTH_SHORT).show();
    }

    protected void windBack() {
        eventBus.post(new WindPodcast(-10000));
    }

    protected void openSpeedMenu() {
        showPlaybackSpeedPicker();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        //Context context = new ContextThemeWrapper(getActivity(), R.style.Theme_MaterialComponents_Light_DarkActionBar);
        // clone the inflater using the ContextThemeWrapper
        //LayoutInflater localInflater = inflater.cloneInContext(context);
        // inflate using the cloned inflater, not the passed in default
        //View view = localInflater.inflate(R.layout.fragment_podcast, container, false);
        binding = FragmentPodcastBinding.inflate(inflater, container, false);

        binding.flPlayPausePodcastWrapper.setOnClickListener((v) -> playPause());
        binding.btnPlayPausePodcastSlider.setOnClickListener((v) -> playPauseSlider());
        binding.btnNextPodcastSlider.setOnClickListener((v) -> windForward());
        binding.btnPreviousPodcastSlider.setOnClickListener((v) -> windBack());
        binding.btnPodcastSpeed.setOnClickListener((v) -> openSpeedMenu());

        binding.btnExitPodcast.setOnClickListener((v) -> eventBus.post(new ExitPlayback()));

        //View view = inflater.inflate(R.layout.fragment_podcast, container, false);

        if(getActivity() instanceof PodcastFragmentActivity) {
            sliding_layout = ((PodcastFragmentActivity) getActivity()).getSlidingLayout();
        }

        if(sliding_layout != null) {
            sliding_layout.setSlideableView(binding.rlPodcast);
            sliding_layout.setDragView(binding.llPodcastHeader);
            //sliding_layout.setEnableDragViewTouchEvents(true);

            sliding_layout.setPanelSlideListener(onPanelSlideListener);
        }

        PodcastFeedArrayAdapter mArrayAdapter = new PodcastFeedArrayAdapter(getActivity(), new PodcastFeedItem[0]);

        if(mArrayAdapter.getCount() > 0) {
            binding.tvNoPodcastsAvailable.setVisibility(View.GONE);
        }

        binding.podcastTitleGrid.setVisibility(View.GONE);
        binding.podcastFeedList.setVisibility(View.VISIBLE);

        binding.sbProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);

        return binding.getRoot();
    }



    private final SlidingUpPanelLayout.PanelSlideListener onPanelSlideListener = new SlidingUpPanelLayout.PanelSlideListener() {
        @Override
        public void onPanelSlide(View view, float v) { }

        @Override
        public void onPanelCollapsed(View view) {
            if(sliding_layout != null)
                sliding_layout.setDragView(binding.llPodcastHeader);
            binding.viewSwitcherProgress.setDisplayedChild(0);
        }

        @Override
        public void onPanelExpanded(View view) {
            if(sliding_layout != null)
                sliding_layout.setDragView(binding.viewSwitcherProgress);
            binding.viewSwitcherProgress.setDisplayedChild(1);
        }

        @Override public void onPanelAnchored(View view) { }

        @Override public void onPanelHidden(View view) { }
    };


    boolean blockSeekbarUpdate = false;
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
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
            blockSeekbarUpdate = true;
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            int after = seekBar.getProgress();
            long ms = Math.round((after / 100d) * maxPositionInMillis);
            Log.v(TAG, "onStopTrackingTouch - after (%): " + after + " - ms: " + ms);

            eventBus.post(new WindPodcast(ms));
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


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());

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

    private final MediaControllerCompat.Callback controllerCallback =
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
        binding.tvTitle.setText(title);
        binding.tvTitleSlider.setText(title);

        String favIconUrl = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        if(favIconUrl != null) {
            Log.d(TAG, "currentPlayingPodcastReceived: " + favIconUrl);
            DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().
                    showImageOnLoading(R.drawable.default_feed_icon_light).
                    showImageForEmptyUri(R.drawable.default_feed_icon_light).
                    showImageOnFail(R.drawable.default_feed_icon_light).
                    build();
            ImageLoader.getInstance().displayImage(favIconUrl, binding.imgFeedFavicon, displayImageOptions);
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

        int drawableId = showPlayingButton ? R.drawable.ic_action_pause : R.drawable.ic_baseline_play_arrow_24;
        int contentDescriptionId = showPlayingButton ? R.string.content_desc_pause : R.string.content_desc_play;

        // If attached to context..
        if(mActivity != null) {
            binding.btnPlayPausePodcast.setImageResource(drawableId);
            binding.btnPlayPausePodcast.setContentDescription(getString(contentDescriptionId));
            binding.btnPlayPausePodcastSlider.setImageResource(drawableId);
        }

        currentPositionInMillis = stateCompat.getPosition();

        updateProgressBar(state);
    }

    private void updateProgressBar(@PlaybackStateCompat.State int state) {
        int hours = (int)(currentPositionInMillis / (1000*60*60));
        int minutes = (int)(currentPositionInMillis % (1000*60*60)) / (1000*60);
        int seconds = (int) ((currentPositionInMillis % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        binding.tvFrom.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        binding.tvFromSlider.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        hours = (int)(maxPositionInMillis / (1000*60*60));
        minutes = (int)(maxPositionInMillis % (1000*60*60)) / (1000*60);
        seconds = (int) ((maxPositionInMillis % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        binding.tvTo.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        binding.tvToSlider.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        if(state == PlaybackStateCompat.STATE_CONNECTING) {
            binding.sbProgress.setVisibility(View.INVISIBLE);
            binding.pbProgress2.setVisibility(View.VISIBLE);

            binding.pbProgress.setIndeterminate(true);
        } else {
            double progress = ((double) currentPositionInMillis / (double) maxPositionInMillis) * 100d;

            if(!blockSeekbarUpdate) {
                binding.sbProgress.setVisibility(View.VISIBLE);
                binding.pbProgress2.setVisibility(View.INVISIBLE);
                binding.sbProgress.setProgress((int) progress);
            }

            binding.pbProgress.setIndeterminate(false);
            binding.pbProgress.setProgress((int) progress);
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

    private final MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
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
