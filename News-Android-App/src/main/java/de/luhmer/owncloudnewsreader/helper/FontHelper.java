/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import com.devspark.robototextview.RobotoTypefaceManager;
import com.devspark.robototextview.widget.RobotoButton;
import com.devspark.robototextview.widget.RobotoCheckBox;
import com.devspark.robototextview.widget.RobotoEditText;
import com.devspark.robototextview.widget.RobotoTextView;

import de.luhmer.owncloudnewsreader.SettingsActivity;

public class FontHelper {
	Context context;
	
	public FontHelper(Context context) {
		this.context = context;
	}
	
	public void setFontForAllChildren(View view, Typeface typeface) {
		ViewGroup viewGroup = (ViewGroup) view;
		setForAllChildrenRekursiv(viewGroup, typeface);	
	}
	
	public void setFontStyleForSingleView(View view, Typeface typeface) {
		setFontForView(view, typeface);
	}
	
	private void setForAllChildrenRekursiv(ViewGroup viewGroup, Typeface typeface) {
		for(int i = 0; i < viewGroup.getChildCount(); i++) {
			View view = viewGroup.getChildAt(i);
			setFontForView(view, typeface);
		}
	}
	
	private void setFontForView(View view, Typeface typeface) {
		if(view instanceof ViewGroup)
			setFontForAllChildren(view, typeface);
		else if(view instanceof RobotoTextView)
			((RobotoTextView)view).setTypeface(typeface);
		else if(view instanceof RobotoButton)
			((RobotoButton)view).setTypeface(typeface);
		else if(view instanceof RobotoCheckBox)
			((RobotoCheckBox)view).setTypeface(typeface);
		else if(view instanceof RobotoEditText)
			((RobotoEditText)view).setTypeface(typeface);
	}
	
	public Typeface getFont() {
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		int font = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_FONT, "2"));
		return RobotoTypefaceManager.obtaintTypeface(context, font);
	}
	
	public Typeface getFontUnreadStyle() {
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		int font = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_FONT, "2"));				
		font = unreadFonts.get(font, font);		
		return RobotoTypefaceManager.obtaintTypeface(context, font);
	}
	
	private final SparseIntArray unreadFonts = new SparseIntArray() {
		{
			put(RobotoTypefaceManager.ROBOTO_THIN, RobotoTypefaceManager.ROBOTO_LIGHT);
			put(RobotoTypefaceManager.ROBOTO_LIGHT, RobotoTypefaceManager.ROBOTO_REGULAR);
			put(RobotoTypefaceManager.ROBOTO_REGULAR, RobotoTypefaceManager.ROBOTO_MEDIUM);
			put(RobotoTypefaceManager.ROBOTO_MEDIUM, RobotoTypefaceManager.ROBOTO_BOLD);
			put(RobotoTypefaceManager.ROBOTO_BOLD, RobotoTypefaceManager.ROBOTO_BLACK);
			put(RobotoTypefaceManager.ROBOTO_BLACK, RobotoTypefaceManager.ROBOTO_BLACK_ITALIC);
			put(RobotoTypefaceManager.ROBOTO_CONDENSED, RobotoTypefaceManager.ROBOTO_CONDENSED_BOLD);
			put(RobotoTypefaceManager.ROBOTO_CONDENSED_BOLD, RobotoTypefaceManager.ROBOTO_CONDENSED_BOLD_ITALIC);
			put(RobotoTypefaceManager.ROBOTOSLAB_THIN, RobotoTypefaceManager.ROBOTOSLAB_LIGHT);
			put(RobotoTypefaceManager.ROBOTOSLAB_LIGHT, RobotoTypefaceManager.ROBOTOSLAB_REGULAR);
			put(RobotoTypefaceManager.ROBOTOSLAB_REGULAR, RobotoTypefaceManager.ROBOTOSLAB_BOLD);
			put(RobotoTypefaceManager.ROBOTOSLAB_BOLD, RobotoTypefaceManager.ROBOTOSLAB_BOLD);
		}
	};
	
	public String getFontName() {
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		int typefaceValue = Integer.parseInt(mPrefs.getString(SettingsActivity.SP_FONT, "2"));
		
		String typeFaceName = "";
		switch (typefaceValue) {
        case RobotoTypefaceManager.ROBOTO_THIN:
        	typeFaceName = "ROBOTO_THIN";
            break;
        case RobotoTypefaceManager.ROBOTO_LIGHT:
        	typeFaceName = "ROBOTO_LIGHT";
            break;
        case RobotoTypefaceManager.ROBOTO_REGULAR:
        	typeFaceName = "ROBOTO_REGULAR";
            break;
        case RobotoTypefaceManager.ROBOTO_MEDIUM:
        	typeFaceName = "ROBOTO_MEDIUM";
            break;
        case RobotoTypefaceManager.ROBOTO_BOLD:
        	typeFaceName = "ROBOTO_BOLD";
            break;
        case RobotoTypefaceManager.ROBOTO_BLACK:
        	typeFaceName = "ROBOTO_BLACK";
            break;
        case RobotoTypefaceManager.ROBOTO_CONDENSED:
        	typeFaceName = "ROBOTO_CONDENSED";
            break;
        case RobotoTypefaceManager.ROBOTO_CONDENSED_BOLD:
        	typeFaceName = "ROBOTO_CONDENSED_BOLD";
            break;
        case RobotoTypefaceManager.ROBOTOSLAB_THIN:
        	typeFaceName = "ROBOTOSLAB_THIN";
            break;
        case RobotoTypefaceManager.ROBOTOSLAB_LIGHT:
        	typeFaceName = "ROBOTOSLAB_LIGHT";
            break;
        case RobotoTypefaceManager.ROBOTOSLAB_REGULAR:
        	typeFaceName = "ROBOTOSLAB_REGULAR";
            break;
        case RobotoTypefaceManager.ROBOTOSLAB_BOLD:
        	typeFaceName = "ROBOTOSLAB_BOLD";
            break;
		}
		return typeFaceName;
	}
}
