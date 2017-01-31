package de.luhmer.owncloudnewsreader.services.podcast;

import android.content.Context;
import android.media.MediaPlayer;
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
    private MediaPlayer mMediaPlayer;
    private View parentResizableView;

    public MediaPlayerPlaybackService(final Context context, PodcastStatusListener podcastStatusListener, MediaItem mediaItem) {
        super(context, podcastStatusListener, mediaItem);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                setStatus(Status.FAILED);
                Toast.makeText(context, "Failed to open podcast", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                setStatus(Status.PAUSED);
                play();
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                pause();//Send the over signal
                podcastCompleted();
            }
        });


        try {
            setStatus(Status.PREPARING);

            mMediaPlayer.setDataSource(((PodcastItem) mediaItem).link);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            setStatus(Status.FAILED);
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
            setStatus(Status.PLAYING);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        mMediaPlayer.start();

        populateVideo();
    }

    @Override
    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        setStatus(Status.PAUSED);
    }

    @Override
    public void seekTo(double percent) {
        double totalDuration = mMediaPlayer.getDuration();
        int position = (int) ((totalDuration / 100d) * percent);
        mMediaPlayer.seekTo(position);
    }

    @Override
    public int getCurrentDuration() {
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


    private void populateVideo() {
        double videoHeightRel = (double) mSurfaceWidth / (double) mMediaPlayer.getVideoWidth();
        int videoHeight = (int) (mMediaPlayer.getVideoHeight() * videoHeightRel);

        if (mSurfaceWidth != 0 && videoHeight != 0 && mSurfaceHolder != null) {
            //mSurfaceHolder.setFixedSize(mSurfaceWidth, videoHeight);

            parentResizableView.getLayoutParams().height = videoHeight;
            parentResizableView.setLayoutParams(parentResizableView.getLayoutParams());
        }
    }

    public void setVideoView(SurfaceView surfaceView, View parentResizableView) {
        if (surfaceView == null) {
            mMediaPlayer.setDisplay(null);
            Log.d(TAG, "Disable Screen output!");

            mMediaPlayer.setScreenOnWhilePlaying(false);
        } else {
            if (surfaceView.getHolder() != mSurfaceHolder) {
                this.parentResizableView = parentResizableView;

                surfaceView.getHolder().addCallback(mSHCallback);
                //videoOutput.surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

                populateVideo();

                Log.d(TAG, "Enable Screen output!");
            }
        }
    }


    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private SurfaceHolder mSurfaceHolder;
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
            mMediaPlayer.setDisplay(mSurfaceHolder); //TODO required
            mMediaPlayer.setScreenOnWhilePlaying(true); //TODO required

            Log.d(TAG, "surfaceCreated");
        }

        public void surfaceDestroyed(SurfaceHolder holder)
        {
            Log.d(TAG, "surfaceDestroyed");
        }
    };
}
