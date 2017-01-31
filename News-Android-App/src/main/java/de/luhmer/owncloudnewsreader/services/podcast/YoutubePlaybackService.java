package de.luhmer.owncloudnewsreader.services.podcast;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubePlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.luhmer.owncloudnewsreader.model.MediaItem;

/**
 * Created by david on 31.01.17.
 */

public class YoutubePlaybackService extends PlaybackService {

    private static final String TAG = YoutubePlaybackService.class.getCanonicalName();
    YouTubePlayer youTubePlayer;
    Context context;

    public YoutubePlaybackService(Context context, PodcastStatusListener podcastStatusListener, MediaItem mediaItem) {
        super(context, podcastStatusListener, mediaItem);
        this.context = context;
        setStatus(Status.PREPARING);
    }

    @Override
    public void destroy() {
        if(youTubePlayer != null) {
            youTubePlayer.pause();
            youTubePlayer = null;
        }
    }

    @Override
    public void play() {
        if(youTubePlayer != null) {
            youTubePlayer.play();
        }
    }

    @Override
    public void pause() {
        if(youTubePlayer != null) {
            youTubePlayer.pause();
        }
    }

    public void seekTo(double percent) {
        if(youTubePlayer != null) {
            double totalDuration = getTotalDuration();
            int position = (int) ((totalDuration / 100d) * percent);
            youTubePlayer.seekToMillis(position);
        }
    }
    public int getCurrentDuration() {
        if(youTubePlayer != null) {
            return youTubePlayer.getCurrentTimeMillis();
        }
        return 0;
    }

    public int getTotalDuration() {
        if(youTubePlayer != null) {
            return youTubePlayer.getDurationMillis();
        }
        return 0;
    }

    @Override
    public VideoType getVideoType() {
        return VideoType.YouTube;
    }

    public void setYoutubePlayer(YouTubePlayer youTubePlayer, boolean wasRestored) {
        this.youTubePlayer = youTubePlayer;
        youTubePlayer.setPlaybackEventListener(youtubePlaybackEventListener);
        youTubePlayer.setPlayerStateChangeListener(youtubePlayerStateChangeListener);

        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);

        // Start buffering
        if (!wasRestored) {
            Pattern youtubeIdPattern = Pattern.compile(".*?v=([^&]*)");
            Matcher matcher = youtubeIdPattern.matcher(getMediaItem().link);
            if(matcher.matches()) {
                String youtubeId = matcher.group(1);
                youTubePlayer.cueVideo(youtubeId);
            } else {
                Toast.makeText(context, "Cannot find youtube video id", Toast.LENGTH_LONG).show();
                setStatus(Status.FAILED);
            }
        }
    }


    YouTubePlayer.PlayerStateChangeListener youtubePlayerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {
            Log.d(TAG, "onLoading() called");
        }

        @Override
        public void onLoaded(String s) {
            Log.d(TAG, "onLoaded() called with: s = [" + s + "]");
            youTubePlayer.play();
        }

        @Override
        public void onAdStarted() {
            Log.d(TAG, "onAdStarted() called");
        }

        @Override
        public void onVideoStarted() {
            Log.d(TAG, "onVideoStarted() called");
        }

        @Override
        public void onVideoEnded() {
            Log.d(TAG, "onVideoEnded() called");
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            Log.d(TAG, "onError() called with: errorReason = [" + errorReason + "]");
        }
    };


    YouTubePlayer.PlaybackEventListener youtubePlaybackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {
            Log.d(TAG, "onPlaying() called");
            setStatus(Status.PLAYING);
        }

        @Override
        public void onPaused() {
            Log.d(TAG, "onPaused() called");
            setStatus(Status.PAUSED);
        }

        @Override
        public void onStopped() {
            Log.d(TAG, "onStopped() called");
            setStatus(Status.PAUSED);
        }

        @Override
        public void onBuffering(boolean b) {
            Log.d(TAG, "onBuffering() called with: b = [" + b + "]");
        }

        @Override
        public void onSeekTo(int i) {
            Log.d(TAG, "onSeekTo() called with: i = [" + i + "]");
        }
    };
}
