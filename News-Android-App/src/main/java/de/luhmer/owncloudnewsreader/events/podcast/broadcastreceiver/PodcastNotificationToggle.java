package de.luhmer.owncloudnewsreader.events.podcast.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.greenrobot.event.EventBus;
import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;

/**
 * Created by David on 30.05.2015.
 */
public class PodcastNotificationToggle extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(new TogglePlayerStateEvent());
    }
}
