package de.luhmer.owncloudnewsreader.helper

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

class ForegroundListener : ActivityLifecycleCallbacks {
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {
        // do nothing
    }

    override fun onActivityStarted(activity: Activity) {
        numStarted++
    }

    override fun onActivityResumed(activity: Activity) {
        // do nothing
    }

    override fun onActivityPaused(activity: Activity) {
        // do nothing
    }

    override fun onActivityStopped(activity: Activity) {
        numStarted--
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) {
        // do nothing
    }

    override fun onActivityDestroyed(activity: Activity) {
        // do nothing
    }

    companion object {
        private var numStarted = 0
        val isInForeground: Boolean
            get() = numStarted > 0
    }
}
