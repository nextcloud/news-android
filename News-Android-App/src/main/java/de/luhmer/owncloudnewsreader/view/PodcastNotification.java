package de.luhmer.owncloudnewsreader.view;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import de.luhmer.owncloudnewsreader.notification.NextcloudNotificationManager;

public class PodcastNotification {

    private static final String TAG = PodcastNotification.class.getCanonicalName();
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    //public static final String ACTION_NEXT = "action_next";
    //public static final String ACTION_PREVIOUS = "action_previous";
    //public static final String ACTION_STOP = "action_stop";

    private final Context mContext;
    private final NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private final String CHANNEL_ID = "Podcast Notification";

    private MediaSessionCompat mSession;
    private @PlaybackStateCompat.State int lastStatus = PlaybackStateCompat.STATE_NONE;

    public final static int NOTIFICATION_ID = 1111;

    public PodcastNotification(Context context, MediaSessionCompat session) {
        this.mContext = context;
        this.mSession = session;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationBuilder = NextcloudNotificationManager.buildPodcastNotification(mContext, CHANNEL_ID, mSession);

        //EventBus.getDefault().register(this);
    }

    @Subscribe
    public void updateStateOfNotification(@PlaybackStateCompat.State int status, long currentPosition, long totalDuration) {
        if(mSession == null) {
            Log.v(TAG, "Session null.. ignore UpdatePodcastStatusEvent");
            return;
        }

        if (status != lastStatus) {
            lastStatus = status;


            /*
            if(podcast.isPlaying()) {
                //Prevent the Podcast Player from getting killed because of low memory
                //For more info see: http://developer.android.com/reference/android/app/Service.html#startForeground(int, android.app.Notification)
                ((PodcastPlaybackService)mContext).startForeground(NOTIFICATION_ID, notificationBuilder.build());

                notificationBuilder.setOngoing(true); // Non cancelable (sort above the others)
            } else {
                ((PodcastPlaybackService)mContext).stopForeground(false);

                notificationBuilder.setOngoing(false); // cancelable
            }
            */

            //Lock screen notification
            /*
            mSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, podcast.getMax())
                    .build());
                    */

            notificationBuilder = NextcloudNotificationManager.buildPodcastNotification(mContext, CHANNEL_ID, mSession);

            //int drawableId = podcast.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
            //String actionText = podcast.isPlaying() ? "Pause" : "Play";
            //notificationBuilder.addAction(new NotificationCompat.Action(drawableId, actionText, intent));

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }


        int hours = (int) (currentPosition / (1000*60*60));
        int minutes = (int) ((currentPosition % (1000*60*60)) / (1000*60));
        int seconds = (int) ((currentPosition % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        String fromText = (String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        hours = (int) (totalDuration / (1000*60*60));
        minutes = (int) ((totalDuration % (1000*60*60)) / (1000*60));
        seconds = (int) ((totalDuration % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        String toText = (String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds));



        double progressDouble = ((double)currentPosition / (double)totalDuration) * 100d;
        int progress = ((int) progressDouble);

        notificationBuilder
                .setContentText(fromText + " - " + toText)
                .setProgress(100, progress, status == PlaybackStateCompat.STATE_CONNECTING); // TODO IMPLEMENT THIS!!!!

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void cancel()
    {
        if(notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
        /*
        if(mSession != null) {
            mSession.setActive(false);
        }
        */
    }

    public void createPodcastNotification() {
        /*
        MediaItem podcastItem = ((PodcastPlaybackService)mContext).getCurrentlyPlayingPodcast();
        */
        /*
        String favIconUrl = podcastItem.favIcon;
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().
                showImageOnLoading(R.drawable.default_feed_icon_light).
                showImageForEmptyUri(R.drawable.default_feed_icon_light).
                showImageOnFail(R.drawable.default_feed_icon_light).
                build();
                */

        //Bitmap bmpAlbumArt = ImageLoader.getInstance().loadImageSync(favIconUrl, displayImageOptions);

        /*
        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, podcastItem.author)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, podcastItem.title)
                .build());
        */

        /*
        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "NA")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "NA")
                //.putString(MediaMetadataCompat.METADATA_KEY_TITLE, podcastItem.title)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 100)
                //.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bmpAlbumArt)
                //.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher))
                .build());
        */

        this.notificationBuilder = NextcloudNotificationManager.buildPodcastNotification(mContext, CHANNEL_ID, mSession);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public Notification getNotification() {
        return notificationBuilder.build();
    }
}
