package de.luhmer.owncloudnewsreader.helper;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.StrictHostnameVerifier;

import android.util.Log;

public class CustomHostnameVerifier implements HostnameVerifier  {
	
	private static final String TAG = "CustomHostnameVerifier";

	@Override
	public boolean verify(String hostname, SSLSession session) {
		Log.d(TAG, session.getCipherSuite().toString());
		return false;
	}
}
