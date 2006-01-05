package org.objectweb.celtix.common.i18n;



public class Exception extends java.lang.Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final Message message;
    
    public Exception(Message msg) {
        message = msg;
    }
    
    public Exception(Message msg, Throwable t) {
        super(t);
        message = msg;
    }
    
    public Exception(Throwable cause) {
        super(cause);
        message = null;
    } 
    
    // the above constructors should be preferred to the following ones
    
    @Deprecated
    public Exception() {
        super();
        message = null;
    }
    
    @Deprecated
    public Exception(String msg) {
        super(msg);
        message = null;
    }
    
    @Deprecated
    public Exception(String msg, Throwable t) {
        super(msg, t);
        message = null;
    }

    public String getCode() {
        if (null != message) {
            return message.getCode();
        }
        return null;
    }
    
    public String getMessage() {
        if (null != message) {
            return message.toString();
        }
        return null;
    }
}
