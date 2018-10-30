package de.luhmer.owncloudnewsreader.view;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;
import de.luhmer.owncloudnewsreader.events.podcast.broadcastreceiver.PodcastNotificationToggle;
import de.luhmer.owncloudnewsreader.model.MediaItem;
import de.luhmer.owncloudnewsreader.notification.NextcloudNotificationManager;
import de.luhmer.owncloudnewsreader.services.PodcastPlaybackService;
import de.luhmer.owncloudnewsreader.services.podcast.PlaybackService;

public class PodcastNotification {

    private static final String TAG = PodcastNotification.class.getCanonicalName();
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    //public static final String ACTION_NEXT = "action_next";
    //public static final String ACTION_PREVIOUS = "action_previous";
    //public static final String ACTION_STOP = "action_stop";

    private final Context mContext;
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder notificationBuilder;
    private final String CHANNEL_ID = "Podcast Notification";

    //private MediaSessionManager mManager;
    private MediaSessionCompat mSession;
    //private MediaControllerCompat mController;
    private int lastDrawableId = -1;


    public final static int NOTIFICATION_ID = 1111;

    public PodcastNotification(Context context) {
        this.mContext = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationBuilder = NextcloudNotificationManager.buildPodcastNotification(mContext, CHANNEL_ID);

        EventBus.getDefault().register(this);
    }

    public void unbind() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mSession != null) {
            mSession.release();
        }
    }


    @Subscribe
    public void onEvent(UpdatePodcastStatusEvent podcast) {
        if(mSession == null) {
            Log.v(TAG, "Session null.. ignore UpdatePodcastStatusEvent");
            return;
        }

        int drawableId = podcast.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        String actionText = podcast.isPlaying() ? "Pause" : "Play";

        if(lastDrawableId != drawableId) {
            lastDrawableId = drawableId;

            notificationBuilder.setContentTitle(podcast.getTitle());
            notificationBuilder.mActions.clear();
            notificationBuilder.addAction(
                    drawableId,
                    actionText,
                    PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, PodcastNotificationToggle.class),
                    PendingIntent.FLAG_ONE_SHOT));


            if(podcast.isPlaying()) {
                //Prevent the Podcast Player from getting killed because of low memory
                //For more info see: http://developer.android.com/reference/android/app/Service.html#startForeground(int, android.app.Notification)
                ((PodcastPlaybackService)mContext).startForeground(NOTIFICATION_ID, notificationBuilder.build());

                notificationBuilder.setOngoing(true); // Non cancelable (sort above the others)
            } else {
                ((PodcastPlaybackService)mContext).stopForeground(false);

                notificationBuilder.setOngoing(false); // cancelable
            }

            //Lock screen notification
            /*
            mSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, podcast.getMax())
                    .build());
                    */

            if(podcast.isPlaying()) {
                mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, podcast.getCurrent(), 1.0f)
                        .setActions(PlaybackStateCompat.ACTION_PAUSE).build());
            } else {
                mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PAUSED, podcast.getCurrent(), 0.0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY).build());
            }
        }


        int hours = (int)( podcast.getCurrent() / (1000*60*60));
        int minutes = (int)(podcast.getCurrent() % (1000*60*60)) / (1000*60);
        int seconds = (int) ((podcast.getCurrent() % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        String fromText = (String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        hours = (int)( podcast.getMax() / (1000*60*60));
        minutes = (int)(podcast.getMax() % (1000*60*60)) / (1000*60);
        seconds = (int) ((podcast.getMax() % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        String toText = (String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds));



        double progressDouble = ((double)podcast.getCurrent() / (double)podcast.getMax()) * 100d;
        int progress = ((int) progressDouble);


        notificationBuilder
                .setContentText(fromText + " - " + toText)
                .setProgress(100, progress, podcast.getStatus() == PlaybackService.Status.PREPARING);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void cancel()
    {
        if(notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
        if(mSession != null) {
            mSession.setActive(false);
        }
    }

    public void podcastChanged() {
        initMediaSessions();
    }

    private void initMediaSessions() {
        String packageName = PodcastNotificationToggle.class.getPackage().getName();
        ComponentName receiver = new ComponentName(packageName, PodcastNotificationToggle.class.getName());
        mSession = new MediaSessionCompat(mContext, "PlayerService", receiver, null);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());


        MediaItem podcastItem = ((PodcastPlaybackService)mContext).getCurrentlyPlayingPodcast();

        String favIconUrl = podcastItem.favIcon;
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().
                showImageOnLoading(R.drawable.default_feed_icon_light).
                showImageForEmptyUri(R.drawable.default_feed_icon_light).
                showImageOnFail(R.drawable.default_feed_icon_light).
                build();

        //TODO networkOnMainThreadExceptionHere!
        //Bitmap bmpAlbumArt = ImageLoader.getInstance().loadImageSync(favIconUrl, displayImageOptions);

        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Test")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Test")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Test")
                        //.putString(MediaMetadataCompat.METADATA_KEY_TITLE, podcastItem.title)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 100)
                //.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bmpAlbumArt)
                /* .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher)) */
                .build());

        mSession.setCallback(new MediaSessionCallback());


        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                // Ignore
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);



        //MediaControllerCompat controller = mSession.getController();

        mSession.setActive(true);
    }

    public Notification getNotification() {
        return notificationBuilder.build();
    }


    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            EventBus.getDefault().post(new TogglePlayerStateEvent());
        }

        @Override
        public void onPause() {
            EventBus.getDefault().post(new TogglePlayerStateEvent());
        }
    }

}
