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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Formatter;

import de.luhmer.owncloudnewsreader.async_tasks.DownloadChangelogTask;
import de.luhmer.owncloudnewsreader.view.ChangeLogFileListView;

/**
 * Displays current app version and changelog.
 */
public class VersionInfoDialogFragment extends DialogFragment {

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // load views
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_version_info, null);

        ChangeLogFileListView clListView = (ChangeLogFileListView) view.findViewById(R.id.changelog_listview);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.changeLogLoadingProgressBar);
        TextView versionTextView = (TextView) view.findViewById(R.id.tv_androidAppVersion);

        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(getString(R.string.menu_About_Changelog));

        // set current version
        versionTextView.setText(getVersionString());

        // load changelog into view
        loadChangeLog(clListView, progressBar);

        return builder.create();
    }

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onStart()
	 */
	@Override
	public void onStart() {
		//Use the full screen for this dialog even in Landscape Mode.
		LayoutParams params = getDialog().getWindow().getAttributes();
       	params.width = LayoutParams.MATCH_PARENT;
        
        getDialog().getWindow().setAttributes(params);
        
		super.onStart();
	}

    private String getVersionString() {
        String version = "?";

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        Formatter formatter = new Formatter();
        String versionString = getString(R.string.current_version);
        return formatter.format(versionString, version).toString();
    }

    /**
     * Loads changelog into the given view and hides progress bar when done.
     */
    private void loadChangeLog(ChangeLogFileListView clListView, final ProgressBar progressBar) {
        new DownloadChangelogTask(getContext(), clListView, new DownloadChangelogTask.Listener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(IOException e) {
                progressBar.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }).execute();
    }
}
