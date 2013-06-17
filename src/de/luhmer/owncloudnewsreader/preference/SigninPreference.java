package de.luhmer.owncloudnewsreader.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

public class SigninPreference extends DialogPreference {
	
	public SigninPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	// along with constructors, you will want to override
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        // view is your layout expanded and added to the dialog
        // find and hang on to your views here, add click listeners etc
        // basically things you would do in onCreate
    	//mTextView = (TextView)view.findViewById(R.Id.mytextview);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
       super.onDialogClosed(positiveResult);

        if (positiveResult) {
            // deal with persisting your values here
        }
    }
}
