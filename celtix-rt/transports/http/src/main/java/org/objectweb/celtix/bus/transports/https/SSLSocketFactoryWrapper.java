package org.objectweb.celtix.bus.transports.https;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.objectweb.celtix.common.logging.LogUtils;

class SSLSocketFactoryWrapper extends SSLSocketFactory {
    
    private static final Logger LOG = LogUtils.getL7dLogger(SSLSocketFactoryWrapper.class);
    
    private SSLSocketFactory sslSocketFactory;
    private String[] ciphers;
    
    public SSLSocketFactoryWrapper(SSLSocketFactory sslSocketFactoryParam, String[] ciphersParam) {
        sslSocketFactory = sslSocketFactoryParam;
        ciphers = ciphersParam;
    }

    
    public String[] getDefaultCipherSuites() {
        return sslSocketFactory.getDefaultCipherSuites();
    }
    
    public String[] getSupportedCipherSuites() {
        return sslSocketFactory.getSupportedCipherSuites(); 
    }
    
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
        throws IOException, UnknownHostException  {
        
        SSLSocket socket = null;
        socket = (SSLSocket)sslSocketFactory.createSocket(s, host, port, autoClose);
        if ((socket != null) && (ciphers != null)) {
            socket.setEnabledCipherSuites(ciphers);
        }

        return socket; 
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        SSLSocket socket = null;
        socket = (SSLSocket)sslSocketFactory.createSocket(host, port);
        if ((socket != null) && (ciphers != null)) {
            socket.setEnabledCipherSuites(ciphers);
        }
        if (socket == null) {
            LogUtils.log(LOG, Level.SEVERE, "PROBLEM_CREATING_OUTBOUND_REQUEST_SOCKET", 
                         new Object[]{host, port});
        }
        return socket; 
    }


    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) 
        throws IOException, UnknownHostException {
        SSLSocket socket = null;
        socket = (SSLSocket)sslSocketFactory.createSocket(host, port, localHost, localPort);
        if ((socket != null) && (ciphers != null)) {
            socket.setEnabledCipherSuites(ciphers);
        }

        if (socket == null) {
            LogUtils.log(LOG, Level.SEVERE, "PROBLEM_CREATING_OUTBOUND_REQUEST_SOCKET", 
                         new Object[]{host, port});
        }
        return socket;
    }


    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocket socket = null;
        socket = (SSLSocket)sslSocketFactory.createSocket(host, port);
        if ((socket != null) && (ciphers != null)) {
            socket.setEnabledCipherSuites(ciphers);
        }

        if (socket == null) {
            LogUtils.log(LOG, Level.SEVERE, "PROBLEM_CREATING_OUTBOUND_REQUEST_SOCKET", 
                         new Object[]{host, port});
        }
        return socket;
    }


    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) 
        throws IOException {
        SSLSocket socket = null;
        socket = (SSLSocket)sslSocketFactory.createSocket(address, port, localAddress, localPort);
        if ((socket != null) && (ciphers != null)) {
            socket.setEnabledCipherSuites(ciphers);
        }

        if (socket == null) {
            LogUtils.log(LOG, Level.SEVERE, "PROBLEM_CREATING_OUTBOUND_REQUEST_SOCKET", 
                         new Object[]{address, port});
        }
        return socket;
    }
    
    /*
     * For testing only
     */
    protected void addLogHandler(Handler handler) {
        LOG.addHandler(handler);
    }
}