package de.luhmer.owncloudnewsreader.interfaces;

import android.content.Context;

public interface ExpListTextClicked {
	void onTextClicked(String idSubscription, Context context, boolean isFolder, String optional_folder_id);
}
