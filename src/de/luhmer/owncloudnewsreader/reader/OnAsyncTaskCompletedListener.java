package de.luhmer.owncloudnewsreader.reader;

public interface OnAsyncTaskCompletedListener {
	public abstract void onAsyncTaskCompleted(final int task_id, final Object task_result);
}
