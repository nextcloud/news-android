package de.luhmer.owncloudnewsreader.helper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ForegroundListener implements Application.ActivityLifecycleCallbacks {

    private static int numStarted;

    public static boolean isInForeground() {
        return numStarted > 0;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // do nothing
    }

    @Override
    public void onActivityStarted(Activity activity) {
        numStarted++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // do nothing
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // do nothing
    }

    @Override
    public void onActivityStopped(Activity activity) {
        numStarted--;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // do nothing
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // do nothing
    }
}
