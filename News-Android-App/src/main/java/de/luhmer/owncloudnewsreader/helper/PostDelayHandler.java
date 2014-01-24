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

package de.luhmer.owncloudnewsreader.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import de.luhmer.owncloudnewsreader.services.SyncItemStateService;

public class PostDelayHandler {
	private static Handler handlerTimer;
	private final int delayTime = 5 * 60000;//60 000 = 1min 
	Context context;
	private static boolean isDelayed = false;
	
	public PostDelayHandler(Context context) {
		if(handlerTimer == null)
			handlerTimer = new Handler();
		this.context = context;
	}
	
	public void stopRunningPostDelayHandler() {
		handlerTimer.removeCallbacksAndMessages(null);
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
