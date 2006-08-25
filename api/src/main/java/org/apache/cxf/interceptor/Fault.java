package org.apache.cxf.interceptor;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.i18n.UncheckedException;

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

    public Fault(Throwable t) {
        super(t);
    }
}
