package org.objectweb.celtix.configuration;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.i18n.UncheckedException;

public class ConfigurationException extends UncheckedException {

    private static final long serialVersionUID = 1L;


    /**
     * Constructs a <code>ConfigurationException</code> with the provided detail message.
     */
    public ConfigurationException(Message msg) {
        super(msg);
    }

    /**
     * Constructs a <code>ConfigurationException</code> with the detail message and cause
     * provided.
     */
    public ConfigurationException(Message msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a <code>ConfigurationException</code> with the provided cause.
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
