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

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.xml.XMLBindingFactory;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.jaxb.JAXBDataReaderFactory;
import org.apache.cxf.jaxb.JAXBDataWriterFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class TestBase extends TestCase {

    protected PhaseInterceptorChain chain;

    protected Message xmlMessage;
    
    protected Bus bus;

    protected IMocksControl control;
    
    protected ServiceInfo serviceInfo;
    
    public void setUp() throws Exception {
        List<Phase> phases = new ArrayList<Phase>();
        Phase phase1 = new Phase("phase1", 1);
        Phase phase2 = new Phase("phase2", 2);
        Phase phase3 = new Phase("phase3", 3);
        phases.add(phase1);
        phases.add(phase2);
        phases.add(phase3);
        chain = new PhaseInterceptorChain(phases);

        Exchange exchange = new ExchangeImpl();
        MessageImpl messageImpl = new MessageImpl();
        messageImpl.setInterceptorChain(chain);
        messageImpl.setExchange(exchange);
        xmlMessage = messageImpl;
    }

    public void tearDown() throws Exception {
    }

    public InputStream getTestStream(Class<?> clz, String file) {
        return clz.getResourceAsStream(file);
    }

    public XMLStreamReader getXMLStreamReader(InputStream is) {
        return StaxUtils.createXMLStreamReader(is);
    }

    public XMLStreamWriter getXMLStreamWriter(OutputStream os) {
        return StaxUtils.createXMLStreamWriter(os);
    }

    public Method getTestMethod(Class<?> sei, String methodName) {
        Method[] iMethods = sei.getMethods();
        for (Method m : iMethods) {
            if (methodName.equals(m.getName())) {
                return m;
            }
        }
        return null;
    }

    public ServiceInfo getTestService(Class<?> clz) {
        // FIXME?!?!?!?? There should NOT be JAX-WS stuff here
        return null;
    }

    protected BindingInfo getTestService(String wsdlUrl, String port) throws Exception {
        ServiceInfo service = getMockedServiceModel(getClass().getResource(wsdlUrl));
        assertNotNull(service);
        BindingInfo binding = service.getEndpoint(new QName(service.getName().getNamespaceURI(), port))
                        .getBinding();
        assertNotNull(binding);
        return binding;
    }

    protected ServiceInfo getMockedServiceModel(URL wsdlUrl) throws Exception {

        WSDLManagerImpl wmi = new WSDLManagerImpl();
        Definition def = wmi.getDefinition(wsdlUrl);        
        
        control = EasyMock.createNiceControl();
        bus = control.createMock(Bus.class);
        
        BindingFactoryManager bindingFactoryManager = control.createMock(BindingFactoryManager.class);
        WSDLServiceBuilder wsdlServiceBuilder = new WSDLServiceBuilder(bus);        
        
        Service service = null;
        for (Iterator<?> it = def.getServices().values().iterator(); it.hasNext();) {
            Object obj = it.next();
            if (obj instanceof Service) {
                service = (Service) obj;
                break;
            }
        }

        EasyMock.expect(bus.getExtension(BindingFactoryManager.class)).andReturn(bindingFactoryManager);
        control.replay();        
        serviceInfo = wsdlServiceBuilder.buildService(def, service);
        serviceInfo.setProperty(WSDLServiceBuilder.WSDL_DEFINITION, null);
        serviceInfo.setProperty(WSDLServiceBuilder.WSDL_SERVICE, null);
        return serviceInfo;
    }

    protected JAXBDataReaderFactory getTestReaderFactory(Class<?> clz) throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(clz);
        JAXBDataReaderFactory readerFacotry = new JAXBDataReaderFactory();
        readerFacotry.setJAXBContext(ctx);
        return readerFacotry;
    }

    protected JAXBDataWriterFactory getTestWriterFactory(Class<?> clz) throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(clz);
        JAXBDataWriterFactory writerFacotry = new JAXBDataWriterFactory();
        writerFacotry.setJAXBContext(ctx);
        return writerFacotry;
    }
    
    protected void common(String wsdl, QName portName, Class seiClazz) throws Exception {
        
        URL wsdlUrl = this.getClass().getResource(wsdl);
        ServiceInfo si = getMockedServiceModel(wsdlUrl);

        EndpointInfo epi = si.getEndpoint(portName);
        Binding xmlBinding = new XMLBindingFactory().createBinding(epi.getBinding());

        control.reset();
        org.apache.cxf.service.Service service = control.createMock(ServiceImpl.class);
        EasyMock.expect(service.getDataReaderFactory()).andReturn(getTestReaderFactory(seiClazz));
        EasyMock.expect(service.getDataWriterFactory()).andReturn(getTestWriterFactory(seiClazz));

        Endpoint endpoint = control.createMock(EndpointImpl.class);
        EasyMock.expect(endpoint.getEndpointInfo()).andReturn(epi);
        EasyMock.expect(endpoint.getBinding()).andReturn(xmlBinding);

        control.replay();

        xmlMessage.getExchange().put(Endpoint.class, endpoint);
        xmlMessage.getExchange().put(org.apache.cxf.service.Service.class, service);
        

    }
}
