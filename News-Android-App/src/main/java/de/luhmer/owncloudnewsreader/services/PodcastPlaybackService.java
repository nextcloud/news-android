package de.luhmer.owncloudnewsreader.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.events.podcast.NewPodcastPlaybackListener;
import de.luhmer.owncloudnewsreader.events.podcast.RegisterVideoOutput;
import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.events.podcast.WindPodcast;
import de.luhmer.owncloudnewsreader.model.PodcastItem;
import de.luhmer.owncloudnewsreader.view.PodcastNotification;

public class PodcastPlaybackService extends Service {

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




    private PodcastItem mCurrentlyPlayingPodcast;
    private static final String TAG = "PodcastPlaybackService";
    PodcastNotification podcastNotification;

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate PodcastPlaybackService");

        podcastNotification = new PodcastNotification(this);

        mediaTitle = getString(R.string.no_podcast_selected);

        super.onCreate();
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
            }
        });


        eventBus.post(new PodcastPlaybackServiceStarted());

        mHandler.postDelayed(mUpdateTimeTask, 0);

        //openFile("/sdcard/Music/#Musik/Finest Tunes/Netsky - Running Low (Ft. Beth Ditto).mp3");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }


    private EventBus eventBus;
    private Handler mHandler;
    private MediaPlayer mMediaPlayer;
    private String mediaTitle;
    View parentResizableView;

    public static final int delay = 500; //In milliseconds

    private boolean canCallGetDuration = false;//Otherwise the player would call getDuration all the time without loading a media file
    private boolean isPreparing = false;
    private boolean isVideoFile = false;

    public void openFile(PodcastItem podcastItem) {
        this.mCurrentlyPlayingPodcast = podcastItem;

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
        if(mMediaPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
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
        try {
            int progress = mMediaPlayer.getCurrentPosition() / mMediaPlayer.getDuration();
            if (progress >= 1) {
                mMediaPlayer.seekTo(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        mMediaPlayer.start();

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
        if(mMediaPlayer.isPlaying())
            mMediaPlayer.pause();

        mHandler.removeCallbacks(mUpdateTimeTask);
        sendMediaStatus();
    }

    public void sendMediaStatus() {
        long totalDuration = 0;
        long currentDuration = 0;
        if(!isPreparing && canCallGetDuration) {
            totalDuration = mMediaPlayer.getDuration();
            currentDuration = mMediaPlayer.getCurrentPosition();
        }

            /*
            // Displaying Total Duration time
            songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);
            */

        long currentRssItemId = -1;
        if(mCurrentlyPlayingPodcast != null)
            currentRssItemId = mCurrentlyPlayingPodcast.itemId;

        UpdatePodcastStatusEvent audioPodcastEvent = new UpdatePodcastStatusEvent(currentDuration, totalDuration, mMediaPlayer.isPlaying(), mediaTitle, isPreparing, canCallGetDuration, isVideoFile, currentRssItemId);
        eventBus.post(audioPodcastEvent);
    }


    public class PodcastPlaybackServiceStarted {

    }

    /*
    public class VideoAvailableState {
        public VideoAvailableState(boolean isVideoAvailable) {
            this.isVideoAvailable = isVideoAvailable;
        }

        public boolean isVideoAvailable;
    }
    */



    int mSurfaceWidth;
    int mSurfaceHeight;
    SurfaceHolder mSurfaceHolder;
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback()
    {
        public void surfaceChanged(SurfaceHolder holder, int format, int surfaceWidth, int surfaceHeight)
        {
            mSurfaceWidth = surfaceWidth;
            mSurfaceHeight = surfaceHeight;

            //populateVideo();

            //Log.d(TAG, "surfaceChanged");

            /*
            if (!isPreparing && mVideoWidth == w && mVideoHeight == h) {
                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                }
                mMediaPlayer.start();
                if (mMediaController != null) {
                    mMediaController.show();
                }
            }
            */
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
