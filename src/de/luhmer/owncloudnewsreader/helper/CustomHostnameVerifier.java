package de.luhmer.owncloudnewsreader.helper;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

import org.apache.http.conn.ssl.AbstractVerifier;

import android.util.Log;

public class CustomHostnameVerifier extends AbstractVerifier {
	
	private static final String TAG = "CustomHostnameVerifier";

	@Override
	public void verify(String host, String[] cns, String[] subjectAlts)
			throws SSLException {
		Log.d(TAG, host);
	}

	/*
	public boolean verify(String hostname, SSLSession session) {
		X509Certificate[] chain = session.getPeerCertificateChain();
		return checked;
	}*/
}
