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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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
		else if(view instanceof TextView)
			((TextView)view).setTypeface(typeface);
		else if(view instanceof Button)
			((Button)view).setTypeface(typeface);
		else if(view instanceof CheckBox)
			((CheckBox)view).setTypeface(typeface);
		else if(view instanceof EditText)
			((EditText)view).setTypeface(typeface);
	}
	
	public Typeface getFont() {
        return Typeface.DEFAULT;
	}
	
	public Typeface getFontUnreadStyle() {
		return Typeface.DEFAULT_BOLD;
	}
}
