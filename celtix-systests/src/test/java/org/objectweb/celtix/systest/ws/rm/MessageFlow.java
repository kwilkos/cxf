package org.objectweb.celtix.systest.ws.rm;

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

    public void verifyMessageNumbers(String[] expectedMessageNumbers, boolean outbound) throws Exception {

        assertEquals(expectedMessageNumbers.length, outbound ? outboundMessages.size()
            : inboundContexts.size());

        for (int i = 0; i < expectedMessageNumbers.length; i++) {
            if (outbound) {
                SOAPElement se = getSequence(outboundMessages.get(i));
                if (null == expectedMessageNumbers[i]) {
                    assertNull(se);
                } else {
                    assertEquals("Outbound message " + i + " does not contain expected message number "
                                 + expectedMessageNumbers[i], expectedMessageNumbers[i], 
                                 getMessageNumber(se));
                }
            } else {
                SequenceType s = getSequence(inboundContexts.get(i));
                String messageNumber = null == s ? null : s.getMessageNumber().toString();
                if (null == expectedMessageNumbers[i]) {
                    assertNull(messageNumber);
                } else {
                    assertEquals("Inbound message " + i + " does not contain expected message number "
                                 + expectedMessageNumbers[i], expectedMessageNumbers[i], messageNumber);
                }
            }
        }
    }

    public void verifyLastMessage(boolean[] expectedLastMessages, boolean outbound) throws Exception {
        
        assertEquals(expectedLastMessages.length, outbound ? outboundMessages.size()
            : inboundContexts.size());
        
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

    private String getMessageNumber(SOAPElement elem) throws Exception {
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
        if (outbound) {
            assertEquals("Unexpected number of outbound messages", nExpected, outboundMessages.size());
        } else {
            assertEquals("Unexpected number of inbound messages", nExpected, inboundContexts.size());
        }
    }
    
    public void verifyInboundMessages(int nExpected) {
        for (int i = 0; i < 10; i++) {
            if (inboundContexts.size() < nExpected) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    // ignore
                }
            } else {
                break;
            }
        }
        assertEquals("Did not receive the expected number of messages.", nExpected, inboundContexts.size());
    }
}
