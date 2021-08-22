/*
* Android ownCloud News
 *
 * @author David Luhmer
 * @copyright 2019 David Luhmer david-dev@live.de
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU AFFERO GENERAL PUBLIC LICENSE for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.luhmer.owncloudnewsreader.helper;

import android.app.Activity;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

public class ThemeUtils {

    // private static final String TAG = ThemeUtils.class.getCanonicalName();

    private ThemeUtils() {}

    /**
     * Use this method to colorize the toolbar to the desired target color
     * @param toolbarView toolbar view being colored
     * @param toolbarBackgroundColor the target background color
     */
    public static void colorizeToolbar(Toolbar toolbarView, @ColorInt int toolbarBackgroundColor) {
        toolbarView.setBackgroundColor(toolbarBackgroundColor);

        for(int i = 0; i < toolbarView.getChildCount(); i++) {
            final View v = toolbarView.getChildAt(i);

            v.setBackgroundColor(toolbarBackgroundColor);

            if (v instanceof ActionMenuView) {
                for (int j = 0; j < ((ActionMenuView) v).getChildCount(); j++) {
                    v.setBackgroundColor(toolbarBackgroundColor);
                }
            }
        }
    }

    /**
     * Use this method to colorize the toolbar to the desired target color
     *
     * @param toolbarView            toolbar view being colored
     * @param toolbarForegroundColor the target background color
     * @param skipMenuItems          how many menu items should not be colored
     */
    public static void colorizeToolbarForeground(Toolbar toolbarView, @ColorInt int toolbarForegroundColor, int skipMenuItems) {
        toolbarView.setTitleTextColor(toolbarForegroundColor);

        ColorFilter cf = new PorterDuffColorFilter(toolbarForegroundColor, PorterDuff.Mode.SRC_IN);
        Drawable drawable = toolbarView.getOverflowIcon();
        if (drawable != null) {
            drawable.setColorFilter(cf);
        }

        for (int i = 0; i < toolbarView.getChildCount(); i++) {
            final View v = toolbarView.getChildAt(i);
            if (v instanceof ImageButton) {
                ((ImageButton) v).setColorFilter(cf);
            } else if (v instanceof ActionMenuView) {
                Menu menu = ((ActionMenuView) v).getMenu();
                for (int x = skipMenuItems; x < menu.size(); x++) {
                    Drawable d = menu.getItem(x).getIcon();
                    if (d != null) {
                        d.setColorFilter(cf);
                    }
                }
            }
            /*
            else {
                Log.d(TAG, v.toString());
            }
            */
        }
    }

    /**
     * Use this method to colorize the status bar to the desired target color
     *
     * @param activity
     * @param statusBarColor
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void changeStatusBarColor(Activity activity, @ColorInt int statusBarColor) {
        Window window = activity.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(statusBarColor);
    }
}
