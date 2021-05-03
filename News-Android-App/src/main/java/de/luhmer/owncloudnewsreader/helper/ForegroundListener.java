package de.luhmer.owncloudnewsreader.helper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class ForegroundListener implements Application.ActivityLifecycleCallbacks {

    private static int numStarted;

    public static boolean isInForeground() {
        return numStarted > 0;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
        // do nothing
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        numStarted++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // do nothing
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // do nothing
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        numStarted--;
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        // do nothing
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // do nothing
    }
}
