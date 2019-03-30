package de.luhmer.owncloudnewsreader.services;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.events.podcast.NewPodcastPlaybackListener;
import de.luhmer.owncloudnewsreader.events.podcast.PodcastCompletedEvent;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterVideoOutput;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterYoutubeOutput;
import de.luhmer.owncloudnewsreader.events.podcast.SpeedPodcast;
import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.events.podcast.WindPodcast;
import de.luhmer.owncloudnewsreader.model.MediaItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.model.TTSItem;
import de.luhmer.owncloudnewsreader.services.podcast.MediaPlayerPlaybackService;
import de.luhmer.owncloudnewsreader.services.podcast.PlaybackService;
import de.luhmer.owncloudnewsreader.services.podcast.TTSPlaybackService;
import de.luhmer.owncloudnewsreader.services.podcast.YoutubePlaybackService;
import de.luhmer.owncloudnewsreader.view.PodcastNotification;

import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;

public class PodcastPlaybackService extends MediaBrowserServiceCompat {

    public static final String MEDIA_ITEM = "MediaItem";

    private static final String TAG = "PodcastPlaybackService";

    public static final String PLAYBACK_SPEED_FLOAT = "PLAYBACK_SPEED";
    public static final String CURRENT_PODCAST_ITEM_MEDIA_ITEM= "CURRENT_PODCAST_ITEM";
    private PodcastNotification podcastNotification;

    private EventBus eventBus;
    private Handler mHandler;

    private PlaybackService mPlaybackService;
    private MediaSessionCompat mSession;

    public static final float PLAYBACK_SPEEDS[] = { 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f };
    private float currentPlaybackSpeed = 1;


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
        result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!isActive()) {
            Log.v(TAG, "Stopping PodcastPlaybackService because of inactivity");
            stopSelf();
        }

        if(podcastNotification != null) {
            podcastNotification.unbind();
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

        mHandler.postDelayed(mUpdateTimeTask, 0);


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

        mHandler.removeCallbacks(mUpdateTimeTask);
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
            mHandler.removeCallbacks(mUpdateTimeTask);

            if(intent.hasExtra(MEDIA_ITEM)) {
                MediaItem mediaItem = (MediaItem) intent.getSerializableExtra(MEDIA_ITEM);

                if (mediaItem instanceof PodcastItem) {
                    if (((PodcastItem) mediaItem).isYoutubeVideo()) {
                        mPlaybackService = new YoutubePlaybackService(this, podcastStatusListener, mediaItem);
                    } else {
                        mPlaybackService = new MediaPlayerPlaybackService(this, podcastStatusListener, mediaItem);
                    }
                } else if (mediaItem instanceof TTSItem) {
                    mPlaybackService = new TTSPlaybackService(this, podcastStatusListener, mediaItem);
                }

                podcastNotification.podcastChanged();
                sendMediaStatus();

                mPlaybackService.playbackSpeedChanged(currentPlaybackSpeed);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private PlaybackService.PodcastStatusListener podcastStatusListener = new PlaybackService.PodcastStatusListener() {
        @Override
        public void podcastStatusUpdated() {
            sendMediaStatus();
        }

        @Override
        public void podcastCompleted() {
            Log.d(TAG, "Podcast completed, cleaning up");
            mHandler.removeCallbacks(mUpdateTimeTask);
            podcastNotification.cancel();

            mPlaybackService.destroy();
            mPlaybackService = null;

            EventBus.getDefault().post(new PodcastCompletedEvent());
        }
    };

    public static final int delay = 500; //In milliseconds


    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            sendMediaStatus();
            mHandler.postDelayed(this, delay);
        }
    };

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
        return (mPlaybackService != null && mPlaybackService.getStatus() == PlaybackService.Status.PLAYING);
    }

    @Subscribe
    public void onEvent(WindPodcast event) {
        if(mPlaybackService != null) {
            mPlaybackService.seekTo(event.toPositionInPercent);
        }
    }

    @Subscribe
    public void onEvent(RegisterVideoOutput videoOutput) {
        if(mPlaybackService != null && mPlaybackService instanceof MediaPlayerPlaybackService) {
            ((MediaPlayerPlaybackService) mPlaybackService).setVideoView(videoOutput.surfaceView, videoOutput.parentResizableView);
        }
    }

    @Subscribe
    public void onEvent(RegisterYoutubeOutput videoOutput) {
        if(mPlaybackService != null && mPlaybackService instanceof YoutubePlaybackService) {
            if(videoOutput.youTubePlayer == null) {
                mPlaybackService.destroy();
            } else {
                ((YoutubePlaybackService) mPlaybackService).setYoutubePlayer(videoOutput.youTubePlayer, videoOutput.wasRestored);
            }
        }
    }

    @Subscribe
    public void onEvent(NewPodcastPlaybackListener newListener) {
        sendMediaStatus();
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
            mPlaybackService.play();
        }

        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 0);
    }

    public void pause() {
        if(mPlaybackService != null) {
            mPlaybackService.pause();
        }

        mHandler.removeCallbacks(mUpdateTimeTask);
        sendMediaStatus();
    }


    public float getPlaybackSpeed() {
        return currentPlaybackSpeed;
    }

    public void sendMediaStatus() {
        UpdatePodcastStatusEvent audioPodcastEvent;

        if(mPlaybackService == null) {
            audioPodcastEvent = new UpdatePodcastStatusEvent(0, 0, PlaybackService.Status.NOT_INITIALIZED, "", "", PlaybackService.VideoType.None, -1, -1);
        } else {
            audioPodcastEvent = new UpdatePodcastStatusEvent(
                    mPlaybackService.getCurrentDuration(),
                    mPlaybackService.getTotalDuration(),
                    mPlaybackService.getStatus(),
                    mPlaybackService.getMediaItem().link,
                    mPlaybackService.getMediaItem().title,
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
    }


    //public class PodcastPlaybackServiceStarted { }

    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                //Incoming call: Pause music
                pause();
            } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                //Not in call: Play music
            } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                //A call is dialing, active or on hold
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };


    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            EventBus.getDefault().post(new TogglePlayerStateEvent());
        }

        @Override
        public void onPause() {
            EventBus.getDefault().post(new TogglePlayerStateEvent());
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            if (command.equals(PLAYBACK_SPEED_FLOAT)) {
                Bundle b = new Bundle();
                b.putFloat(PLAYBACK_SPEED_FLOAT, currentPlaybackSpeed);
                cb.send(0, b);
            } else if(command.equals(CURRENT_PODCAST_ITEM_MEDIA_ITEM)) {
                Bundle b = new Bundle();
                b.putSerializable(CURRENT_PODCAST_ITEM_MEDIA_ITEM, mPlaybackService.getMediaItem());
                cb.send(0, b);
            }
            super.onCommand(command, extras, cb);
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
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());

        mSession.setCallback(new MediaSessionCallback());

        //Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        //mediaButtonIntent.setClass(mContext, MediaButtonReceiver.class);
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, mediaButtonIntent, 0);
        //mSession.setMediaButtonReceiver(pendingIntent);


        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                // Ignore
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        //MediaControllerCompat controller = mSession.getController();

        //mSession.setActive(true);

        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
                .build());
    }
}
