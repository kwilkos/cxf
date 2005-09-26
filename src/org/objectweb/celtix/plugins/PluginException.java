package org.objectweb.celtix.plugins;

import org.objectweb.celtix.common.i18n.Exception;
import org.objectweb.celtix.common.i18n.Message;

public class PluginException extends Exception {
   
    private static final long serialVersionUID = 1L;

    public PluginException(Message msg) {
        super(msg);
    }
    
    public PluginException(Message msg, Throwable cause) {
        super(msg, cause);
    }
}
