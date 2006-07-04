package org.objectweb.celtix.jca.core.resourceadapter;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.objectweb.celtix.common.logging.LogUtils;

public class ResourceAdapterInternalException extends javax.resource.spi.ResourceAdapterInternalException {

    private static final long serialVersionUID = 6769505138041263456L;    
    private static final String REASON_PREFIX = ", reason: ";
    private static final Logger LOGGER = LogUtils.getL7dLogger(ResourceAdapterInternalException.class);

    public ResourceAdapterInternalException(String msg) {
        this(msg, null);
    }

    public ResourceAdapterInternalException(String msg, Throwable cause) {
        super(msg + ResourceAdapterInternalException.optionalReasonFromCause(cause));
        setCause(cause);
        if (cause != null) {
            LOGGER.warning(cause.toString());
        }
    }

    private static String optionalReasonFromCause(Throwable cause) {
        String reason = "";
        if (cause != null) {
            if (cause instanceof InvocationTargetException) {
                reason = REASON_PREFIX + ((InvocationTargetException)cause).getTargetException();
            } else {
                reason = REASON_PREFIX + cause;
            }
        }
        return reason;
    }

    private void setCause(Throwable cause) {
        if (getCause() != null) {
            return;
        }

        if (cause instanceof InvocationTargetException
            && (((InvocationTargetException)cause).getTargetException() != null)) {
            initCause(((InvocationTargetException)cause).getTargetException());
        } else {
            initCause(cause);
        }
    }

    public Exception getLinkedException() {
        Exception linkedEx = null;
        if (getCause() instanceof Exception) {
            linkedEx = (Exception)getCause();
        }
        return linkedEx;
    }
}
