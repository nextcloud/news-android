package de.luhmer.owncloudnewsreader.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * An extremely simple {@link LinearLayout} descendant that simply reverses the 
 * order of its child views on Android 4.0+. The reason for this is that on 
 * Android 4.0+, negative buttons should be shown to the left of positive buttons.
 */
public class LoginButtonBar extends LinearLayout {

    public LoginButtonBar(Context context) {
        super(context);
    }

    public LoginButtonBar(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public LoginButtonBar(Context context, AttributeSet attributes, int def_style) {
        super(context, attributes, def_style);
    }

    @Override
    public View getChildAt(int index) {
        if (_has_ics)
            // Flip the buttons so that "OK | Cancel" becomes "Cancel | OK" on ICS
            return super.getChildAt(getChildCount() - 1 - index);

        return super.getChildAt(index);
    }

    private final static boolean _has_ics = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
}
