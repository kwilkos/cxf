package org.objectweb.celtix.transports.https;

import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

class DummyHttpsConnection extends HttpsURLConnection {

    protected DummyHttpsConnection(URL arg0) {
        super(arg0);
    }

    public String getCipherSuite() {
        return null;
    }

    
    public void disconnect() {
       
    }

    @Override
    public boolean usingProxy() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void connect() throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Certificate[] getLocalCertificates() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        // TODO Auto-generated method stub
        return null;
    }
  
}
