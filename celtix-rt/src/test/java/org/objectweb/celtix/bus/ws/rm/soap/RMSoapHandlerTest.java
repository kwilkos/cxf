package org.objectweb.celtix.bus.ws.rm.soap;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.bindings.soap.SOAPBindingImpl;
import org.objectweb.celtix.bus.bindings.soap.W3CConstants;
import org.objectweb.celtix.bus.ws.rm.Names;
import org.objectweb.celtix.bus.ws.rm.RMContextUtils;
import org.objectweb.celtix.bus.ws.rm.RMUtils;
import org.objectweb.celtix.bus.ws.rm.TestInputStreamContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.ws.rm.AckRequestedType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.objectweb.celtix.ws.rm.SequenceType;


public class RMSoapHandlerTest extends TestCase {

    private static final String SEQ_IDENTIFIER = "http://Business456.com/RM/ABC";
    private static final BigInteger MSG1_MESSAGE_NUMBER = BigInteger.ONE;
    private static final BigInteger MSG2_MESSAGE_NUMBER = BigInteger.ONE.add(BigInteger.ONE);

    SequenceType s1;
    SequenceType s2;
    SequenceAcknowledgement ack1;
    SequenceAcknowledgement ack2;
    AckRequestedType ar1;
    AckRequestedType ar2;
    SOAPBindingImpl sb = new SOAPBindingImpl(false);

    public void testGetHeaders() throws Exception {

        RMSoapHandler codec = new RMSoapHandler();
        codec.init(null);
        Set<QName> headers = codec.getHeaders();
        assertTrue("expected Sequence header", headers.contains(Names.WSRM_SEQUENCE_QNAME));
        assertTrue("expected SequenceAcknowledgment header", headers.contains(Names.WSRM_SEQUENCE_ACK_QNAME));
        assertTrue("expected AckRequested header", headers.contains(Names.WSRM_ACK_REQUESTED_QNAME));
    }

    public void testOutbound() throws Exception {

        setUpOutbound();
        SOAPMessageContext context = null;
        RMSoapHandler codec = new RMSoapHandler();

        // no RM headers

        context = setupOutboundContext();
        assertTrue("expected dispatch to proceed", codec.handleMessage(context));
        verifyHeaders(context, new String[] {});
        codec.close(context);

        // one sequence header

        context = setupOutboundContext();
        RMContextUtils.storeSequence(context, s1);
        assertTrue("expected dispatch to proceed", codec.handleMessage(context));
        verifyHeaders(context, new String[] {Names.WSRM_SEQUENCE_NAME});
        codec.close(context);

        // one acknowledgment header

        context = setupOutboundContext();
        Collection<SequenceAcknowledgement> acks = new ArrayList<SequenceAcknowledgement>();
        acks.add(ack1);
        RMContextUtils.storeAcknowledgments(context, acks);
        assertTrue("expected dispatch to proceed", codec.handleMessage(context));
        verifyHeaders(context, new String[] {Names.WSRM_SEQUENCE_ACK_NAME});
        codec.close(context);

        // two acknowledgment headers

        context = setupOutboundContext();
        acks.add(ack2);
        RMContextUtils.storeAcknowledgments(context, acks);
        assertTrue("expected dispatch to proceed", codec.handleMessage(context));
        verifyHeaders(context, new String[] {Names.WSRM_SEQUENCE_ACK_NAME, Names.WSRM_SEQUENCE_ACK_NAME});
        codec.close(context);

        // one ack requested header

        context = setupOutboundContext();
        Collection<AckRequestedType> requested = new ArrayList<AckRequestedType>();
        requested.add(ar1);
        RMContextUtils.storeAcksRequested(context, requested);
        assertTrue("expected dispatch to proceed", codec.handleMessage(context));
        verifyHeaders(context, new String[] {Names.WSRM_ACK_REQUESTED_NAME});
        codec.close(context);

        // two ack requested headers

        context = setupOutboundContext();
        requested.add(ar2);
        RMContextUtils.storeAcksRequested(context, requested);
        assertTrue("expected dispatch to proceed", codec.handleMessage(context));
        verifyHeaders(context, new String[] {Names.WSRM_ACK_REQUESTED_NAME, Names.WSRM_ACK_REQUESTED_NAME});
        codec.close(context);
    }

    public void testInboundSequence() throws IOException {
        RMSoapHandler codec = new RMSoapHandler();
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(TestInputStreamContext.class
            .getResourceAsStream("resources/spec/Message1.xml"));
        sb = new SOAPBindingImpl(false);
        ObjectMessageContext objectCtx = new ObjectMessageContextImpl();
        SOAPMessageContext context = (SOAPMessageContext)sb.createBindingMessageContext(objectCtx);
        sb.read(istreamCtx, context);
        assertTrue(codec.handleMessage(context));
        SequenceType st = RMContextUtils.retrieveSequence(context);
        assertNotNull(st);
        assertEquals(st.getIdentifier().getValue(), SEQ_IDENTIFIER);
        assertEquals(st.getMessageNumber(), MSG1_MESSAGE_NUMBER);
        assertNull(RMContextUtils.retrieveAcknowledgments(context));
        assertNull(RMContextUtils.retrieveAcksRequested(context));

    }

    public void testInboundAcknowledgements() throws IOException {
        RMSoapHandler codec = new RMSoapHandler();
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(TestInputStreamContext.class
            .getResourceAsStream("resources/spec/Acknowledgment.xml"));
        sb = new SOAPBindingImpl(false);
        ObjectMessageContext objectCtx = new ObjectMessageContextImpl();
        SOAPMessageContext context = (SOAPMessageContext)sb.createBindingMessageContext(objectCtx);
        sb.read(istreamCtx, context);
        
        assertTrue(codec.handleMessage(context));
        Collection<SequenceAcknowledgement> acks = RMContextUtils.retrieveAcknowledgments(context);
        assertNotNull(acks);
        assertEquals(1, acks.size());
        SequenceAcknowledgement ack = acks.iterator().next();
        assertNotNull(ack);
        assertEquals(ack.getIdentifier().getValue(), SEQ_IDENTIFIER);
        assertEquals(2, ack.getAcknowledgementRange().size());
        assertNull(RMContextUtils.retrieveSequence(context));
        assertNull(RMContextUtils.retrieveAcksRequested(context));
    }

    public void testInboundAcksRequested() throws IOException {
        RMSoapHandler codec = new RMSoapHandler();
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(TestInputStreamContext.class
            .getResourceAsStream("resources/spec/Retransmission.xml"));
        sb = new SOAPBindingImpl(false);
        ObjectMessageContext objectCtx = new ObjectMessageContextImpl();
        SOAPMessageContext context = (SOAPMessageContext)sb.createBindingMessageContext(objectCtx);
        sb.read(istreamCtx, context);
        
        assertTrue(codec.handleMessage(context));
        Collection<AckRequestedType> requested = RMContextUtils.retrieveAcksRequested(context);
        assertNotNull(requested);
        assertEquals(1, requested.size());
        AckRequestedType ar = requested.iterator().next();
        assertNotNull(ar);
        assertEquals(ar.getIdentifier().getValue(), SEQ_IDENTIFIER);

        SequenceType s = RMContextUtils.retrieveSequence(context);
        assertNotNull(s);
        assertEquals(s.getIdentifier().getValue(), SEQ_IDENTIFIER);
        assertEquals(s.getMessageNumber(), MSG2_MESSAGE_NUMBER);

        assertNull(RMContextUtils.retrieveAcknowledgments(context));
    }

    private void setUpOutbound() {
        org.objectweb.celtix.ws.rm.ObjectFactory factory = RMUtils.getWSRMFactory();
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
        AcknowledgementRange r = factory.createSequenceAcknowledgementAcknowledgementRange();
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

        sb = new SOAPBindingImpl(false);
    }

    private SOAPMessageContext setupOutboundContext() throws Exception {
        ObjectMessageContext objectCtx = new ObjectMessageContextImpl();
        objectCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);
        objectCtx.put(MESSAGE_OUTBOUND_PROPERTY, Boolean.TRUE);
        SOAPMessageContext context = (SOAPMessageContext)sb.createBindingMessageContext(objectCtx);

        MessageFactory msgFactory = MessageFactory.newInstance();
        SOAPMessage msg = msgFactory.createMessage();
        msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(W3CConstants.NP_SCHEMA_XSD,
                                                                W3CConstants.NU_SCHEMA_XSD);
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(W3CConstants.NP_SCHEMA_XSI,
                                                                W3CConstants.NU_SCHEMA_XSI);
        context.setMessage(msg);
        return context;
    }

    private void verifyHeaders(SOAPMessageContext context, String... names) throws SOAPException {
        SOAPMessage message = context.getMessage();
        SOAPEnvelope env = message.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();

        // check all expected headers are present

        for (String name : names) {
            boolean found = false;
            Iterator headerElements = header.examineAllHeaderElements();
            while (headerElements.hasNext()) {
                SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
                Name headerName = headerElement.getElementName();
                String localName = headerName.getLocalName();
                if (headerName.getURI().equals(RMUtils.getRMConstants().getNamespaceURI())
                    && localName.equals(name)) {
                    found = true;
                    break;
                } else if (headerName.getURI().equals(
                    org.objectweb.celtix.bus.ws.addressing.Names.WSA_NAMESPACE_NAME)
                    && localName.equals(name)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Could not find header element " + name, found);
        }

        // no other headers should be present

        Iterator headerElements = header.examineAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement)headerElements.next();
            Name headerName = headerElement.getElementName();
            String localName = headerName.getLocalName();
            assertTrue(RMUtils.getRMConstants().getNamespaceURI().equals(headerName.getURI()) 
                || org.objectweb.celtix.bus.ws.addressing.Names.WSA_NAMESPACE_NAME
                .equals(headerName.getURI()));
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
}
