package org.objectweb.celtix.context;

import java.io.InputStream;

import javax.xml.ws.handler.MessageContext;


public interface InputStreamMessageContext extends MessageContext {

    InputStream getInputStream();
    
    void setInputStream(InputStream ins);
      
    void setFault(boolean isFault);
    boolean isFault();
}
