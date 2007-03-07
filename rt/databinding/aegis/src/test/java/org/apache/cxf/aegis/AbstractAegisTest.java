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
package org.apache.cxf.aegis;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.BusFactory;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.aegis.databinding.AegisServiceConfiguration;
import org.apache.cxf.aegis.util.XmlConstants;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerRegistry;
import org.apache.cxf.frontend.AbstractEndpointFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.cxf.test.AbstractCXFTest;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;
import org.apache.cxf.wsdl11.WSDLManagerImpl;

public abstract class AbstractAegisTest extends AbstractCXFTest {
    protected LocalTransportFactory localTransport;

    private Bus bus;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        bus = getBus();
        
        SoapBindingFactory bindingFactory = new SoapBindingFactory();

        bus.getExtension(BindingFactoryManager.class)
            .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);

        SoapTransportFactory soapDF = new SoapTransportFactory();
        soapDF.setBus(bus);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/", soapDF);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/", soapDF);
        dfm.registerDestinationFactory("http://cxf.apache.org/transports/local", soapDF);
        
        localTransport = new LocalTransportFactory();
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", localTransport);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/http", localTransport);
        dfm.registerDestinationFactory("http://cxf.apache.org/bindings/xformat", localTransport);
        dfm.registerDestinationFactory("http://cxf.apache.org/transports/local", localTransport);

        ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator(LocalTransportFactory.TRANSPORT_ID, localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/soap/", localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/soap/", localTransport);
        
        bus.setExtension(new WSDLManagerImpl(), WSDLManager.class);
        

        addNamespace("wsdl", XmlConstants.WSDL11_NS);
        addNamespace("wsdlsoap", XmlConstants.WSDL11_SOAP_NS);
        addNamespace("xsd", XmlConstants.XSD);


    }

    @Override
    protected Bus createBus() throws BusException {
        ExtensionManagerBus bus = new ExtensionManagerBus();
        BusFactory.setDefaultBus(bus);
        return bus;
    }
    
    protected void invoke(String service, String message) throws Exception {
        invoke(service, LocalTransportFactory.TRANSPORT_ID, message);
    }
    
    public Server createService(Class serviceClass, QName name) {
        return createService(serviceClass, serviceClass.getSimpleName(), name);
    }
    
    public Server createService(Class serviceClass, 
                                String address, QName name) {
        ServerFactoryBean sf = createServiceFactory(serviceClass, address, name);
        return sf.create();
    }

    protected ServerFactoryBean createServiceFactory(Class serviceClass, String address, QName name) {
        ServerFactoryBean sf = new ServerFactoryBean();
        sf.setServiceClass(serviceClass);
        sf.getServiceFactory().setServiceName(name);
        sf.setAddress(address);
        setupAegis(sf);
        return sf;
    }

    protected void setupAegis(AbstractEndpointFactory sf) {
        sf.getServiceFactory().getServiceConfigurations().add(0, new AegisServiceConfiguration());
        sf.getServiceFactory().setDataBinding(new AegisDatabinding());
    }
    

    protected Document getWSDLDocument(String string) throws WSDLException {
        ServerRegistry svrMan = getBus().getExtension(ServerRegistry.class);
        for (Server s : svrMan.getServers()) {
            Service svc = s.getEndpoint().getService();
            if (svc.getName().getLocalPart().equals(string)) {
                ServiceWSDLBuilder builder = new ServiceWSDLBuilder(svc.getServiceInfo());
                Definition definition = builder.build();
                
                WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
                return writer.getDocument(definition);
            }
        }
        
        return null;
    }

    
}
