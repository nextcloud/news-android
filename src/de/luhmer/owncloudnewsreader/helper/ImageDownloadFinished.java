package de.luhmer.owncloudnewsreader.helper;

import java.lang.ref.WeakReference;

import android.widget.ImageView;

public interface ImageDownloadFinished {
	void DownloadFinished(int AsynkTaskId, String fileCachePath);
}
