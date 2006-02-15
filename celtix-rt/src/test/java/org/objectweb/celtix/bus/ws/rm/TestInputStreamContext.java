package org.objectweb.celtix.bus.ws.rm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;

public class TestInputStreamContext extends GenericMessageContext implements InputStreamMessageContext {

    private static final long serialVersionUID = 1L;
    private InputStream inputStream;

    public TestInputStreamContext() {
        put(ObjectMessageContext.MESSAGE_INPUT, false);
        /*
        put(HTTP_RESPONSE_HEADERS, connection.getHeaderFields());
        put(HTTP_RESPONSE_CODE, getResponseCode(connection));
        */ 
    }
    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream ins) {
        inputStream = ins;
    }
    
    public void setInputStream(byte[] bArray) {
        inputStream = new ByteArrayInputStream(bArray);
    }

    public boolean isFault() {
        return false;
    }

    public void setFault(boolean b) {
    }
}
