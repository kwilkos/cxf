package org.objectweb.celtix;

public class BusException extends Exception {

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
