package org.objectweb.celtix.common.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public abstract class Exception extends java.lang.Exception {
    
    private Object[] args;
    
    public Exception(String msg) {   
        super(msg);
        args = null;
    }
    
    public Exception(String msg, Object... objects) {
        super(msg);
        args = objects;  
    }

    public Exception(String msg, Throwable cause) {
        super(msg, cause);
        args = null;
    }
     
    public Exception(String msg, Throwable cause, Object... params) {
        super(MessageFormat.format(msg, params), cause);
        args = params;
    }

    public Exception(Throwable cause) {
        super(cause);
        args = null;
    }
    
    public String getCode() {
        return super.getMessage();
    }
    
    public String getMessage() {
        String msg = super.getMessage();
        if (null != msg) {
            msg = MessageFormat.format(msg, args);
        }
        return msg;
    }
    
    protected abstract ResourceBundle getResourceBundle();

}
