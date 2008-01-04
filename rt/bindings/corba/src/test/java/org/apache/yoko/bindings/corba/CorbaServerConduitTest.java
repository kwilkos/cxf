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
package org.apache.yoko.bindings.corba;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl11.WSDLServiceFactory;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ServerRequest;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.apache.yoko.bindings.corba.runtime.CorbaStreamableImpl;
import org.apache.yoko.bindings.corba.types.CorbaPrimitiveHandler;
import org.apache.yoko.bindings.corba.utils.OrbConfig;
import org.apache.yoko.orb.CORBA.NVList;
import org.apache.yoko.wsdl.CorbaConstants;

public class CorbaServerConduitTest extends TestCase {
        
    IMocksControl control;
    ORB orb;
    Bus bus;
    protected EndpointInfo endpointInfo;
    protected EndpointReferenceType target;    
    protected MessageObserver observer;
    Message inMessage;
    CorbaBindingFactory factory;
    TestUtils testUtils;
    OrbConfig orbConfig;
    CorbaTypeMap corbaTypeMap;

    public CorbaServerConduitTest(String arg0) {
        super(arg0);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(CorbaBindingFactoryTest.class);
    }
    
    public void setUp() throws Exception {
        control = EasyMock.createNiceControl();
     
        bus = BusFactory.getDefaultBus(); 
     
        java.util.Properties props = System.getProperties();
        props.put("org.omg.CORBA.ORBClass", "org.apache.yoko.orb.CORBA.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass", "org.apache.yoko.orb.CORBA.ORBSingleton");
        props.put("yoko.orb.id", "Yoko-Server-Binding");
        orb = ORB.init(new String[0], props);
        orbConfig = new OrbConfig();
        orbConfig.setOrbClass("org.apache.yoko.orb.CORBA.ORB");
        orbConfig.setOrbSingletonClass("org.apache.yoko.orb.CORBA.ORBSingleton");
    }
    
    public void tearDown() {        
        bus.shutdown(true);
        if (orb != null) {
            try {
                orb.destroy();
            } catch (Exception ex) {
                // Do nothing.  Throw an Exception?
            }
        } 
    }
    
    public void testCorbaServerConduit() throws Exception {
        CorbaServerConduit conduit = setupCorbaServerConduit(false);        
        assertTrue("conduit should not be null", conduit != null);
    }
    
    public void testPrepare() throws Exception {       
        setupServiceInfo("http://yoko.apache.org/simple",
                         "/wsdl/simpleIdl.wsdl", "SimpleCORBAService",
                         "SimpleCORBAPort");
        CorbaDestination destination = new CorbaDestination(endpointInfo, orbConfig);
        CorbaServerConduit conduit = new CorbaServerConduit(endpointInfo,
                                                            destination.getAddress(),
                                                            orbConfig,
                                                            corbaTypeMap);
        CorbaMessage message = new CorbaMessage(new MessageImpl());
        try {
            conduit.prepare(message);
        } catch (Exception ex) {
            ex.printStackTrace();            
        }
        OutputStream os = message.getContent(OutputStream.class);
        assertTrue("OutputStream should not be null", os != null);        
        ORB orb = (ORB)message.get("orb");
        assertTrue("Orb should not be null", orb != null);
        Object obj = message.get("endpoint");
        assertTrue("EndpointReferenceType should not be null", obj != null);
        destination.shutdown();
    }
       
    
    public void testGetTargetReference() throws Exception {
        setupServiceInfo("http://yoko.apache.org/simple",
                         "/wsdl/simpleIdl.wsdl", "SimpleCORBAService",
                         "SimpleCORBAPort");

        CorbaDestination destination = new CorbaDestination(endpointInfo, orbConfig);
        CorbaServerConduit conduit = new CorbaServerConduit(endpointInfo,
                                                            destination.getAddress(),
                                                            orbConfig,
                                                            corbaTypeMap);
        
        EndpointReferenceType t = null;
        EndpointReferenceType ref = conduit.getTargetReference(t);
        assertTrue("ref should not be null", ref != null);
        destination.shutdown();
    }
    
    public void testGetAddress() throws Exception  {
        setupServiceInfo("http://yoko.apache.org/simple",
                         "/wsdl/simpleIdl.wsdl", "SimpleCORBAService",
                         "SimpleCORBAPort");

        CorbaDestination destination = new CorbaDestination(endpointInfo, orbConfig);
        endpointInfo.setAddress("corbaloc::localhost:40000/Simple");
        CorbaServerConduit conduit = new CorbaServerConduit(endpointInfo,
                                                            destination.getAddress(),
                                                            orbConfig,
                                                            corbaTypeMap);
        String address = conduit.getAddress();
        assertTrue("address should not be null", address != null);
        assertEquals(address, "corbaloc::localhost:40000/Simple");        
    }
    
    public void testClose() throws Exception {   
        
        Method m = CorbaServerConduit.class.getDeclaredMethod("buildRequestResult", 
            new Class[] {CorbaMessage.class});
        CorbaServerConduit conduit = EasyMock.createMock(CorbaServerConduit.class, new Method[] {m});       
        
        CorbaMessage msg = control.createMock(CorbaMessage.class);        
        conduit.buildRequestResult(msg);
        EasyMock.expectLastCall();
        OutputStream stream =control.createMock(OutputStream.class);
        EasyMock.expect(msg.getContent(OutputStream.class)).andReturn(stream);
        stream.close();
        EasyMock.expectLastCall();
        
        control.replay();
        conduit.close(msg);
        control.verify();
    }        
                
    public void testBuildRequestResult() {
        NVList list = (NVList)orb.create_list(0);        
        CorbaServerConduit conduit = setupCorbaServerConduit(false);  
        CorbaMessage msg = control.createMock(CorbaMessage.class);
        Exchange exchange = control.createMock(Exchange.class);        
        ServerRequest request = control.createMock(ServerRequest.class);
        
        EasyMock.expect(msg.getExchange()).andReturn(exchange);
        EasyMock.expect(exchange.get(ServerRequest.class)).andReturn(request);
                
        EasyMock.expect(exchange.isOneWay()).andReturn(false);
        CorbaMessage inMsg = EasyMock.createMock(CorbaMessage.class);
        EasyMock.expect(msg.getExchange()).andReturn(exchange);
        EasyMock.expect(exchange.getInMessage()).andReturn(inMsg);
                
        EasyMock.expect(inMsg.getList()).andReturn(list);
        EasyMock.expect(msg.getStreamableException()).andReturn(null);                        
        EasyMock.expect(msg.getStreamableArguments()).andReturn(null);
        EasyMock.expect(msg.getStreamableReturn()).andReturn(null);
        
        control.replay();
        conduit.buildRequestResult(msg);
        control.verify();        
    }
    
    public void testBuildRequestResultException() {
        NVList list = (NVList)orb.create_list(0);        
        CorbaServerConduit conduit = setupCorbaServerConduit(false);  
        CorbaMessage msg = control.createMock(CorbaMessage.class);
        Exchange exchange = control.createMock(Exchange.class);        
        ServerRequest request = control.createMock(ServerRequest.class);
        
        EasyMock.expect(msg.getExchange()).andReturn(exchange);
        EasyMock.expect(exchange.get(ServerRequest.class)).andReturn(request);
                
        EasyMock.expect(exchange.isOneWay()).andReturn(false);
        CorbaMessage inMsg = EasyMock.createMock(CorbaMessage.class);
        EasyMock.expect(msg.getExchange()).andReturn(exchange);
        EasyMock.expect(exchange.getInMessage()).andReturn(inMsg);                

        EasyMock.expect(inMsg.getList()).andReturn(list);        
        QName objName = new QName("object");
        QName objIdlType = new QName(CorbaConstants.NU_WSDL_CORBA, "short", CorbaConstants.NP_WSDL_CORBA);
        TypeCode objTypeCode = orb.get_primitive_tc(TCKind.tk_short);
        CorbaPrimitiveHandler obj = new CorbaPrimitiveHandler(objName, objIdlType, objTypeCode, null);
        CorbaStreamable exception = new CorbaStreamableImpl(obj, objName);       

        EasyMock.expect(msg.getStreamableException()).andReturn(exception);
        EasyMock.expect(msg.getStreamableException()).andReturn(exception);
               
        control.replay();
        conduit.buildRequestResult(msg);
        control.verify();        
    }

    public void testBuildRequestResultArgumentReturn() {
        CorbaStreamable[] arguments = new CorbaStreamable[1];
        QName objName = new QName("object");
        QName objIdlType = new QName(CorbaConstants.NU_WSDL_CORBA, "short", CorbaConstants.NP_WSDL_CORBA);
        TypeCode objTypeCode = orb.get_primitive_tc(TCKind.tk_short);
        CorbaPrimitiveHandler obj = new CorbaPrimitiveHandler(objName, objIdlType, objTypeCode, null);
        CorbaStreamable arg = new CorbaStreamableImpl(obj, objName);        
        arguments[0] = arg;
        arguments[0].setMode(org.omg.CORBA.ARG_OUT.value);        
        
        NVList nvlist = (NVList)orb.create_list(2);    
        Any value = orb.create_any();
        value.insert_Streamable(arguments[0]);
        nvlist.add_value(arguments[0].getName(), value, arguments[0].getMode());
        
        CorbaServerConduit conduit = setupCorbaServerConduit(false);  
        CorbaMessage msg = control.createMock(CorbaMessage.class);
        Exchange exchange = control.createMock(Exchange.class);        
        ServerRequest request = control.createMock(ServerRequest.class);
        
        EasyMock.expect(msg.getExchange()).andReturn(exchange);
        EasyMock.expect(exchange.get(ServerRequest.class)).andReturn(request);
                
        EasyMock.expect(exchange.isOneWay()).andReturn(false);        
        EasyMock.expect(msg.getExchange()).andReturn(exchange);        
        Message message = new MessageImpl();
        CorbaMessage corbaMessage = new CorbaMessage(message);
        corbaMessage.setList(nvlist);
        
        EasyMock.expect(exchange.getInMessage()).andReturn(corbaMessage);                
        EasyMock.expect(msg.getStreamableException()).andReturn(null);
        EasyMock.expect(msg.getStreamableArguments()).andReturn(arguments);        
        EasyMock.expect(msg.getStreamableReturn()).andReturn(arg);
               
        control.replay();
        conduit.buildRequestResult(msg);
        control.verify();        
    }
    
    public void testGetTarget()  {
        CorbaServerConduit conduit = setupCorbaServerConduit(false);
        EndpointReferenceType endpoint = conduit.getTarget();
        assertTrue("EndpointReferenceType should not be null", endpoint != null);
    }
    
           
    protected CorbaServerConduit setupCorbaServerConduit(boolean send) {
        target = EasyMock.createMock(EndpointReferenceType.class);                   
        endpointInfo = EasyMock.createMock(EndpointInfo.class);
        CorbaServerConduit CorbaServerConduit = 
            new CorbaServerConduit(endpointInfo, target, orbConfig, corbaTypeMap);
        
        if (send) {
            // setMessageObserver
            observer = new MessageObserver() {
                public void onMessage(Message m) {                    
                    inMessage = m;
                }
            };
            CorbaServerConduit.setMessageObserver(observer);
        }
        
        return CorbaServerConduit;        
    }
    
    protected void setupServiceInfo(String ns, String wsdl, String serviceName, String portName) {        
        URL wsdlUrl = getClass().getResource(wsdl);
        assertNotNull(wsdlUrl);
        WSDLServiceFactory factory = new WSDLServiceFactory(bus, wsdlUrl, new QName(ns, serviceName));

        Service service = factory.create();        
        endpointInfo = service.getEndpointInfo(new QName(ns, portName));
   
    }

    
       
           
}