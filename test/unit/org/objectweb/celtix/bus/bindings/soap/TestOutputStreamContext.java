package org.objectweb.celtix.bus.bindings.soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.OutputStreamMessageContext;

class TestOutputStreamContext
    extends MessageContextWrapper
    implements OutputStreamMessageContext {
    ByteArrayOutputStream baos;

    public TestOutputStreamContext(URL url, MessageContext ctx) throws IOException {
        super(ctx);
    }

    void flushHeaders() throws IOException { }

    public void setFault(boolean isFault) { }

    public boolean isFault() {
        return false;
    }

    public OutputStream getOutputStream() {
        if (baos == null) {
            baos = new ByteArrayOutputStream();
        }
        try {
            baos.flush(); 
        } catch (IOException ioe) {
            //to do nothing
        }
        return baos;
    }

    public byte[] getOutputStreamBytes() {
        return baos.toByteArray();
    }
    
    public void setOutputStream(OutputStream o) { }

    public InputStreamMessageContext createInputStreamContext() throws IOException {
        return new TestInputStreamContext(baos.toByteArray());
    }
}
