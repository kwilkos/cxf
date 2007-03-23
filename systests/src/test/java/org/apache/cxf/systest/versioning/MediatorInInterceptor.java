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
import java.util.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(MediatorInInterceptor.class.getName());

    public MediatorInInterceptor() {
        super();
        setPhase(Phase.POST_STREAM);
        addBefore(StaxInInterceptor.class.getName());
    }

    public void handleMessage(SoapMessage message) {
        if (isGET(message)) {
            LOG.info("StaxInInterceptor skipped in HTTP GET method");
            return;
        }

        String schemaNamespace = "";
        InterceptorChain chain = message.getInterceptorChain();

        try {
            //create a buffered stream so that we can roll back to original stream after finishing scaning
            InputStream is = message.getContent(InputStream.class);
            BufferedInputStream pis = new BufferedInputStream(is);
            pis.mark(pis.available());
            message.setContent(InputStream.class, pis);

            //TODO: need to process attachements

            
            //Scan the schema namespace, which is used to indicate the service version
            String encoding = (String)message.get(Message.ENCODING);
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(pis, encoding);
            DepthXMLStreamReader xmlReader = new DepthXMLStreamReader(reader);

            if (xmlReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                String ns = xmlReader.getNamespaceURI();
                SoapVersion soapVersion = SoapVersionFactory.getInstance().getSoapVersion(ns);
                StaxUtils.toNextTag(xmlReader, soapVersion.getBody());

                // advance just past body.
                xmlReader.nextTag();
            }

            schemaNamespace = xmlReader.getName().getNamespaceURI();

            //Roll back to the original inputStream
            pis.reset();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        Bus bus = CXFBusFactory.getDefaultBus();
        ServerRegistry serverRegistry = bus.getExtension(ServerRegistry.class);
        List<Server> servers = serverRegistry.getServers();

        Server targetServer = null;
        for (Server server : servers) {
            targetServer = server;
            String address = server.getEndpoint().getEndpointInfo().getAddress();
            if (schemaNamespace.indexOf("2007/03/21") != -1) {
                if (address.indexOf("SoapContext2") != -1) {
                    break;
                }
            } else if (address.indexOf("SoapContext1") != -1) {
                break;
            }
        }

        //Redirect the request
        MessageObserver ob = targetServer.getMessageObserver();
        ob.onMessage(message);

        chain.abort();
    }

}
