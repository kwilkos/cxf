package org.objectweb.celtix;

import java.util.ResourceBundle;


public class BusException extends org.objectweb.celtix.common.i18n.Exception {

    private static final long serialVersionUID = 1L;
    private static ResourceBundle resourceBundle;
        

    public BusException(String msg) {
        super(msg);
    }
    public BusException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public BusException(Throwable cause) {
        super(cause);
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.celtix.common.i18n.Exception#getBundle()
     */
    @Override
    protected ResourceBundle getResourceBundle() {
        if (null == resourceBundle) {
            resourceBundle = ResourceBundle.getBundle(BusException.class.getName() + ".properties");
        }
        return resourceBundle;
    }
    
    
}
