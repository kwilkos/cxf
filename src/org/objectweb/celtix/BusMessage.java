package org.objectweb.celtix;

import java.util.ResourceBundle;

import org.objectweb.celtix.common.i18n.Message;

public class BusMessage extends Message {
    
    private static ResourceBundle resourceBundle;
    
    public BusMessage(String code, Object...params) {
        super(code, params);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.common.i18n.Exception#getBundle()
     */
    @Override
    protected ResourceBundle getResourceBundle() {
        if (null == resourceBundle) {
            resourceBundle = ResourceBundle.getBundle(BusMessage.class.getName() + ".properties");
        }
        return resourceBundle;
    }
}
