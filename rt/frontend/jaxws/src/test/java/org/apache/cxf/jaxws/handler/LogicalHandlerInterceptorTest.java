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

import javax.xml.ws.Binding;
import javax.xml.ws.handler.LogicalMessageContext;

import junit.framework.TestCase;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.easymock.classextension.IMocksControl;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceControl;


public class LogicalHandlerInterceptorTest extends TestCase {
    
    private IMocksControl control;
    private Binding binding;
    private HandlerChainInvoker invoker;
    private Message message;
    private Exchange exchange;
    
    public void setUp() {
        control = createNiceControl();
        binding = control.createMock(Binding.class);
        invoker = control.createMock(HandlerChainInvoker.class);
        message = control.createMock(Message.class);
        exchange = control.createMock(Exchange.class);        
    }
    
    public void tearDown() {
        control.verify();
    }
    
    public void testInterceptSuccess() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        expect(invoker.invokeLogicalHandlers(eq(true),
            isA(LogicalMessageContext.class))).andReturn(true);
        control.replay();
        LogicalHandlerInterceptor<Message> li = new LogicalHandlerInterceptor<Message>(binding);
        assertEquals("unexpected phase", "user-logical", li.getPhase());
        li.handleMessage(message);
    }
    
    public void testInterceptFailure() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        expect(invoker.invokeLogicalHandlers(eq(true), 
            isA(LogicalMessageContext.class))).andReturn(false);
        control.replay();
        LogicalHandlerInterceptor<Message> li = new LogicalHandlerInterceptor<Message>(binding);
        li.handleMessage(message);   
    }
    
    public void testOnCompletion() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        invoker.mepComplete(message);
        expectLastCall();
        control.replay();
        LogicalHandlerInterceptor<Message> li = new LogicalHandlerInterceptor<Message>(binding);
        li.onCompletion(message);
    }
}
