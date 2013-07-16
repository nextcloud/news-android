package de.luhmer.owncloudnewsreader.helper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.params.HttpParams;

public class CustomSSLSocketFactory implements LayeredSocketFactory {/* extends SSLSocketFactory {*/

	private SSLSocketFactory socketFactory;
    private X509HostnameVerifier hostnameVerifier;

    public CustomSSLSocketFactory(SSLContext sslCtx,
            X509HostnameVerifier hostnameVerifier) {
        this.socketFactory = sslCtx.getSocketFactory();
        this.hostnameVerifier = hostnameVerifier;
    }

    //..

    @Override
    public Socket createSocket() throws IOException {
        return socketFactory.createSocket();
    }

	@Override
	public Socket connectSocket(Socket arg0, String arg1, int arg2,
			InetAddress arg3, int arg4, HttpParams arg5) throws IOException,
			UnknownHostException, ConnectTimeoutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSecure(Socket arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3)
			throws IOException, UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	public static KeyStore getTruststore() {
		KeyStore truststore = null;
		try { 
			truststore = KeyStore.getInstance("BKS");
			truststore.load(null, null);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
		return truststore;
	}
	
    private SSLContext sslContext = SSLContext.getInstance("TLS");

    public CustomSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {    	
        super(truststore);

        TrustManager tm = new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        };

        sslContext.init(null, new TrustManager[] {tm}, null);

        // Registering schemes for both HTTP and HTTPS
	    SchemeRegistry registry = new SchemeRegistry();	    
		//SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    	setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);    	
    	registry.register(new Scheme("https", this, 443));
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }*/
}
