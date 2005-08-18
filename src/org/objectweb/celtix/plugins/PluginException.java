package org.objectweb.celtix.plugins;

import org.objectweb.celtix.common.i18n.Exception;
import org.objectweb.celtix.common.i18n.Message;

public class PluginException extends Exception {
   
    private static final long serialVersionUID = 1L;

    public PluginException(String msg, Object...objects) {
        super(msg, objects);
    }
    
    public PluginException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public PluginException(String msg, Throwable cause, Object...objects) {
        super(msg, cause);
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.common.i18n.Exception#createMessage(java.lang.String, java.lang.Object...)
     */
    @Override
    protected Message createMessage(String code, Object... params) {
        return new PluginMessage(code, params);
    }
}
