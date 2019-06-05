/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 - Nathan Barraille
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package helper;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.hamcrest.Matcher;

import java.util.Collection;

import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * An Espresso ViewAction that changes the orientation of the screen
 */
public class OrientationChangeAction implements ViewAction {

    private final int orientation;
    private Activity activity;

    private OrientationChangeAction(int orientation, Activity activity) {
        this.orientation = orientation;
        this.activity = activity;
    }

    @Override
    public Matcher<View> getConstraints() {
        return isRoot();
    }

    @Override
    public String getDescription() {
        return "change orientation to " + orientation;
    }

    @Override
    public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();
        activity.setRequestedOrientation(orientation);

        Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
        if (resumedActivities.isEmpty()) {
            throw new RuntimeException("Could not change orientation");
        }
    }

    public static ViewAction orientationLandscape(Activity activity) {
        return new OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, activity);
    }

    public static ViewAction orientationPortrait(Activity activity) {
        return new OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity);
    }
}