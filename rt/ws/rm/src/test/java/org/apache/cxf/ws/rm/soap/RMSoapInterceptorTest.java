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

package org.apache.cxf.ws.rm.soap;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.ws.rm.AckRequestedType;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.RMConstants;
import org.apache.cxf.ws.rm.RMContextUtils;
import org.apache.cxf.ws.rm.RMProperties;
import org.apache.cxf.ws.rm.SequenceAcknowledgement;
import org.apache.cxf.ws.rm.SequenceType;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class RMSoapInterceptorTest extends TestCase {

    private static final String SEQ_IDENTIFIER = "http://Business456.com/RM/ABC";
    private static final BigInteger MSG1_MESSAGE_NUMBER = BigInteger.ONE;
    private static final BigInteger MSG2_MESSAGE_NUMBER = BigInteger.ONE.add(BigInteger.ONE);

    private IMocksControl control;
    
    private SequenceType s1;
    private SequenceType s2;
    private SequenceAcknowledgement ack1;
    private SequenceAcknowledgement ack2;
    private AckRequestedType ar1;
    private AckRequestedType ar2;
    
    public void setUp() {
        control = EasyMock.createNiceControl(); 
    }

    public void testGetUnderstoodHeaders() throws Exception {
        RMSoapInterceptor codec = new RMSoapInterceptor();
        Set<QName> headers = codec.getUnderstoodHeaders();
        assertTrue("expected Sequence header", 
                   headers.contains(RMConstants.WSRM_SEQUENCE_QNAME));
        assertTrue("expected SequenceAcknowledgment header", 
                   headers.contains(RMConstants.WSRM_SEQUENCE_ACK_QNAME));
        assertTrue("expected AckRequested header", 
                   headers.contains(RMConstants.WSRM_ACK_REQUESTED_QNAME));
    }
    
    public void testHandleMessage() throws NoSuchMethodException {
        Method m = RMSoapInterceptor.class.getDeclaredMethod("mediate", 
            new Class[] {SoapMessage.class});
        RMSoapInterceptor codec = control.createMock(RMSoapInterceptor.class, new Method[] {m});
        SoapMessage msg = control.createMock(SoapMessage.class);
        codec.mediate(msg);
        EasyMock.expectLastCall();
        
        control.replay();
        codec.handleMessage(msg);
        control.verify();
    }
    
    public void testHandleFault() throws NoSuchMethodException {
        Method m = RMSoapInterceptor.class.getDeclaredMethod("mediate", 
            new Class[] {SoapMessage.class});
        RMSoapInterceptor codec = control.createMock(RMSoapInterceptor.class, new Method[] {m});
        SoapMessage msg = control.createMock(SoapMessage.class);
        codec.mediate(msg);
        EasyMock.expectLastCall();
        
        control.replay();
        codec.handleFault(msg);
        control.verify();
    }
    
    public void testMediate() throws NoSuchMethodException {
        Method m1 = RMSoapInterceptor.class.getDeclaredMethod("encode", 
                                                             new Class[] {SoapMessage.class});
        Method m2 = RMSoapInterceptor.class.getDeclaredMethod("decode", 
                                                              new Class[] {SoapMessage.class});
        RMSoapInterceptor codec = control.createMock(RMSoapInterceptor.class, new Method[] {m1, m2});
        
        SoapMessage msg = control.createMock(SoapMessage.class);
        Exchange exchange = control.createMock(Exchange.class);
        EasyMock.expect(msg.getExchange()).andReturn(exchange);
        EasyMock.expect(exchange.getOutMessage()).andReturn(msg);
        codec.encode(msg);
        EasyMock.expectLastCall();
        
        control.replay();
        codec.mediate(msg);
        control.verify();
                
        control.reset();
        EasyMock.expect(msg.getExchange()).andReturn(null);
        codec.decode(msg);
        EasyMock.expectLastCall();
        
        control.replay();
        codec.mediate(msg);
        control.verify();
        
    }

    public void testEncode() throws Exception {
        RMSoapInterceptor codec = new RMSoapInterceptor();
        setUpOutbound();
        SoapMessage message = setupOutboundMessage();

        // no RM headers
   
        codec.handleMessage(message);
        verifyHeaders(message, new String[] {});

        // one sequence header

        message = setupOutboundMessage();        
        RMProperties rmps = RMContextUtils.retrieveRMProperties(message, true);     
        rmps.setSequence(s1);        
        codec.encode(message);
        verifyHeaders(message, new String[] {RMConstants.WSRM_SEQUENCE_NAME});

        // one acknowledgment header

        message = setupOutboundMessage(); 
        rmps = RMContextUtils.retrieveRMProperties(message, true);          
        Collection<SequenceAcknowledgement> acks = new ArrayList<SequenceAcknowledgement>();
        acks.add(ack1);
        rmps.setAcks(acks);        
        codec.encode(message);
        verifyHeaders(message, new String[] {RMConstants.WSRM_SEQUENCE_ACK_NAME});

        // two acknowledgment headers

        message = setupOutboundMessage();
        rmps = RMContextUtils.retrieveRMProperties(message, true);        
        acks.add(ack2);
        rmps.setAcks(acks);
        codec.encode(message);
        verifyHeaders(message, new String[] {RMConstants.WSRM_SEQUENCE_ACK_NAME, 
                                             RMConstants.WSRM_SEQUENCE_ACK_NAME});

        // one ack requested header

        message = setupOutboundMessage();
        rmps = RMContextUtils.retrieveRMProperties(message, true);        
        Collection<AckRequestedType> requested = new ArrayList<AckRequestedType>();
        requested.add(ar1);
        rmps.setAcksRequested(requested);
        codec.encode(message);
        verifyHeaders(message, new String[] {RMConstants.WSRM_ACK_REQUESTED_NAME});

        // two ack requested headers

        message = setupOutboundMessage();
        rmps = RMContextUtils.retrieveRMProperties(message, true);         
        requested.add(ar2);
        rmps.setAcksRequested(requested);
        codec.encode(message);
        verifyHeaders(message, new String[] {RMConstants.WSRM_ACK_REQUESTED_NAME, 
                                             RMConstants.WSRM_ACK_REQUESTED_NAME});
    }

    public void testDecodeSequence() throws XMLStreamException {
        SoapMessage message = setUpInboundMessage("resources/Message1.xml");
        RMSoapInterceptor codec = new RMSoapInterceptor();
        codec.handleMessage(message);
        RMProperties rmps = RMContextUtils.retrieveRMProperties(message, false);
        SequenceType st = rmps.getSequence();
        assertNotNull(st);
        assertEquals(st.getIdentifier().getValue(), SEQ_IDENTIFIER);
        assertEquals(st.getMessageNumber(), MSG1_MESSAGE_NUMBER);
        
        assertNull(rmps.getAcks());
        assertNull(rmps.getAcksRequested());

    }

    public void testDecodeAcknowledgements() throws XMLStreamException {
        SoapMessage message = setUpInboundMessage("resources/Acknowledgment.xml");
        RMSoapInterceptor codec = new RMSoapInterceptor();
        codec.handleMessage(message);
        RMProperties rmps = RMContextUtils.retrieveRMProperties(message, false);
        Collection<SequenceAcknowledgement> acks = rmps.getAcks();
        assertNotNull(acks);
        assertEquals(1, acks.size());
        SequenceAcknowledgement ack = acks.iterator().next();
        assertNotNull(ack);
        assertEquals(ack.getIdentifier().getValue(), SEQ_IDENTIFIER);
        assertEquals(2, ack.getAcknowledgementRange().size());
        assertNull(rmps.getSequence());
        assertNull(rmps.getAcksRequested());
    }

    public void testDecodeAcksRequested() throws XMLStreamException {
        SoapMessage message = setUpInboundMessage("resources/Retransmission.xml");
        RMSoapInterceptor codec = new RMSoapInterceptor();
        codec.handleMessage(message);
        RMProperties rmps = RMContextUtils.retrieveRMProperties(message, false);
        Collection<AckRequestedType> requested = rmps.getAcksRequested();
        assertNotNull(requested);
        assertEquals(1, requested.size());
        AckRequestedType ar = requested.iterator().next();
        assertNotNull(ar);
        assertEquals(ar.getIdentifier().getValue(), SEQ_IDENTIFIER);

        SequenceType s = rmps.getSequence();
        assertNotNull(s);
        assertEquals(s.getIdentifier().getValue(), SEQ_IDENTIFIER);
        assertEquals(s.getMessageNumber(), MSG2_MESSAGE_NUMBER);

        assertNull(rmps.getAcks());
    }

    private void setUpOutbound() {
        org.apache.cxf.ws.rm.ObjectFactory factory = new org.apache.cxf.ws.rm.ObjectFactory();
        s1 = factory.createSequenceType();
        Identifier sid = factory.createIdentifier();
        sid.setValue("sequence1");
        s1.setIdentifier(sid);
        s1.setMessageNumber(BigInteger.ONE);
        s2 = factory.createSequenceType();
        sid = factory.createIdentifier();
        sid.setValue("sequence2");
        s2.setIdentifier(sid);
        s2.setMessageNumber(BigInteger.TEN);

        ack1 = factory.createSequenceAcknowledgement();
        SequenceAcknowledgement.AcknowledgementRange r = 
            factory.createSequenceAcknowledgementAcknowledgementRange();
        r.setLower(BigInteger.ONE);
        r.setUpper(BigInteger.ONE);
        ack1.getAcknowledgementRange().add(r);
        ack1.setIdentifier(s1.getIdentifier());

        ack2 = factory.createSequenceAcknowledgement();
        r = factory.createSequenceAcknowledgementAcknowledgementRange();
        r.setLower(BigInteger.ONE);
        r.setUpper(BigInteger.TEN);
        ack2.getAcknowledgementRange().add(r);
        ack2.setIdentifier(s2.getIdentifier());

        ar1 = factory.createAckRequestedType();
        ar1.setIdentifier(s1.getIdentifier());

        ar2 = factory.createAckRequestedType();
        ar2.setIdentifier(s2.getIdentifier());
    }

    private SoapMessage setupOutboundMessage() throws Exception {
        Message message = new MessageImpl();
        SoapMessage soapMessage = new SoapMessage(message);         
        RMProperties rmps = new RMProperties();
        RMContextUtils.storeRMProperties(soapMessage, rmps, true);
        
        return soapMessage;
    }

    private void verifyHeaders(SoapMessage message, String... names) {
        Element header = message.getHeaders(Element.class);

        // check all expected headers are present

        for (String name : names) {
            boolean found = false;
            NodeList headerElements = header.getChildNodes();
            for (int i = 0; i < headerElements.getLength(); i++) {
                Element headerElement = (Element)headerElements.item(i);
                String namespace = headerElement.getNamespaceURI();
                String localName = headerElement.getLocalName();
                if (RMConstants.WSRM_NAMESPACE_NAME.equals(namespace)
                    && localName.equals(name)) {
                    found = true;
                    break;
                } else if (RMConstants.WSA_NAMESPACE_NAME.equals(namespace)
                    && localName.equals(name)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Could not find header element " + name, found);
        }

        // no other headers should be present

        NodeList headerElements = header.getChildNodes();
        for (int i = 0; i < headerElements.getLength(); i++) {
            Element headerElement = (Element)headerElements.item(i);  
            String namespace = headerElement.getNamespaceURI();
            String localName = headerElement.getLocalName();
            assertTrue(RMConstants.WSRM_NAMESPACE_NAME.equals(namespace) 
                || RMConstants.WSA_NAMESPACE_NAME.equals(namespace));
            boolean found = false;
            for (String name : names) {
                if (localName.equals(name)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Unexpected header element " + localName, found);
        }
    }
    
    private SoapMessage setUpInboundMessage(String resource) throws XMLStreamException {
        Message message = new MessageImpl();
        SoapMessage soapMessage = new SoapMessage(message);
        InputStream is = RMSoapInterceptorTest.class.getResourceAsStream(resource);
        assertNotNull(is);
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
        soapMessage.setContent(XMLStreamReader.class, reader);
        ReadHeadersInterceptor rji = new ReadHeadersInterceptor();
        rji.handleMessage(soapMessage); 
        return soapMessage;
    }
}
