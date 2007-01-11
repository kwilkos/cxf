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

package org.apache.cxf.systest.handlers;

import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.systest.common.ClientServerSetupBase;
import org.apache.cxf.systest.common.ClientServerTestBase;
import org.apache.handlers.AddNumbers;
import org.apache.handlers.AddNumbersService;
import org.apache.handlers.types.AddNumbersResponse;
import org.apache.handlers.types.ObjectFactory;


public class HandlerInvocationUsingAddNumbersTest extends ClientServerTestBase {

    static QName serviceName = new QName("http://apache.org/handlers", "AddNumbersService");
    static QName portName = new QName("http://apache.org/handlers", "AddNumbersPort");
   
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(HandlerInvocationUsingAddNumbersTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(HandlerServer.class));
            }
        };
    }

    public void testAddHandlerProgrammaticallyClientSide() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/addNumbers.wsdl");

        AddNumbersService service = new AddNumbersService(wsdl, serviceName);
        AddNumbers port = (AddNumbers)service.getPort(portName, AddNumbers.class);
        
        SmallNumberHandler sh = new SmallNumberHandler();
        addHandlersProgrammatically((BindingProvider)port, sh);

        int result = port.addNumbers(10, 20);
        assertEquals(200, result);
        int result1 = port.addNumbers(5, 6);
        assertEquals(11, result1);
    }
    
    public void testAddHandlerByAnnotationClientSide() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/addNumbers.wsdl");

        AddNumbersServiceWithAnnotation service = new AddNumbersServiceWithAnnotation(wsdl, serviceName);
        AddNumbers port = (AddNumbers)service.getPort(portName, AddNumbers.class);

        int result = port.addNumbers(10, 20);
        assertEquals(200, result);
        int result1 = port.addNumbers(5, 6);
        assertEquals(11, result1);
    }
    
    public void testInvokeFromDispatchWithJAXBPayload() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/addNumbers.wsdl");
        assertNotNull(wsdl);

        AddNumbersService service = new AddNumbersService(wsdl, serviceName);
        assertNotNull(service);

        JAXBContext jc = JAXBContext.newInstance("org.apache.handlers.types");
        Dispatch<Object> disp = service.createDispatch(portName, jc, Service.Mode.PAYLOAD);
 
        SmallNumberHandler sh = new SmallNumberHandler();
        addHandlersProgrammatically(disp, sh);
        
        org.apache.handlers.types.AddNumbers req = new org.apache.handlers.types.AddNumbers();        
        req.setArg0(10);
        req.setArg1(20);        
        ObjectFactory factory = new ObjectFactory();        
        JAXBElement e = factory.createAddNumbers(req);        

        JAXBElement response = (JAXBElement)disp.invoke(e);
        assertNotNull(response);
        AddNumbersResponse value = (AddNumbersResponse)response.getValue();
        assertEquals(200, value.getReturn());
    }
    
    private void addHandlersProgrammatically(BindingProvider bp, Handler...handlers) {
        List<Handler> handlerChain = bp.getBinding().getHandlerChain();
        assertNotNull(handlerChain);
        for (Handler h : handlers) {
            handlerChain.add(h);
        }    
    }

}
