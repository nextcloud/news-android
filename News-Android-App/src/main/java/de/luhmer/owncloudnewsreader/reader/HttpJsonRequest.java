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

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.luhmer.owncloudnewsreader.SettingsActivity;
import de.luhmer.owncloudnewsreader.model.Tuple;
import de.luhmer.owncloudnewsreader.ssl.MemorizingTrustManager;
import de.luhmer.owncloudnewsreader.ssl.TLSSocketFactory;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpJsonRequest {

    @SuppressWarnings("unused")
    private static final String TAG = "HttpJsonRequest";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static HttpJsonRequest instance;

    public static void init(Context context) {
        instance = new HttpJsonRequest(context);
    }

    public static HttpJsonRequest getInstance() {
        if(instance == null)
            throw new IllegalStateException("Must be initialized first");
        return instance;
    }

    private final OkHttpClient client;
    private final OkHttpClient imageClient;

    private String credentials;
    private HttpUrl oc_root_url;


    private X509TrustManager systemDefaultTrustManager() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
    }

    private HttpJsonRequest(Context context) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        // set location of the keystore
        MemorizingTrustManager.setKeyStoreFile("private", "sslkeys.bks");

        // register MemorizingTrustManager for HTTPS
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, MemorizingTrustManager.getInstanceList(context), new java.security.SecureRandom());
            // enables TLSv1.1/1.2 for Jelly Bean Devices
            TLSSocketFactory tlsSocketFactory = new TLSSocketFactory(sc);
            clientBuilder.sslSocketFactory(tlsSocketFactory, systemDefaultTrustManager());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        clientBuilder.connectTimeout(10, TimeUnit.SECONDS);
        clientBuilder.readTimeout(120, TimeUnit.SECONDS);

        // disable hostname verification, when preference is set
        // (this still shows a certification dialog, which requires user interaction!)
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp.getBoolean(SettingsActivity.CB_DISABLE_HOSTNAME_VERIFICATION_STRING, false))
            clientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });


        clientBuilder.interceptors().add(new AuthorizationInterceptor());
        imageClient = clientBuilder.build();
        client = clientBuilder.build();

        setCredentials(sp.getString(SettingsActivity.EDT_USERNAME_STRING, null), sp.getString(SettingsActivity.EDT_PASSWORD_STRING, null), sp.getString(SettingsActivity.EDT_OWNCLOUDROOTPATH_STRING, null));
    }

    public void setCredentials(final String username, final String password, final String oc_root_path) {
        if(username != null)
            credentials = Credentials.basic(username, password);
        else
            credentials = null;

        if(oc_root_path != null) {
            // Add empty path segment to ensure trailing slash
            oc_root_url = HttpUrl.parse(oc_root_path).newBuilder().addPathSegment("").build();
        }
    }

    public HttpUrl getRootUrl() {
        return oc_root_url;
    }

    private class AuthorizationInterceptor implements Interceptor {
        public AuthorizationInterceptor() {
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            // only add Authorization header for urls on the configured owncloud host
            if(oc_root_url.host().equals(request.url().host()))
                request = request.newBuilder()
                    .addHeader("Authorization",credentials)
                    .build();
            return chain.proceed(request);
        }
    }

    public OkHttpClient getImageClient() {
        return imageClient;
    }

    public InputStream PerformJsonRequest(HttpUrl url) throws Exception
	{
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

		//http://nelenkov.blogspot.de/2011/12/using-custom-certificate-trust-store-on.html
		//http://stackoverflow.com/questions/5947162/https-and-self-signed-certificate-issue
        //http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e537
		//http://stackoverflow.com/questions/859111/how-do-i-accept-a-self-signed-certificate-with-a-java-httpsurlconnection
		//http://developer.android.com/training/articles/security-ssl.html

        // CookieHandler.setDefault(new CookieManager());

        Response response = client.newCall(request).execute();

        if(response.isSuccessful()) {
        	return response.body().byteStream();
        } else {
            throw new Exception(response.message());
        }
	}

    public int performCreateFeedRequest(HttpUrl url, String feedUrlString, long folderId) throws Exception {
        HttpUrl feedUrl = url.newBuilder()
                .setQueryParameter("url", feedUrlString)
                .setQueryParameter("folderId", String.valueOf(folderId))
                .build();

        Request request = new Request.Builder()
                .url(feedUrl)
                .post(RequestBody.create(JSON, ""))
                .build();

        Response response = client.newCall(request).execute();

        return response.code();
    }

    public int performRemoveFeedRequest(HttpUrl url, long feedId) throws Exception {
        HttpUrl feedUrl = url.newBuilder()
                .addPathSegment(String.valueOf(feedId))
                .build();

        Request request = new Request.Builder()
                .url(feedUrl)
                .delete()
                .build();

        Response response = client.newCall(request).execute();

        return response.code();
    }

    public int performRenameFeedRequest(HttpUrl url, long feedId, String newFeedName) throws Exception {
        HttpUrl feedUrl = url.newBuilder()
                .addPathSegment(String.valueOf(feedId))
                .addPathSegment("rename")
                .build();

        Request request = new Request.Builder()
                .url(feedUrl)
                .put(RequestBody.create(JSON, new JSONObject().put("feedTitle", newFeedName).toString()))
                .build();

        Response response = client.newCall(request).execute();

        return response.code();
    }

	public int performTagChangeRequest(HttpUrl url, String content) throws Exception
	{
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(JSON, content))
                .build();

        Response response = client.newCall(request).execute();

		return response.code();
	}

    public Tuple<Integer, String> performCreateFolderRequest(HttpUrl url, String folderName) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON, new JSONObject().put("name", folderName).toString()))
                .build();

        Response response = client.newCall(request).execute();
        String body = response.body().string();
        return new Tuple<>(response.code(), body);
    }
}
