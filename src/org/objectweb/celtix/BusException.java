package org.objectweb.celtix;

import org.objectweb.celtix.common.i18n.Message;


public class BusException extends org.objectweb.celtix.common.i18n.Exception {

    private static final long serialVersionUID = 1L;
        
    public BusException(Message msg) {
        super(msg);
    }
    public BusException(Message msg, Throwable cause) {
        super(msg, cause);
    }
    public BusException(Throwable cause) {
        super(cause);
    }
}
