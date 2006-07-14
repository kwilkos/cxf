package org.objectweb.celtix.examples;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.message.Message;

public class GZipOutInterceptor implements Interceptor {

    public void handleMessage(Message message) {
        try {
            message.getInterceptorChain().doIntercept(message);

            OutputStream out = message.getSource(OutputStream.class);
            out = new GZIPOutputStream(out);
            message.setSource(OutputStream.class, out);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void handleFault(Message message) {
        
    }

}
