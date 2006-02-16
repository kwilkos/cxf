package org.objectweb.celtix.jbi.transport;



import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.OutputStreamMessageContext;



public class JBIOutputStreamMessageContext extends MessageContextWrapper 
    implements OutputStreamMessageContext { 
        
    private OutputStream out = new ByteArrayOutputStream();
    private boolean fault; 
    private boolean oneway; 
        
    public JBIOutputStreamMessageContext(MessageContext wrapped) { 
        super(wrapped);
    } 

    public OutputStream getOutputStream() { 
        return out;
    } 
    
    public void setOutputStream(OutputStream os) { 
        out = os;
    } 
      
    public void setFault(boolean isFault) { 
        fault = isFault;
    } 

    public boolean isFault() { 
        return fault;
    } 
    
    public void setOneWay(boolean isOneWay) { 
        oneway = isOneWay;
    } 
    
    public boolean isOneWay() { 
        return oneway;
    } 
}
