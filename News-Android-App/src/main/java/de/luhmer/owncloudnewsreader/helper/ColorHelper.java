package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

/**
 * Created by daniel on 11.07.15.
 */
public class ColorHelper {
    public static String getCssColor(int color) {
        // using %f for the double value would result in a localized string, e.g. 0,12 which
        // would be an invalid css color string
        return String.format("rgba(%d,%d,%d,%s)",
                Color.red(color),
                Color.green(color),
                Color.blue(color),
                Double.toString(Color.alpha(color)/255.0));
    }

    public static int[] getColorsFromAttributes(Context context, int... attr) {
        final TypedArray a = context
                .obtainStyledAttributes(attr);
        int[] colors = new int[a.getIndexCount()];
        for(int i=0; i<a.getIndexCount(); i++) {
            colors[i] = a.getColor(i,0);
        }
        a.recycle();
        return colors;
    }
}
