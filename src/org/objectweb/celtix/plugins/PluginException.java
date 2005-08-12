package org.objectweb.celtix.plugins;

import java.util.ResourceBundle;

import org.objectweb.celtix.common.i18n.Exception;

public class PluginException extends Exception {
   
    private static final long serialVersionUID = 1L;
    private static ResourceBundle resourceBundle;

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
     * @see org.objectweb.celtix.common.i18n.Exception#getResourceBundle()
     */
    @Override
    protected ResourceBundle getResourceBundle() {
        if (null == resourceBundle) {
            resourceBundle = ResourceBundle.getBundle(PluginException.class.getName() + ".properties");
        }
        return resourceBundle;
    }
    
    
    
    
}
