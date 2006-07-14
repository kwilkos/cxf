package org.objectweb.celtix.jaxws.handlers;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.objectweb.celtix.common.logging.LogUtils;
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

    private static final Logger LOG = 
        LogUtils.getL7dLogger(StreamMessageContextImpl.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();
    
    public StreamMessageContextImpl(Message m) { 
        super(m); 
    }

    public InputStream getInputStream() {   
        InputStream is = getWrappedMessage().getSource(InputStream.class);
        if (is == null) { 
            throw new IllegalStateException(BUNDLE.getString("NO_INPUT_STREAM_EXC"));
        }
        return is;
    } 

    public void setInputStream(InputStream is) { 
        getWrappedMessage().setSource(InputStream.class, is);
    } 

    public OutputStream getOutputStream() { 
        OutputStream os =  getWrappedMessage().getSource(OutputStream.class);
        if (os == null) { 
            throw new IllegalStateException(BUNDLE.getString("NO_OUTPUT_STREAM_EXC"));
        }
        return os;
    } 

    public void setOutputStream(OutputStream os) { 
        getWrappedMessage().setSource(OutputStream.class, os);
    } 
}
