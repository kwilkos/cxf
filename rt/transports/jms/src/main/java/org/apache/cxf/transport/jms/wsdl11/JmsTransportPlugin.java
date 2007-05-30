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

package org.apache.cxf.transport.jms.wsdl11;

import java.util.Map;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;

import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.transport.jms.AddressType;
import org.apache.cxf.wsdl.AbstractWSDLPlugin;

public class JmsTransportPlugin extends AbstractWSDLPlugin {
    public ExtensibilityElement createExtension(Map<String, Object> args) throws WSDLException {
        AddressType jmsAddress = null;

        jmsAddress = (AddressType)registry.createExtension(Port.class, ToolConstants.JMS_ADDRESS);
        if (optionSet(args, ToolConstants.JMS_ADDR_DEST_STYLE)) {
            //jmsAddress.setDestinationStyle((String)env.get(ToolConstants.JMS_ADDR_DEST_STYLE));
        }
        if (optionSet(args, ToolConstants.JMS_ADDR_INIT_CTX)) {
            //jmsAddress.setInitialContextFactory((String)env.get(ToolConstants.JMS_ADDR_INIT_CTX));
        }
        if (optionSet(args, ToolConstants.JMS_ADDR_JNDI_DEST)) {
            jmsAddress.setJndiDestinationName(getOption(args, ToolConstants.JMS_ADDR_JNDI_DEST));
        }
        if (optionSet(args, ToolConstants.JMS_ADDR_JNDI_FAC)) {
            jmsAddress.setJndiConnectionFactoryName(getOption(args, ToolConstants.JMS_ADDR_JNDI_FAC));
        }
        if (optionSet(args, ToolConstants.JMS_ADDR_JNDI_URL)) {
            //jmsAddress.setJndiProviderURL((String)env.get(ToolConstants.JMS_ADDR_JNDI_URL));
        }
        if (optionSet(args, ToolConstants.JMS_ADDR_MSGID_TO_CORRID)) {
            //jmsAddress.setUseMessageIDAsCorrelationID(Boolean.getBoolean((String)env
            //.get(ToolConstants.JMS_ADDR_MSGID_TO_CORRID)));
        }
        if (optionSet(args, ToolConstants.JMS_ADDR_SUBSCRIBER_NAME)) {
            //jmsAddress.setDurableSubscriberName((String)env
            //  .get(ToolConstants.JMS_ADDR_SUBSCRIBER_NAME));
        }
        return jmsAddress;
    }
}
