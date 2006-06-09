package org.objectweb.celtix.bus.ws.rm.persistence;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.i18n.UncheckedException;

public class RMStoreException  extends UncheckedException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>RMPersistenceException</code> with the provided detail message.
     */
    public RMStoreException(Message msg) {
        super(msg);
    }

    /**
     * Constructs a <code>RMPersistenceException</code> with the detail message and cause
     * provided.
     */
    public RMStoreException(Message msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a <code>RMPersistenceException</code> with the provided cause.
     */
    public RMStoreException(Throwable cause) {
        super(cause);
    }
}
