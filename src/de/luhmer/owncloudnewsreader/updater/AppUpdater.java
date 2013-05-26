package de.luhmer.owncloudnewsreader.updater;

import java.io.File;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;
import de.luhmer.owncloudnewsreader.Constants;

public class AppUpdater {	
	private long enqueue;
    private DownloadManager dm;
	private Activity act;
    private Boolean forceUpdate;
	
	static String VERSION = "0.5.0.0"; 
	
    public AppUpdater(Activity act, Boolean forceUpdate){
    	this.act = act;
    	this.forceUpdate = forceUpdate;
    }    
    
	@SuppressWarnings("deprecation")
	public void UpdateApp()
    {
		if(!forceUpdate)
		{
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(act);
			long dt = sharedPref.getLong("last_update", 0);		
	        Date date = new Date();        
	        date.setHours(0);
	        date.setMinutes(0);
	        date.setSeconds(0);
	        Date date2 = new Date(dt);
	        date2.setHours(0);
	        date2.setMinutes(0);
	        date2.setSeconds(0);
	        
	        long diffInSeconds = (date2.getTime() - date.getTime()) / 1000;
	        double diffInDays = (((diffInSeconds / 60d) / 60d) / 24d);
	        
	        if(diffInDays != 0)
	        {        
	        	CheckVersion cv = new CheckVersion();
	        	cv.execute();
	        	sharedPref.edit().putLong("last_update", date.getTime()).commit();
	        }	        
		}
		else
        {
        	CheckVersion cv = new CheckVersion();
        	cv.execute();
        }
    }
	
	private void DownloadApp()
	{    	
    	BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    //long downloadId = intent.getLongExtra(
                    //        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    Query query = new Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            File mFile = new File(Uri.parse(uriString).getPath());
                            //Toast.makeText(getApplicationContext(), uriString, Toast.LENGTH_LONG).show();
                            openFile(mFile);
                        }
                        else
                        	Toast.makeText(act.getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
 
        act.registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    	
        try
        {
        	String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        	
	    	dm = (DownloadManager) act.getSystemService(Context.DOWNLOAD_SERVICE);
        	
	        Request request = new Request(Uri.parse(Constants.UPDATE_SERVER_HOSTNAME + "/" + Constants.FILENAME));
	        
	        File f = new File(dir + "/" + Constants.FILENAME);
	        if (f.exists()) { 
	            f.delete();
	        }
	        enqueue = dm.enqueue(request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Constants.FILENAME));
	        //enqueue = dm.enqueue(request);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	}

	protected void openFile(File file) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        try
        {        	
        	install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        	act.startActivity(install);
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
    }
	
	
	
	public class CheckVersion extends AsyncTask<Object, Void, String> {
		
		@Override
		protected String doInBackground(Object... params) {
			String output = getOutputFromUrl(params);
			return output;
		}
 
        private String getOutputFromUrl(Object... val) {
            String output = null;
            try {
            	HttpResponse httpResponse = postDataForInsert(val);
                HttpEntity httpEntity = httpResponse.getEntity();
                output = EntityUtils.toString(httpEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return output;
        }
 
        @Override
        protected void onPostExecute(String output) {
        	if(!VERSION.equals(output))
        	{
        		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        		    @Override
        		    public void onClick(DialogInterface dialog, int which) {
        		        switch (which){
        		        case DialogInterface.BUTTON_POSITIVE:
        		            //Yes button clicked
        		        	DownloadApp();
        		            break;

        		        case DialogInterface.BUTTON_NEGATIVE:
        		            //No button clicked
        		            break;
        		        }
        		    }
        		};

        		AlertDialog.Builder builder = new AlertDialog.Builder(act);
        		builder.setMessage("Aktuelle Version: " + VERSION + "\nNeue Version " + output + " verfügbar\nMöchten Sie diese nun herunterladen ?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
        	}
        	else if(forceUpdate)
        	{
        		AlertDialog.Builder builder = new AlertDialog.Builder(act);
        		builder.setMessage("Ihre App ist auf dem neuesten Stand\nAktuelle Version: " + VERSION).setNeutralButton("Ok",  null).show();
        	}
        }		
    }

	public static HttpResponse postDataForInsert(Object... val) {
		HttpResponse response = null;		
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(Constants.UPDATE_SERVER_HOSTNAME + "/index.php");
	    
	    try {	        
			response = httpclient.execute(httppost);
	    }  
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	    return response;
	}
}
