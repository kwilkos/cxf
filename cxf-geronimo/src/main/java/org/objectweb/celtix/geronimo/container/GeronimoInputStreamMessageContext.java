package org.objectweb.celtix.geronimo.container;

import java.io.IOException;
import java.io.InputStream;

import org.apache.geronimo.webservices.WebServiceContainer.Request;
import org.apache.geronimo.webservices.WebServiceContainer.Response;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;

public class GeronimoInputStreamMessageContext extends ObjectMessageContextImpl 
                                               implements InputStreamMessageContext {

    static final String REQUEST = GeronimoInputStreamMessageContext.class.getName() + ".REQUEST";
    static final String RESPONSE = GeronimoInputStreamMessageContext.class.getName() + ".RESPONSE";
    private boolean isFault;
    private InputStream inStream; 
    
    GeronimoInputStreamMessageContext() {
    }
    
    Request getRequest() {
        return (Request)get(REQUEST);
    }
    
    void setRequest(Request req) { 
        put(REQUEST, req);
    }
    
    Response getResponse() {
        return (Response)get(RESPONSE);
    }
    
    void setResponse(Response resp) {
        put(RESPONSE, resp);
    }

    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        if (inStream != null) {
            return inStream;
        }
        try {
            return getRequest().getInputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setInputStream(InputStream ins) {
        inStream = ins;
    }

    public void setFault(boolean fault) {
        isFault = fault;
    }

    public boolean isFault() {
        // TODO Auto-generated method stub
        return isFault;
    }
}
