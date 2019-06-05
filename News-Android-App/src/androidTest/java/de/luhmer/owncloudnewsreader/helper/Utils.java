package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.nextcloud.android.sso.aidl.NextcloudRequest;

import org.mockito.ArgumentCaptor;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.di.TestApiProvider;
import de.luhmer.owncloudnewsreader.reader.nextcloud.API;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class Utils {

    public static void initMaterialShowCaseView(Context context) {
        String PREFS_NAME = "material_showcaseview_prefs";
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit()
            .putInt("status_SWIPE_LEFT_RIGHT_AND_PTR", -1)
            .putInt("status_LOGO_SYNC", -1)
            .commit();
    }

    public static void clearFocus() {
        sleep(200);
        onView(withId(R.id.toolbar)).perform(click());
        sleep(200);
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleep(float seconds) {
        try {
            Thread.sleep((long) seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
