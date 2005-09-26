package org.objectweb.celtix.configuration;

import org.objectweb.celtix.common.i18n.Message;

public class ConfigurationException extends org.objectweb.celtix.common.i18n.Exception {
    
    private static final long serialVersionUID = 1L;

    public ConfigurationException(Message message) {
        super(message);
    }  
    
    public ConfigurationException(Message msg, Throwable t) {
        super(msg, t);
    }
  
}
