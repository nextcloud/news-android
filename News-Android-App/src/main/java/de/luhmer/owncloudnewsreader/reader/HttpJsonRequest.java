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

package de.luhmer.owncloudnewsreader.reader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import de.luhmer.owncloudnewsreader.ssl.TLSSocketFactory;
import de.luhmer.owncloudnewsreader.util.Base64;

public class HttpJsonRequest {
	//private static final String TAG = "HttpJsonRequest";

	//@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("DefaultLocale")
	public static InputStream PerformJsonRequest(String urlString, List<NameValuePair> nameValuePairs, final String username, final String password, Context context) throws AuthenticationException, Exception
	{
		if(nameValuePairs != null)
            urlString += "&" + URLEncodedUtils.format(nameValuePairs, "utf-8");

		URL url = new URL(API.validateURL(urlString));

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


        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
        //	CookieHandler.setDefault(new CookieManager());


        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0");
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
		URLConnection urlConnection = url.openConnection();

		// If https is used, use MemorizingTrustManager for certificate verification
		if (urlConnection instanceof HttpsURLConnection) {
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;

            /*
            try {
                TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                } };
                // Install the all-trusting trust manager
                final SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            */


			// set location of the keystore
			MemorizingTrustManager.setKeyStoreFile("private", "sslkeys.bks");

			// register MemorizingTrustManager for HTTPS
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, MemorizingTrustManager.getInstanceList(context),
					new java.security.SecureRandom());

            // enables TLSv1.1/1.2 for Jelly Bean Devices
            TLSSocketFactory tlsSocketFactory = new TLSSocketFactory(sc);

            httpsURLConnection.setSSLSocketFactory(tlsSocketFactory);


			// disable redirects to reduce possible confusion
			//httpsURLConnection.setFollowRedirects(false);

			// disable hostname verification, when preference is set
			// (this still shows a certification dialog, which requires user interaction!)
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
	        if(sp.getBoolean(SettingsActivity.CB_DISABLE_HOSTNAME_VERIFICATION_STRING, false))
	        	httpsURLConnection.setHostnameVerifier(new AllowAllHostnameVerifier());
	        else
	        	httpsURLConnection.setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
		}

		if(username != null && password != null)
    		urlConnection.setRequestProperty("Authorization", "Basic " + Base64.encode((username + ":" + password).getBytes()));

		return (HttpURLConnection) urlConnection;
	}

	/*
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
	    		//HttpsURLConnection.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
	    		HttpsURLConnection.setDefaultHostnameVerifier(new BrowserCompatHostnameVerifier());
	        }
	        urlConnection = (HttpURLConnection) url.openConnection();
		}

		if(username != null && password != null)
    		urlConnection.setRequestProperty("Authorization", "Basic " + Base64.encode((username + ":" + password).getBytes()));

		return urlConnection;
	}
	*/

    public static int performCreateFeedRequest(String urlString, String username, String password, Context context, String feedUrl, long folderId) throws Exception {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("url", feedUrl));
        nameValuePairs.add(new BasicNameValuePair("folderId", String.valueOf(folderId)));
        if(nameValuePairs != null)
            urlString += "&" + URLEncodedUtils.format(nameValuePairs, "utf-8");


        URL url = new URL(API.validateURL(urlString));
        HttpURLConnection urlConnection = getUrlConnection(url, context, username, password);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(false);
        urlConnection.setDoInput(true);
        //urlConnection.setFollowRedirects(true);
        urlConnection.setUseCaches(false);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(120000);//2min
        urlConnection.setRequestProperty("Content-Type","application/json");


        urlConnection.connect();

        /*
        int HttpResult = urlConnection.getResponseCode();
        if(HttpResult == HttpURLConnection.HTTP_OK) {
            return urlConnection.getInputStream();
        } else {
            if(urlConnection.getResponseMessage().equals("Unauthorized"))
                throw new AuthenticationException(urlConnection.getResponseMessage());
            else
                throw new Exception(urlConnection.getResponseMessage());
        }
        */

        return urlConnection.getResponseCode();
    }


	@SuppressLint("DefaultLocale")
	public static int performTagChangeRequest(String urlString, String username, String password, Context context, String content) throws Exception
	{
		URL url = new URL(API.validateURL(urlString));
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
