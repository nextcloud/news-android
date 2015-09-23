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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.reader.owncloud.API;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import de.luhmer.owncloudnewsreader.ssl.TLSSocketFactory;

public class HttpJsonRequest {
    private static final String TAG = "HttpJsonRequest";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static HttpJsonRequest instance;

    public static void init(Context context) {
        if(instance != null)
            throw new IllegalStateException("Already initialized");
        instance = new HttpJsonRequest(context);
    }

    public static HttpJsonRequest getInstance() {
        if(instance == null)
            throw new IllegalStateException("Must be initialized first");
        return instance;
    }

    private final OkHttpClient client;
    private String credentials;

    private HttpJsonRequest(Context context) {
        client = new OkHttpClient();
        // set location of the keystore
        MemorizingTrustManager.setKeyStoreFile("private", "sslkeys.bks");

        // register MemorizingTrustManager for HTTPS
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, MemorizingTrustManager.getInstanceList(context),
                    new java.security.SecureRandom());
            // enables TLSv1.1/1.2 for Jelly Bean Devices
            TLSSocketFactory tlsSocketFactory = new TLSSocketFactory(sc);
            client.setSslSocketFactory(tlsSocketFactory);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        client.setConnectTimeout(10000, TimeUnit.MILLISECONDS);
        client.setReadTimeout(120, TimeUnit.SECONDS);

        // disable hostname verification, when preference is set
        // (this still shows a certification dialog, which requires user interaction!)
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp.getBoolean(SettingsActivity.CB_DISABLE_HOSTNAME_VERIFICATION_STRING, false))
            client.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        client.interceptors().add(new AuthorizationInterceptor());

        setCredentials(sp.getString(SettingsActivity.EDT_USERNAME_STRING, null), sp.getString(SettingsActivity.EDT_PASSWORD_STRING, null));
    }

    public void setCredentials(String username, String password) {
        if(username != null)
            credentials = Credentials.basic(username, password);
        else
            credentials = null;
    }

    private class AuthorizationInterceptor implements Interceptor {
        public AuthorizationInterceptor() {
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            request = request.newBuilder()
                .addHeader("Authorization",credentials)
                .build();
            return chain.proceed(request);
        }
    }

	public InputStream PerformJsonRequest(String urlString, HashMap<String,String> nameValuePairs) throws Exception
	{
        HttpUrl.Builder urlBuilder = HttpUrl.parse(API.validateURL(urlString)).newBuilder();

		if(nameValuePairs != null) {
            for(String key: nameValuePairs.keySet())
                urlBuilder.addQueryParameter(key,nameValuePairs.get(key));
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

		//http://nelenkov.blogspot.de/2011/12/using-custom-certificate-trust-store-on.html
		//http://stackoverflow.com/questions/5947162/https-and-self-signed-certificate-issue
        //http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e537
		//http://stackoverflow.com/questions/859111/how-do-i-accept-a-self-signed-certificate-with-a-java-httpsurlconnection
		//http://developer.android.com/training/articles/security-ssl.html

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
        //	CookieHandler.setDefault(new CookieManager());

        Response response = client.newCall(request).execute();

        if(response.isSuccessful()) {
        	return response.body().byteStream();
        } else {
            throw new Exception(response.message());
        }
	}

    public int performCreateFeedRequest(String urlString, String feedUrl, long folderId) throws Exception {
        HttpUrl url = HttpUrl.parse(API.validateURL(urlString)).newBuilder()
                .setQueryParameter("url", feedUrl)
                .setQueryParameter("folderId", String.valueOf(folderId))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON, ""))
                .build();

        Response response = client.newCall(request).execute();

        return response.code();
    }

	public int performTagChangeRequest(String urlString, String content) throws Exception
	{
		HttpUrl url = HttpUrl.parse(API.validateURL(urlString));

        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(JSON, content))
                .build();

        Response response = client.newCall(request).execute();

		return response.code();
	}
}
