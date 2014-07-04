package de.luhmer.owncloudnewsreader.view;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.events.podcast.UpdatePodcastStatusEvent;

/**
 * Created by David on 22.06.2014.
 */
public class PodcastNotification {

    Context context;
    NotificationManager notificationManager;
    EventBus eventBus;
    NotificationCompat.Builder notificationBuilder;
    PendingIntent resultPendingIntent;

    private final static int NOTIFICATION_ID = 1111;

    public PodcastNotification(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        eventBus = EventBus.getDefault();
        eventBus.register(this);
    }

    private void createNewNotificationBuilder() {
        // Creates an explicit intent for an ResultActivity to receive.
        Intent resultIntent = new Intent(context, NewsReaderListActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Create the final Notification object.
        notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);
    }


    int lastDrawableId;

    public void onEvent(UpdatePodcastStatusEvent podcast) {
        if(!podcast.isFileLoaded())
            return;

        int drawableId = podcast.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        String actionText = podcast.isPlaying() ? "Pause" : "Play";


        if(lastDrawableId != drawableId) {
            lastDrawableId = drawableId;

            createNewNotificationBuilder();
            //notificationBuilder.addAction(drawableId, actionText, resultPendingIntent);//TODO Pause/Play
        }


        int hours = (int)( podcast.getCurrent() / (1000*60*60));
        int minutes = (int)(podcast.getCurrent() % (1000*60*60)) / (1000*60);
        int seconds = (int) ((podcast.getCurrent() % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        String fromText = (String.format("%02d:%02d", minutes, seconds));

        hours = (int)( podcast.getMax() / (1000*60*60));
        minutes = (int)(podcast.getMax() % (1000*60*60)) / (1000*60);
        seconds = (int) ((podcast.getMax() % (1000*60*60)) % (1000*60) / 1000);
        minutes += hours * 60;
        String toText = (String.format("%02d:%02d", minutes, seconds));



        double progressDouble = ((double)podcast.getCurrent() / (double)podcast.getMax()) * 100d;
        int progress = ((int) progressDouble);


        notificationBuilder
                .setContentTitle(podcast.getTitle())
                .setContentText(fromText + " - " + toText)
                .setProgress(100, progress, podcast.isPreparingFile())
                .build();

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        //.setLargeIcon(R.drawable.ic_launcher)
        //.addAction(android.R.drawable.ic_media_pause, "More", resultPendingIntent)
    }



}
