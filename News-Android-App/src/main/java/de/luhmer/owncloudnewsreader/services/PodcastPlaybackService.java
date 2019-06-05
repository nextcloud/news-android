package de.luhmer.owncloudnewsreader.services;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import androidx.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import androidx.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.events.podcast.NewPodcastPlaybackListener;
import de.luhmer.owncloudnewsreader.events.podcast.PodcastCompletedEvent;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterVideoOutput;
import de.luhmer.owncloudnewsreader.events.podcast.SpeedPodcast;
import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.podcast.WindPodcast;
import de.luhmer.owncloudnewsreader.model.MediaItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.model.TTSItem;
import de.luhmer.owncloudnewsreader.services.podcast.MediaPlayerPlaybackService;
import de.luhmer.owncloudnewsreader.services.podcast.PlaybackService;
import de.luhmer.owncloudnewsreader.services.podcast.TTSPlaybackService;
import de.luhmer.owncloudnewsreader.view.PodcastNotification;

import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;

public class PodcastPlaybackService extends MediaBrowserServiceCompat {

    public static final String MEDIA_ITEM = "MediaItem";

    private static final String TAG = "PodcastPlaybackService";

    public static final String PLAYBACK_SPEED_FLOAT = "PLAYBACK_SPEED";
    public static final String CURRENT_PODCAST_ITEM_MEDIA_ITEM = "CURRENT_PODCAST_ITEM";

    public static final String CURRENT_PODCAST_MEDIA_TYPE = "CURRENT_PODCAST_MEDIA_TYPE";

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

    private PodcastNotification podcastNotification;

    private EventBus eventBus;
    private Handler mHandler;

    private PlaybackService mPlaybackService;
    private MediaSessionCompat mSession;

    public static final float PLAYBACK_SPEEDS[] = { 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f };
    private float currentPlaybackSpeed = 1;


    public static final int delay = 500; //In milliseconds
    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;


    public MediaItem getCurrentlyPlayingPodcast() {
        if(mPlaybackService != null) {
            return mPlaybackService.getMediaItem();
        }
        return null;
    }

    public boolean isActive() {
        return mPlaybackService != null;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String s, int i, @Nullable Bundle bundle) {
        return new MediaBrowserServiceCompat.BrowserRoot(
                getString(R.string.app_name),// Name visible in Android Auto
                null);
    }

    @Override
    public void onLoadChildren(@NonNull String s, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onLoadChildren() called with: s = [" + s + "], result = [" + result + "]");
        result.sendResult(new ArrayList<>());
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind() called with: intent = [" + intent + "]");
        if (!isActive()) {
            Log.v(TAG, "Stopping PodcastPlaybackService because of inactivity");
            stopSelf();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mSession != null) {
                mSession.release();
            }
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate PodcastPlaybackService");

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        initMediaSessions();

        podcastNotification = new PodcastNotification(this, mSession);
        mHandler = new Handler();
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        //eventBus.post(new PodcastPlaybackServiceStarted());

        setSessionToken(mSession.getSessionToken());

        Intent intent = new Intent(this, NewsReaderListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        //startForeground(PodcastNotification.NOTIFICATION_ID, podcastNotification.getNotification());

        /*
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
        */
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy PodcastPlaybackService");

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        mExecutorService.shutdown();
        podcastNotification.cancel();

        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mSession, intent);

        if (intent != null) {
            if (mPlaybackService != null) {
                mPlaybackService.destroy();
                mPlaybackService = null;
            }

            stopProgressUpdates();

            if(intent.hasExtra(MEDIA_ITEM)) {
                MediaItem mediaItem = (MediaItem) intent.getSerializableExtra(MEDIA_ITEM);

                if (mediaItem instanceof PodcastItem) {
                    //if (((PodcastItem) mediaItem).isYoutubeVideo()) {
                    //    mPlaybackService = new YoutubePlaybackService(this, podcastStatusListener, mediaItem);
                    //} else {
                        mPlaybackService = new MediaPlayerPlaybackService(this, podcastStatusListener, mediaItem);
                    //}
                } else if (mediaItem instanceof TTSItem) {
                    mPlaybackService = new TTSPlaybackService(this, podcastStatusListener, mediaItem);
                }

                updateMetadata(mediaItem);

                // Update notification after setting metadata (notification uses metadata information)
                podcastNotification.createPodcastNotification();

                mPlaybackService.playbackSpeedChanged(currentPlaybackSpeed);

                startProgressUpdates();

                requestAudioFocus();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateMetadata(MediaItem mediaItem) {
        MediaItem mi = mediaItem;
        if(mi == null) {
            mi = new PodcastItem(-1, "", "", "", "", false, null, false);
        }

        int totalDuration = 0;
        if(mPlaybackService != null) {
            totalDuration = mPlaybackService.getTotalDuration();
        }

        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mi.author)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mi.title)
                //.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, mediaItem.author) // Android Auto
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, mi.favIcon)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(mi.itemId))
                .putString(CURRENT_PODCAST_MEDIA_TYPE, getCurrentlyPlayedMediaType().toString())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, totalDuration)
                //.putLong(EXTRA_IS_EXPLICIT, EXTRA_METADATA_ENABLED_VALUE) // Android Auto
                //.putLong(EXTRA_IS_DOWNLOADED, EXTRA_METADATA_ENABLED_VALUE) // Android Auto
                .build());
    }

    /*
    private Long getVideoWidth() {
        if(mPlaybackService instanceof MediaPlayerPlaybackService) {
            return ((MediaPlayerPlaybackService)mPlaybackService).getVideoWidth();
        }
        return null;
    }
    */

    private PlaybackService.PodcastStatusListener podcastStatusListener = new PlaybackService.PodcastStatusListener() {
        @Override
        public void podcastStatusUpdated() {
            syncMediaAndPlaybackStatus();
            if(mPlaybackService != null) {
                updateMetadata(mPlaybackService.getMediaItem());
            }
        }

        @Override
        public void podcastCompleted() {
            Log.d(TAG, "Podcast completed, cleaning up");

            endCurrentMediaPlayback();

            EventBus.getDefault().post(new PodcastCompletedEvent());
        }
    };

    private void endCurrentMediaPlayback() {
        Log.d(TAG, "endCurrentMediaPlayback() called");
        stopProgressUpdates();

        // Set metadata
        updateMetadata(null);

        if(mPlaybackService != null) {
            mPlaybackService.destroy();
            mPlaybackService = null;
        }

        syncMediaAndPlaybackStatus();

        Log.d(TAG, "cancel notification");
        podcastNotification.cancel();

        abandonAudioFocus();
    }


    @Subscribe
    public void onEvent(TogglePlayerStateEvent event) {
        Log.d(TAG, "onEvent() called with: event = [" + event + "]");
        if(event.getState() == TogglePlayerStateEvent.State.Toggle) {
            if (isPlaying()) {
                Log.v(TAG, "calling pause()");
                pause();
            } else {
                Log.v(TAG, "calling play()");
                play();
            }
        } else if(event.getState() == TogglePlayerStateEvent.State.Play) {
            Log.v(TAG, "calling play()");
            play();
        } else if(event.getState() == TogglePlayerStateEvent.State.Pause) {
            Log.v(TAG, "calling pause()");
            pause();
        }
    }

    private boolean isPlaying() {
        return (mPlaybackService != null && mPlaybackService.getStatus() == PlaybackStateCompat.STATE_PLAYING);
    }

    @Subscribe
    public void onEvent(WindPodcast event) {
        if(mPlaybackService != null) {
            int seekTo = (int) (mPlaybackService.getCurrentPosition() + event.milliSeconds);
            if(seekTo < 0) {
                seekTo = 0;
            }
            mPlaybackService.seekTo(seekTo);
        }
    }

    @Subscribe
    public void onEvent(RegisterVideoOutput videoOutput) {
        if(mPlaybackService != null && mPlaybackService instanceof MediaPlayerPlaybackService) {
            ((MediaPlayerPlaybackService) mPlaybackService).setVideoView(videoOutput.surfaceView, videoOutput.parentResizableView);
        }
    }

    @Subscribe
    public void onEvent(NewPodcastPlaybackListener newListener) {
        syncMediaAndPlaybackStatus();
    }

    @Subscribe
    public void onEvent(SpeedPodcast event) {
        this.currentPlaybackSpeed = event.playbackSpeed;

        if(mPlaybackService != null) {
            mPlaybackService.playbackSpeedChanged(currentPlaybackSpeed);
        }
    }

    public void play() {
        if(mPlaybackService != null) {
            // Start playback
            mPlaybackService.play();
            startProgressUpdates();

            requestAudioFocus();
        }
    }

    public void pause() {
        if(mPlaybackService != null) {
            mPlaybackService.pause();
        }
        stopProgressUpdates();

        abandonAudioFocus();
    }


    private void requestAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback
        int result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "AUDIOFOCUS_REQUEST_GRANTED");
        }
    }

    private void abandonAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Abandon audio focus when playback complete
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Permanent loss of audio focus
            // Pause playback immediately
            mSession.getController().getTransportControls().pause();
        }
        else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            // Pause playback
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // Lower the volume, keep playing
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Your app has been granted audio focus again
            // Raise volume to normal, restart playback if necessary
        }
    };

    private void startProgressUpdates() {
        mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                () -> mHandler.post(PodcastPlaybackService.this::syncMediaAndPlaybackStatus), PROGRESS_UPDATE_INITIAL_INTERVAL,
                PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
    }

    private void stopProgressUpdates() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
        syncMediaAndPlaybackStatus(); // Send one last update
    }


    public float getPlaybackSpeed() {
        return currentPlaybackSpeed;
    }

    public void syncMediaAndPlaybackStatus() {
        /*
        if(mPlaybackService == null) {
            audioPodcastEvent = new UpdatePodcastStatusEvent(0, 0, PlaybackService.Status.NOT_INITIALIZED, "", "", PlaybackService.VideoType.None, -1, -1);
        } else {
            audioPodcastEvent = new UpdatePodcastStatusEvent(
                    mPlaybackService.getCurrentPosition(),
                    mPlaybackService.getTotalDuration(),
                    mPlaybackService.getStatus(),
                    mPlaybackService.getMediaItem().link,
                    "NOT SUPPORTED ANYMORE!!!",
                    mPlaybackService.getVideoType(),
                    mPlaybackService.getMediaItem().itemId,
                    getPlaybackSpeed());
        }

        eventBus.post(audioPodcastEvent);

        if(audioPodcastEvent.isPlaying()) {
            startForeground(PodcastNotification.NOTIFICATION_ID, podcastNotification.getNotification());
        } else {
            stopForeground(false);
        }
        */


        @PlaybackStateCompat.State int playbackState;
        int currentPosition = 0;
        int totalDuration = 0;
        if(mPlaybackService == null || mPlaybackService.getMediaItem().itemId == -1) {
            // When podcast is not initialized or playback is finished
            playbackState = PlaybackStateCompat.STATE_NONE;

            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(playbackState, currentPosition, 1.0f)
                    .setActions(buildPlaybackActions(playbackState, false))
                    .build());
            stopForeground(false);
        } else {
            currentPosition = mPlaybackService.getCurrentPosition();
            totalDuration = mPlaybackService.getTotalDuration();
            playbackState = mPlaybackService.getStatus();

            if (playbackState== PlaybackStateCompat.STATE_PLAYING) {
                startForeground(PodcastNotification.NOTIFICATION_ID, podcastNotification.getNotification());
            } else {
                stopForeground(false);
            }

            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(playbackState, currentPosition, 1.0f)
                .setActions(buildPlaybackActions(playbackState, true))
                .build());
        }

        if(playbackState == PlaybackStateCompat.STATE_PLAYING) {
            mSession.setActive(true);
        } else {
            mSession.setActive(false);
        }

        podcastNotification.updateStateOfNotification(playbackState, currentPosition, totalDuration);
    }

    private long buildPlaybackActions(int playbackState, boolean mediaLoaded) {
        long actions = playbackState == PlaybackStateCompat.STATE_PLAYING ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY;
        actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
                //PlaybackStateCompat.ACTION_STOP;

        if(mediaLoaded) {
            actions |= PlaybackStateCompat.ACTION_SEEK_TO;
        }
        return actions;
    }

    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                //Incoming call: Pause music
                pause();
            }
            /*
            else if(state == TelephonyManager.CALL_STATE_IDLE) {
                //Not in call: Play music
            } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                //A call is dialing, active or on hold
            }
            */
            super.onCallStateChanged(state, incomingNumber);
        }
    };


    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay() called");
            play();
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause() called");
            pause();
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            Log.d(TAG, "onPlayFromSearch() called with: query = [" + query + "], extras = [" + extras + "]");
            // TODO Implement this
            super.onPlayFromSearch(query, extras);
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            if (command.equals(PLAYBACK_SPEED_FLOAT)) {
                Bundle b = new Bundle();
                b.putFloat(PLAYBACK_SPEED_FLOAT, currentPlaybackSpeed);
                cb.send(0, b);
            } else if(command.equals(CURRENT_PODCAST_ITEM_MEDIA_ITEM)) {
                Bundle b = new Bundle();
                if(mPlaybackService != null) {
                    b.putSerializable(CURRENT_PODCAST_ITEM_MEDIA_ITEM, mPlaybackService.getMediaItem());
                } else {
                    b.putSerializable(CURRENT_PODCAST_ITEM_MEDIA_ITEM, null);
                }
                cb.send(0, b);
            }
            super.onCommand(command, extras, cb);
        }

        @Override
        public void onSeekTo(long pos) {
            Log.d(TAG, "onSeekTo() called with: pos = [" + pos + "]");
            super.onSeekTo(pos);
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "onSkipToNext() called");
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            Log.d(TAG, "onSkipToPrevious() called");
            super.onSkipToPrevious();
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            Log.d(TAG, mediaButtonEvent.getAction());

            if(mediaButtonEvent.hasExtra("android.intent.extra.KEY_EVENT")) {
                KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra("android.intent.extra.KEY_EVENT");
                Log.d(TAG, keyEvent.toString());

                // Stop requested (e.g. notification was swiped away)
                if(keyEvent.getKeyCode() == KEYCODE_MEDIA_STOP) {
                    pause();
                    endCurrentMediaPlayback();
                    stopSelf();
                    /*
                    boolean isPlaying = mSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
                    if(isPlaying) {
                        EventBus.getDefault().post(new TogglePlayerStateEvent());
                    }
                    */
                }
            }
            return super.onMediaButtonEvent(mediaButtonEvent);
        }
    }

    private void initMediaSessions() {

        //String packageName = PodcastNotificationToggle.class.getPackage().getName();
        //ComponentName receiver = new ComponentName(packageName, PodcastNotificationToggle.class.getName());
        ComponentName mediaButtonReceiver = new ComponentName(this, MediaButtonReceiver.class);
        mSession = new MediaSessionCompat(this, "PlayerService", mediaButtonReceiver, null);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 0)
                .setActions(buildPlaybackActions(PlaybackStateCompat.STATE_PAUSED, false)).build());

        mSession.setCallback(new MediaSessionCallback());

        //Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        //mediaButtonIntent.setClass(mContext, MediaButtonReceiver.class);
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, mediaButtonIntent, 0);
        //mSession.setMediaButtonReceiver(pendingIntent);


        updateMetadata(null);
    }

    private PlaybackService.VideoType getCurrentlyPlayedMediaType() {
        if(mPlaybackService != null) {
            return mPlaybackService.getVideoType();
        } else {
            return PlaybackService.VideoType.None;
        }
    }
}
