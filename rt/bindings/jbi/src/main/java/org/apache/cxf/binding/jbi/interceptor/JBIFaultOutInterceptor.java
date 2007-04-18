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
package org.apache.cxf.binding.jbi.interceptor;

import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.cxf.binding.jbi.JBIMessage;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;

public class JBIFaultOutInterceptor extends AbstractPhaseInterceptor<JBIMessage> {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JBIFaultOutInterceptor.class);

    public JBIFaultOutInterceptor() {
        setPhase(Phase.MARSHAL);
    }

    public void handleMessage(JBIMessage message) throws Fault {
        XMLStreamWriter writer = message.getContent(XMLStreamWriter.class);
        Fault fault = (Fault) message.getContent(Exception.class);
        try {
            if (!fault.hasDetails()) {
                writer.writeEmptyElement("fault");
            } else {
                Element detail = fault.getDetail();
                NodeList details = detail.getChildNodes();
                for (int i = 0; i < details.getLength(); i++) {
                    if (details.item(i) instanceof Element) {
                        StaxUtils.writeNode(details.item(i), writer, true);
                        break;
                    }
                }
            }
        } catch (XMLStreamException xe) {
            throw new Fault(new Message("XML_WRITE_EXC", BUNDLE), xe);
        }
    }

}
