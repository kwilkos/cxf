package org.objectweb.celtix.interceptors;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.i18n.UncheckedException;

/**
 * A Fault that occurs during invocation processing.
 */
public class Fault extends UncheckedException {
    
    public Fault(Message message, Throwable throwable) {
        super(message, throwable);
    }
    
    public Fault(Message message) {
        super(message);
    }
}
