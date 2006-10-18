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

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.apache.cxf.message.Message;

public class ContextPropertiesMappingTest extends TestCase {
    private static final String ADDRESS = "test address";
    private static final String REQUEST_METHOD = "GET";
    private Map<String, Object> message = new HashMap<String, Object>();
    private Map<String, Object> requestContext = new HashMap<String, Object>();
    private Map<String, Object> responseContext = new HashMap<String, Object>();
    
    
    public void setUp() throws Exception {
        message.clear();
        message.put(Message.ENDPOINT_ADDRESS, ADDRESS);
        message.put(Message.HTTP_REQUEST_METHOD, REQUEST_METHOD);
        requestContext.clear();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ADDRESS + "jaxws");
        responseContext.clear();
    }
    
    public void testJaxws2Cxf() {
        Object address = requestContext.get(Message.ENDPOINT_ADDRESS);
        assertTrue("address should be null", address == null);
        ContextPropertiesMapping.mapJaxws2Cxf(requestContext);
        address = requestContext.get(Message.ENDPOINT_ADDRESS);
        assertTrue("address should not be null", address != null);
        assertEquals("address should get from requestContext", address, ADDRESS + "jaxws");
        message.putAll(requestContext);
        address = message.get(Message.ENDPOINT_ADDRESS);
        address = requestContext.get(Message.ENDPOINT_ADDRESS);
        assertTrue("address should not be null", address != null);
        assertEquals("address should get from requestContext", address, ADDRESS + "jaxws");        
    }
    
    public void testCxf2Jaxws() {
        responseContext.putAll(message);
        Object requestMethod = responseContext.get(MessageContext.HTTP_REQUEST_METHOD);
        assertTrue("requestMethod should be null", requestMethod == null);
        ContextPropertiesMapping.mapCxf2Jaxws(responseContext);
        requestMethod = responseContext.get(MessageContext.HTTP_REQUEST_METHOD);
        assertTrue("requestMethod should not be null", requestMethod != null);
        assertEquals(requestMethod, REQUEST_METHOD);
    }
    

}
