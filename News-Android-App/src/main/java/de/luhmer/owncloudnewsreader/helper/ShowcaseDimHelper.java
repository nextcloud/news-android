package de.luhmer.owncloudnewsreader.helper;

import android.os.Build;
import android.view.View;

/**
 * Created by David on 06.04.2014.
 */
public class ShowcaseDimHelper {

    private static final float ALPHA_DIM_VALUE = 0.1f;

    public static void dimView(View view) {
       view.setAlpha(ALPHA_DIM_VALUE);
    }

    public static void undoDimView(View view) {
        view.setAlpha(1f);
    }
}
