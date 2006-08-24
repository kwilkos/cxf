package org.apache.cxf;

import org.apache.cxf.common.i18n.Message;

/**
 * The BusException class is used to indicate a bus exception has occured.
 */
public class BusException extends org.apache.cxf.common.i18n.Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>BusException</code> with the provided detail message.
     */
    public BusException(Message msg) {
        super(msg);
    }

    /**
     * Constructs a <code>BusException</code> with the detail message and cause
     * provided.
     */
    public BusException(Message msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a <code>BusException</code> with the provided cause.
     */
    public BusException(Throwable cause) {
        super(cause);
    }
}
