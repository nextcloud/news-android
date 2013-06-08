package de.luhmer.owncloudnewsreader.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.SSLHttpClient;
import de.luhmer.owncloudnewsreader.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import javax.net.ssl.*;

public class HttpJsonRequest {
	private static final String TAG = "HttpJsonRequest";

	
	public static JSONObject PerformJsonRequest(String urlString, List<NameValuePair> nameValuePairs, String username, String password, Context context) throws Exception
	{	
        if(nameValuePairs != null)
        {
            urlString += "&" + URLEncodedUtils.format(nameValuePairs, "utf-8");
            /*
            JSONObject jObj = new JSONObject();

            for (NameValuePair nameValuePair : nameValuePairs) {
                jObj.put(nameValuePair.getName(), nameValuePair.getValue());
            }*/

            //request.setEntity(new ByteArrayEntity(jObj.toString().getBytes("UTF8")));

            //httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        }

        URL url = new URL(urlString);

        // Instantiate an HttpClient
        //HttpClient httpclient = new DefaultHttpClient(p);
        DefaultHttpClient httpClient = null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp.getBoolean(SettingsActivity.CB_ALLOWALLSSLCERTIFICATES_STRING, false) && url.getProtocol().toLowerCase().equals("https"))
            httpClient = new SSLHttpClient(context);
        else
            httpClient = new DefaultHttpClient();

        if(username != null && password != null)
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(username,password));

        //HttpGet request = new HttpGet(url);
        //HttpPost request = new HttpPost(url);
        //httpClient.setParams(params)

        /*
        HttpParams params = new BasicHttpParams();
        for (NameValuePair nameValuePair : nameValuePairs) {
            params.setIntParameter(nameValuePair.getName(), Integer.parseInt(nameValuePair.getValue()));
        }

        httpClient.setParams(params);*/

        // Instantiate a GET HTTP method
        HttpGet request = new HttpGet(url.toString());

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpClient.execute(request, responseHandler);
        // Parse
        JSONObject json = new JSONObject(responseBody);
        return json;

        // Log.i(getClass().getSimpleName(), "send  task - end");


        /*
        URLConnection conn = null;
        InputStream inputStream = null;
        URL urlInstance = new URL(url);
        conn = urlInstance.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) conn;
        httpConn.setRequestMethod("GET");
        httpConn.connect();
        if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            inputStream = httpConn.getInputStream();

            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }

            return new JSONObject(total.toString());
        }
        else
            Log.d(TAG, "Response Code: " + httpConn.getResponseCode());
        */
	}
	
	/*
	public static int performTagChangeRequest(String url)
	{
		try
		{
			URLConnection conn = null;
	        //InputStream inputStream = null;
	        URL urlInstance = new URL(url);
	        conn = urlInstance.openConnection();
	        HttpURLConnection httpConn = (HttpURLConnection) conn;
	        httpConn.setRequestMethod("GET");
	        httpConn.connect();
	        return httpConn.getResponseCode();
        }
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return -1;
	}
	*/


	@SuppressLint("DefaultLocale")
	public static int performTagChangeRequest(String urlString, String username, String password, Context context, List<NameValuePair> nameValuePairs) throws Exception
	{
        //url = "http://192.168.10.126/owncloud/ocs/v1.php/apps/news/items/3787/read";
/*
        String authString = username + ":" + password;
        String authStringEnc = Base64.encode(authString.getBytes());

        URL urlConn = new URL(url);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        HttpURLConnection httpConnection = null;
        if (urlConn.getProtocol().toLowerCase().equals("https") && sp.getBoolean(SettingsActivity.CB_ALLOWALLSSLCERTIFICATES_STRING, false)) {
            trustAllHosts();
            HttpsURLConnection https = (HttpsURLConnection) urlConn.openConnection();
            https.setHostnameVerifier(DO_NOT_VERIFY);
            httpConnection = https;
        } else {
            httpConnection = (HttpURLConnection) urlConn.openConnection();
        }

        httpConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
        httpConnection.setRequestMethod("PUT");

        if(nameValuePairs != null)
        {
            httpConnection.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        }

        InputStreamReader in = new InputStreamReader((InputStream) httpConnection.getContent());
        BufferedReader buff = new BufferedReader(in);
        String text = "";
        String line;
        do {
            line = buff.readLine();
            if(line != null)
                text += line + "\n";
        } while (line != null);
        Log.d(TAG, text);


        return httpConnection.getResponseCode();
        */


        URL url = new URL(urlString);
        DefaultHttpClient httpClient;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp.getBoolean(SettingsActivity.CB_ALLOWALLSSLCERTIFICATES_STRING, false) && url.getProtocol().toLowerCase().equals("https"))
            httpClient = new SSLHttpClient(context);
        else
            httpClient = new DefaultHttpClient();

        if(username != null && password != null)
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(username,password));

        /*
        HttpParams params = new BasicHttpParams();
        if(nameValuePairs != null)
	        for (NameValuePair nameValuePair : nameValuePairs)
	            params.setParameter(nameValuePair.getName(), nameValuePair.getValue());        
        httpClient.setParams(params);
        */
        
        HttpPut request = new HttpPut(url.toString());
        if(nameValuePairs != null)
        	request.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
        
        HttpResponse response = httpClient.execute(request);
        //ResponseHandler<String> responseHandler = new BasicResponseHandler();
        //String responseBody = httpClient.execute(request, responseHandler);
        
        //Thread.sleep(5000);
        
        return response.getStatusLine().getStatusCode();
        //return 200;
	}




    // always verify the host - dont check for certificate
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
