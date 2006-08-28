/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
