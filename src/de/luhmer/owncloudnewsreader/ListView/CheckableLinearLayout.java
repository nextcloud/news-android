package de.luhmer.owncloudnewsreader.ListView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.luhmer.owncloudnewsreader.R;

/*
 * This class is useful for using inside of ListView that needs to have checkable items.
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {
	@SuppressWarnings("unused")
	private TextView _textview;
	private CheckBox _checkbox_starred;
	@SuppressWarnings("unused")
	private CheckBox _checkbox_read;
    	
    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
	}
    
    @Override
    protected void onFinishInflate() {
    	super.onFinishInflate();
    	// find checked text view
    	
    	_textview = (TextView) findViewById(R.id.summary);
    	_checkbox_starred = (CheckBox) findViewById(R.id.cb_lv_item_starred);
    	_checkbox_read = (CheckBox) findViewById(R.id.cb_lv_item_read);
    	
    	/*
		int childCount = getChildCount();
		for (int i = 0; i < childCount; ++i) {
			View v = getChildAt(i);
			if (v instanceof CheckBox) {
				_checkbox = (CheckedTextView)v;
			}
		} */   	
    }
    
    @Override 
    public boolean isChecked() { 
        return _checkbox_starred != null ? _checkbox_starred.isChecked() : false; 
    }
    
    @Override 
    public void setChecked(boolean checked) {
    	if (_checkbox_starred != null) {
    		_checkbox_starred.setChecked(checked);
    	}
    }
    
    @Override 
    public void toggle() { 
    	if (_checkbox_starred != null) {
    		_checkbox_starred.toggle();
    	}
    } 
} 
