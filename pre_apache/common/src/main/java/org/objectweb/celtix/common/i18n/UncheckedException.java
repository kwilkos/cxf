package org.objectweb.celtix.common.i18n;



public class UncheckedException extends java.lang.RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final Message message;
    
    public UncheckedException(Message msg) {
        message = msg;
    }
    
    public UncheckedException(Message msg, Throwable t) {
        super(t);
        message = msg;
    }
    
    public UncheckedException(Throwable cause) {
        super(cause);
        message = null;
    } 
    
    // the above constructors should be preferred to the following ones
    
    @Deprecated
    public UncheckedException() {
        super();
        message = null;
    }
    
    @Deprecated
    public UncheckedException(String msg) {
        super(msg);
        message = null;
    }
    
    @Deprecated
    public UncheckedException(String msg, Throwable t) {
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
