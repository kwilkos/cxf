package org.objectweb.celtix.context;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.ws.handler.MessageContext;


public interface StreamMessageContext extends MessageContext {

    InputStream getInputStream();
    
    void setInputStream(InputStream ins);
    
    
    OutputStream getOutputStream();
    
    void setOutputStream(OutputStream out);    
    
}
