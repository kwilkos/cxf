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

package org.apache.cxf.jaxws.spi;


import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.ServiceDelegate;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.w3c.dom.Element;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.EndpointUtils;
import org.apache.cxf.jaxws.ServiceImpl;
import org.apache.cxf.staxutils.StaxUtils;

public class ProviderImpl extends javax.xml.ws.spi.Provider {
    public static final String JAXWS_PROVIDER = ProviderImpl.class.getName();
    protected static final String W3C_NS = "http://www.w3.org/2005/08/addressing";
    private static final Logger LOG = LogUtils.getL7dLogger(ProviderImpl.class);


    private static JAXBContext jaxbContext;

    @Override
    public ServiceDelegate createServiceDelegate(URL url, QName qname, Class cls) {
        Bus bus = BusFactory.getThreadDefaultBus();
        return new ServiceImpl(bus, url, qname, cls);
    }

    @Override
    public Endpoint createEndpoint(String bindingId, Object implementor) {

        Endpoint ep = null;
        if (EndpointUtils.isValidImplementor(implementor)) {
            Bus bus = BusFactory.getThreadDefaultBus();
            ep = new EndpointImpl(bus, implementor, bindingId);
            return ep;
        } else {
            throw new WebServiceException(new Message("INVALID_IMPLEMENTOR_EXC", LOG).toString());
        }
    }

    @Override
    public Endpoint createAndPublishEndpoint(String url, Object implementor) {
        Endpoint ep = createEndpoint(null, implementor);
        ep.publish(url);
        return ep;
    }

    public W3CEndpointReference createW3CEndpointReference(String address, QName serviceName, QName portName,
                                                           List<Element> metadata,
                                                           String wsdlDocumentLocation,
                                                           List<Element> referenceParameters) {
        CachedOutputStream cos = new CachedOutputStream();
        XMLStreamWriter writer = StaxUtils.createXMLStreamWriter(cos);

        try {
            //TODO: when serviceName/portName is null            
            writer.setPrefix("wsa", W3C_NS);

            String portNamePrefix = null;
            String serviceNamePrefix = (serviceName.getPrefix() == null 
                || serviceName.getPrefix().length() == 0)
                ? "ns" : serviceName.getPrefix();
            
            writer.writeStartElement("wsa", "EndpointReference", W3C_NS);
            writer.writeNamespace("wsa", W3C_NS);
            writer.writeNamespace(serviceNamePrefix, serviceName.getNamespaceURI());
            
            if (!portName.getNamespaceURI().equals(serviceName.getNamespaceURI())) {
                portNamePrefix = (portName.getPrefix() == null 
                    || portName.getPrefix().length() == 0)
                    ? "ns1" : portName.getPrefix();

                writer.writeNamespace(portNamePrefix, portName.getNamespaceURI());                
            } else {
                portNamePrefix = serviceNamePrefix;
            }

            writer.writeStartElement("wsa", "Address", W3C_NS);
            if (address != null) {
                writer.writeCharacters(address);
            }
            writer.writeEndElement();
            
            writer.writeStartElement("wsa", "portName", W3C_NS);
            writer.writeCharacters(portNamePrefix + ":" + portName.getLocalPart());
            writer.writeEndElement();
            
            writer.writeStartElement("wsa", "ServiceName", W3C_NS);
            writer.writeCharacters(serviceNamePrefix + ":" + serviceName.getLocalPart());
            writer.writeEndElement();

            if (referenceParameters != null) {
                for (Element referenceParameter : referenceParameters) {
                    StaxUtils.writeElement(referenceParameter, writer, true);
                }
            } 
            
            if (metadata != null) {
                for (Element meta : metadata) {
                    StaxUtils.writeElement(meta, writer, true);
                }
            }   
            
            //TODO: Write wsdlDocumentLocation
            
            writer.writeEndElement();
            writer.flush();

        } catch (XMLStreamException e) {
            throw new WebServiceException(
                new Message("ERROR_UNMARSHAL_ENDPOINTREFERENCE", LOG).toString(), e);
        }

        try {
            Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
            return (W3CEndpointReference)unmarshaller.unmarshal(cos.getInputStream());

        } catch (JAXBException e) {
            throw new WebServiceException(
                new Message("ERROR_UNMARSHAL_ENDPOINTREFERENCE", LOG).toString(), e);
        } catch (IOException e) {
            throw new WebServiceException(
                new Message("ERROR_UNMARSHAL_ENDPOINTREFERENCE", LOG).toString(), e);
        }

    }

    public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface,
                         WebServiceFeature... features) {
        ServiceDelegate sd = createServiceDelegate(null, null, serviceEndpointInterface);
        return sd.getPort(endpointReference, serviceEndpointInterface, features);
    }

    public EndpointReference readEndpointReference(Source eprInfoset) {
        try {
            Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
            return (EndpointReference)unmarshaller.unmarshal(eprInfoset);
        } catch (JAXBException e) {
            throw new WebServiceException(
                new Message("ERROR_UNMARSHAL_ENDPOINTREFERENCE", LOG).toString(), e);
        }
    }

    private JAXBContext getJAXBContext() {
        if (jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(W3CEndpointReference.class);
            } catch (JAXBException e) {
                throw new WebServiceException(new Message("JAXBCONTEXT_CREATION_FAILED", LOG).toString(), e);
            }
        }
        return jaxbContext;
    }

}
