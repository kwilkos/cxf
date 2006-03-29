package org.objectweb.celtix.context;

import java.io.InputStream;

import javax.xml.ws.handler.MessageContext;


public interface InputStreamMessageContext extends MessageContext {

    String DECOUPLED_RESPONSE = "org.objectweb.celtix.decoupled.response";
    String ASYNC_ONEWAY_DISPATCH = "org.objectweb.celtix.async.oneway.dispatch";
    
    InputStream getInputStream();
    
    void setInputStream(InputStream ins);
      
    void setFault(boolean isFault);
    boolean isFault();
}
