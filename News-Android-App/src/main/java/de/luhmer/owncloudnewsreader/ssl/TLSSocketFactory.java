package de.luhmer.owncloudnewsreader.ssl;

/* This class should enable TLSv1.1 and TLSv1.2 on devices where they are available but not enabled.
   According to https://developer.android.com/reference/javax/net/ssl/SSLSocket.html
   this should only affect API Level 16 - 20.

   DISCLAIMER: The author is neither an Android/Java developer nor a software developer at all.
   Since this class affects security it shouldn't be used unless it was reviewed and tested
   by an qualified person.

*/

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLSSocketFactory extends SSLSocketFactory {

    private final javax.net.ssl.SSLSocketFactory socketFactory;

    public TLSSocketFactory(SSLContext sslContext) {
        super();
        this.socketFactory = sslContext.getSocketFactory();
    }

    @Override
    public Socket createSocket(
            final Socket socket,
            final String host,
            final int port,
            final boolean autoClose
    ) throws java.io.IOException {

        SSLSocket sslSocket = (SSLSocket) this.socketFactory.createSocket(
                socket,
                host,
                port,
                autoClose
        );

        //Enable all supported Protocols
        sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());

        return sslSocket;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.socketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.socketFactory.getSupportedCipherSuites();
    }

    //NoTLS
    @Override
    public Socket createSocket(String s, int i) throws IOException {
        return null;
    }
    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i2) throws IOException {
        return null;
    }
    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        return null;
    }
    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) throws IOException {
        return null;
    }


 }