package de.luhmer.owncloudnewsreader.reader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.SSLHttpClient;
import de.luhmer.owncloudnewsreader.util.Base64;

public class HttpJsonRequest {
	//private static final String TAG = "HttpJsonRequest";

	@SuppressLint("DefaultLocale")
	public static InputStream PerformJsonRequest(String urlString, List<NameValuePair> nameValuePairs, final String username, final String password, Context context) throws Exception
	{	
		if(nameValuePairs != null)
            urlString += "&" + URLEncodedUtils.format(nameValuePairs, "utf-8"); 
		
		URL url = new URL(urlString);
		
		HttpURLConnection urlConnection = null;
		
		if(url.getProtocol().toLowerCase(Locale.ENGLISH).equals("http"))
			urlConnection = (HttpURLConnection) url.openConnection();
		else	
		{
			HttpsURLConnection sslConnection = (HttpsURLConnection) url.openConnection();
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);    	
	        if(sp.getBoolean(SettingsActivity.CB_ALLOWALLSSLCERTIFICATES_STRING, false))
	        {
	        	//TODO SSL Certificate stuff needs to be implemented here..
	        }
			urlConnection = sslConnection;
		}
		//urlConnection.setHostnameVerifier(new CustomHostnameVerifier());
		//HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		
		// Define an array of pins.  One of these must be present
		// in the certificate chain you receive.  A pin is a hex-encoded
		// hash of a X.509 certificate's SubjectPublicKeyInfo. A pin can
		// be generated using the provided pin.py script:
		// python ./tools/pin.py certificate_file.pem

		
		//String[] pins                 = new String[] {"f30012bbc18c231ac1a44b788e410ce754182513"};
		//HttpsURLConnection urlConnection = PinningHelper.getPinnedHttpsURLConnection(context, pins, url);
				
		
		//TODO Implement the SSL Socket stuff here..
		//http://nelenkov.blogspot.de/2011/12/using-custom-certificate-trust-store-on.html
		//http://stackoverflow.com/questions/5947162/https-and-self-signed-certificate-issue
        //http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e537
		//http://stackoverflow.com/questions/859111/how-do-i-accept-a-self-signed-certificate-with-a-java-httpsurlconnection
		//http://developer.android.com/training/articles/security-ssl.html
		
		
		
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);    	
        if(sp.getBoolean(SettingsActivity.CB_ALLOWALLSSLCERTIFICATES_STRING, false) && url.getProtocol().toLowerCase(Locale.ENGLISH).equals("https")) {
        	//urlConnection.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

        	//CustomSSLSocketFactory sslCtx = new CustomSSLSocketFactory(CustomSSLSocketFactory.getTruststore());
        	//urlConnection.setSSLSocketFactory(sslCtx.getSocketFactory());
        	
        	/*
        	MyTrustManager myTrustManager = new MyTrustManager();
        	TrustManager[] tms = new TrustManager[] { myTrustManager };
        	SSLContext sslCtx = SSLContext.getInstance("TLS");
        	sslCtx.init(null,  tms,  null);
        	  */      	
        	//MySSLSocketFactory sslSocketFactory = new MySSLSocketFactory();
        	//		sslCtx, new BrowserCompatHostnameVerifier());
        	//schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
        	//urlConnection.setSSLSocketFactory(MySSLSocketFactory.getSocketFactory());
        }

    	if(username != null && password != null)
    		urlConnection.setRequestProperty("Authorization", "Basic " + Base64.encode((username + ":" + password).getBytes()));        		

        urlConnection.setDoOutput(false);
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");
        //urlConnection.setFollowRedirects(true); 
        urlConnection.setUseCaches(false);  
        urlConnection.setConnectTimeout(10000);  
        urlConnection.setReadTimeout(120000);//2min  
        urlConnection.setRequestProperty("Content-Type","application/json");   
        
        //urlConnection.setRequestProperty("Host", "de.luhmer.ownCloudNewsReader");
        urlConnection.connect();  
        
        int HttpResult = urlConnection.getResponseCode();  
        if(HttpResult == HttpURLConnection.HTTP_OK) {
        	return urlConnection.getInputStream();
        } else {  
           throw new Exception(urlConnection.getResponseMessage());  
        }
	}
	
	/*
	@SuppressLint("DefaultLocale")
	public static JSONObject PerformJsonRequest_old(String urlString, List<NameValuePair> nameValuePairs, String username, String password, Context context) throws Exception
	{	
        if(nameValuePairs != null)
            urlString += "&" + URLEncodedUtils.format(nameValuePairs, "utf-8");

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
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials(username,password));

        //HttpGet request = new HttpGet(url);
        //HttpPost request = new HttpPost(url);
        //httpClient.setParams(params)


        // Instantiate a GET HTTP method  
        HttpGet request = new HttpGet(url.toString());

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpClient.execute(request, responseHandler);
        JSONObject json = new JSONObject(responseBody);
        return json;
        //HttpResponse response = httpClient.execute(request);
        //return null;

        // Log.i(getClass().getSimpleName(), "send  task - end");
	}
	*/
	

	@SuppressLint("DefaultLocale")
	public static int performTagChangeRequest(String urlString, String username, String password, Context context, String content) throws Exception
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
        //if(nameValuePairs != null)
        //	request.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
        request.setEntity(new StringEntity(content));
        request.addHeader("Accept", "application/json");
        
        
        HttpResponse response = httpClient.execute(request);
        //ResponseHandler<String> responseHandler = new BasicResponseHandler();
        //String responseBody = httpClient.execute(request, responseHandler);
        
        //Thread.sleep(5000);
        
        return response.getStatusLine().getStatusCode();
        //return 200;
	}




    /**
     * Trust every server - dont check for any certificate
     */
    /*
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
    }*/
}
