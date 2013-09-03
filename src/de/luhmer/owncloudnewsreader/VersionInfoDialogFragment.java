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

package de.luhmer.owncloudnewsreader;

import java.io.InputStream;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class VersionInfoDialogFragment extends SherlockDialogFragment {
	
	private static String web_template = null;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_version_info, null);
        builder.setView(view)
        	/*
        	// Add action buttons
           .setPositiveButton(R.string.action_sign_in_short, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int id) {
            	   //attemptLogin();
               }
           })
           .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   LoginDialogFragment.this.getDialog().cancel();
               }
           })*/
           .setTitle(getString(R.string.menu_About_Changelog));  
        
        
        init_webTemplate(getActivity());
        WebView webView = (WebView) view;
        WebSettings webSettings = webView.getSettings();    
        webSettings.setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL("", web_template, "text/html", "UTF-8", "");        
        
        return builder.create();
    }
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onStart()
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onStart() {
		//Use the full screen for this dialog even in Landscape Mode.
		LayoutParams params = getDialog().getWindow().getAttributes();
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1)
        	params.width = LayoutParams.FILL_PARENT;
        else
        	params.width = LayoutParams.MATCH_PARENT;
        
        getDialog().getWindow().setAttributes(params);
        
		super.onStart();
	}

	private void init_webTemplate(Context context)
	{
		if(web_template == null)
		{
			InputStream input = null;
			try {
				//Activity act = getActivity();
				input = context.getAssets().open("version_changelog_web_template.html");
		        int size = input.available();
		        byte[] buffer = new byte[size];
		        input.read(buffer);
		        input.close();
		
		        // byte buffer into a string
		        web_template = new String(buffer);
		        
		        PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		        String version = pInfo.versionName;
		        web_template = web_template.replace("X_VERSION", version);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
