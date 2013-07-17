package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import de.luhmer.owncloudnewsreader.services.SyncItemStateService;

public class PostDelayHandler {
	Handler handlerTimer;
	private final int delayTime = 5 * 60000;//60 000 = 1min 
	Context context;
	private static boolean isDelayed = false;
	
	public PostDelayHandler(Context context) {
		handlerTimer = new Handler();
		this.context = context;
	}
	
	public void DelayTimer() {
		if(!isDelayed) {
			isDelayed = true;
			handlerTimer.postDelayed(new Runnable(){
		        public void run() {
		        	isDelayed = false;
		        	if((!SyncItemStateService.isMyServiceRunning(context)) && NetworkConnection.isNetworkAvailable(context))
		        	{	        	
		        		Intent iService = new Intent(context, SyncItemStateService.class); 
		        		context.startService(iService);
		        	}
		      }}, delayTime);
		}
	}
}
