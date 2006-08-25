package org.objectweb.celtix.systest.ws.rm;

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
import javax.xml.ws.handler.LogicalMessageContext;

import junit.framework.Assert;

import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.rm.Names;
import org.objectweb.celtix.bus.ws.rm.RMContextUtils;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.rm.RMProperties;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceType;



public class MessageFlow extends Assert {
    
    private List<SOAPMessage> outboundMessages;
    private List<LogicalMessageContext> inboundContexts;
    
    
    public MessageFlow(List<SOAPMessage> o, List<LogicalMessageContext> i) {
        outboundMessages = o;
        inboundContexts = i;
    }
    
    public List<SOAPMessage> getOutboundMessages() {
        return outboundMessages;
    }
    
    public List<LogicalMessageContext> getInboundContexts() {
        return inboundContexts;
    }
    
    public void clear() {
        getOutboundMessages().clear();
        getInboundContexts().clear();
    }
    
    public void verifyActions(String[] expectedActions, boolean outbound) throws Exception {

        assertEquals(expectedActions.length, outbound ? outboundMessages.size() : inboundContexts.size());

        for (int i = 0; i < expectedActions.length; i++) {
            String action = outbound ? getAction(outboundMessages.get(i)) : getAction(inboundContexts.get(i));
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

        if (expectedActions.length != (outbound ? outboundMessages.size() : inboundContexts.size())) {
            return false;
        }

        for (int i = 0; i < expectedActions.length; i++) {
            String action = outbound ? getAction(outboundMessages.get(i)) : getAction(inboundContexts.get(i));
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
        int messageCount = outbound ? outboundMessages.size() : inboundContexts.size();
        int count = 0;
        for (int i = 0; i < messageCount; i++) {
            String action = outbound ? getAction(outboundMessages.get(i)) : getAction(inboundContexts.get(i));
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
            outbound ? outboundMessages.size() : inboundContexts.size();
        if (exact) {
            assertEquals(expectedMessageNumbers.length, actualMessageCount);
        } else {
            assertTrue(expectedMessageNumbers.length <= actualMessageCount);
        }

        if (exact) {
            for (int i = 0; i < expectedMessageNumbers.length; i++) {
                if (outbound) {
                    SOAPElement se = getSequence(outboundMessages.get(i));
                    if (null == expectedMessageNumbers[i]) {
                        assertNull("Outbound message " + i + " contains unexpected message number ", se);
                    } else {
                        assertEquals("Outbound message " + i + " does not contain expected message number "
                                     + expectedMessageNumbers[i], expectedMessageNumbers[i], 
                                     getMessageNumber(se));
                    }
                } else {
                    SequenceType s = getSequence(inboundContexts.get(i));
                    String messageNumber = null == s ? null : s.getMessageNumber().toString();
                    if (null == expectedMessageNumbers[i]) {
                        assertNull("Inbound message " + i + " contains unexpected message number ",
                                   messageNumber);
                    } else {
                        assertEquals("Inbound message " + i + " does not contain expected message number "
                                     + expectedMessageNumbers[i], expectedMessageNumbers[i], messageNumber);
                    }
                }
            }
        } else {
            boolean[] matches = new boolean[expectedMessageNumbers.length];
            for (int i = 0; i < actualMessageCount; i++) {
                String messageNumber = null;
                if (outbound) {
                    SOAPElement se = getSequence(outboundMessages.get(i));
                    messageNumber = null == se ? null : getMessageNumber(se);
                } else {
                    SequenceType s = getSequence(inboundContexts.get(i));
                    messageNumber = null == s ? null : s.getMessageNumber().toString();
                }
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
            outbound ? outboundMessages.size() : inboundContexts.size();
        if (exact) {
            assertEquals(expectedLastMessages.length, actualMessageCount);
        } else {
            assertTrue(expectedLastMessages.length <= actualMessageCount);
        }
        
        for (int i = 0; i < expectedLastMessages.length; i++) { 
            boolean lastMessage;
            if (outbound) {
                SOAPElement se = getSequence(outboundMessages.get(i));
                lastMessage = null == se ? false : getLastMessage(se);
            } else {
                SequenceType s = getSequence(inboundContexts.get(i));
                lastMessage = null == s ? false : null != s.getLastMessage();
            }
            assertEquals("Outbound message " + i 
                         + (expectedLastMessages[i] ? " does not contain expected last message element."
                             : " contains last message element."),
                         expectedLastMessages[i], lastMessage);  
        
        }
    }
    
    public void verifyAcknowledgements(boolean[] expectedAcks, boolean outbound) throws Exception {
        assertEquals(expectedAcks.length, outbound ? outboundMessages.size()
            : inboundContexts.size());
        
        for (int i = 0; i < expectedAcks.length; i++) {
            boolean ack = outbound ? (null != getAcknowledgment(outboundMessages.get(i))) 
                : (null != getAcknowledgment(inboundContexts.get(i)));
            
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
            outbound ? outboundMessages.size() : inboundContexts.size();
        int ackCount = 0;
        for (int i = 0; i < actualMessageCount; i++) {
            boolean ack = outbound ? (null != getAcknowledgment(outboundMessages.get(i))) 
                : (null != getAcknowledgment(inboundContexts.get(i)));
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


    public void verifyAckRequestedOutbound() throws Exception {
        boolean found = false;
        for (SOAPMessage m : outboundMessages) {
            SOAPElement se = getAckRequested(m);
            if (se != null) {
                found = true;
                break;
            }
        }
        assertTrue("expected AckRequested", found);
    }
    
    public void verifyAckRequestedInbound(List<LogicalMessageContext> contexts) throws Exception {
        boolean found = false;
        for (LogicalMessageContext context : contexts) {
            RMProperties rmps = RMContextUtils.retrieveRMProperties(context, false);
            if (null != rmps 
                && rmps.getAcksRequested() != null 
                && rmps.getAcksRequested().size() > 0) {
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
            if ((headerName.getURI().equals(org.objectweb.celtix.bus.ws.addressing.Names.WSA_NAMESPACE_NAME) 
                || headerName.getURI().equals(org.objectweb.celtix.bus.ws.addressing.VersionTransformer
                                              .Names200408.WSA_NAMESPACE_NAME))
                && localName.equals(org.objectweb.celtix.bus.ws.addressing.Names.WSA_ACTION_NAME)) {
                return headerElement.getTextContent();
            }
        }
        return null;
    }

    protected String getAction(LogicalMessageContext context) {
        AddressingProperties maps = ContextUtils.retrieveMAPs(context, false, false);
        if (null != maps && null != maps.getAction()) {
            return maps.getAction().getValue();
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
            if (headerName.getURI().equals(Names.WSRM_NAMESPACE_NAME)
                && localName.equals(Names.WSRM_SEQUENCE_NAME)) {
                return (SOAPElement)header.getChildElements().next();
            }
        }
        return null;
    }

    public String getMessageNumber(SOAPElement elem) throws Exception {
        SOAPElement se = (SOAPElement)elem.getChildElements(
                                                            new QName(Names.WSRM_NAMESPACE_NAME,
                                                                      "MessageNumber")).next();
        return se.getTextContent();
    }

    protected SequenceType getSequence(LogicalMessageContext context) {
        RMProperties rmps = RMContextUtils.retrieveRMProperties(context, false);
        return rmps == null ? null : rmps.getSequence();
    }

    private boolean getLastMessage(SOAPElement elem) throws Exception {
        return elem.getChildElements(new QName(Names.WSRM_NAMESPACE_NAME, "LastMessage")).hasNext();
    }

    protected SOAPElement getAcknowledgment(SOAPMessage msg) throws Exception {
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();
        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            if (headerName.getURI().equals(Names.WSRM_NAMESPACE_NAME)
                && localName.equals(Names.WSRM_SEQUENCE_ACK_NAME)) {
                return (SOAPElement)header.getChildElements().next();
            }
        }
        return null;
    }
    
    protected SequenceAcknowledgement getAcknowledgment(LogicalMessageContext context) {
        RMProperties rmps = RMContextUtils.retrieveRMProperties(context, false);
        if (null != rmps && null != rmps.getAcks() && rmps.getAcks().size() > 0) {
            return rmps.getAcks().iterator().next();
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
            if (headerName.getURI().equals(Names.WSRM_NAMESPACE_NAME)
                && localName.equals(Names.WSRM_ACK_REQUESTED_NAME)) {
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
                assertEquals("Unexpected number of inbound messages", nExpected, inboundContexts.size());
            } else {
                assertTrue("Unexpected number of inbound messages: " + inboundContexts.size(),
                           nExpected <= inboundContexts.size());                
            }
        }
    }
    
    public void verifyMessages(int nExpected, boolean outbound, int interval, int attempts) {
        verifyMessages(nExpected, outbound, interval, attempts, false);
    }
    
    public void verifyMessages(int nExpected, boolean outbound, int interval, int attempts, boolean exact) {
        for (int i = 0; i < attempts; i++) {
            if ((outbound && outboundMessages.size() < nExpected)
                || (!outbound && inboundContexts.size() < nExpected)) {
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
        for (int i = inboundContexts.size() - 1; i >= 0; i--) {
            if (isPartialResponse(inboundContexts.get(i))) {
                inboundContexts.remove(i);
            }
        }
    }
    
    public void verifyPartialResponses(int nExpected) throws Exception {
        int npr = 0;
        for (int i =  0; i < inboundContexts.size(); i++) {
            if (isPartialResponse(inboundContexts.get(i))) {
                npr++;   
            }
        }
        assertEquals("Inbound messages did not contain expected number of partial responses.",
                     nExpected, npr);
    }
    
    public boolean isPartialResponse(LogicalMessageContext ctx) throws Exception {
        return null == getAction(ctx) && emptyBody(ctx);
    }
    
    public boolean emptyBody(LogicalMessageContext ctx) throws Exception {
        return !((SOAPMessage)ctx.get("org.objectweb.celtix.bindings.soap.message"))
            .getSOAPPart().getEnvelope().getBody().hasChildNodes();
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
