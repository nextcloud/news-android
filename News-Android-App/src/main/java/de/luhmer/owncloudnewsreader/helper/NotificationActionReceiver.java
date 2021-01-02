package de.luhmer.owncloudnewsreader.helper;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import de.luhmer.owncloudnewsreader.database.DatabaseConnectionOrm;
import de.luhmer.owncloudnewsreader.services.events.SyncFinishedEvent;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static de.luhmer.owncloudnewsreader.Constants.NOTIFICATION_ACTION_MARK_ALL_AS_READ_STRING;

public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = NotificationActionReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (NOTIFICATION_ACTION_MARK_ALL_AS_READ_STRING.equals(action)) {
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
            Log.d(TAG, "NOTIFICATION_ACTION_MARK_ALL_AS_READ_STRING");
            dbConn.markAllItemsAsRead();
            EventBus.getDefault().post(new SyncFinishedEvent());

            Integer notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
            if (notificationId != -1) {
                NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancel(notificationId);
            }
        } else {
            Log.d(TAG, action);
        }
    }
}
