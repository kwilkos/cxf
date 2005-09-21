package org.objectweb.celtix.common.i18n;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Message {
    String code;
    Object[] parameters;
    
    public Message(String key, Object...params) {
        code = key;
        parameters = params;
    }
    
    public String toString() {
        String fmt = null;
        try {
            ResourceBundle bundle = getResourceBundle();
            if (null == bundle) {
                return code;
            }
            fmt = bundle.getString(code);  
        } catch (MissingResourceException ex) {
            return code;
        }
        return MessageFormat.format(fmt, parameters);
    }
    
    public String getCode() {
        return code;      
    }
    
    public Object[] getParameters() {
        return parameters;
        
    }
    
    protected ResourceBundle getResourceBundle() {
        return null;
    }
}
