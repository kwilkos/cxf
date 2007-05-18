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

package org.apache.cxf.jaxws.handler;

import java.util.List;
import java.util.Map;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

public class AnnotationHandlerChainBuilderTest extends TestCase {

    public void setUp() {
    }

    public void testFindHandlerChainAnnotation() {
        HandlerTestImpl handlerTestImpl = new HandlerTestImpl();
        AnnotationHandlerChainBuilder chainBuilder = new AnnotationHandlerChainBuilder();
        List<Handler> handlers = chainBuilder.buildHandlerChainFromClass(handlerTestImpl.getClass());
        assertNotNull(handlers);
        assertEquals(5, handlers.size());
        assertEquals(TestLogicalHandler.class, handlers.get(0).getClass());
        assertEquals(TestLogicalHandler.class, handlers.get(1).getClass());
        assertEquals(TestLogicalHandler.class, handlers.get(2).getClass());
        assertEquals(TestLogicalHandler.class, handlers.get(3).getClass());
        assertEquals(TestProtocolHandler.class, handlers.get(4).getClass());
    }    
    
    public void testFindHandlerChainAnnotationPerPort() {
        HandlerTestImpl handlerTestImpl = new HandlerTestImpl();
        AnnotationHandlerChainBuilder chainBuilder = new AnnotationHandlerChainBuilder();
        QName portName = new QName("namespacedoesntsupportyet", "SoapPort1");
        List<Handler> handlers = chainBuilder
            .buildHandlerChainFromClass(handlerTestImpl.getClass(), portName);
        assertNotNull(handlers);
        assertEquals(5, handlers.size());
    }
    
    public static class TestLogicalHandler implements LogicalHandler {
        Map config;
        boolean initCalled;

        public void close(MessageContext arg0) {
        }

        public boolean handleFault(MessageContext arg0) {
            return false;
        }

        public boolean handleMessage(MessageContext arg0) {
            return false;
        }

        public final void init(final Map map) {
            config = map;
            initCalled = true;
        }
    }

    public static class TestProtocolHandler implements Handler {

        public void close(MessageContext arg0) {
        }

        public boolean handleFault(MessageContext arg0) {
            return false;
        }

        public boolean handleMessage(MessageContext arg0) {
            return false;
        }
    }

    @WebService()
    @HandlerChain(file = "./handlers.xml", name = "TestHandlerChain")
    public class HandlerTestImpl {

    }

}
