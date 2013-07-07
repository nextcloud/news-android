package de.luhmer.owncloudnewsreader.async_tasks;

import android.os.AsyncTask;
import android.widget.TextView;

public class FillTextForTextViewAsyncTask extends AsyncTask<Void, Void, String> {
	IGetTextForTextViewAsyncTask iGetter;
	TextView textView;
	
	public FillTextForTextViewAsyncTask(TextView textView, IGetTextForTextViewAsyncTask iGetter)
	{
		this.iGetter = iGetter;
		this.textView = textView; 
	}
	
	@Override
	protected String doInBackground(Void... params) {
		return iGetter.getText();
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(String result) {
		if(result != null)
			if(!result.equals("0"))
				textView.setText(result);
		super.onPostExecute(result);
	}
}
