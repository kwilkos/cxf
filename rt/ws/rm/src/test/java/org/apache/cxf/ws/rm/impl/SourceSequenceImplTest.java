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

package org.apache.cxf.ws.rm.impl;

import java.math.BigInteger;
import java.util.Date;

import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.jaxb.DatatypeFactory;
import org.apache.cxf.ws.rm.Expires;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.ObjectFactory;
import org.apache.cxf.ws.rm.SequenceAcknowledgement;
import org.apache.cxf.ws.rm.SequenceFault;
import org.apache.cxf.ws.rm.interceptor.SequenceTerminationPolicyType;
import org.apache.cxf.ws.rm.interceptor.SourcePolicyType;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class SourceSequenceImplTest extends TestCase {

    private IMocksControl control;
    private ObjectFactory factory;
    private Identifier id;

    private Source source;
    private RMInterceptor interceptor;
    private SourcePolicyType sp;
    private SequenceTerminationPolicyType stp;
   
 
    public void setUp() {        
        factory = new ObjectFactory();
        id = factory.createIdentifier();
        id.setValue("seq");
        
        control = EasyMock.createNiceControl();
    }
    
    public void tearDown() {
        source = null;
        interceptor = null;
        sp = null;
        stp = null;
    }
    
    void setUpSource() {
        source = control.createMock(Source.class); 
        interceptor = control.createMock(RMInterceptor.class);
        EasyMock.expect(source.getInterceptor()).andReturn(interceptor).anyTimes();

        // default termination policy
        
        org.apache.cxf.ws.rm.interceptor.ObjectFactory cfgFactory = 
            new org.apache.cxf.ws.rm.interceptor.ObjectFactory();
        sp = cfgFactory.createSourcePolicyType();
        stp = cfgFactory
            .createSequenceTerminationPolicyType(); 
        sp.setSequenceTerminationPolicy(stp);
        EasyMock.expect(interceptor.getSourcePolicy()).andReturn(sp).anyTimes();
    }
    

    public void testConstructors() {

        Identifier otherId = factory.createIdentifier();
        otherId.setValue("otherSeq");
       
        SourceSequenceImpl seq = null;
        
        seq = new SourceSequenceImpl(id);
        assertEquals(id, seq.getIdentifier());
        assertTrue(!seq.isLastMessage());
        assertTrue(!seq.isExpired());
        assertEquals(BigInteger.ZERO, seq.getCurrentMessageNr());
        assertNotNull(seq.getAcknowledgement());
        assertEquals(0, seq.getAcknowledgement().getAcknowledgementRange().size());
        assertTrue(!seq.allAcknowledged());
        assertFalse(seq.offeredBy(otherId));
        
        Date expiry = new Date(System.currentTimeMillis() + 3600 * 1000);
        
        seq = new SourceSequenceImpl(id, expiry, null);
        assertEquals(id, seq.getIdentifier());
        assertTrue(!seq.isLastMessage());
        assertTrue(!seq.isExpired());
        assertEquals(BigInteger.ZERO, seq.getCurrentMessageNr());
        assertNotNull(seq.getAcknowledgement());
        assertEquals(0, seq.getAcknowledgement().getAcknowledgementRange().size());
        assertTrue(!seq.allAcknowledged());
        assertFalse(seq.offeredBy(otherId));
        
        seq = new SourceSequenceImpl(id, expiry, otherId);
        assertTrue(seq.offeredBy(otherId));
        assertFalse(seq.offeredBy(id));
    }
    
    public void testSetExpires() {
        SourceSequenceImpl seq = new SourceSequenceImpl(id);
        
        Expires expires = factory.createExpires();
        seq.setExpires(expires);
            
        assertTrue(!seq.isExpired());
        
        Duration d = DatatypeFactory.PT0S;          
        expires.setValue(d); 
        seq.setExpires(expires);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            assertTrue(!seq.isExpired());
        }
        
        d = DatatypeFactory.createDuration("PT1S");        
        expires.setValue(d);
        seq.setExpires(expires);
        assertTrue(!seq.isExpired());
        
        d = DatatypeFactory.createDuration("-PT1S");  
        expires.setValue(d);
        seq.setExpires(expires);
        assertTrue(seq.isExpired());   
    }

    public void testEqualsAndHashCode() {
        SourceSequenceImpl seq = new SourceSequenceImpl(id);
        SourceSequenceImpl otherSeq = null;
        assertTrue(!seq.equals(otherSeq));
        otherSeq = new SourceSequenceImpl(id);
        assertEquals(seq, otherSeq);
        assertEquals(seq.hashCode(), otherSeq.hashCode());
        Identifier otherId = factory.createIdentifier();
        otherId.setValue("otherSeq");
        otherSeq = new SourceSequenceImpl(otherId);
        assertTrue(!seq.equals(otherSeq));
        assertTrue(seq.hashCode() != otherSeq.hashCode()); 
        assertTrue(!seq.equals(this));
    }
    

    public void testSetAcknowledged() {
        SourceSequenceImpl seq = new SourceSequenceImpl(id);
        SequenceAcknowledgement ack = seq.getAcknowledgement();
        ack = factory.createSequenceAcknowledgement();
        SequenceAcknowledgement.AcknowledgementRange r = 
            factory.createSequenceAcknowledgementAcknowledgementRange();
        r.setLower(new BigInteger("1"));
        r.setUpper(new BigInteger("2"));
        ack.getAcknowledgementRange().add(r);
        r = factory.createSequenceAcknowledgementAcknowledgementRange();
        r.setLower(new BigInteger("4"));
        r.setUpper(new BigInteger("6"));
        ack.getAcknowledgementRange().add(r);
        r = factory.createSequenceAcknowledgementAcknowledgementRange();
        r.setLower(new BigInteger("8"));
        r.setUpper(new BigInteger("10"));
        ack.getAcknowledgementRange().add(r);
        seq.setAcknowledged(ack);
        assertSame(ack, seq.getAcknowledgement());
        assertEquals(3, ack.getAcknowledgementRange().size());
        assertTrue(!seq.isAcknowledged(new BigInteger("3")));  
        assertTrue(seq.isAcknowledged(new BigInteger("5")));
    } 

    public void testAllAcknowledged() throws SequenceFault {
        
        SourceSequenceImpl seq = new SourceSequenceImpl(id, null, null, new BigInteger("4"), false);        
        
        assertTrue(!seq.allAcknowledged());
        seq.setLastMessage(true);
        assertTrue(!seq.allAcknowledged());
        SequenceAcknowledgement ack = factory.createSequenceAcknowledgement();
        SequenceAcknowledgement.AcknowledgementRange r = 
            factory.createSequenceAcknowledgementAcknowledgementRange();
        r.setLower(BigInteger.ONE);
        r.setUpper(new BigInteger("2"));
        ack.getAcknowledgementRange().add(r);
        seq.setAcknowledged(ack);
        assertTrue(!seq.allAcknowledged());
        r.setUpper(new BigInteger("4"));
        assertTrue(seq.allAcknowledged());
    }
    
    public void testNextMessageNumber() {     
        SourceSequenceImpl seq = null;        
        setUpSource();
        control.replay();
        
        // default termination policy

        seq = new SourceSequenceImpl(id);  
        seq.setSource(source);
        assertTrue(!nextMessages(seq, 10));
        control.verify();
        
        // termination policy max length = 1
        
        seq = new SourceSequenceImpl(id); 
        seq.setSource(source);
        stp.setMaxLength(BigInteger.ONE);
        assertTrue(nextMessages(seq, 10));
        assertEquals(BigInteger.ONE, seq.getCurrentMessageNr());
        control.verify();
        
        // termination policy max length = 5
        seq = new SourceSequenceImpl(id); 
        seq.setSource(source);
        stp.setMaxLength(new BigInteger("5"));
        assertTrue(!nextMessages(seq, 2));
        control.verify();
        
        // termination policy max range exceeded
        
        seq = new SourceSequenceImpl(id); 
        seq.setSource(source);
        stp.setMaxLength(null);
        stp.setMaxRanges(new Integer(3));
        acknowledge(seq, 1, 2, 4, 5, 6, 8, 9, 10);
        assertTrue(nextMessages(seq, 10));
        assertEquals(BigInteger.ONE, seq.getCurrentMessageNr());
        control.verify();
        
        // termination policy max range not exceeded
        
        seq = new SourceSequenceImpl(id); 
        seq.setSource(source);
        stp.setMaxLength(null);
        stp.setMaxRanges(new Integer(4));
        acknowledge(seq, 1, 2, 4, 5, 6, 8, 9, 10);
        assertTrue(!nextMessages(seq, 10));
        control.verify();
        
        // termination policy max unacknowledged 
    }
    
    public void testGetEndpointIdentfier() {
        setUpSource();
        QName qn = new QName("abc", "xyz");
        EasyMock.expect(source.getName()).andReturn(qn);
        control.replay();
        
        SourceSequenceImpl seq = new SourceSequenceImpl(id);
        seq.setSource(source);
        assertEquals("Unexpected endpoint identifier", "{abc}xyz", seq.getEndpointIdentifier());
        control.verify();
    }
    
    public void testCheckOfferingSequenceClosed() {
        SourceSequenceImpl seq = null;
        
        setUpSource();
 
        RMEndpoint rme = control.createMock(RMEndpoint.class);
        EasyMock.expect(source.getReliableEndpoint()).andReturn(rme).anyTimes();
        Destination destination = control.createMock(Destination.class);
        EasyMock.expect(rme.getDestination()).andReturn(destination).anyTimes();
        DestinationSequenceImpl dseq = control.createMock(DestinationSequenceImpl.class); 
        Identifier did = control.createMock(Identifier.class);
        EasyMock.expect(destination.getSequenceImpl(did)).andReturn(dseq).anyTimes();
        EasyMock.expect(dseq.getLastMessageNumber()).andReturn(BigInteger.ONE).anyTimes();
        EasyMock.expect(did.getValue()).andReturn("dseq").anyTimes();
        
        control.replay();
        
        seq = new SourceSequenceImpl(id, null, did);  
        seq.setSource(source);        
        seq.nextMessageNumber(did, BigInteger.ONE);
        assertTrue(seq.isLastMessage());
        
        control.verify();
    }
   
    private boolean nextMessages(SourceSequenceImpl seq, 
                                 int n) {
        int i = 0;
        while ((i < n) && !seq.isLastMessage()) {            
            assertNotNull(seq.nextMessageNumber());
            i++;
        }
        return seq.isLastMessage();
    }
    
    protected void acknowledge(SourceSequenceImpl seq, int... messageNumbers) {
        SequenceAcknowledgement ack = factory.createSequenceAcknowledgement();
        int i = 0;
        while (i < messageNumbers.length) {
            SequenceAcknowledgement.AcknowledgementRange r = 
                factory.createSequenceAcknowledgementAcknowledgementRange();
            Integer li = new Integer(messageNumbers[i]);
            BigInteger l = new BigInteger(li.toString());
            r.setLower(l);
            i++;
            
            while (i < messageNumbers.length && (messageNumbers[i] - messageNumbers[i - 1]) == 1) {
                i++;
            }
            Integer ui = new Integer(messageNumbers[i - 1]);
            BigInteger u = new BigInteger(ui.toString());
            r.setUpper(u);
            ack.getAcknowledgementRange().add(r);
        }
        seq.setAcknowledged(ack);
    }
}
