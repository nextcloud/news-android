package de.luhmer.owncloudnewsreader.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.events.podcast.NewPodcastPlaybackListener;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterVideoOutput;
import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.events.podcast.WindPodcast;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.model.TTSItem;
import de.luhmer.owncloudnewsreader.view.PodcastNotification;

public class PodcastPlaybackService extends Service implements TextToSpeech.OnInitListener {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public PodcastItem getCurrentlyPlayingPodcast() {
        return mCurrentlyPlayingPodcast;
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
        if (mCurrentlyPlayingPodcast == null && mCurrentlyPlayingTTS == null) {
            Log.v(TAG, "Stopping PodcastPlaybackService because of inactivity");
            stopSelf();
        }
        return super.onUnbind(intent);
    }

    public static final String PODCAST_ITEM = "PODCAST_ITEM";
    public static final String TTS_ITEM = "TTS_ITEM";

    private PodcastItem mCurrentlyPlayingPodcast;
    private TTSItem mCurrentlyPlayingTTS;

    private static final String TAG = "PodcastPlaybackService";
    private PodcastNotification podcastNotification;

    private EventBus eventBus;
    private Handler mHandler;
    private MediaPlayer mMediaPlayer;
    private TextToSpeech ttsController;
    private String mediaTitle;
    private PlaybackType mPlaybackType;
    private View parentResizableView;

    private enum PlaybackType { PODCAST, TTS }

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate PodcastPlaybackService");

        podcastNotification = new PodcastNotification(this);
        mediaTitle = getString(R.string.no_podcast_selected);

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy PodcastPlaybackService");

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        super.onDestroy();
    }

    public PodcastPlaybackService() {
        mMediaPlayer = new MediaPlayer();
        mHandler = new Handler();
        eventBus = EventBus.getDefault();

        eventBus.register(this);

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                isPreparing = false;
                Toast.makeText(PodcastPlaybackService.this, "Failed to open podcast", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                play();
                isPreparing = false;
                canCallGetDuration = true;
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                pause();//Send the over signal
                podcastNotification.cancel();
            }
        });


        eventBus.post(new PodcastPlaybackServiceStarted());

        mHandler.postDelayed(mUpdateTimeTask, 0);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.hasExtra(PODCAST_ITEM)) {
                openFile((PodcastItem) intent.getSerializableExtra(PODCAST_ITEM));
            } else if(intent.hasExtra(TTS_ITEM)) {
                openTtsFeed((TTSItem) intent.getSerializableExtra(TTS_ITEM));
            }
        }
        return Service.START_STICKY;
    }


    public static final int delay = 500; //In milliseconds

    private boolean canCallGetDuration = false;//Otherwise the player would call getDuration all the time without loading a media file
    private boolean isPreparing = false;
    private boolean isVideoFile = false;

    public void openTtsFeed(TTSItem textToSpeechItem) {
        this.mCurrentlyPlayingTTS = textToSpeechItem;
        this.mCurrentlyPlayingPodcast = null;

        this.mPlaybackType = PlaybackType.TTS;
        this.isVideoFile = false;

        try {
            if(mMediaPlayer.isPlaying())
                pause();

            this.mediaTitle = textToSpeechItem.title;

            isPreparing = true;
            mHandler.postDelayed(mUpdateTimeTask, 0);

            if(ttsController == null)
                ttsController = new TextToSpeech(this, this);
            else
                onInit(TextToSpeech.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            /*
            int result = ttsController.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                ttsController.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }*/

            ttsController.speak(mCurrentlyPlayingTTS.text, TextToSpeech.QUEUE_FLUSH, null);

            isPreparing = false;

        } else {
            Log.e("TTS", "Initilization Failed!");
            ttsController = null;
        }
    }

    public void openFile(PodcastItem podcastItem) {
        this.mPlaybackType = PlaybackType.PODCAST;
        this.mCurrentlyPlayingPodcast = podcastItem;
        this.mCurrentlyPlayingTTS = null;

        this.isVideoFile = podcastItem.isVideoPodcast;
        try {
            if(mMediaPlayer.isPlaying())
                pause();

            this.mediaTitle = podcastItem.title;

            isPreparing = true;
            mHandler.postDelayed(mUpdateTimeTask, 0);

            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(podcastItem.link);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            isPreparing = false;
        }
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            sendMediaStatus();

            mHandler.postDelayed(this, delay);
        }
    };

    public void onEvent(TogglePlayerStateEvent event) {
        if (isPlaying()) {
            Log.v(TAG, "calling pause()");
            pause();
        } else {
            Log.v(TAG, "calling play()");
            play();
        }
    }

    private boolean isPlaying() {
        return (mPlaybackType == PlaybackType.PODCAST && mMediaPlayer.isPlaying()) || //If podcast is running
                mPlaybackType == PlaybackType.TTS && ttsController.isSpeaking(); // or if tts is running
    }

    public void onEvent(WindPodcast event) {
        if(mMediaPlayer != null) {
            double totalDuration = mMediaPlayer.getDuration();
            int position = (int)((totalDuration / 100d) * event.toPositionInPercent);
            mMediaPlayer.seekTo(position);
        }
    }

    /*
    public void onEventBackgroundThread(OpenPodcastEvent event) {
        this.isVideoFile = event.isVideoFile;
        openFile(event.pathToFile, event.mediaTitle);
    }
    */

    public void onEvent(RegisterVideoOutput videoOutput) {
        if(mMediaPlayer != null) {
            if(videoOutput.surfaceView == null) {
                mMediaPlayer.setDisplay(null);
                Log.d(TAG, "Disable Screen output!");

                mMediaPlayer.setScreenOnWhilePlaying(false);
            } else {
                if(videoOutput.surfaceView.getHolder() != mSurfaceHolder) {
                    parentResizableView = videoOutput.parentResizableView;

                    videoOutput.surfaceView.getHolder().addCallback(mSHCallback);
                    //videoOutput.surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

                    populateVideo();

                    Log.d(TAG, "Enable Screen output!");
                }
            }
        }
    }

    public void onEvent(NewPodcastPlaybackListener newListener) {
        sendMediaStatus();
    }




    public void play() {
        if(mPlaybackType == PlaybackType.PODCAST) {
            try {
                int progress = mMediaPlayer.getCurrentPosition() / mMediaPlayer.getDuration();
                if (progress >= 1) {
                    mMediaPlayer.seekTo(0);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mMediaPlayer.start();
        } else {
            onInit(TextToSpeech.SUCCESS);//restart last tts
        }

        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 0);

        populateVideo();
    }

    private void populateVideo() {
        double videoHeightRel = (double) mSurfaceWidth / (double) mMediaPlayer.getVideoWidth();
        int videoHeight = (int) (mMediaPlayer.getVideoHeight() * videoHeightRel);

        if (mSurfaceWidth != 0 && videoHeight != 0 && mSurfaceHolder != null) {
            //mSurfaceHolder.setFixedSize(mSurfaceWidth, videoHeight);

            parentResizableView.getLayoutParams().height = videoHeight;
            parentResizableView.setLayoutParams(parentResizableView.getLayoutParams());
        }
    }

    public void pause() {
        if(mPlaybackType == PlaybackType.PODCAST) {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.pause();
        }

        if(mPlaybackType == PlaybackType.TTS) {
            if (ttsController.isSpeaking()) {
                ttsController.stop();

                //The tts service needs a few ms's to end playing. So wait 100ms
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        mHandler.removeCallbacks(mUpdateTimeTask);
        sendMediaStatus();
    }

    public void sendMediaStatus() {
        long totalDuration = 0;
        long currentDuration = 0;

        if(mPlaybackType == PlaybackType.PODCAST) {
            if (!isPreparing && canCallGetDuration) {
                totalDuration = mMediaPlayer.getDuration();
                currentDuration = mMediaPlayer.getCurrentPosition();
            }

            long currentRssItemId = -1;
            if (mCurrentlyPlayingPodcast != null)
                currentRssItemId = mCurrentlyPlayingPodcast.itemId;

            UpdatePodcastStatusEvent audioPodcastEvent = new UpdatePodcastStatusEvent(currentDuration, totalDuration, mMediaPlayer.isPlaying(), mediaTitle, isPreparing, canCallGetDuration, isVideoFile, currentRssItemId);
            eventBus.post(audioPodcastEvent);
        } else if(mPlaybackType == PlaybackType.TTS) {
            UpdatePodcastStatusEvent audioPodcastEvent = new UpdatePodcastStatusEvent(0, 0, ttsController.isSpeaking(), mediaTitle, isPreparing, true, false, mCurrentlyPlayingTTS.itemId);
            eventBus.post(audioPodcastEvent);
        }
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




    int mSurfaceWidth;
    int mSurfaceHeight;
    SurfaceHolder mSurfaceHolder;
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback()
    {
        public void surfaceChanged(SurfaceHolder holder, int format, int surfaceWidth, int surfaceHeight)
        {
            mSurfaceWidth = surfaceWidth;
            mSurfaceHeight = surfaceHeight;
        }

        public void surfaceCreated(SurfaceHolder holder)
        {
            mSurfaceHolder = holder;
            mMediaPlayer.setDisplay(mSurfaceHolder);

            mMediaPlayer.setScreenOnWhilePlaying(true);

            Log.d(TAG, "surfaceCreated");
        }

        public void surfaceDestroyed(SurfaceHolder holder)
        {
            Log.d(TAG, "surfaceDestroyed");
        }
    };


}
