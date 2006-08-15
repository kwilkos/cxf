package org.objectweb.celtix.transports.http;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.objectweb.celtix.message.Message;


/**
 * Wrapper stream required for two reasons:
 * <ol>
 * <li> So that that no data is written onto the wire until the headers are frozen
 * <li> To intercept the close call
 * </ol>
 */

public abstract class AbstractWrappedOutputStream extends FilterOutputStream {

    protected Message outMessage;
        
    AbstractWrappedOutputStream(Message m) {
        super(new ByteArrayOutputStream());
        outMessage = m;
    } 

    public void flush() throws IOException {
        out.flush();
        doFlush();
    }

    public void close() throws IOException {
        out.flush();
        out.close();
        doClose();
    }

    /**
     * Perform any actions required on stream flush (freeze headers,
     * reset output stream ... etc.)
     */
    protected abstract void doFlush() throws IOException;

    /**
     * Perform any actions required on stream closure (handle response etc.)
     */
    protected abstract void doClose() throws IOException;
    
    /**
     * @return the underlying output stream
     */
    protected OutputStream getOut() {
        return out;
    }

    /**
     * Copy the cached output stream to the "real" output stream, 
     * i.e. onto the wire.
     * 
     * @param realOS the real output stream
     * @throws IOException
     */
    protected void resetOut(OutputStream realOS) throws IOException {
        ByteArrayOutputStream bout = (ByteArrayOutputStream)out;
        if (bout.size() > 0) {
            bout.writeTo(realOS);
        }
        out = realOS;
    }
}
