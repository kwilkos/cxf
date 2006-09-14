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

package org.apache.cxf.binding.xml.interceptor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.binding.xml.XMLConstants;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class XMLFaultInInterceptor extends AbstractInDatabindingInterceptor {

    public void handleMessage(Message message) throws Fault {

        XMLStreamReader reader = message.getContent(XMLStreamReader.class);
        DepthXMLStreamReader dr = new DepthXMLStreamReader(reader);
        // List<Exception> exlist = new ArrayList<Exception>();

        StaxUtils.nextEvent(dr);
        if (StaxUtils.toNextElement(dr)) {
            QName startQName = new QName(dr.getNamespaceURI(), dr.getLocalName());
            if (startQName.equals(XMLConstants.XML_FAULT_ROOT)) {
                while (StaxUtils.toNextElement(dr)) {
                    // marshall exception detail by jaxb, and add into exList

                }
            }
        }
    }
}
