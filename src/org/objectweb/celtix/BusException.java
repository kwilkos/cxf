package org.objectweb.celtix;

public class BusException extends Exception {

    private static final long serialVersionUID = 1L;

    public BusException(String msg) {
        super(msg);
    }
    public BusException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public BusException(Throwable cause) {
        super(cause);
    }
}
