package de.luhmer.owncloudnewsreader.ssl

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/* This class should enable TLSv1.1 and TLSv1.2 on devices where they are available but not enabled.
   According to https://developer.android.com/reference/javax/net/ssl/SSLSocket.html
   this should only affect API Level 16 - 20.

   DISCLAIMER: The author is neither an Android/Java developer nor a software developer at all.
   Since this class affects security it shouldn't be used unless it was reviewed and tested
   by an qualified person.

*/
class TLSSocketFactory(sslContext: SSLContext) : SSLSocketFactory() {
    private val socketFactory: SSLSocketFactory

    init {
        socketFactory = sslContext.socketFactory
    }

    @Throws(IOException::class)
    override fun createSocket(
        socket: Socket,
        host: String,
        port: Int,
        autoClose: Boolean,
    ): Socket {
        val sslSocket =
            socketFactory.createSocket(
                socket,
                host,
                port,
                autoClose,
            ) as SSLSocket

        // Enable all supported Protocols
        sslSocket.enabledProtocols = sslSocket.supportedProtocols
        return sslSocket
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return socketFactory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return socketFactory.supportedCipherSuites
    }

    // NoTLS
    override fun createSocket(
        s: String,
        i: Int,
    ): Socket {
        return super.createSocket()
    }

    override fun createSocket(
        s: String,
        i: Int,
        inetAddress: InetAddress,
        i2: Int,
    ): Socket {
        return super.createSocket()
    }

    override fun createSocket(
        inetAddress: InetAddress,
        i: Int,
    ): Socket {
        return super.createSocket()
    }

    override fun createSocket(
        inetAddress: InetAddress,
        i: Int,
        inetAddress2: InetAddress,
        i2: Int,
    ): Socket {
        return super.createSocket()
    }
}
