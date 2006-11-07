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

package org.apache.cxf.systest.ws.rm;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import junit.framework.Assert;

import org.apache.cxf.ws.rm.RMConstants;



public class MessageFlow extends Assert {
    
    private List<SOAPMessage> outboundMessages;
    private List<SOAPMessage> inboundMessages;
    
    
    public MessageFlow(List<SOAPMessage> o, List<SOAPMessage> i) {
        outboundMessages = o;
        inboundMessages = i;
    }
    
    public List<SOAPMessage> getOutboundMessages() {
        return outboundMessages;
    }
    
    public List<SOAPMessage> getInboundMessages() {
        return inboundMessages;
    }
    
    public void verifyActions(String[] expectedActions, boolean outbound) throws Exception {

        assertEquals(expectedActions.length, outbound ? outboundMessages.size() : inboundMessages.size());

        for (int i = 0; i < expectedActions.length; i++) {
            String action = outbound ? getAction(outboundMessages.get(i)) : getAction(inboundMessages.get(i));
            if (null == expectedActions[i]) {
                assertNull((outbound ? "Outbound " : "Inbound") + " message " + i
                           + " has unexpected action: " + action, action);
            } else {
                assertEquals((outbound ? "Outbound " : "Inbound") + " message " + i
                             + " does not contain expected action header"
                             + System.getProperty("line.separator"), expectedActions[i], action);
            }
        }
    }
    
    public boolean checkActions(String[] expectedActions, boolean outbound) throws Exception {

        if (expectedActions.length != (outbound ? outboundMessages.size() : inboundMessages.size())) {
            return false;
        }

        for (int i = 0; i < expectedActions.length; i++) {
            String action = outbound ? getAction(outboundMessages.get(i)) : getAction(inboundMessages.get(i));
            if (null == expectedActions[i]) {
                if (action != null) {
                    return false;
                }
            } else {
                if (!expectedActions[i].equals(action)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void verifyAction(String expectedAction,
                             int expectedCount,
                             boolean outbound,
                             boolean exact) throws Exception {
        int messageCount = outbound ? outboundMessages.size() : inboundMessages.size();
        int count = 0;
        for (int i = 0; i < messageCount; i++) {
            String action = outbound ? getAction(outboundMessages.get(i)) : getAction(inboundMessages.get(i));
            if (null == expectedAction) {
                if (action == null) {
                    count++;
                }
            } else {
                if (expectedAction.equals(action)) {
                    count++;                    
                }
            }
        }
        if (exact) {
            assertEquals("unexpected count for action: " + expectedAction,
                         expectedCount,
                         count);
        } else {
            assertTrue("unexpected count for action: " + expectedAction + ": " + count,
                       expectedCount <= count);
        }
        
    }

    public void verifyMessageNumbers(String[] expectedMessageNumbers, boolean outbound) throws Exception {
        verifyMessageNumbers(expectedMessageNumbers, outbound, true);
    }

    public void verifyMessageNumbers(String[] expectedMessageNumbers,
                                     boolean outbound,
                                     boolean exact) throws Exception {

        int actualMessageCount = 
            outbound ? outboundMessages.size() : inboundMessages.size();
        if (exact) {
            assertEquals(expectedMessageNumbers.length, actualMessageCount);
        } else {
            assertTrue(expectedMessageNumbers.length <= actualMessageCount);
        }

        if (exact) {
            for (int i = 0; i < expectedMessageNumbers.length; i++) {
                SOAPElement se = outbound ? getSequence(outboundMessages.get(i))
                    : getSequence(inboundMessages.get(i));
                if (null == expectedMessageNumbers[i]) {
                    assertNull((outbound ? "Outbound" : "Inbound") + " message " + i
                        + " contains unexpected message number ", se);
                } else {
                    assertEquals((outbound ? "Outbound" : "Inbound") + " message " + i
                        + " does not contain expected message number "
                                 + expectedMessageNumbers[i], expectedMessageNumbers[i], 
                                 getMessageNumber(se));
                }
            }
        } else {
            boolean[] matches = new boolean[expectedMessageNumbers.length];
            for (int i = 0; i < actualMessageCount; i++) {
                String messageNumber = null;
                SOAPElement se = outbound ? getSequence(outboundMessages.get(i))
                    : getSequence(inboundMessages.get(i));
                messageNumber = null == se ? null : getMessageNumber(se);
                for (int j = 0; j < expectedMessageNumbers.length; j++) {
                    if (messageNumber == null) {
                        if (expectedMessageNumbers[j] == null && !matches[j]) {
                            matches[j] = true;
                            break;
                        }
                    } else {
                        if (messageNumber.equals(expectedMessageNumbers[j]) && !matches[j]) {
                            matches[j] = true;
                            break;                                
                        }
                    }
                }
            }
            for (int k = 0; k < expectedMessageNumbers.length; k++) {
                assertTrue("no match for message number: " + expectedMessageNumbers[k],
                           matches[k]);
            }
        }
    }

    public void verifyLastMessage(boolean[] expectedLastMessages,
                                  boolean outbound) throws Exception {
        verifyLastMessage(expectedLastMessages, outbound, true);
    }

    public void verifyLastMessage(boolean[] expectedLastMessages,
                                  boolean outbound,
                                  boolean exact) throws Exception {
        
        int actualMessageCount =
            outbound ? outboundMessages.size() : inboundMessages.size();
        if (exact) {
            assertEquals(expectedLastMessages.length, actualMessageCount);
        } else {
            assertTrue(expectedLastMessages.length <= actualMessageCount);
        }
        
        for (int i = 0; i < expectedLastMessages.length; i++) { 
            boolean lastMessage;
            SOAPElement se = outbound ? getSequence(outboundMessages.get(i))
                : getSequence(inboundMessages.get(i));
            lastMessage = null == se ? false : getLastMessage(se);
            assertEquals("Outbound message " + i 
                         + (expectedLastMessages[i] ? " does not contain expected last message element."
                             : " contains last message element."),
                         expectedLastMessages[i], lastMessage);  
        
        }
    }
    
    public void verifyAcknowledgements(boolean[] expectedAcks, boolean outbound) throws Exception {
        assertEquals(expectedAcks.length, outbound ? outboundMessages.size()
            : inboundMessages.size());
        
        for (int i = 0; i < expectedAcks.length; i++) {
            boolean ack = outbound ? (null != getAcknowledgment(outboundMessages.get(i))) 
                : (null != getAcknowledgment(inboundMessages.get(i)));
            
            if (expectedAcks[i]) {
                assertTrue((outbound ? "Outbound" : "Inbound") + " message " + i 
                           + " does not contain expected acknowledgement", ack);
            } else {
                assertFalse((outbound ? "Outbound" : "Inbound") + " message " + i 
                           + " contains unexpected acknowledgement", ack);
            }
        }
    }
    
    public void verifyAcknowledgements(int expectedAcks,
                                       boolean outbound,
                                       boolean exact) throws Exception {
        
        int actualMessageCount = 
            outbound ? outboundMessages.size() : inboundMessages.size();
        int ackCount = 0;
        for (int i = 0; i < actualMessageCount; i++) {
            boolean ack = outbound ? (null != getAcknowledgment(outboundMessages.get(i))) 
                : (null != getAcknowledgment(inboundMessages.get(i)));
            if (ack) {
                ackCount++;
            }
        }
        if (exact) {
            assertEquals("unexpected number of acks", expectedAcks, ackCount);
        } else {
            assertTrue("unexpected number of acks: " + ackCount,
                       expectedAcks <= ackCount);
        }
        
        
    }


    public void verifyAckRequestedOutbound(boolean outbound) throws Exception {
        boolean found = false;
        List<SOAPMessage> messages = outbound ? outboundMessages : inboundMessages;
        for (SOAPMessage m : messages) {
            SOAPElement se = getAckRequested(m);
            if (se != null) {
                found = true;
                break;
            }
        }
        assertTrue("expected AckRequested", found);
    }
   
    protected String getAction(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            if ((headerName.getURI().equals(org.apache.cxf.ws.addressing.Names.WSA_NAMESPACE_NAME) 
                || headerName.getURI().equals(org.apache.cxf.ws.addressing.VersionTransformer
                                              .Names200408.WSA_NAMESPACE_NAME))
                && localName.equals(org.apache.cxf.ws.addressing.Names.WSA_ACTION_NAME)) {
                return headerElement.getTextContent();
            }
        }
        return null;
    }

    protected SOAPElement getSequence(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            if (headerName.getURI().equals(RMConstants.WSRM_NAMESPACE_NAME)
                && localName.equals(RMConstants.WSRM_SEQUENCE_NAME)) {
                return headerElement;
            }
        }
        return null;
    }

    public String getMessageNumber(SOAPElement elem) throws Exception {
        SOAPElement se = (SOAPElement)elem.getChildElements(
                                                            new QName(RMConstants.WSRM_NAMESPACE_NAME,
                                                                      "MessageNumber")).next();
        return se.getTextContent();
    }

    private boolean getLastMessage(SOAPElement elem) throws Exception {
        return elem.getChildElements(new QName(RMConstants.WSRM_NAMESPACE_NAME, "LastMessage")).hasNext();
    }

    protected SOAPElement getAcknowledgment(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            if (headerName.getURI().equals(RMConstants.WSRM_NAMESPACE_NAME)
                && localName.equals(RMConstants.WSRM_SEQUENCE_ACK_NAME)) {
                return (SOAPElement)header.getChildElements().next();
            }
        }
        return null;
    }
    
    private SOAPElement getAckRequested(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            if (headerName.getURI().equals(RMConstants.WSRM_NAMESPACE_NAME)
                && localName.equals(RMConstants.WSRM_ACK_REQUESTED_NAME)) {
                return (SOAPElement)header.getChildElements().next();
            }
        }
        return null;
    }
    
    public void verifyMessages(int nExpected, boolean outbound) {
        verifyMessages(nExpected, outbound, true);
    }
    
    public void verifyMessages(int nExpected, boolean outbound, boolean exact) {
        if (outbound) {
            if (exact) {
                assertEquals("Unexpected number of outbound messages" + outboundDump(),
                             nExpected, outboundMessages.size());
            } else {
                assertTrue("Unexpected number of outbound messages: " + outboundDump(),
                           nExpected <= outboundMessages.size());
            }
        } else {
            if (exact) {
                assertEquals("Unexpected number of inbound messages", nExpected, inboundMessages.size());
            } else {
                assertTrue("Unexpected number of inbound messages: " + inboundMessages.size(),
                           nExpected <= inboundMessages.size());                
            }
        }
    }
    
    public void verifyMessages(int nExpected, boolean outbound, int interval, int attempts) {
        verifyMessages(nExpected, outbound, interval, attempts, false);
    }
    
    public void verifyMessages(int nExpected, boolean outbound, int interval, int attempts, boolean exact) {
        for (int i = 0; i < attempts; i++) {
            if ((outbound && outboundMessages.size() < nExpected)
                || (!outbound && inboundMessages.size() < nExpected)) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    // ignore
                }
            } else {
                break;
            }
        }
        verifyMessages(nExpected, outbound, exact);
    }
    
    public void purgePartialResponses() throws Exception {
        for (int i = inboundMessages.size() - 1; i >= 0; i--) {
            if (isPartialResponse(inboundMessages.get(i))) {
                inboundMessages.remove(i);
            }
        }
    }
    
    public void verifyPartialResponses(int nExpected) throws Exception {
        int npr = 0;
        for (int i =  0; i < inboundMessages.size(); i++) {
            if (isPartialResponse(inboundMessages.get(i))) {
                npr++;   
            }
        }
        assertEquals("Inbound messages did not contain expected number of partial responses.",
                     nExpected, npr);
    }
    
    public boolean isPartialResponse(SOAPMessage msg) throws Exception {
        // return null == getAction(ctx) && emptyBody(msg);
        return false;
    }
    
    public boolean emptyBody(SOAPMessage msg) throws Exception {
        return !msg.getSOAPPart().getEnvelope().getBody().hasChildNodes();
    }
    
    private String outboundDump() {
        StringBuffer buf = new StringBuffer();
        try {
            buf.append(System.getProperty("line.separator"));
            for (int i = 0; i < outboundMessages.size(); i++) {
                SOAPMessage sm = outboundMessages.get(i);
                buf.append("[");
                buf.append(i);
                buf.append("] : ");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                sm.writeTo(bos);
                buf.append(bos.toString());
                buf.append(System.getProperty("line.separator"));
            }
        } catch (Exception ex) {
            return "";
        }
        
        return buf.toString();
    }
    
}
