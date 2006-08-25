package org.objectweb.celtix.bus.context;

import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.MessageContextWrapper; 
import org.objectweb.celtix.context.OutputStreamMessageContext; 
import org.objectweb.celtix.context.StreamMessageContext; 




/**
 * Describe class StreamMessageContextImpl here.
 *
 *
 * Created: Thu Nov 17 14:52:48 2005
 *
 * @author <a href="mailto:codea@iona.com">Conrad O'Dea</a>
 * @version 1.0
 */
public class StreamMessageContextImpl extends MessageContextWrapper implements StreamMessageContext {

    private InputStreamMessageContext inContext; 
    private OutputStreamMessageContext outContext; 

    public StreamMessageContextImpl(InputStreamMessageContext wrapped) { 
        super(wrapped); 
        inContext = wrapped;
        if  (wrapped == null) { 
            throw new NullPointerException("wrapped context must not be null"); 
        }
    }

    public StreamMessageContextImpl(OutputStreamMessageContext wrapped) { 
        super(wrapped); 
        outContext = wrapped; 
        if  (wrapped == null) { 
            throw new NullPointerException("wrapped context must not be null"); 
        }
    }

    public InputStream getInputStream() { 
        if (inContext == null) { 
            throw new IllegalStateException("Context intialised for OutputStream");
        }
        return inContext.getInputStream();
    } 

    public void setInputStream(InputStream in) { 
        if (inContext == null) { 
            throw new IllegalStateException("Context intialised for OutputStream");
        }
        inContext.setInputStream(in); 
    } 

    public OutputStream getOutputStream() { 
        if (outContext == null) { 
            throw new IllegalStateException("Context intialised for OutputStream");
        }
        return outContext.getOutputStream();
    } 

    public void setOutputStream(OutputStream out) { 
        if (outContext == null) { 
            throw new IllegalStateException("Context intialised for OutputStream");
        }
        outContext.setOutputStream(out);
    } 
}
