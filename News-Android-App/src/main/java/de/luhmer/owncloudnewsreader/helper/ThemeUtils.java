package de.luhmer.owncloudnewsreader.helper;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;

import java.lang.reflect.Field;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import de.luhmer.owncloudnewsreader.R;

public class ThemeUtils {

    private static final String TAG = ThemeUtils.class.getCanonicalName();

    private ThemeUtils() {}

    /**
     * Sets the color of the SearchView to {@code color} (cursor.
     * @param searchView
     */
    public static void colorSearchViewCursorColor(SearchView searchView, @ColorInt int color) {
        try {
            Field searchTextViewRef = SearchView.class.getDeclaredField("mSearchSrcTextView");
            searchTextViewRef.setAccessible(true);
            Object searchAutoComplete = searchTextViewRef.get(searchView);

            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchAutoComplete, R.drawable.cursor);


            // Set color of handle
            // https://stackoverflow.com/a/49555923

            //get the pointer resource id
            Field textSelectHandleRef = TextView.class.getDeclaredField("mTextSelectHandleRes");
            textSelectHandleRef.setAccessible(true);
            int drawableResId = textSelectHandleRef.getInt(searchAutoComplete);

            //get the editor
            Field editorRef = TextView.class.getDeclaredField("mEditor");
            editorRef.setAccessible(true);
            Object editor = editorRef.get(searchAutoComplete);

            //tint drawable
            Drawable drawable = ContextCompat.getDrawable(searchView.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            //set the drawable
            Field mSelectHandleCenter = editor.getClass().getDeclaredField("mSelectHandleCenter");
            mSelectHandleCenter.setAccessible(true);
            mSelectHandleCenter.set(editor, drawable);

            Field mSelectHandleLeft = editor.getClass().getDeclaredField("mSelectHandleLeft");
            mSelectHandleLeft.setAccessible(true);
            mSelectHandleLeft.set(editor, drawable);

            Field mSelectHandleRight = editor.getClass().getDeclaredField("mSelectHandleRight");
            mSelectHandleRight.setAccessible(true);
            mSelectHandleRight.set(editor, drawable);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't apply color to search view cursor", e);
        }
    }

}
