package org.objectweb.celtix.configuration;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.i18n.UncheckedException;

public class ConfigurationException extends UncheckedException {

    private static final long serialVersionUID = 1L;

    public ConfigurationException(Message message) {
        super(message);
    }

    public ConfigurationException(Message msg, Throwable t) {
        super(msg, t);
    }

}
