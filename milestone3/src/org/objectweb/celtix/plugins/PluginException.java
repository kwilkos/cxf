package org.objectweb.celtix.plugins;

import org.objectweb.celtix.common.i18n.Exception;
import org.objectweb.celtix.common.i18n.Message;

/**
 * Used to indicate an exception when managing a plugin object.
 */
public class PluginException extends Exception {
   
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>PluginException</code> with the given detail message.
     */
    public PluginException(Message msg) {
        super(msg);
    }
    
    /**
     * Constructs a <code>PluginException</code> with the given detail message
     * and cause.
     */
    public PluginException(Message msg, Throwable cause) {
        super(msg, cause);
    }
}
