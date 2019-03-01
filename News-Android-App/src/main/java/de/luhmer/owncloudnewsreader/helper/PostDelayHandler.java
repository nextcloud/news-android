/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import de.luhmer.owncloudnewsreader.services.SyncItemStateService;

public class PostDelayHandler {

    private static final String TAG = "PostDelayHandler";
    private static Handler handlerTimer;
    private Context context;
    private static boolean isDelayed = false;

    public PostDelayHandler(Context context) {
        this.context = context;
        if(handlerTimer == null) {
            handlerTimer = new Handler();
        }
    }

    public void stopRunningPostDelayHandler() {
        Log.v(TAG, "stopRunningPostDelayHandler() called");
        handlerTimer.removeCallbacksAndMessages(null);
        isDelayed = false;
    }

    public void delayTimer() {
        // Time to wait until a sync is triggered (after last change in the app)
        //60 000 = 1min
        delay(5 * 60000);
        //delay(10000); // 10 seconds
    }

    public void delayOnExitTimer() {
        stopRunningPostDelayHandler();

        // Time to wait until a sync is triggered when the user switches activities / exists the app
        //delay(10000); // 10 seconds
        delay(5000); // 5 seconds
    }

    private void delay(final int time) {
        Log.v(TAG, "delay() called with: time = [" + time + "]");
        if(!isDelayed) {
            isDelayed = true;
            handlerTimer.postDelayed(new Runnable(){
                public void run() {
                    isDelayed = false;
                    Log.v(TAG, "Time exceeded.. Sync state of changed items. Delay was: " + time);
                    if((!SyncItemStateService.isMyServiceRunning(context)) && NetworkConnection.isNetworkAvailable(context))
                    {
                        Log.v(TAG, "Starting SyncItemStateService");

                        SyncItemStateService.enqueueWork(context, new Intent());
                    }
                }}, time);
        }
    }
}
