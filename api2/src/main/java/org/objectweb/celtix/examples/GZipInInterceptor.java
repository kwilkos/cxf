package org.objectweb.celtix.examples;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.message.Message;

public class GZipInInterceptor implements Interceptor {

    public void handleMessage(Message message) {
        try {
            InputStream in = message.getSource(InputStream.class);
            in = new GZIPInputStream(in);
            message.setSource(InputStream.class, in);
            
            message.getInterceptorChain().doIntercept(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void handleFault(Message message) {
        
    }

}
