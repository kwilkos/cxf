package org.objectweb.celtix.common.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public abstract class Message {
    String code;
    Object[] parameters;
    
    public Message(String key, Object...params) {
        code = key;
        parameters = params;
    }
    
    public String toString() {
        String fmt = getResourceBundle().getString(code);
        return MessageFormat.format(fmt, parameters);
    }
    
    public String getCode() {
        return code;      
    }
    
    public Object[] getParameters() {
        return parameters;
        
    }
    
    protected abstract ResourceBundle getResourceBundle();
}
