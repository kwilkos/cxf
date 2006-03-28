package org.objectweb.celtix.geronimo.container;

import java.io.OutputStream;

import javax.xml.ws.handler.MessageContext;

import org.apache.geronimo.webservices.WebServiceContainer.Response;
import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.OutputStreamMessageContext;

public class GeronimoOutputStreamServerMessageContext extends MessageContextWrapper implements
    OutputStreamMessageContext {

    private static final int ERROR_CODE = 400; 
    private static final int OK_CODE = 200; 
    
    private final Response response; 
    private OutputStream outStream; 
    private boolean isOneWay; 
    
    public GeronimoOutputStreamServerMessageContext(MessageContext ctx) {
        super(ctx);
        response = (Response)context.get(GeronimoInputStreamMessageContext.RESPONSE);
        assert response != null : "response not available in context";
        outStream = response.getOutputStream();
        response.setStatusCode(OK_CODE);
    }
    

    public OutputStream getOutputStream() {
        return outStream;
    }

    public void setOutputStream(OutputStream out) {
        outStream = out;
    }

    public void setFault(boolean isFault) {
        response.setStatusCode(ERROR_CODE);
    }

    public boolean isFault() {
        return response.getStatusCode() == ERROR_CODE;
        
    }

    public void setOneWay(boolean oneway) {
        isOneWay = oneway;
    }

    public boolean isOneWay() {
        return isOneWay;
    }
}
