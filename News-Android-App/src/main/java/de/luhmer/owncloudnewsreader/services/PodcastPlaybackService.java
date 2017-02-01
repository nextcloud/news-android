package de.luhmer.owncloudnewsreader.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import de.luhmer.owncloudnewsreader.events.podcast.NewPodcastPlaybackListener;
import de.luhmer.owncloudnewsreader.events.podcast.PodcastCompletedEvent;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterVideoOutput;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterYoutubeOutput;
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

public class PodcastPlaybackService extends Service {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public MediaItem getCurrentlyPlayingPodcast() {
        if(mPlaybackService != null) {
            return mPlaybackService.getMediaItem();
        }
        return null;
    }

    public boolean isActive() {
        return mPlaybackService != null;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public PodcastPlaybackService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PodcastPlaybackService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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

    public static final String MEDIA_ITEM = "MediaItem";

    private static final String TAG = "PodcastPlaybackService";
    private PodcastNotification podcastNotification;

    private EventBus eventBus;
    private Handler mHandler;

    private PlaybackService mPlaybackService;


    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate PodcastPlaybackService");

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        podcastNotification = new PodcastNotification(this);
        mHandler = new Handler();
        eventBus = EventBus.getDefault();
        eventBus.register(this);
        eventBus.post(new PodcastPlaybackServiceStarted());

        mHandler.postDelayed(mUpdateTimeTask, 0);

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy PodcastPlaybackService");

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        podcastNotification.cancel();

        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if(mPlaybackService != null) {
                mPlaybackService.destroy();
                mPlaybackService = null;
            }

            MediaItem mediaItem = (MediaItem) intent.getSerializableExtra(MEDIA_ITEM);

            if (mediaItem instanceof PodcastItem) {
                if(((PodcastItem)mediaItem).isYoutubeVideo()) {
                    mPlaybackService = new YoutubePlaybackService(this, podcastStatusListener, mediaItem);
                } else {
                    mPlaybackService = new MediaPlayerPlaybackService(this, podcastStatusListener, mediaItem);
                }
            } else if(mediaItem instanceof TTSItem) {
                mPlaybackService = new TTSPlaybackService(this, podcastStatusListener, mediaItem);
            }

            podcastNotification.podcastChanged();
            sendMediaStatus();
        }

        return Service.START_STICKY;
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

    public void sendMediaStatus() {
        UpdatePodcastStatusEvent audioPodcastEvent;

        if(mPlaybackService == null) {
            audioPodcastEvent = new UpdatePodcastStatusEvent(0, 0, PlaybackService.Status.NOT_INITIALIZED, "", PlaybackService.VideoType.None, -1);
        } else {
            audioPodcastEvent = new UpdatePodcastStatusEvent(
                    mPlaybackService.getCurrentDuration(),
                    mPlaybackService.getTotalDuration(),
                    mPlaybackService.getStatus(),
                    mPlaybackService.getMediaItem().title,
                    mPlaybackService.getVideoType(),
                    mPlaybackService.getMediaItem().itemId);
        }
        eventBus.post(audioPodcastEvent);
    }


    public class PodcastPlaybackServiceStarted {

    }

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
}
