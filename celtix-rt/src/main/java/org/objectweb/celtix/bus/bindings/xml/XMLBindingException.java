package org.objectweb.celtix.bus.bindings.xml;

public class XMLBindingException extends RuntimeException {
    
    private static final long serialVersionUID = -4418907917249006910L;

    public XMLBindingException() {
        super();
    }

    public XMLBindingException(String msg) {
        super(msg);
    }

    public XMLBindingException(String msg, Throwable t) {
        super(msg, t);
    }

    public XMLBindingException(Throwable t) {
        super(t);
    }
}
