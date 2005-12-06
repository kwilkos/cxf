package org.objectweb.celtix.bus.transports.jms;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.OutputStreamMessageContext;

public class JMSOutputStreamContext 
    extends MessageContextWrapper 
    implements OutputStreamMessageContext {
    OutputStream os;
    
    public JMSOutputStreamContext(MessageContext ctx) {
        super(ctx);
    }
    
    
    public OutputStream getOutputStream() {
        // TODO Auto-generated method stub
        if (os == null) {
            os = new ByteArrayOutputStream();
        }
        return os;
    }

    public void setOutputStream(OutputStream out) {
        // TODO Auto-generated method stub
        os = out;
    }

    public void setFault(boolean isFault) {
        // TODO Auto-generated method stub

    }

    public boolean isFault() {
        // TODO Auto-generated method stub
        return false;
    }
    
    public void setOneWay(boolean isOneWay) {
        put(ONEWAY_MESSAGE_TF, isOneWay);
    }
    
    public boolean isOneWay() {
        return ((Boolean)get(ONEWAY_MESSAGE_TF)).booleanValue();
    }

}
