package org.objectweb.celtix.geronimo.container;

import java.io.OutputStream;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.OutputStreamMessageContext;

public class GeronimoOutputStreamMessageContext extends MessageContextWrapper implements
    OutputStreamMessageContext {

    public GeronimoOutputStreamMessageContext(MessageContext ctx) {
        super(ctx);
        // TODO Auto-generated constructor stub
    }

    public OutputStream getOutputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setOutputStream(OutputStream out) {
        // TODO Auto-generated method stub

    }

    public void setFault(boolean isFault) {
        // TODO Auto-generated method stub

    }

    public boolean isFault() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setOneWay(boolean isOneWay) {
        // TODO Auto-generated method stub

    }

    public boolean isOneWay() {
        // TODO Auto-generated method stub
        return false;
    }
}
