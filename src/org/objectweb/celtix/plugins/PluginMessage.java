package org.objectweb.celtix.plugins;

import java.util.ResourceBundle;

import org.objectweb.celtix.common.i18n.Message;

public class PluginMessage extends Message {

    private static ResourceBundle resourceBundle;

    public PluginMessage(String code, Object... params) {
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
            resourceBundle = ResourceBundle.getBundle(PluginMessage.class.getName());
        }
        return resourceBundle;
    }

}
