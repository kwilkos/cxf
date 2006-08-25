package org.objectweb.celtix.transports.http;


import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.messaging.AbstractCachedOutputStream;


/**
 * Wrapper stream required for two reasons:
 * <ol>
 * <li> So that that no data is written onto the wire until the headers are frozen
 * <li> To intercept the close call
 * </ol>
 */

public abstract class AbstractWrappedOutputStream extends AbstractCachedOutputStream {

    protected Message outMessage;
    private boolean flushed;
    
    AbstractWrappedOutputStream(Message m) {
        super();
        outMessage = m;
    }

    /**
     * @return true if already flushed
     */
    protected boolean alreadyFlushed() {
        boolean ret = flushed;
        flushed = true;
        return ret;
    }
}
