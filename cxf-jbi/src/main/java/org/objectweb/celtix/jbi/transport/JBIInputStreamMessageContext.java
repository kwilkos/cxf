package org.objectweb.celtix.jbi.transport;



import java.io.InputStream;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.MessageContextWrapper;

public class JBIInputStreamMessageContext extends MessageContextWrapper 
    implements InputStreamMessageContext { 
    
    private InputStream inputStream; 
    private boolean isFault; 
    
    public JBIInputStreamMessageContext(MessageContext wrapped, InputStream in) { 
        super(wrapped);
        inputStream = in;
    } 
    
    public InputStream getInputStream() { 
        return inputStream;
    } 
    
    public void setInputStream(InputStream in) {
        inputStream = in;
    }
    
    public void setFault(boolean fault) { 
        isFault = fault;
    } 
    
    public boolean isFault() { 
        return isFault;
    }  
}
