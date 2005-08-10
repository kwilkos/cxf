package org.objectweb.celtix.bus;

/**
 * Signals that a bus exception of some sort has occured.
 */

public class BusException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a <code>BusException</code> with null as the detail message.
     */
    public BusException() {
        super();
    }

    /**
     * Constructs a <code>BusException</code> with the specified message.
     * @param message The detail message.
     */
    public BusException(String message) {
        super(message);
    }

    /**
     * Constructs a <code>BusException</code> with the specified cause.
     * @param cause The cause of this exception.
     */
    public BusException(Throwable cause) {
        super(cause);
    }


    /**
     * Constructs a <code>BusException</code> with the specified
     * message and cause cause.
     * @param message The detail message.
     * @param cause the cause of this exception.
     */
    public BusException(String message, Throwable cause) {
        super(cause);
    }
}
