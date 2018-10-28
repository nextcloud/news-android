package de.luhmer.owncloudnewsreader.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import de.luhmer.owncloudnewsreader.services.events.StopWebArchiveDownloadEvent;

import static de.luhmer.owncloudnewsreader.Constants.NOTIFICATION_ACTION_STOP_STRING;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (NOTIFICATION_ACTION_STOP_STRING.equals(action)) {
            EventBus.getDefault().post(new StopWebArchiveDownloadEvent());
        }
    }
}
