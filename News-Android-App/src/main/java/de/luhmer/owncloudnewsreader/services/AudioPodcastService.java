package de.luhmer.owncloudnewsreader.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.events.OpenAudioPodcastEvent;
import de.luhmer.owncloudnewsreader.events.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.view.PodcastNotification;

public class AudioPodcastService extends Service {

    PodcastNotification podcastNotification;

    @Override
    public void onCreate() {
        podcastNotification = new PodcastNotification(this);

        super.onCreate();
    }

    public AudioPodcastService() {
        mediaPlayer = new MediaPlayer();
        mHandler = new Handler();
        eventBus = EventBus.getDefault();

        eventBus.register(this);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mHandler.removeCallbacks(mUpdateTimeTask);
            }
        });


        //openFile("/sdcard/Music/#Musik/Finest Tunes/Netsky - Running Low (Ft. Beth Ditto).mp3");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private EventBus eventBus;
    private Handler mHandler;
    private MediaPlayer mediaPlayer;
    private String mediaTitle;

    public static final int delay = 500; //In milliseconds

    public void openFile(String pathToFile, String mediaTitle) {
        try {
            this.mediaTitle = mediaTitle;

            mediaPlayer.reset();
            mediaPlayer.setDataSource(pathToFile);
            mediaPlayer.prepare();

            play();
        } catch (IOException e) {
            e.printStackTrace();
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
        if(mediaPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void onEventBackgroundThread(OpenAudioPodcastEvent event) {
        openFile(event.pathToFile, event.mediaTitle);
    }




    public void play() {
        mediaPlayer.start();
        mHandler.postDelayed(mUpdateTimeTask, 0);
    }

    public void pause() {
        mediaPlayer.pause();
        mHandler.removeCallbacks(mUpdateTimeTask);

        sendMediaStatus();
    }

    public void sendMediaStatus() {
        long totalDuration = mediaPlayer.getDuration();
        long currentDuration = mediaPlayer.getCurrentPosition();

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

        UpdatePodcastStatusEvent audioPodcastEvent = new UpdatePodcastStatusEvent(currentDuration, totalDuration, mediaPlayer.isPlaying(), mediaTitle);
        eventBus.post(audioPodcastEvent);
    }

}
