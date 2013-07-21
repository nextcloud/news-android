package de.luhmer.owncloudnewsreader.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.helper.CustomTrustManager;
import de.luhmer.owncloudnewsreader.util.Base64;

public class HttpJsonRequest {
	//private static final String TAG = "HttpJsonRequest";

	@SuppressLint("DefaultLocale")
	public static InputStream PerformJsonRequest(String urlString, List<NameValuePair> nameValuePairs, final String username, final String password, Context context) throws AuthenticationException, Exception
	{	
		if(nameValuePairs != null)
            urlString += "&" + URLEncodedUtils.format(nameValuePairs, "utf-8"); 
		
		URL url = new URL(urlString);
		
		HttpURLConnection urlConnection = getUrlConnection(url, context, username, password);
		//HttpsURLConnection urlConnection = null;
		
		
		// Define an array of pins.  One of these must be present
		// in the certificate chain you receive.  A pin is a hex-encoded
		// hash of a X.509 certificate's SubjectPublicKeyInfo. A pin can
		// be generated using the provided pin.py script:
		// python ./tools/pin.py certificate_file.pem

		
		//String[] pins                 = new String[] {"f30012bbc18c231ac1a44b788e410ce754182513"};
		//HttpsURLConnection urlConnection = PinningHelper.getPinnedHttpsURLConnection(context, pins, url);
				
		
		
		//http://nelenkov.blogspot.de/2011/12/using-custom-certificate-trust-store-on.html
		//http://stackoverflow.com/questions/5947162/https-and-self-signed-certificate-issue
        //http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e537
		//http://stackoverflow.com/questions/859111/how-do-i-accept-a-self-signed-certificate-with-a-java-httpsurlconnection
		//http://developer.android.com/training/articles/security-ssl.html
		
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
        	if(urlConnection.getResponseMessage().equals("Unauthorized"))
        		throw new AuthenticationException(urlConnection.getResponseMessage());
        	else
        		throw new Exception(urlConnection.getResponseMessage());  
        }
	}
	
	
	private static HttpURLConnection getUrlConnection(URL url, Context context, String username, String password) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		HttpURLConnection urlConnection = null; 
		if(url.getProtocol().toLowerCase(Locale.ENGLISH).equals("http"))
			urlConnection = (HttpURLConnection) url.openConnection();
		else	
		{	
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);    	
	        if(sp.getBoolean(SettingsActivity.CB_ALLOWALLSSLCERTIFICATES_STRING, false))
	        {	        	
	        	TrustManager[] trustAllCerts = new TrustManager[] { new CustomTrustManager() };
	    		SSLContext sc = SSLContext.getInstance("SSL");
	    		sc.init(null, trustAllCerts, new java.security.SecureRandom());
	    		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	        	
	    		// Install the all-trusting host verifier
	    		//HttpsURLConnection.setDefaultHostnameVerifier(new CustomHostnameVerifier());
	    		
	    		//HttpsURLConnection.setDefaultHostnameVerifier(new StrictHostnameVerifier());	    		
	    		HttpsURLConnection.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
	        }
	        urlConnection = (HttpURLConnection) url.openConnection();
		}
		
		if(username != null && password != null)
    		urlConnection.setRequestProperty("Authorization", "Basic " + Base64.encode((username + ":" + password).getBytes()));
		
		return urlConnection;
	}
	

	@SuppressLint("DefaultLocale")
	public static int performTagChangeRequest(String urlString, String username, String password, Context context, String content) throws Exception
	{
		URL url = new URL(urlString);
		HttpURLConnection urlConnection = getUrlConnection(url, context, username, password);
		urlConnection.setDoOutput(true);
		urlConnection.setRequestMethod("PUT");
		urlConnection.setRequestProperty("Content-Type","application/json");
		
		if(content != null) {
			OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
			out.write(content);
			out.close();
		}
		
		return urlConnection.getResponseCode();
		
		/*
        URL url = new URL(urlString);
        DefaultHttpClient httpClient;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp.getBoolean(SettingsActivity.CB_ALLOWALLSSLCERTIFICATES_STRING, false) && url.getProtocol().toLowerCase().equals("https"))
            httpClient = new SSLHttpClient(context);
        else
            httpClient = new DefaultHttpClient();

        if(username != null && password != null)
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(username,password));

        HttpPut request = new HttpPut(url.toString());     
        request.setEntity(new StringEntity(content));
        request.addHeader("Accept", "application/json");
        
        HttpResponse response = httpClient.execute(request);
        return response.getStatusLine().getStatusCode();
        */
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
