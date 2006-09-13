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

package org.apache.cxf.ws.addressing;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

import junit.framework.TestCase;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;


import static org.apache.cxf.binding.soap.Soap11.SOAP_NAMESPACE;
import static org.apache.cxf.message.Message.ONEWAY_MESSAGE;
import static org.apache.cxf.message.Message.REQUESTOR_ROLE;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;

public class MAPAggregatorTest extends TestCase {

    private MAPAggregator aggregator;
    private IMocksControl control;
    private AddressingPropertiesImpl expectedMAPs;
    private String expectedTo;
    private String expectedReplyTo;
    private String expectedRelatesTo;
    private String expectedAction;
    
    public void setUp() {
        aggregator = new MAPAggregator();
        control = EasyMock.createNiceControl();
    }

    public void tearDown() {
        expectedMAPs = null;
        expectedTo = null;
        expectedReplyTo = null;
        expectedRelatesTo = null;
        expectedAction = null;
    }

    public void testRequestorOutboundUsingAddressingMAPsInContext() 
        throws Exception {
        Message message = setUpMessage(true, true, false, true, true);
        aggregator.handleMessage(message);
        control.verify();
    }
    
    public void testRequestorOutboundUsingAddressingMAPsInContextZeroLengthAction() 
        throws Exception {
        Message message = setUpMessage(true, true, false, true, true, true);
        aggregator.handleMessage(message);
        control.verify();
    }

    public void testRequestorOutboundUsingAddressingMAPsInContextFault() 
        throws Exception {
        Message message = setUpMessage(true, true, false, true, true);
        aggregator.handleFault(message);
        control.verify();
    }

    public void testRequestorOutboundUsingAddressingNoMAPsInContext() 
        throws Exception {
        Message message = setUpMessage(true, true, false, true, false);
        aggregator.handleMessage(message);
        control.verify();
    }
    
    public void testRequestorOutboundUsingAddressingNoMAPsInContextFault() 
        throws Exception {
        Message message = setUpMessage(true, true, false, true, false);
        aggregator.handleFault(message);
        control.verify();
    }

    public void testRequestorOutboundNotUsingAddressing() throws Exception {
        Message message = setUpMessage(true, true, false, false);
        aggregator.handleMessage(message);
        control.verify();
    }

    public void testRequestorOutboundNotUsingAddressingFault() 
        throws Exception {
        Message message = setUpMessage(true, true, false, false);
        aggregator.handleFault(message);
        control.verify();
    }

    public void testRequestorOutboundOnewayUsingAddressingMAPsInContext() 
        throws Exception {
        Message message = setUpMessage(true, true, true, true, true);
        aggregator.handleMessage(message);
        control.verify();
    }

    public void testRequestorOutboundOnewayUsingAddressingMAPsInContextFault() 
        throws Exception {
        Message message = setUpMessage(true, true, true, true, true);
        aggregator.handleFault(message);
        control.verify();
    }

    public void testRequestorOutboundOnewayUsingAddressingNoMAPsInContext() 
        throws Exception {
        Message message = setUpMessage(true, true, true, true, false);
        aggregator.handleMessage(message);
        control.verify();
    }

    public void testRequestorOutboundOnewayUsingAddressingNoMAPsInContextFault() 
        throws Exception {
        Message message = setUpMessage(true, true, true, true, false);
        aggregator.handleFault(message);
        control.verify();
    }

    public void testRequestorOutboundOnewayNotUsingAddressing() throws Exception {
        Message message = setUpMessage(true, true, true, false);
        aggregator.handleMessage(message);
        control.verify();
    }

    public void testRequestorOutboundOnewayNotUsingAddressingFault() 
        throws Exception {
        Message message = setUpMessage(true, true, true, false);
        aggregator.handleFault(message);
        control.verify();
    }

    public void testResponderInboundValidMAPs() throws Exception {
        Message message = setUpMessage(false, false, false);
        aggregator.handleMessage(message);
        control.verify();
    }
    
    public void testResponderInboundDecoupled() throws Exception {
        Message message = 
            setUpMessage(false, false, false, true, false, true);
        aggregator.handleMessage(message);
        control.verify();
    }
    
    public void testResponderInboundOneway() throws Exception {
        Message message = 
            setUpMessage(false, false, true, true, false, true);
        aggregator.handleMessage(message);
        control.verify();
    }

    public void testResponderInboundValidMAPsFault() throws Exception {
        Message message = setUpMessage(false, false, false);
        aggregator.handleFault(message);
        control.verify();
    }

    public void testResponderInboundInvalidMAPs() throws Exception {
        aggregator.messageIDs.put("urn:uuid:12345", "urn:uuid:12345");
        Message message = setUpMessage(false, false, false);
        aggregator.handleMessage(message);
        control.verify();
    }

    public void testResponderInboundInvalidMAPsFault() throws Exception {
        aggregator.messageIDs.put("urn:uuid:12345", "urn:uuid:12345");
        Message message = setUpMessage(false, false, false);
        aggregator.handleFault(message);
        control.verify();
    }

    public void testResponderOutbound() throws Exception {
        Message message = setUpMessage(false, true, false);
        aggregator.handleMessage(message);
        control.verify();
    }
    
    public void testResponderOutboundZeroLengthAction() throws Exception {
        Message message = 
            setUpMessage(false, true, false, false, false, false, false);
        aggregator.handleMessage(message);
        control.verify();
    }

    public void testResponderOutboundFault() throws Exception {
        Message message = setUpMessage(new boolean[] {false,
                                                      true,
                                                      false,
                                                      false,
                                                      false,
                                                      true,
                                                      false,
                                                      true});
        aggregator.handleFault(message);
        control.verify();
    }

    public void testRequestorInbound() throws Exception {
        Message message = setUpMessage(true, true, false);
        aggregator.handleMessage(message);
        control.verify();
    }

    public void testRequestorInboundFault() throws Exception {
        Message message = setUpMessage(true, true, false);
        aggregator.handleFault(message);
        control.verify();
    }

    private Message setUpMessage(boolean requestor, 
                                 boolean outbound,
                                 boolean oneway) 
        throws Exception {
        return setUpMessage(requestor, outbound, oneway, false, false, false);
    }

    private Message setUpMessage(boolean requestor, 
                                 boolean outbound,
                                 boolean oneway,
                                 boolean usingAddressing)
        throws Exception {
        return setUpMessage(requestor,
                            outbound,
                            oneway,
                            usingAddressing,
                            false,
                            false);
    }

    private Message setUpMessage(boolean requestor, 
                                 boolean outbound,
                                 boolean oneway,
                                 boolean usingAddressing,
                                 boolean mapsInContext) 
        throws Exception {
        return setUpMessage(requestor,
                            outbound,
                            oneway,
                            usingAddressing,
                            mapsInContext,
                            false);
    }

    private Message setUpMessage(boolean requestor, 
                                 boolean outbound,
                                 boolean oneway,
                                 boolean usingAddressing,
                                 boolean mapsInContext,
                                 boolean decoupled) 
        throws Exception {
        return setUpMessage(requestor, 
                            outbound,
                            oneway,
                            usingAddressing,
                            mapsInContext,
                            decoupled,
                            false);
    }

    
    private Message setUpMessage(boolean requestor, 
                                 boolean outbound,
                                 boolean oneway,
                                 boolean usingAddressing,
                                 boolean mapsInContext,
                                 boolean decoupled,
                                 boolean zeroLengthAction) 
        throws Exception {
        boolean[] params = {requestor, 
                            outbound,
                            oneway,
                            usingAddressing,
                            mapsInContext,
                            decoupled,
                            zeroLengthAction,
                            false};  
        return setUpMessage(params);
    }

    /**
     * Boolean array is used to work around checkstyle rule limiting
     * parameter cardinality to 7. 
     */
    private Message setUpMessage(boolean[] params)
        throws Exception {
        boolean requestor = params[0]; 
        boolean outbound = params[1];
        boolean oneway = params[2];
        boolean usingAddressing = params[3];
        boolean mapsInContext = params[4];
        boolean decoupled = params[5];
        boolean zeroLengthAction = params[6];
        boolean fault = params[7];
        Message message = control.createMock(Message.class);
        message.get(MESSAGE_OUTBOUND_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(outbound));
        message.get(REQUESTOR_ROLE);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(requestor));
        Exchange exchange = control.createMock(Exchange.class);
        if (outbound && requestor) {
            setUpUsingAddressing(message, exchange, usingAddressing);
            if (usingAddressing) {
                setUpRequestor(message,
                               exchange,
                               oneway, 
                               mapsInContext,
                               decoupled,
                               zeroLengthAction);
            } 
        } else if (!requestor) {
            setUpResponder(message,
                           exchange,
                           oneway,
                           outbound,
                           decoupled,
                           zeroLengthAction,
                           fault);
        }
        control.replay();
        return message;
    }

    private void setUpUsingAddressing(Message message,
                                      Exchange exchange,
                                      boolean usingAddressing) {
        message.getExchange();
        EasyMock.expectLastCall().andReturn(exchange);
        Endpoint endpoint = control.createMock(Endpoint.class);
        exchange.get(Endpoint.class);
        EasyMock.expectLastCall().andReturn(endpoint);
        EndpointInfo endpointInfo = control.createMock(EndpointInfo.class);
        endpoint.getEndpointInfo();
        EasyMock.expectLastCall().andReturn(endpointInfo);
        List<ExtensibilityElement> endpointExts =
            new ArrayList<ExtensibilityElement>();
        endpointInfo.getExtensors(EasyMock.eq(ExtensibilityElement.class));
        EasyMock.expectLastCall().andReturn(endpointExts);
        BindingInfo bindingInfo = control.createMock(BindingInfo.class);
        endpointInfo.getBinding();
        EasyMock.expectLastCall().andReturn(bindingInfo).times(2);
        bindingInfo.getExtensors(EasyMock.eq(ExtensibilityElement.class));
        EasyMock.expectLastCall().andReturn(Collections.EMPTY_LIST);
        ServiceInfo serviceInfo = control.createMock(ServiceInfo.class);
        endpointInfo.getService();
        EasyMock.expectLastCall().andReturn(serviceInfo).times(2);
        serviceInfo.getExtensors(EasyMock.eq(ExtensibilityElement.class));
        EasyMock.expectLastCall().andReturn(Collections.EMPTY_LIST);
        ExtensibilityElement ext = 
            control.createMock(ExtensibilityElement.class);
        if (usingAddressing) {
            QName elementType = usingAddressing 
                ? Names.WSAW_USING_ADDRESSING_QNAME 
                : new QName(SOAP_NAMESPACE, "encodingStyle");
            ext.getElementType();
            EasyMock.expectLastCall().andReturn(elementType);
            endpointExts.add(ext);
        }
    }
    
    private void setUpRequestor(Message message,
                                Exchange exchange,
                                boolean oneway,
                                boolean mapsInContext,
                                boolean decoupled,
                                boolean zeroLengthAction) throws Exception {
        message.get(REQUESTOR_ROLE);
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        AddressingPropertiesImpl maps = mapsInContext 
                                        ? new AddressingPropertiesImpl()
                                        : null;
        if (zeroLengthAction) {
            maps.setAction(ContextUtils.getAttributedURI(""));
        }
        message.get(CLIENT_ADDRESSING_PROPERTIES);
        EasyMock.expectLastCall().andReturn(maps);
        Method method = SEI.class.getMethod("op", new Class[0]);
        if (!zeroLengthAction) {
            setUpMethod(message, exchange, method);
            message.get(REQUESTOR_ROLE);
            EasyMock.expectLastCall().andReturn(Boolean.TRUE);
            expectedAction = "http://foo/bar/SEI/opRequest";
        }
        message.get(REQUESTOR_ROLE);
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        message.get(ONEWAY_MESSAGE);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(oneway));
        EasyMock.eq(CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
        expectedMAPs = maps;
        expectedTo = Names.WSA_NONE_ADDRESS;
        expectedReplyTo = oneway 
                          ? Names.WSA_NONE_ADDRESS
                          : Names.WSA_ANONYMOUS_ADDRESS;
        EasyMock.reportMatcher(new MAPMatcher());
        message.put(CLIENT_ADDRESSING_PROPERTIES_OUTBOUND,
                    mapsInContext
                    ? maps
                    : new AddressingPropertiesImpl());
        EasyMock.expectLastCall().andReturn(null);
    }

    private void setUpResponder(Message message,
                                Exchange exchange,
                                boolean oneway,
                                boolean outbound,
                                boolean decoupled,
                                boolean zeroLengthAction,
                                boolean fault) throws Exception {
        message.get(REQUESTOR_ROLE);
        EasyMock.expectLastCall().andReturn(Boolean.FALSE);
        AddressingPropertiesImpl maps = new AddressingPropertiesImpl();
        EndpointReferenceType replyTo = new EndpointReferenceType();
        replyTo.setAddress(
            ContextUtils.getAttributedURI(decoupled
                                          ? "http://localhost:9999/decoupled"
                                          : Names.WSA_ANONYMOUS_ADDRESS));
        maps.setReplyTo(replyTo);
        EndpointReferenceType faultTo = new EndpointReferenceType();
        faultTo.setAddress(
            ContextUtils.getAttributedURI(decoupled
                                          ? "http://localhost:9999/fault"
                                          : Names.WSA_ANONYMOUS_ADDRESS));
        maps.setFaultTo(faultTo);
        AttributedURIType id = 
            ContextUtils.getAttributedURI("urn:uuid:12345");
        maps.setMessageID(id);
        if (zeroLengthAction) {
            maps.setAction(ContextUtils.getAttributedURI(""));
        }
        message.get(SERVER_ADDRESSING_PROPERTIES_INBOUND);
        EasyMock.expectLastCall().andReturn(maps);
        
        if (!outbound && (oneway || decoupled)) {
            message.get(ONEWAY_MESSAGE);
            EasyMock.expectLastCall().andReturn(Boolean.valueOf(oneway));
            setUpRebase(message);
        }
        if (outbound || aggregator.messageIDs.size() > 0) {
            if (!zeroLengthAction) {
                Method method = SEI.class.getMethod("op", new Class[0]);
                setUpMethod(message, exchange, method);
                message.get(REQUESTOR_ROLE);
                EasyMock.expectLastCall().andReturn(Boolean.FALSE);
                expectedAction = "http://foo/bar/SEI/opResponse";
            }
            message.get(REQUESTOR_ROLE);
            EasyMock.expectLastCall().andReturn(Boolean.FALSE);
            message.get(SERVER_ADDRESSING_PROPERTIES_INBOUND);
            EasyMock.expectLastCall().andReturn(maps);
            if (fault) {
                // REVISIT test double rebase does not occur
                setUpRebase(message);
            }
            EasyMock.eq(SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
            expectedTo = decoupled
                         ? "http://localhost:9999/decoupled"
                         : Names.WSA_ANONYMOUS_ADDRESS;
            expectedRelatesTo = maps.getMessageID().getValue();
            EasyMock.reportMatcher(new MAPMatcher());
            message.put(SERVER_ADDRESSING_PROPERTIES_OUTBOUND,
                        new AddressingPropertiesImpl());
            EasyMock.expectLastCall().andReturn(null);
        }
    }
    
    private void setUpRebase(Message message) throws Exception {
        message.get("org.apache.cxf.ws.addressing.partial.response.sent");
        EasyMock.expectLastCall().andReturn(Boolean.FALSE);
        Destination target = control.createMock(Destination.class);
        message.getDestination();
        EasyMock.expectLastCall().andReturn(target);
        Conduit backChannel = control.createMock(Conduit.class);
        target.getBackChannel(EasyMock.eq(message),
                              EasyMock.isA(Message.class),
                              EasyMock.isA(EndpointReferenceType.class));
        EasyMock.expectLastCall().andReturn(backChannel);
        // REVISIT test interceptor chain setup & send
    }

    private void setUpMethod(Message message, Exchange exchange, Method method) {
        message.getExchange();
        EasyMock.expectLastCall().andReturn(exchange);
        OperationInfo opInfo = new OperationInfo(); 
        opInfo.setProperty(Method.class.getName(), method);
        BindingOperationInfo bindingOpInfo = new TestBindingOperationInfo(opInfo);
        exchange.get(BindingOperationInfo.class);
        EasyMock.expectLastCall().andReturn(bindingOpInfo);
        // Usual fun with EasyMock not always working as expected
        //BindingOperationInfo bindingOpInfo =
        //    EasyMock.createMock(BindingOperationInfo.class); 
        //OperationInfo opInfo = EasyMock.createMock(OperationInfo.class); 
        //bindingOpInfo.getOperationInfo();
        //EasyMock.expectLastCall().andReturn(opInfo);
        //opInfo.getProperty(EasyMock.eq(Method.class.getName()));
        //EasyMock.expectLastCall().andReturn(method);
    }

    private final class MAPMatcher implements IArgumentMatcher {
        public boolean matches(Object obj) {
            if (obj instanceof AddressingPropertiesImpl) {
                AddressingPropertiesImpl other = (AddressingPropertiesImpl)obj;
                return compareExpected(other);
            }
            return false;
        }    

        public void appendTo(StringBuffer buffer) {
            buffer.append("MAPs did not match");
        }

        private boolean compareExpected(AddressingPropertiesImpl other) {
            boolean ret = false;
            if (expectedMAPs == null || expectedMAPs == other) {
                boolean toOK = 
                    expectedTo == null 
                    || expectedTo.equals(other.getTo().getValue());
                boolean replyToOK = 
                    expectedReplyTo == null 
                    || expectedReplyTo.equals(
                           other.getReplyTo().getAddress().getValue());
                boolean relatesToOK =
                    expectedRelatesTo == null 
                    || expectedRelatesTo.equals(
                           other.getRelatesTo().getValue());
                boolean actionOK =
                    expectedAction == null
                    || expectedAction.equals(other.getAction().getValue());
                boolean messageIdOK = other.getMessageID() != null;
                ret = toOK 
                      && replyToOK 
                      && relatesToOK 
                      && actionOK
                      && messageIdOK;
            }
            return ret;
        }
    } 
    
    private static interface SEI {
        @RequestWrapper(targetNamespace = "http://foo/bar", className = "SEI", localName = "opRequest")
        @ResponseWrapper(targetNamespace = "http://foo/bar", className = "SEI", localName = "opResponse")
        String op();
    }
    
    private static class TestBindingOperationInfo extends BindingOperationInfo {
        public TestBindingOperationInfo(OperationInfo oi) {
            opInfo = oi;
        }
    }
}
