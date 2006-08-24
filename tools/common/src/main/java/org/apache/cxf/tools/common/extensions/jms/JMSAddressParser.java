package org.apache.cxf.tools.common.extensions.jms;

import org.w3c.dom.*;

import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.tools.common.ToolConstants;

public class JMSAddressParser {

    private XMLUtils xmlUtils = new XMLUtils();

    public void parseElement(JMSAddress jmsAddress, Element element) {
        try {
            Attr jndiURL = xmlUtils.getAttribute(element, ToolConstants.JMS_ADDR_JNDI_URL);
            if (jndiURL != null) {
                jmsAddress.setJndiProviderURL(jndiURL.getValue());
            }

            Attr destStyle = xmlUtils.getAttribute(element, ToolConstants.JMS_ADDR_DEST_STYLE);
            if (destStyle != null) {
                jmsAddress.setDestinationStyle(destStyle.getValue());
            }

            Attr initCtx = xmlUtils.getAttribute(element, ToolConstants.JMS_ADDR_INIT_CTX);
            if (initCtx != null) {
                jmsAddress.setInitialContextFactory(initCtx.getValue());
            }

            Attr jndiDest = xmlUtils.getAttribute(element, ToolConstants.JMS_ADDR_JNDI_DEST);
            if (jndiDest != null) {
                jmsAddress.setJndiDestinationName(jndiDest.getValue());
            }

            Attr jndiFac = xmlUtils.getAttribute(element, ToolConstants.JMS_ADDR_JNDI_FAC);
            if (jndiFac != null) {
                jmsAddress.setJndiConnectionFactoryName(jndiFac.getValue());
            }

            Attr msgType = xmlUtils.getAttribute(element, ToolConstants.JMS_ADDR_MSG_TYPE);
            if (msgType != null) {
                jmsAddress.setMessageType(msgType.getValue());
            }

            Attr msgID = xmlUtils.getAttribute(element, ToolConstants.JMS_ADDR_MSGID_TO_CORRID);
            if (msgID != null) {
                jmsAddress.setUseMessageIDAsCorrelationID(Boolean.parseBoolean(msgID.getValue()));
            }

            Attr subsName = xmlUtils.getAttribute(element, ToolConstants.JMS_ADDR_SUBSCRIBER_NAME);
            if (subsName != null) {
                jmsAddress.setDurableSubscriberName(subsName.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
