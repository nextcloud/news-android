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
}
