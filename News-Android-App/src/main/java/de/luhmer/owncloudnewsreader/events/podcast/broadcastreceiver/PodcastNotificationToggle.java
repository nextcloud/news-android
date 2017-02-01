package de.luhmer.owncloudnewsreader.events.podcast.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import de.luhmer.owncloudnewsreader.events.podcast.TogglePlayerStateEvent;

public class PodcastNotificationToggle extends BroadcastReceiver {

    public static final String TAG = PodcastNotificationToggle.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");

        //TODO problem:  only the headphone unplug event is triggered. Somehow the headphone plug-in event is not triggered at all..
        //TODO expected: receive the headphone plug-in event and trigger the "play" event
        if(intent.getAction().equals("android.media.AUDIO_BECOMING_NOISY")) {
            EventBus.getDefault().post(new TogglePlayerStateEvent(TogglePlayerStateEvent.State.Pause));
        } else {
            EventBus.getDefault().post(new TogglePlayerStateEvent());
        }
    }
}
