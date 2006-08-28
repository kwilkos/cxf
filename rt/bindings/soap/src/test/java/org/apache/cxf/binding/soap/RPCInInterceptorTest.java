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

package org.apache.cxf.binding.soap;


public class RPCInInterceptorTest extends TestBase {
//    
//    public void setUp() throws Exception {
//        super.setUp();
//
//        soapMessage.getExchange().put(SoapMessage.DATAREADER_FACTORY_KEY, "test.reader.factory");
//        soapMessage.put(SoapMessage.BINDING_INFO,
//                        getTestService("resources/wsdl/hello_world_rpc_lit.wsdl", "SoapPortRPCLit"));
//        soapMessage.put("IMPLEMENTOR_METHOD", getTestMethod(GreeterRPCLit.class, "sendReceiveData"));
//    }
//
    public void testInterceptorRPCLitInbound() throws Exception {
//        RPCInInterceptor interceptor = new RPCInInterceptor();
//
//        soapMessage.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
//            .createXMLStreamReader(getTestStream(getClass(), "resources/greetMeRpcLitReq.xml")));
//        soapMessage.put(Message.INBOUND_MESSAGE, Message.INBOUND_MESSAGE);
//
//        interceptor.handleMessage(soapMessage);
//
//        List<?> parameters = (List<?>) soapMessage.get(Message.INVOCATION_OBJECTS);
//        assertEquals(1, parameters.size());
//
//        assertEquals("sendReceiveData", (String) soapMessage.get(Message.INVOCATION_OPERATION));
//        
//        Object obj = parameters.get(0);
//        assertTrue(obj instanceof MyComplexStruct);
//        MyComplexStruct s = (MyComplexStruct) obj;
//        assertEquals("this is element 2", s.getElem2());
    }
//
//    public void testInterceptorRPCLitOutbound() throws Exception {
//        RPCInInterceptor interceptor = new RPCInInterceptor();
//        soapMessage.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
//            .createXMLStreamReader(getTestStream(getClass(), "resources/greetMeRpcLitResp.xml")));
//        interceptor.handleMessage(soapMessage);
//
//        List<?> objs = (List<?>) soapMessage.get(Message.INVOCATION_OBJECTS);
//        assertEquals(1, objs.size());
//        
//        Object retValue = objs.get(0);
//        assertNotNull(retValue);
//        
//        assertTrue(retValue instanceof MyComplexStruct);
//        MyComplexStruct s = (MyComplexStruct) retValue;
//        assertEquals("return is element 2", s.getElem2());
//    }
//
//    protected BindingInfo getTestService(String wsdlUrl, String port) throws Exception {
//        BindingInfo binding = super.getTestService(wsdlUrl, port);
//
//        assertEquals(3, binding.getOperations().size());
//
//        BindingOperationInfo op = binding.getOperation(new QName("http://apache.org/hello_world_rpclit",
//                                                                 "sendReceiveData"));
//        assertNotNull(op);
//        assertEquals(1, op.getInput().getMessageInfo().size());
//        assertEquals(1, op.getOutput().getMessageInfo().size());
//        MessageInfo msg = op.getInput().getMessageInfo();
//        assertEquals(1, msg.getMessageParts().size());
//        MessagePartInfo part = msg.getMessageParts().get(0);
//        assertEquals(new QName("http://apache.org/hello_world_rpclit", "in"), part.getName());
//
//        OperationInfo oi = op.getOperationInfo();
//        oi.setProperty("test.reader.factory", getTestReaderFactory(GreeterRPCLit.class));
//
//        return binding;
//    }

}

