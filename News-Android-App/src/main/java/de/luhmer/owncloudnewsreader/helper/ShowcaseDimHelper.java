package de.luhmer.owncloudnewsreader.helper;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

/**
 * Created by David on 06.04.2014.
 */
public class ShowcaseDimHelper {

    private static final float ALPHA_DIM_VALUE = 0.1f;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void dimView(View view) {
        if (isHoneycombOrAbove()) {
            view.setAlpha(ALPHA_DIM_VALUE);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void undoDimView(View view) {
        if (isHoneycombOrAbove()) {
            view.setAlpha(1f);
        }
    }

    public static boolean isHoneycombOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

}
