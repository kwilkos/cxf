package org.apache.cxf.common.i18n;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class Message {
    String code;
    Object[] parameters;
    ResourceBundle bundle;
    
    /**
     * Constructor.
     *
     * @param key the message catalog (resource bundle) key
     * @param logger a logger with an associated resource bundle
     * @param params the message substitution parameters
     */
    public Message(String key, Logger logger, Object...params) {
        this(key, logger.getResourceBundle(), params);
    }

    /**
     * Constructor.
     *
     * @param key the message catalog (resource bundle) key
     * @param catalog the resource bundle
     * @param params the message substitution parameters
     */
    public Message(String key, ResourceBundle catalog, Object...params) {
        code = key;
        bundle = catalog;
        parameters = params;
    }
    
    public String toString() {
        String fmt = null;
        try {
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
}
