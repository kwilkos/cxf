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

package org.apache.cxf.ws.addressing.soap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.Names;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.v200408.AttributedURI;
import org.apache.cxf.ws.addressing.v200408.Relationship;

import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import static org.apache.cxf.message.Message.MIME_HEADERS;
import static org.apache.cxf.message.Message.REQUESTOR_ROLE;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;
import static org.apache.cxf.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;


public class MAPCodecTest extends TestCase {

    private MAPCodec codec;
    private IMocksControl control;
    private QName[] expectedNames;
    private Class<?>[] expectedDeclaredTypes;
    private Object[] expectedValues;
    private int expectedIndex;
    private String expectedNamespaceURI;
    private Map<String, List<String>> mimeHeaders;
    private Exchange correlatedExchange;
    private boolean expectRelatesTo;

    public void setUp() {
        codec = new MAPCodec();
        control = EasyMock.createNiceControl();
    }

    public void tearDown() {
        expectedNames = null;
        expectedDeclaredTypes = null;
        expectedValues = null;
        expectedIndex = 0;
        expectedNamespaceURI = null;
        mimeHeaders = null;
        correlatedExchange = null;
    }

    public void testGetHeaders() throws Exception {
        Set<QName> headers = codec.getUnderstoodHeaders();
        assertTrue("expected From header", 
                   headers.contains(Names.WSA_FROM_QNAME));
        assertTrue("expected To header", 
                   headers.contains(Names.WSA_TO_QNAME));
        assertTrue("expected ReplyTo header", 
                   headers.contains(Names.WSA_REPLYTO_QNAME));
        assertTrue("expected FaultTo header", 
                   headers.contains(Names.WSA_FAULTTO_QNAME));
        assertTrue("expected Action header", 
                   headers.contains(Names.WSA_ACTION_QNAME));
        assertTrue("expected MessageID header", 
                   headers.contains(Names.WSA_MESSAGEID_QNAME));
    }

    public void testRequestorOutbound() throws Exception {
        SoapMessage message = setUpMessage(true, true);
        codec.handleMessage(message);
        control.verify();
        verifyMessage(message, true, true, true);
    }
    
    public void testRequestorOutboundPreExistingSOAPAction() throws Exception {
        SoapMessage message = setUpMessage(true, true, false, true);
        codec.handleMessage(message);
        verifyAction();
        control.verify();
        verifyMessage(message, true, true, true);
    }
    
    public void testRequestorOutboundNonNative() throws Exception {
        String uri = VersionTransformer.Names200408.WSA_NAMESPACE_NAME;
        SoapMessage message = 
            setUpMessage(true, true, false, false, uri);
        codec.handleMessage(message);
        control.verify();
        verifyMessage(message, true, true, false);
    }
    
    public void testResponderInbound() throws Exception {
        SoapMessage message = setUpMessage(false, false);
        codec.handleMessage(message);
        control.verify();
        verifyMessage(message, false, false, true);
    }

    public void testResponderOutbound() throws Exception {
        SoapMessage message = setUpMessage(false, true);
        codec.handleMessage(message);
        control.verify();
        verifyMessage(message, false, true, true);
    }
    
    public void testResponderInboundNonNative() throws Exception {
        String uri = VersionTransformer.Names200408.WSA_NAMESPACE_NAME;
        SoapMessage message = 
            setUpMessage(false, false, false, false, uri);
        codec.handleMessage(message);
        control.verify();
        verifyMessage(message, false, false, false);
    }
    
    public void testResponderOutboundInvalidMAP() throws Exception {
        SoapMessage message = setUpMessage(false, true, true);
        try {
            codec.handleMessage(message);
            fail("expected SOAPFaultException on invalid MAP");
        } catch (SOAPFaultException sfe) {
            assertEquals("unexpected fault string",
                         "Duplicate Message ID urn:uuid:12345", 
                         sfe.getFault().getFaultString());
        }
        control.verify();
        verifyMessage(message, false, true, true);
    }
    
    public void testResponderOutboundPreExistingSOAPAction() throws Exception {
        SoapMessage message = setUpMessage(false, true, false, true);
        codec.handleMessage(message);
        verifyAction();
        control.verify();
        verifyMessage(message, false, true, true);
    }

    public void testResponderOutboundNonNative() throws Exception {
        String uri = VersionTransformer.Names200408.WSA_NAMESPACE_NAME;
        SoapMessage message = 
            setUpMessage(false, true, false, false, uri);
        codec.handleMessage(message);
        control.verify();
        verifyMessage(message, false, true, false);
    }
    
    public void testRequestorInbound() throws Exception {
        SoapMessage message = setUpMessage(true, false);
        codec.handleMessage(message);
        control.verify();
        verifyMessage(message, true, false, true);
    }
    
    public void testRequestorInboundNonNative() throws Exception {
        String uri = VersionTransformer.Names200408.WSA_NAMESPACE_NAME;
        SoapMessage message = 
            setUpMessage(true, false, false, false, uri);
        codec.handleMessage(message);
        control.verify();
        verifyMessage(message, true, false, false);
    }

    private SoapMessage setUpMessage(boolean requestor, 
                                     boolean outbound)
        throws Exception {
        return setUpMessage(requestor, outbound, false); 
    }

    private SoapMessage setUpMessage(boolean requestor, 
                                     boolean outbound,
                                     boolean invalidMAP) 
        throws Exception {
        return setUpMessage(requestor, 
                            outbound,
                            invalidMAP,
                            false);
    }

    private SoapMessage setUpMessage(boolean requestor, 
                                     boolean outbound,
                                     boolean invalidMAP,
                                     boolean preExistingSOAPAction) 
        throws Exception {
        return setUpMessage(requestor, 
                            outbound,
                            invalidMAP,
                            preExistingSOAPAction,
                            Names.WSA_NAMESPACE_NAME);
    }

    private SoapMessage setUpMessage(boolean requestor, 
                                     boolean outbound,
                                     boolean invalidMAP,
                                     boolean preExistingSOAPAction,
                                     String exposeAs) 
        throws Exception {
        SoapMessage message = new SoapMessage(new MessageImpl());
        setUpOutbound(message, outbound);
        expectRelatesTo = (requestor && !outbound) || (!requestor && outbound);
        message.put(REQUESTOR_ROLE, Boolean.valueOf(requestor));
        String mapProperty = getMAPProperty(requestor, outbound);
        AddressingPropertiesImpl maps = getMAPs(requestor, outbound, exposeAs);
        Element header = control.createMock(Element.class);
        message.setHeaders(Element.class, header);
        JAXBContext jaxbContext = control.createMock(JAXBContext.class);
        ContextUtils.setJAXBContext(jaxbContext);
        VersionTransformer.Names200408.setJAXBContext(jaxbContext);
        if (outbound) {
            setUpEncode(requestor,
                        message,
                        header,
                        maps,
                        mapProperty,
                        invalidMAP,
                        preExistingSOAPAction);
        } else {
            setUpDecode(message, header, maps, mapProperty, requestor);
        }
        control.replay();
        return message;
    }

    private void setUpEncode(boolean requestor,
                             SoapMessage message,
                             Element header,
                             AddressingPropertiesImpl maps,
                             String mapProperty,
                             boolean invalidMAP,
                             boolean preExistingSOAPAction) throws Exception { 
        message.put(mapProperty, maps);
        NodeList headerElements = control.createMock(NodeList.class);
        header.getElementsByTagNameNS(EasyMock.eq(maps.getNamespaceURI()),
                                      EasyMock.eq("*"));
        EasyMock.expectLastCall().andReturn(headerElements);
        headerElements.getLength();
        EasyMock.expectLastCall().andReturn(0);
        header.setAttributeNS(EasyMock.eq("http://www.w3.org/2000/xmlns/"),
                              EasyMock.eq("xmlns:" + Names.WSA_NAMESPACE_PREFIX),
                              EasyMock.eq(maps.getNamespaceURI()));
        EasyMock.expectLastCall();
        Marshaller marshaller = control.createMock(Marshaller.class);
        ContextUtils.getJAXBContext().createMarshaller();
        EasyMock.expectLastCall().andReturn(marshaller);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        EasyMock.expectLastCall();
        IArgumentMatcher matcher = new JAXBEltMatcher();
        int expectedMarshals = requestor 
                               ? expectedValues.length - 1
                               : expectedValues.length;
        for (int i = 0; i < expectedMarshals; i++) {
            EasyMock.reportMatcher(matcher);
            EasyMock.eq(header);
            marshaller.marshal(null, header);
            EasyMock.expectLastCall();
        }
        mimeHeaders = new HashMap<String, List<String>>();
        message.put(MIME_HEADERS, mimeHeaders);
        if (preExistingSOAPAction) {
            List<String> soapAction = new ArrayList<String>();
            soapAction.add("\"foobar\"");
            mimeHeaders.put("SOAPAction", soapAction);
        }
        if (invalidMAP) {
            message.put("org.apache.cxf.ws.addressing.map.fault.name",
                        Names.DUPLICATE_MESSAGE_ID_NAME);
            message.put("org.apache.cxf.ws.addressing.map.fault.reason",
                        "Duplicate Message ID urn:uuid:12345");
        }
    }
    
    private void setUpDecode(SoapMessage message, 
                             Element header,
                             AddressingPropertiesImpl maps,
                             String mapProperty,
                             boolean requestor) throws Exception {
        Unmarshaller unmarshaller = control.createMock(Unmarshaller.class);
        ContextUtils.getJAXBContext().createUnmarshaller();
        EasyMock.expectLastCall().andReturn(unmarshaller);
        NodeList headerElements = control.createMock(NodeList.class);
        header.getChildNodes();
        EasyMock.expectLastCall().andReturn(headerElements);
        String uri = maps.getNamespaceURI();
        boolean exposedAsNative = Names.WSA_NAMESPACE_NAME.equals(uri);
        boolean exposedAs200408 = 
            VersionTransformer.Names200408.WSA_NAMESPACE_NAME.equals(uri);
        assertTrue("unexpected namescape URI: " + uri, 
                   exposedAsNative || exposedAs200408);
        headerElements.getLength();
        EasyMock.expectLastCall().andReturn(6);
        setUpHeaderDecode(headerElements,
                          uri,
                          Names.WSA_MESSAGEID_NAME,
                          exposedAsNative
                          ? AttributedURIType.class
                          : AttributedURI.class,
                          0,
                          unmarshaller);
        setUpHeaderDecode(headerElements,
                          uri,
                          Names.WSA_TO_NAME,
                          exposedAsNative
                          ? AttributedURIType.class
                          : AttributedURI.class,
                          1,
                          unmarshaller);
        setUpHeaderDecode(headerElements,
                          uri,
                          Names.WSA_REPLYTO_NAME,
                          exposedAsNative
                          ? EndpointReferenceType.class
                          : VersionTransformer.Names200408.EPR_TYPE,
                          2,
                          unmarshaller);
        setUpHeaderDecode(headerElements,
                          uri,
                          Names.WSA_FAULTTO_NAME,
                          exposedAsNative
                          ? EndpointReferenceType.class
                          : VersionTransformer.Names200408.EPR_TYPE,
                          3,
                          unmarshaller);
        setUpHeaderDecode(headerElements,
                          uri,
                          Names.WSA_RELATESTO_NAME,
                          exposedAsNative
                          ? RelatesToType.class
                          : Relationship.class,
                          4,
                          unmarshaller);
        setUpHeaderDecode(headerElements,
                          uri,
                          Names.WSA_ACTION_NAME,
                          exposedAsNative
                          ? AttributedURIType.class
                          : AttributedURI.class,
                          5,
                          unmarshaller);
    }

    private <T> void setUpHeaderDecode(NodeList headerElements,
                                       String uri,
                                       String name,
                                       Class<T> clz,
                                       int index,
                                       Unmarshaller unmarshaller) 
        throws Exception { 
        Element headerElement = control.createMock(Element.class);
        headerElements.item(index);
        EasyMock.expectLastCall().andReturn(headerElement).times(2);
        headerElement.getNamespaceURI();
        EasyMock.expectLastCall().andReturn(uri);
        headerElement.getLocalName();
        EasyMock.expectLastCall().andReturn(name);
        Object v = expectedValues[index];
        JAXBElement<?> jaxbElement = 
            new JAXBElement<T>(new QName(uri, name), clz, clz.cast(v));
        unmarshaller.unmarshal(headerElement, clz);
        EasyMock.expectLastCall().andReturn(jaxbElement);
    }
    
    private void setUpOutbound(Message message, boolean outbound) {
        Exchange exchange = new ExchangeImpl();
        exchange.setOutMessage(outbound ? message : new MessageImpl());
        message.setExchange(exchange);
    }

    private String getMAPProperty(boolean requestor, boolean outbound) { 
        return requestor
               ? outbound
                 ? CLIENT_ADDRESSING_PROPERTIES_OUTBOUND
                 : CLIENT_ADDRESSING_PROPERTIES_INBOUND
               : outbound
                 ? SERVER_ADDRESSING_PROPERTIES_OUTBOUND
                 : SERVER_ADDRESSING_PROPERTIES_INBOUND;
    }

    private AddressingPropertiesImpl getMAPs(boolean requestor,
                                             boolean outbound,
                                             String uri) {
        AddressingPropertiesImpl maps = new AddressingPropertiesImpl();
        boolean exposeAsNative = Names.WSA_NAMESPACE_NAME.equals(uri);
        boolean exposeAs200408 = 
            VersionTransformer.Names200408.WSA_NAMESPACE_NAME.equals(uri);
        AttributedURIType id = 
            ContextUtils.getAttributedURI("urn:uuid:12345");
        maps.setMessageID(id);
        AttributedURIType to =
            ContextUtils.getAttributedURI("foobar");
        maps.setTo(to);
        EndpointReferenceType replyTo = new EndpointReferenceType();
        String anonymous = 
            exposeAsNative
            ? Names.WSA_ANONYMOUS_ADDRESS
            : VersionTransformer.Names200408.WSA_ANONYMOUS_ADDRESS;
        replyTo.setAddress(
            ContextUtils.getAttributedURI(anonymous));
        maps.setReplyTo(replyTo);
        EndpointReferenceType faultTo = new EndpointReferenceType();
        anonymous = 
            exposeAsNative
            ? Names.WSA_ANONYMOUS_ADDRESS
            : VersionTransformer.Names200408.WSA_ANONYMOUS_ADDRESS;
        faultTo.setAddress(
            ContextUtils.getAttributedURI(anonymous));
        maps.setFaultTo(faultTo);
        RelatesToType relatesTo = null;
        if (expectRelatesTo) {
            String correlationID = "urn:uuid:67890";
            relatesTo = new RelatesToType(); 
            relatesTo.setValue(correlationID);
            maps.setRelatesTo(relatesTo);
            correlatedExchange = new ExchangeImpl();
            codec.uncorrelatedExchanges.put(correlationID, correlatedExchange);
        }
        AttributedURIType action = 
            ContextUtils.getAttributedURI("http://foo/bar/SEI/opRequest");
        maps.setAction(action);
        maps.exposeAs(uri);
        expectedNamespaceURI = uri;

        expectedNames = 
            new QName[] {new QName(uri, Names.WSA_MESSAGEID_NAME), 
                         new QName(uri, Names.WSA_TO_NAME), 
                         new QName(uri, Names.WSA_REPLYTO_NAME),
                         new QName(uri, Names.WSA_FAULTTO_NAME),
                         new QName(uri, Names.WSA_RELATESTO_NAME),
                         new QName(uri, Names.WSA_ACTION_NAME)};
        if (exposeAsNative) {
            expectedValues = new Object[] {id, to, replyTo, faultTo, relatesTo, action};
            expectedDeclaredTypes = 
                new Class<?>[] {AttributedURIType.class,
                                AttributedURIType.class,
                                EndpointReferenceType.class,
                                EndpointReferenceType.class,
                                RelatesToType.class, 
                                AttributedURIType.class};
        } else if (exposeAs200408) {
            expectedValues = new Object[] {VersionTransformer.convert(id),
                                           VersionTransformer.convert(to),
                                           VersionTransformer.convert(replyTo),
                                           VersionTransformer.convert(faultTo),
                                           VersionTransformer.convert(relatesTo),
                                           VersionTransformer.convert(action)};
            if (!outbound) {
                // conversion from 2004/08 to 2005/08 anonymous address
                // occurs transparently in VersionTransformer
                VersionTransformer.Names200408.EPR_TYPE.cast(expectedValues[2]).
                    getAddress().setValue(Names.WSA_ANONYMOUS_ADDRESS);
                VersionTransformer.Names200408.EPR_TYPE.cast(expectedValues[3]).
                    getAddress().setValue(Names.WSA_ANONYMOUS_ADDRESS);    
            }
            expectedDeclaredTypes = 
                new Class<?>[] {AttributedURI.class,
                                AttributedURI.class,
                                VersionTransformer.Names200408.EPR_TYPE,
                                VersionTransformer.Names200408.EPR_TYPE,
                                Relationship.class, 
                                AttributedURI.class};
        } else {
            fail("unexpected namespace URI: " + uri);
        }
        return maps;
    }
    
    private final class JAXBEltMatcher implements IArgumentMatcher {
        public boolean matches(Object obj) {
            QName name = expectedNames[expectedIndex];
            Class<?> declaredType = expectedDeclaredTypes[expectedIndex];
            Object value = expectedValues[expectedIndex];
            boolean ret = false;
            expectedIndex++;
            if (expectedIndex == 5 && !expectRelatesTo) {
                return true;
            }
            if (obj instanceof JAXBElement) {
                JAXBElement other = (JAXBElement)obj;
                ret = name.equals(other.getName()) 
                      && declaredType.isAssignableFrom(other.getDeclaredType())
                      && compare(value, other.getValue());
            }
            return ret;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("JAXBElements did not match");
        }
        
        private boolean compare(Object a, Object b) {
            boolean ret = false;
            if (a instanceof AttributedURI && b instanceof AttributedURI) {
                ret = ((AttributedURI)a).getValue().equals(((AttributedURI)b).getValue());
            } else if (a instanceof AttributedURIType && b instanceof AttributedURIType) {
                ret = ((AttributedURIType)a).getValue().equals(((AttributedURIType)b).getValue());
            } else if (a instanceof EndpointReferenceType && b instanceof EndpointReferenceType) {
                EndpointReferenceType aEPR = (EndpointReferenceType)a;
                EndpointReferenceType bEPR = (EndpointReferenceType)b;
                ret = aEPR.getAddress() != null
                      && bEPR.getAddress() != null
                      && aEPR.getAddress().getValue().equals(bEPR.getAddress().getValue());
            } else if (VersionTransformer.Names200408.EPR_TYPE.isInstance(a)
                       && VersionTransformer.Names200408.EPR_TYPE.isInstance(b)) {
                ret = VersionTransformer.Names200408.EPR_TYPE.cast(a).getAddress() != null
                      && VersionTransformer.Names200408.EPR_TYPE.cast(b).getAddress() != null
                      && VersionTransformer.Names200408.EPR_TYPE.cast(a).getAddress().getValue().equals(
                             VersionTransformer.Names200408.EPR_TYPE.cast(b).getAddress().getValue());
            } else if (a instanceof Relationship && b instanceof Relationship) {
                ret = ((Relationship)a).getValue().equals(((Relationship)b).getValue());
            } else if (a instanceof RelatesToType && b instanceof RelatesToType) {
                ret = ((RelatesToType)a).getValue().equals(((RelatesToType)b).getValue());
            } 
            return ret;
        }
    }

    private boolean verifyMAPs(Object obj) {
        if (obj instanceof AddressingPropertiesImpl) {
            AddressingPropertiesImpl other = (AddressingPropertiesImpl)obj;
            return compareExpected(other);
        } 
        return false;
    }    

    private boolean compareExpected(AddressingPropertiesImpl other) {
        boolean ret = false;
        String uri = other.getNamespaceURI();
        boolean exposedAsNative = Names.WSA_NAMESPACE_NAME.equals(uri);
        boolean exposedAs200408 = 
            VersionTransformer.Names200408.WSA_NAMESPACE_NAME.equals(uri);
        if (exposedAsNative || exposedAs200408) {
            String expectedMessageID = 
                exposedAsNative
                ? ((AttributedURIType)expectedValues[0]).getValue()
                : ((AttributedURI)expectedValues[0]).getValue();
            String expectedTo =
                exposedAsNative
                ? ((AttributedURIType)expectedValues[1]).getValue()
                : ((AttributedURI)expectedValues[1]).getValue();
            String expectedReplyTo = 
                exposedAsNative
                ? ((EndpointReferenceType)expectedValues[2]).getAddress().getValue()
                : (VersionTransformer.Names200408.EPR_TYPE.cast(expectedValues[2])).
                    getAddress().getValue();
            String expectedAction =                    
                exposedAsNative
                ? ((AttributedURIType)expectedValues[5]).getValue()
                : ((AttributedURI)expectedValues[5]).getValue();

            ret = expectedMessageID.equals(other.getMessageID().getValue())
                  && expectedTo.equals(other.getTo().getValue())
                  && expectedReplyTo.equals(
                        other.getReplyTo().getAddress().getValue())
                  && expectedAction.equals(other.getAction().getValue())
                  && expectedNamespaceURI.equals(other.getNamespaceURI());
            if (expectRelatesTo) {
                String expectedRelatesTo = 
                    exposedAsNative
                    ? ((RelatesToType)expectedValues[4]).getValue()
                    : ((Relationship)expectedValues[4]).getValue();
                ret = ret
                      && expectedRelatesTo.equals(other.getRelatesTo().getValue());
            }
        } 
        return ret;
    }
        
    private void verifyAction() {
        List<?> soapAction = (List<?>)mimeHeaders.get("SOAPAction");
        assertNotNull("expected propogated action", soapAction);
        assertEquals("expected single action", 1, soapAction.size());
        String expectedAction =
            "\"" + ((AttributedURIType)expectedValues[5]).getValue() + "\"";
        assertEquals("expected propogated action", 
                     expectedAction,
                     soapAction.get(0));
    }

    private void verifyMessage(SoapMessage message,
                               boolean requestor,
                               boolean outbound,
                               boolean exposedAsNative) {
        if (requestor) {
            if (outbound) {
                String id = expectedValues[0] instanceof AttributedURIType
                            ? ((AttributedURIType)expectedValues[0]).getValue()
                            : ((AttributedURI)expectedValues[0]).getValue();
                //assertTrue("expected correlationID : " + id + " in map: " + codec.uncorrelatedExchanges,
                //           codec.uncorrelatedExchanges.containsKey(id));
                assertSame("unexpected correlated exchange",
                           codec.uncorrelatedExchanges.get(id),
                           message.getExchange());
            } else {
                assertSame("unexpected correlated exchange",
                           correlatedExchange,
                           message.getExchange());
            }
        }
        assertTrue("unexpected MAPs",
                   verifyMAPs(message.get(getMAPProperty(requestor, outbound))));
        
    }
}


