package org.objectweb.celtix.extension;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.i18n.UncheckedException;

public class ExtensionException extends UncheckedException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a <code>ExtensionException</code> with the provided detail message.
     */
    ExtensionException(Message msg) {
        super(msg);
    }

    /**
     * Constructs a <code>ExtensionException</code> with the detail message and cause
     * provided.
     */
    ExtensionException(Message msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an <code>ExtensionException</code> with the provided cause.
     */
    ExtensionException(Throwable cause) {
        super(cause);
    }
}
