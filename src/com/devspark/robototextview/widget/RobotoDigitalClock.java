package com.devspark.robototextview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.DigitalClock;

import com.devspark.robototextview.RobotoTypefaceManager;

import de.luhmer.owncloudnewsreader.R;

/**
 * Implementation of a {@link DigitalClock} with native support for all the Roboto fonts.
 *
 * @author e.shishkin
 */
@SuppressWarnings("deprecation")
public class RobotoDigitalClock extends DigitalClock {

    /**
     * Simple constructor to use when creating a widget from code.
     *
     * @param context The Context the widget is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public RobotoDigitalClock(Context context) {
        super(context);
        onInitTypeface(context, null);
    }

    /**
     * Constructor that is called when inflating a widget from XML. This is called
     * when a widget is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * <p/>
     * <p/>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the widget is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the widget.
     */
    public RobotoDigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInitTypeface(context, attrs);
    }

    /**
     * Setup Roboto typeface.
     *
     * @param context The Context the widget is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the widget.
     */
    private void onInitTypeface(Context context, AttributeSet attrs) {
        // Typeface.createFromAsset doesn't work in the layout editor, so skipping.
        if (isInEditMode()) {
            return;
        }

        int typefaceValue = 0;
        if (attrs != null) {
            TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.RobotoTextView);
            typefaceValue = values.getInt(R.styleable.RobotoTextView_typeface, 0);
            values.recycle();
        }

        Typeface robotoTypeface = RobotoTypefaceManager.obtaintTypeface(context, typefaceValue);
        setTypeface(robotoTypeface);
    }

}
