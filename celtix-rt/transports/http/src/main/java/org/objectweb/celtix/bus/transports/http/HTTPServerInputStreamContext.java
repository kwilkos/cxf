package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;

public class HTTPServerInputStreamContext
    extends GenericMessageContext
    implements InputStreamMessageContext {

    public static final String HTTP_REQUEST =
        HTTPServerInputStreamContext.class.getName() + ".REQUEST";
    public static final String HTTP_RESPONSE =
        HTTPServerInputStreamContext.class.getName() + ".RESPONSE";
    
    private static final long serialVersionUID = 1L;

    protected final AbstractHTTPServerTransport transport;
    protected InputStream origInputStream;
    protected InputStream inStream;

    public HTTPServerInputStreamContext(AbstractHTTPServerTransport tr)
        throws IOException {
        transport = tr;
    }

    public void initContext() throws IOException {
        transport.setHeaders(this);
        inStream = origInputStream;
    }
    
    public InputStream getInputStream() {
        return inStream;
    }

    public void setInputStream(InputStream ins) {
        inStream = ins;
    }

    public void setFault(boolean isFault) {
        //nothing to do
    }

    public boolean isFault() {
        return false;
    }
}