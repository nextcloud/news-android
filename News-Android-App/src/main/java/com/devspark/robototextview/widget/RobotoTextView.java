/*
* Copyright 2013 Evgeny Shishkin
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.devspark.robototextview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.devspark.robototextview.RobotoTypefaceManager;

import de.luhmer.owncloudnewsreader.R;

/**
 * Implementation of a {@link TextView} with native support for all the Roboto fonts.
 *
 * @author e.shishkin
 */
public class RobotoTextView extends TextView {

    /**
     * Simple constructor to use when creating a widget from code.
     *
     * @param context The Context the widget is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public RobotoTextView(Context context) {
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
     * @see #RobotoTextView(Context, AttributeSet, int)
     */
    public RobotoTextView(Context context, AttributeSet attrs) {
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
     * @see #RobotoTextView(Context, AttributeSet)
     */
    public RobotoTextView(Context context, AttributeSet attrs, int defStyle) {
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
