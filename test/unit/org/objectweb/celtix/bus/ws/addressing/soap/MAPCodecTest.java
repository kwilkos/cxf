package org.objectweb.celtix.bus.ws.addressing.soap;


import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
//import javax.xml.soap.SOAPFactory;
//import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

import junit.framework.TestCase;

import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.addressing.Names;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.RelatesToType;

import static org.objectweb.celtix.context.ObjectMessageContext.REQUESTOR_ROLE_PROPERTY;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;


public class MAPCodecTest extends TestCase {

    private static final QName[] EXPECTED_NAMES = 
        new QName[] {Names.WSA_MESSAGEID_QNAME, Names.WSA_TO_QNAME, 
                     Names.WSA_REPLYTO_QNAME, Names.WSA_RELATESTO_QNAME};
    private static final Class<?>[] EXPECTED_DECLARED_TYPES = 
        new Class<?>[] {AttributedURIType.class, AttributedURIType.class,
                        EndpointReferenceType.class, RelatesToType.class};
    private MAPCodec codec;
    private IMocksControl control;
    private Object[] expectedValues;
    private int expectedIndex;

    public void setUp() {
        codec = new MAPCodec();
        codec.init(null);
        control = EasyMock.createNiceControl();
        expectedIndex = 0;
    }

    public void tearDown() {
        codec.destroy();
    }

    public void testGetHeaders() throws Exception {
        Set<QName> headers = codec.getHeaders();
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
        SOAPMessageContext context = setUpContext(true, true);
        boolean proceed = codec.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }

    public void testRequestorOutboundFault() throws Exception {
        SOAPMessageContext context = setUpContext(true, true);
        boolean proceed = codec.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }

    public void testResponderInbound() throws Exception {
        SOAPMessageContext context = setUpContext(false, false);
        boolean proceed = codec.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }

    public void testResponderInboundFault() throws Exception {
        SOAPMessageContext context = setUpContext(false, false);
        boolean proceed = codec.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }

    public void testResponderOutbound() throws Exception {
        SOAPMessageContext context = setUpContext(false, true);
        boolean proceed = codec.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }

    public void testResponderOutboundInvalidMAP() throws Exception {
        SOAPMessageContext context = setUpContext(false, true, true);
        try {
            codec.handleMessage(context);
            fail("expected SOAPFaultException on invalid MAP");
        } catch (SOAPFaultException sfe) {
            assertEquals("unexpected fault string",
                         "Duplicate Message ID urn:uuid:12345", 
                         sfe.getFault().getFaultString());
        }
        control.verify();
        codec.close(context);
    }

    public void testResponderOutboundFault() throws Exception {
        SOAPMessageContext context = setUpContext(false, true);
        boolean proceed = codec.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }

    public void testResponderOutboundFaultInvalidMAP() throws Exception {
        SOAPMessageContext context = setUpContext(false, true, true);
        try {
            codec.handleFault(context);
            fail("expected SOAPFaultException on invalid MAP");
        } catch (SOAPFaultException sfe) {
            assertEquals("unexpected fault string",
                         "Duplicate Message ID urn:uuid:12345",
                         sfe.getFault().getFaultString());
        }
        control.verify();
        codec.close(context);
    }

    public void testRequestorInbound() throws Exception {
        SOAPMessageContext context = setUpContext(true, false);
        boolean proceed = codec.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }

    public void testRequestorInboundFault() throws Exception {
        SOAPMessageContext context = setUpContext(true, false);
        boolean proceed = codec.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }

    private SOAPMessageContext setUpContext(boolean requestor, 
                                            boolean outbound)
        throws Exception {
        return setUpContext(requestor, outbound, false); 
    }

    private SOAPMessageContext setUpContext(boolean requestor, 
                                            boolean outbound,
                                            boolean invalidMAP) 
        throws Exception {
        SOAPMessageContext context =
            control.createMock(SOAPMessageContext.class);
        context.get(MESSAGE_OUTBOUND_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(outbound));
        context.get(REQUESTOR_ROLE_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(requestor));
        String mapProperty = getMAPProperty(requestor, outbound);
        AddressingPropertiesImpl maps = getMAPs();
        SOAPHeader header = setUpSOAPHeader(context, outbound);
        codec.jaxbContext = control.createMock(JAXBContext.class);
        if (outbound) {
            setUpEncode(context, header, maps, mapProperty, invalidMAP);
        } else {
            setUpDecode(context, header, maps, mapProperty);
        }
        control.replay();
        return context;
    }

    private SOAPHeader setUpSOAPHeader(SOAPMessageContext context, 
                                       boolean outbound) 
        throws Exception {
        SOAPMessage message = control.createMock(SOAPMessage.class);
        context.getMessage();
        EasyMock.expectLastCall().andReturn(message);
        SOAPPart part = control.createMock(SOAPPart.class);
        message.getSOAPPart();
        EasyMock.expectLastCall().andReturn(part);
        SOAPEnvelope env = control.createMock(SOAPEnvelope.class);         
        part.getEnvelope();
        EasyMock.expectLastCall().andReturn(env);
        SOAPHeader header = control.createMock(SOAPHeader.class);
        env.getHeader();
        EasyMock.expectLastCall().andReturn(header);
        if (outbound) {
            env.getHeader();
            EasyMock.expectLastCall().andReturn(header);
        }
        return header;
    }

    private void setUpEncode(SOAPMessageContext context,
                             SOAPHeader header,
                             AddressingPropertiesImpl maps,
                             String mapProperty,
                             boolean invalidMAP) throws Exception { 
        context.get(mapProperty);
        EasyMock.expectLastCall().andReturn(maps);
        Iterator headerItr = control.createMock(Iterator.class);
        header.examineAllHeaderElements();
        EasyMock.expectLastCall().andReturn(headerItr);
        headerItr.hasNext();
        EasyMock.expectLastCall().andReturn(Boolean.FALSE);
        header.addNamespaceDeclaration(Names.WSA_NAMESPACE_PREFIX,
                                       Names.WSA_NAMESPACE_NAME);
        EasyMock.expectLastCall().andReturn(null);
        Marshaller marshaller = control.createMock(Marshaller.class);
        codec.jaxbContext.createMarshaller();
        EasyMock.expectLastCall().andReturn(marshaller);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        EasyMock.expectLastCall();
        IArgumentMatcher matcher = new JAXBEltMatcher();
        for (int i = 0; i < expectedValues.length; i++) {
            EasyMock.reportMatcher(matcher);
            EasyMock.eq(header);
            marshaller.marshal(null, header);
            EasyMock.expectLastCall();
        }
        if (invalidMAP) {
            context.get("org.objectweb.celtix.ws.addressing.map.fault.name");
            EasyMock.expectLastCall().andReturn(Names.DUPLICATE_MESSAGE_ID_NAME);
            context.get("org.objectweb.celtix.ws.addressing.map.fault.reason");
            EasyMock.expectLastCall().andReturn("Duplicate Message ID urn:uuid:12345"); 
        }
    }

    private void setUpDecode(SOAPMessageContext context, 
                             SOAPHeader header,
                             AddressingPropertiesImpl maps,
                             String mapProperty) throws Exception {
        Unmarshaller unmarshaller = control.createMock(Unmarshaller.class);
        codec.jaxbContext.createUnmarshaller();
        EasyMock.expectLastCall().andReturn(unmarshaller);
        Iterator headerItr = control.createMock(Iterator.class);
        header.examineAllHeaderElements();
        EasyMock.expectLastCall().andReturn(headerItr);
        setUpHeaderDecode(headerItr,
                          Names.WSA_MESSAGEID_NAME,
                          Names.WSA_MESSAGEID_QNAME,
                          AttributedURIType.class,
                          0,
                          unmarshaller);
        setUpHeaderDecode(headerItr,
                          Names.WSA_TO_NAME,
                          Names.WSA_TO_QNAME,
                          AttributedURIType.class,
                          1,
                          unmarshaller);
        setUpHeaderDecode(headerItr,
                          Names.WSA_REPLYTO_NAME,
                          Names.WSA_REPLYTO_QNAME,
                          EndpointReferenceType.class,
                          2,
                          unmarshaller);
        setUpHeaderDecode(headerItr,
                          Names.WSA_RELATESTO_NAME,
                          Names.WSA_RELATESTO_QNAME,
                          RelatesToType.class,
                          3,
                          unmarshaller);
        EasyMock.eq(mapProperty);
        EasyMock.reportMatcher(new MAPMatcher());
        context.put(mapProperty, maps);
        EasyMock.expectLastCall().andReturn(null);
        context.setScope(mapProperty, MessageContext.Scope.HANDLER);
        EasyMock.expectLastCall();
    }

    private <T> void setUpHeaderDecode(Iterator headerItr,
                                   String name,
                                   QName qname,
                                   Class<T> clz,
                                   int index,
                                   Unmarshaller unmarshaller) throws Exception { 
        headerItr.hasNext();
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        SOAPHeaderElement headerElement = 
            control.createMock(SOAPHeaderElement.class);
        headerItr.next();
        EasyMock.expectLastCall().andReturn(headerElement);
        Name headerName = control.createMock(Name.class);
        headerElement.getElementName();
        EasyMock.expectLastCall().andReturn(headerName);
        headerName.getURI();
        EasyMock.expectLastCall().andReturn(Names.WSA_NAMESPACE_NAME);
        headerName.getLocalName();
        EasyMock.expectLastCall().andReturn(name);
        Object v = expectedValues[index];
        JAXBElement<?> jaxbElement = 
            new JAXBElement<T>(qname, clz, clz.cast(v));
        unmarshaller.unmarshal(headerElement, clz);
        EasyMock.expectLastCall().andReturn(jaxbElement);
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

    private AddressingPropertiesImpl getMAPs() {
        AddressingPropertiesImpl maps = new AddressingPropertiesImpl();
        AttributedURIType id = 
            ContextUtils.getAttributedURI("urn:uuid:12345");
        maps.setMessageID(id);
        AttributedURIType to =
            ContextUtils.getAttributedURI("foobar");
        maps.setTo(to);
        EndpointReferenceType replyTo = new EndpointReferenceType();
        replyTo.setAddress(
            ContextUtils.getAttributedURI(Names.WSA_ANONYMOUS_ADDRESS));
        maps.setReplyTo(replyTo);
        RelatesToType relatesTo = new RelatesToType(); 
        relatesTo.setValue("urn:uuid:67890");
        maps.setRelatesTo(relatesTo);
        expectedValues = new Object[] {id, to, replyTo, relatesTo};
        return maps;
    }

    private final class JAXBEltMatcher implements IArgumentMatcher {
        public boolean matches(Object obj) {
            QName name = EXPECTED_NAMES[expectedIndex];
            Class<?> declaredType = EXPECTED_DECLARED_TYPES[expectedIndex];
            Object value = expectedValues[expectedIndex];
            expectedIndex++;

            if (obj instanceof JAXBElement) {
                JAXBElement other = (JAXBElement)obj;
                return name.equals(other.getName()) 
                       && declaredType.isAssignableFrom(other.getDeclaredType())
                       && value.equals(other.getValue());
            }
            return false;
        }    

        public void appendTo(StringBuffer buffer) {
            buffer.append("JAXBElements did not match");
        }
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
            String expectedMessageID = ((AttributedURIType)expectedValues[0]).getValue();
            String expectedTo = ((AttributedURIType)expectedValues[1]).getValue();
            ret = expectedMessageID.equals(other.getMessageID().getValue())
                  && expectedTo.equals(other.getTo().getValue());
            return ret;
        }
    } 
}


