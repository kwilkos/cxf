package org.objectweb.celtix.bus.configuration;

import java.util.ResourceBundle;

import org.objectweb.celtix.common.i18n.Message;

public class ConfigurationMessage extends Message {

    private static ResourceBundle resourceBundle;

    public ConfigurationMessage(String code, Object... params) {
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
            resourceBundle = ResourceBundle.getBundle(ConfigurationMessage.class.getName());
        }
        return resourceBundle;
    }

}
