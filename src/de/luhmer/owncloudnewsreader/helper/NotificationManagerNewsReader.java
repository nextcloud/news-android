package de.luhmer.owncloudnewsreader.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;

/**
 * Created by david on 29.10.13.
 */
public class NotificationManagerNewsReader {
    private static NotificationManagerNewsReader instance;
    private final int NOTIFICATION_ID = 0;
    private Context context;

    public static NotificationManagerNewsReader getInstance(Context context)
    {
        if(instance == null)
            instance = new NotificationManagerNewsReader(context);
        return instance;
    }

    public NotificationManagerNewsReader(Context context)
    {
        this.context = context;
        //NOTIFICATION_ID = new Random().nextInt();
        //NOTIFICATION_ID = new Random().nextInt();
    }



    public void ShowMessage(String title, String tickerMessage, String message)
    {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setTicker(tickerMessage)
                        .setContentTitle(title)
                                //.setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentText(message);


    /*

    myNotification = new NotificationCompat.Builder(context)
      .setContentTitle("Exercise of Notification!")
      .setContentText("http://android-er.blogspot.com/")
      .setTicker("Notification!")
      .setWhen(System.currentTimeMillis())
      .setContentIntent(pendingIntent)
      .setDefaults(Notification.DEFAULT_SOUND)
      .setAutoCancel(true)
      .setSmallIcon(R.drawable.ic_launcher)
      .build();

    */

        Intent notificationIntent = new Intent(context, NewsReaderListActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    // Remove notification
    public void RemoveNotification() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }
}
