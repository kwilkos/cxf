package org.objectweb.celtix.common.i18n;



public abstract class Exception extends java.lang.Exception {
    
    private Message message;
    
    public Exception(Message msg) {
        message = msg;
    }
    
    public Exception(String msg, Object... params) {   
        message = createMessage(msg, params);
    }
    
    public Exception(String msg) {   
        message = createMessage(msg);
    }
    
    public Exception(String msg, Throwable cause) {
        super(cause);
        message = createMessage(msg);
    }
     
    public Exception(String msg, Throwable cause, Object... params) {
        super(cause);
        message = createMessage(msg, params);
    }

    public Exception(Throwable cause) {
        super(cause);
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
    
    protected abstract Message createMessage(String code, Object...params);
}
