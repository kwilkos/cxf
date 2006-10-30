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

package org.apache.cxf.jaxb;

import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.hello_world_soap_http.types.GreetMe;
import org.apache.hello_world_soap_http.types.GreetMeResponse;

public class BareInInterceptorTest extends TestBase {

    public void testInterceptorInbound() throws Exception {
        BareInInterceptor interceptor = new BareInInterceptor();
        message.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
            .createXMLStreamReader(getTestStream(getClass(), "resources/GreetMeDocLiteralReq.xml")));

        message.put(Message.INBOUND_MESSAGE, Message.INBOUND_MESSAGE);

        interceptor.handleMessage(message);

        assertNull(message.getContent(Exception.class));

        List<?> parameters = message.getContent(List.class);
        assertEquals(1, parameters.size());

        Object obj = parameters.get(0);
        assertTrue(obj instanceof GreetMe);
        GreetMe greet = (GreetMe)obj;
        assertEquals("TestSOAPInputPMessage", greet.getRequestType());
        
        BindingOperationInfo bop = message.getExchange().get(BindingOperationInfo.class);
        assertNotNull(bop);
        
        assertEquals("greetMe", bop.getName().getLocalPart());
    }

    public void testInterceptorOutbound() throws Exception {
        BareInInterceptor interceptor = new BareInInterceptor();

        message.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
            .createXMLStreamReader(getTestStream(getClass(), "resources/GreetMeDocLiteralResp.xml")));
        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);
        interceptor.handleMessage(message);

        List<?> parameters = message.getContent(List.class);
        assertEquals(1, parameters.size());

        Object obj = parameters.get(0);

        assertTrue(obj instanceof GreetMeResponse);
        GreetMeResponse greet = (GreetMeResponse)obj;
        assertEquals("TestSOAPOutputPMessage", greet.getResponseType());

        BindingOperationInfo bop = message.getExchange().get(BindingOperationInfo.class);
        assertNotNull(bop);
    }
}
