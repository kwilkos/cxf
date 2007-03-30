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

package org.apache.cxf.systest.versioning;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.SoapVersionFactory;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerRegistry;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.transport.MessageObserver;


public class MediatorInInterceptor extends AbstractPhaseInterceptor<SoapMessage> {

    public MediatorInInterceptor() {
        super();
        setPhase(Phase.POST_STREAM);
        addBefore(StaxInInterceptor.class.getName());
    }

    public void handleMessage(SoapMessage message) {
        String schemaNamespace = "";
        InterceptorChain chain = message.getInterceptorChain();

        //scan the incoming message for its schema namespace
        try {
            //create a buffered stream so that we get back the original stream after scaning
            InputStream is = message.getContent(InputStream.class);
            BufferedInputStream pis = new BufferedInputStream(is);
            pis.mark(pis.available());
            message.setContent(InputStream.class, pis);

            //TODO: process attachements

            String encoding = (String)message.get(Message.ENCODING);
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(pis, encoding);
            DepthXMLStreamReader xmlReader = new DepthXMLStreamReader(reader);

            if (xmlReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                String ns = xmlReader.getNamespaceURI();
                SoapVersion soapVersion = SoapVersionFactory.getInstance().getSoapVersion(ns);
                //advance just past header
                StaxUtils.toNextTag(xmlReader, soapVersion.getBody());
                //past body.
                xmlReader.nextTag();
            }

            schemaNamespace = xmlReader.getName().getNamespaceURI();

            pis.reset();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        //Look up for all available endpoints registered on the bus
        Bus bus = CXFBusFactory.getDefaultBus();
        ServerRegistry serverRegistry = bus.getExtension(ServerRegistry.class);
        List<Server> servers = serverRegistry.getServers();

        //if the incoming message has a namespace contained "2007/03/21", we redirect the message
        //to the new version of service on endpoint "local://localhost:9027/SoapContext/version2/SoapPort"
        Server targetServer = null;
        for (Server server : servers) {
            targetServer = server;
            String address = server.getEndpoint().getEndpointInfo().getAddress();
            if (schemaNamespace.indexOf("2007/03/21") != -1) {
                if (address.indexOf("version2") != -1) {
                    break;
                }
            } else if (address.indexOf("version1") != -1) {
                break;
            }
        }

        //Redirect the request
        MessageObserver mo = targetServer.getMessageObserver();
        mo.onMessage(message);

        //Now the response has been put in the message, abort the chain 
        chain.abort();
    }

}
