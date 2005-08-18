package org.objectweb.celtix.context;

import java.io.OutputStream;

import javax.xml.ws.handler.MessageContext;


public interface OutputStreamMessageContext extends MessageContext {

    OutputStream getOutputStream();
    
    void setOutputStream(OutputStream ins);
      
    
}
