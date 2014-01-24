package com.devspark.robototextview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.devspark.robototextview.RobotoTypefaceManager;

import de.luhmer.owncloudnewsreader.R;

/**
 * Implementation of a {@link EditText} with native support for all the Roboto fonts.
 *
 * @author e.shishkin
 */
public class RobotoEditText extends EditText {

    /**
     * Simple constructor to use when creating a widget from code.
     *
     * @param context The Context the widget is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public RobotoEditText(Context context) {
        super(context);
        onInitTypeface(context, null, 0);
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
     * @see #RobotoEditText(Context, android.util.AttributeSet, int)
     */
    public RobotoEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInitTypeface(context, attrs, 0);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of View allows subclasses to use their own base style when
     * they are inflating.
     *
     * @param context  The Context the widget is running in, through which it can
     *                 access the current theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the widget.
     * @param defStyle The default style to apply to this widget. If 0, no style
     *                 will be applied (beyond what is included in the theme). This may
     *                 either be an attribute resource, whose value will be retrieved
     *                 from the current theme, or an explicit style resource.
     * @see #RobotoEditText(Context, AttributeSet)
     */
    public RobotoEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onInitTypeface(context, attrs, defStyle);
    }

    /**
     * Setup Roboto typeface.
     *
     * @param context  The Context the widget is running in, through which it can
     *                 access the current theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the widget.
     * @param defStyle The default style to apply to this widget. If 0, no style
     *                 will be applied (beyond what is included in the theme).
     */
    private void onInitTypeface(Context context, AttributeSet attrs, int defStyle) {
        // Typeface.createFromAsset doesn't work in the layout editor, so skipping.
        if (isInEditMode()) {
            return;
        }

        int typefaceValue = 0;
        if (attrs != null) {
            TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.RobotoTextView, defStyle, 0);
            typefaceValue = values.getInt(R.styleable.RobotoTextView_typeface, 0);
            values.recycle();
        }

        Typeface robotoTypeface = RobotoTypefaceManager.obtaintTypeface(context, typefaceValue);
        setTypeface(robotoTypeface);
    }

}
