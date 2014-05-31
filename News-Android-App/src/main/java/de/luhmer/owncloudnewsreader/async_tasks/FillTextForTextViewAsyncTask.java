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

package de.luhmer.owncloudnewsreader.async_tasks;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import de.luhmer.owncloudnewsreader.cursor.NewsListCursorAdapter;

public class FillTextForTextViewAsyncTask extends AsyncTask<Void, Void, String> {
	IGetTextForTextViewAsyncTask iGetter;
	WeakReference<TextView> textView;
    boolean enableAnimation;

	public FillTextForTextViewAsyncTask(TextView textView, IGetTextForTextViewAsyncTask iGetter, boolean enableAnimation)
	{
		this.iGetter = iGetter;
		this.textView = new WeakReference<TextView>(textView);
        this.enableAnimation = enableAnimation;
	}

    @Override
    protected void onPreExecute() {
        if(enableAnimation && textView.get() != null)
            textView.get().setVisibility(View.INVISIBLE);

        super.onPreExecute();
    }

    //http://stackoverflow.com/a/14217816
	@Override
	protected String doInBackground(Void... params) {
		//Process.setThreadPriority(9);
		return iGetter.getText();
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(String text) {
        if (isCancelled()) {
            text = null;
        }

		if(text != null) {
            if (!text.equals("0")) {
                if (textView.get() != null) {
                    textView.get().setText(text);

                    if(enableAnimation)
                        NewsListCursorAdapter.FadeInTextView(textView.get());
                }
            }
        }
	}
}
