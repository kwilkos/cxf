package org.objectweb.celtix.context;

import java.io.OutputStream;

import javax.xml.ws.handler.MessageContext;


public interface OutputStreamMessageContext extends MessageContext {
    String ONEWAY_MESSAGE_TF = 
        "org.objectweb.celtix.transport.isOneWayMessage";

    OutputStream getOutputStream();
    
    void setOutputStream(OutputStream out);
      
    void setFault(boolean isFault);

    boolean isFault();
    
    void setOneWay(boolean isOneWay);
    
    boolean isOneWay();
}
