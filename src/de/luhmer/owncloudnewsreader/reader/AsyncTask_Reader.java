package de.luhmer.owncloudnewsreader.reader;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

public abstract class AsyncTask_Reader extends AsyncTask<Object, Void, Object> {
	protected Context context;
	protected int task_id;
	protected OnAsyncTaskCompletedListener[] listener;
	
	public AsyncTask_Reader(final int task_id, final Context context, final OnAsyncTaskCompletedListener[] listener) {
		this.context = context;
		this.task_id = task_id;
		this.listener = listener;
	}
	
	//public abstract void attach(final Activity context, final OnAsyncTaskCompletedListener[] listener);
	
	//Activity meldet sich zurueck nach OrientationChange
    public void attach(final Activity context, final OnAsyncTaskCompletedListener[] listener) {
        this.context = context;
        this.listener = listener;
    }

    //Activity meldet sich ab
    public void detach() {
        if (context != null) {
            context = null;
        }

        if (listener != null) {
            listener = null;
        }
    }
       
    @Override
    protected void onPostExecute(Object ex) {
        for (OnAsyncTaskCompletedListener listenerInstance : listener) {
            if(listenerInstance != null)
                listenerInstance.onAsyncTaskCompleted(task_id, ex);
        }

        detach();
    }
}
