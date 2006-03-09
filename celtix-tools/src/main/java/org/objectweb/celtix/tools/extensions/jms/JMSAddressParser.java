package org.objectweb.celtix.tools.extensions.jms;

import org.w3c.dom.*;

import org.objectweb.celtix.helpers.XMLUtils;
import org.objectweb.celtix.tools.common.ToolConstants;

public class JMSAddressParser {

    private XMLUtils xmlUtils = new XMLUtils();
    
    public void parseElement(JMSAddress jmsAddress, Element element) {
        try {
            Attr jndiURL = xmlUtils.getAttribute(element, ToolConstants.JMS_ADDR_JNDI_URL);
            
            if (jndiURL != null) {
                jmsAddress.setJndiProviderURL(jndiURL.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
