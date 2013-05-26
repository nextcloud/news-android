package de.luhmer.owncloudnewsreader.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.util.Log;
import de.luhmer.owncloudnewsreader.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class HttpJsonRequest {
	private static final String TAG = "HttpJsonRequest";

	
	public static JSONObject PerformJsonRequest(String url, List<NameValuePair> nameValuePairs, String username, String password) throws Exception
	{
        // http://androidarabia.net/quran4android/phpserver/connecttoserver.php

        // Log.i(getClass().getSimpleName(), "send  task - start");
        //HttpParams httpParams = new BasicHttpParams();
        //HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
        //HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
        //
        //HttpParams p = new BasicHttpParams();
        // p.setParameter("name", pvo.getName());
        //p.setParameter("user", "1");

        // Instantiate an HttpClient
        //HttpClient httpclient = new DefaultHttpClient(p);
        DefaultHttpClient httpClient = new DefaultHttpClient();
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

        if(nameValuePairs != null)
        {
            url += "&" + URLEncodedUtils.format(nameValuePairs, "utf-8");
            /*
            JSONObject jObj = new JSONObject();

            for (NameValuePair nameValuePair : nameValuePairs) {
                jObj.put(nameValuePair.getName(), nameValuePair.getValue());
            }*/

            //request.setEntity(new ByteArrayEntity(jObj.toString().getBytes("UTF8")));

            //httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        }
        HttpGet request = new HttpGet(url);

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


	public static int performTagChangeRequest(String url, String username, String password) throws Exception
	{
        //url = "http://192.168.10.126/owncloud/ocs/v1.php/apps/news/items/3787/read";

        String authString = username + ":" + password;
        String authStringEnc = Base64.encode(authString.getBytes());

        URL urlConn = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlConn.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
        connection.setRequestMethod("PUT");

        /*
        InputStreamReader in = new InputStreamReader((InputStream) connection.getContent());
        BufferedReader buff = new BufferedReader(in);
        String text = "";
        String line;
        do {
            line = buff.readLine();
            if(line != null)
                text += line + "\n";
        } while (line != null);
        Log.d(TAG, text);
        */

        return connection.getResponseCode();
	}
}
