package org.objectweb.celtix.jaxws.handlers;

import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.celtix.jaxws.context.WrappedMessageContext;
import org.objectweb.celtix.message.Message;



/**
 * Describe class StreamMessageContextImpl here.
 *
 *
 * Created: Thu Nov 17 14:52:48 2005
 *
 * @author <a href="mailto:codea@iona.com">Conrad O'Dea</a>
 * @version 1.0
 */
public class StreamMessageContextImpl extends WrappedMessageContext 
    implements StreamMessageContext {

    public StreamMessageContextImpl(Message m) { 
        super(m); 
    }

    public InputStream getInputStream() {   
        // Message m = getWrappedMessage();
        InputStream is = null;
        // TODO: get input stream from message
        if (is == null) { 
            throw new IllegalStateException("Context intialised for OutputStream");
        }
        return is;
    } 

    public void setInputStream(InputStream in) { 
        // Message m = getWrappedMessage();
        // replace input stream in message with this one or else
        throw new IllegalStateException("Context intialised for OutputStream");
    } 

    public OutputStream getOutputStream() { 
        // Message m = getWrappedMessage();
        OutputStream os = null;
        // TODO: get output stream from message
        if (os == null) { 
            throw new IllegalStateException("Context intialised for InputStream");
        }
        return os;
    } 

    public void setOutputStream(OutputStream out) { 
        // Message m = getWrappedMessage();
        // replace output stream in message with this one or else 
        throw new IllegalStateException("Context intialised for InputStream");
    } 
}
