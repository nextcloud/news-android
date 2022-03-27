package de.luhmer.owncloudnewsreader.services.podcast;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import de.luhmer.owncloudnewsreader.model.MediaItem;
import de.luhmer.owncloudnewsreader.model.PodcastItem;

/**
 * Created by david on 31.01.17.
 */

public class MediaPlayerPlaybackService extends PlaybackService {
    private static final String TAG = MediaPlayerPlaybackService.class.getCanonicalName();
    private final MediaPlayer mMediaPlayer;
    //private View parentView;

    public MediaPlayerPlaybackService(final Context context, PodcastStatusListener podcastStatusListener, MediaItem mediaItem) {
        super(podcastStatusListener, mediaItem);

        mMediaPlayer = new MediaPlayer();

        //mMediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> configureVideo(width, height));

        mMediaPlayer.setOnErrorListener((mediaPlayer, i, i2) -> {
            setStatus(PlaybackStateCompat.STATE_ERROR);
            Toast.makeText(context, "Failed to open podcast", Toast.LENGTH_LONG).show();
            return false;
        });

        mMediaPlayer.setOnPreparedListener(mediaPlayer -> {
            podcastStatusListener.podcastStatusUpdated();
            setStatus(PlaybackStateCompat.STATE_PAUSED);
            play();
        });

        mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
            pause();//Send the over signal
            podcastCompleted();
        });


        try {
            setStatus(PlaybackStateCompat.STATE_CONNECTING);

            mMediaPlayer.setDataSource(((PodcastItem) mediaItem).link);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            setStatus(PlaybackStateCompat.STATE_ERROR);
        }
    }

    @Override
    public void destroy() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }

    @Override
    public void play() {
        try {
            int progress = mMediaPlayer.getCurrentPosition() / mMediaPlayer.getDuration();
            if (progress >= 1) {
                mMediaPlayer.seekTo(0);
            }
            setStatus(PlaybackStateCompat.STATE_PLAYING);
        } catch (Exception ex) {
            Log.e(TAG, "Error while playing", ex);
        }

        mMediaPlayer.start();

        //populateVideo();
    }

    @Override
    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        setStatus(PlaybackStateCompat.STATE_PAUSED);
    }

    @Override
    public void playbackSpeedChanged(float currentPlaybackSpeed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mMediaPlayer.setPlaybackParams(mMediaPlayer.getPlaybackParams().setSpeed(currentPlaybackSpeed));
        }
    }

    @Override
    public void seekTo(int position) {
        double totalDuration = mMediaPlayer.getDuration();
        Log.d(TAG, "seekTo position: " + position + " totalDuration: " + totalDuration);
        //int position = (int) ((totalDuration / 100d) * percent);
        mMediaPlayer.seekTo(position);
    }

    @Override
    public int getCurrentPosition() {
        if (mMediaPlayer != null && isMediaLoaded()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public int getTotalDuration() {
        if (mMediaPlayer != null && isMediaLoaded()) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public VideoType getVideoType() {
        return ((PodcastItem) getMediaItem()).isVideoPodcast ? VideoType.Video : VideoType.None;
    }


    /*
    private void populateVideo() {
        double videoHeightRel = (double) mSurfaceWidth / (double) mMediaPlayer.getVideoWidth();
        int videoHeight = (int) (mMediaPlayer.getVideoHeight() * videoHeightRel);

        if (mSurfaceWidth != 0 && videoHeight != 0 && mSurfaceHolder != null) {
            //mSurfaceHolder.setFixedSize(mSurfaceWidth, videoHeight);

            parentView.getLayoutParams().height = videoHeight;
            parentView.setLayoutParams(parentView.getLayoutParams());
        }
    }*/

    public long getVideoWidth() {
        return mMediaPlayer.getVideoWidth();
    }

    public void setVideoView(SurfaceView surfaceView) {
        if (surfaceView == null) {
            mMediaPlayer.setDisplay(null);
            //Log.v(TAG, "Disable Screen output!");

            mMediaPlayer.setScreenOnWhilePlaying(false);
        } else {
            if (surfaceView.getHolder() != mSurfaceHolder) {
                //this.parentView = parentResizableView;

                surfaceView.getHolder().addCallback(mSHCallback);
                //videoOutput.surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

            }
        }
    }


    //private int mSurfaceWidth;
    //private int mSurfaceHeight;
    private SurfaceHolder mSurfaceHolder;
    private final SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int surfaceWidth, int surfaceHeight) {
            Log.v(TAG, "surfaceChanged() called with: holder = [" + holder + "], format = [" + format + "], surfaceWidth = [" + surfaceWidth + "], surfaceHeight = [" + surfaceHeight + "]");
            //mSurfaceWidth = surfaceWidth;
            //mSurfaceHeight = surfaceHeight;
            //populateVideo();
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.v(TAG, "surfaceCreated() called with: holder = [" + holder + "]");
            mSurfaceHolder = holder;
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setScreenOnWhilePlaying(true);
        }

        public void surfaceDestroyed(SurfaceHolder holder)
        {
            Log.d(TAG, "surfaceDestroyed");
        }
    };
}
