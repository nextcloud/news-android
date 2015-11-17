package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.database.model.Feed;

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

    public static int getColorFromAttribute(Context context, int attr) {
        int[] colors = getColorsFromAttributes(context, attr);
        if(colors.length >= 1)
            return colors[0];
        else
            return 0;
    }

    public static int getFeedColor(Context context, Feed item) {
        int color;
        if(item != null && item.getAvgColour() != null)
            color = Integer.parseInt(item.getAvgColour());
        else
            color = getColorFromAttribute(context, R.attr.dividerLineColor);
        return color;
    }
}
