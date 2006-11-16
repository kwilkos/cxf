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

package org.apache.cxf.jaxws.support;

import java.lang.reflect.Method;

import java.util.Iterator;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.apache.cxf.wsdl4jutils.WSDLLocatorImpl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class JaxWsServiceConfigurationTest extends TestCase {

    public void setUp() throws Exception {

    }

    public void testGetInPartName() throws Exception {
        QName opName = new QName("http://cxf.com/", "sayHello");
        Method sayHelloMethod = Hello.class.getMethod("sayHello", new Class[]{String.class, String.class});
        ServiceInfo si = getMockedServiceModel("/wsdl/default_partname_test.wsdl");
        JaxWsServiceConfiguration jwsc = new JaxWsServiceConfiguration();
        QName partName = jwsc.getInPartName(si.getInterface().getOperation(opName), sayHelloMethod, 0);
        assertEquals("get wrong in partName for first param", new QName("http://cxf.com/", "arg0"), partName);
        partName = jwsc.getInPartName(si.getInterface().getOperation(opName), sayHelloMethod, 1);
        assertEquals("get wrong in partName for first param", new QName("http://cxf.com/", "arg1"), partName);
    }

    public void testGetOutPartName() throws Exception {
        QName opName = new QName("http://cxf.com/", "sayHi");
        Method sayHiMethod = Hello.class.getMethod("sayHi", new Class[]{});
        ServiceInfo si = getMockedServiceModel("/wsdl/default_partname_test.wsdl");
        JaxWsServiceConfiguration jwsc = new JaxWsServiceConfiguration();
        QName partName = jwsc.getOutPartName(si.getInterface().getOperation(opName), sayHiMethod, -1);
        assertEquals("get wrong return partName", new QName("http://cxf.com/", "return"), partName);
    }

    private ServiceInfo getMockedServiceModel(String wsdlUrl) throws Exception {
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        Definition def = wsdlReader.readWSDL(new WSDLLocatorImpl(wsdlUrl));

        IMocksControl control = EasyMock.createNiceControl();
        Bus bus = control.createMock(Bus.class);
        BindingFactoryManager bindingFactoryManager = control.createMock(BindingFactoryManager.class);
        DestinationFactoryManager dfm = control.createMock(DestinationFactoryManager.class);
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
        EasyMock.expect(bus.getExtension(DestinationFactoryManager.class)).andStubReturn(dfm);
        control.replay();

        ServiceInfo serviceInfo = wsdlServiceBuilder.buildService(def, service);
        serviceInfo.setProperty(WSDLServiceBuilder.WSDL_DEFINITION, null);
        serviceInfo.setProperty(WSDLServiceBuilder.WSDL_SERVICE, null);
        return serviceInfo;
    }

    @WebService(name = "Hello", targetNamespace = "http://cxf.com/")
    public interface Hello {
        @SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE, 
                style = javax.jws.soap.SOAPBinding.Style.RPC, use = javax.jws.soap.SOAPBinding.Use.LITERAL)
        @WebMethod(operationName = "sayHi", exclude = false)
        String sayHi();

        @SOAPBinding(parameterStyle = javax.jws.soap.SOAPBinding.ParameterStyle.BARE, 
                style = javax.jws.soap.SOAPBinding.Style.RPC, use = javax.jws.soap.SOAPBinding.Use.LITERAL)
        @WebMethod(operationName = "sayHello", exclude = false)
        String sayHello(String arg0, String arg1);
    }

}
