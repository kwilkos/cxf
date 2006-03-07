package org.objectweb.celtix.bus.ws.addressing.soap;


import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
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
import org.objectweb.celtix.ws.addressing.addressing200408.AttributedURI;
import org.objectweb.celtix.ws.addressing.addressing200408.Relationship;

import static org.objectweb.celtix.context.ObjectMessageContext.REQUESTOR_ROLE_PROPERTY;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;


public class MAPCodecTest extends TestCase {

    private MAPCodec codec;
    private IMocksControl control;
    private QName[] expectedNames;
    private Class<?>[] expectedDeclaredTypes;
    private Object[] expectedValues;
    //private JAXBElement<?>[] expectedJAXBElements; 
    private int expectedIndex;
    private String expectedNamespaceURI;

    public void setUp() {
        codec = new MAPCodec();
        codec.init(null);
        control = EasyMock.createNiceControl();
    }

    public void tearDown() {
        codec.destroy();
        expectedNames = null;
        expectedDeclaredTypes = null;
        //expectedJAXBElements = null;
        expectedValues = null;
        expectedIndex = 0;
        expectedNamespaceURI = null;
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
        boolean proceed = codec.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }
    
    public void testRequestorOutboundPreExistingSOAPAction() throws Exception {
        SOAPMessageContext context = setUpContext(true, true, false, true);
        boolean proceed = codec.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }
    
    public void testRequestorOutboundNonNative() throws Exception {
        String uri = VersionTransformer.Names200408.WSA_NAMESPACE_NAME;
        SOAPMessageContext context = 
            setUpContext(true, true, false, false, uri);
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
    
    public void testResponderInboundNonNative() throws Exception {
        String uri = VersionTransformer.Names200408.WSA_NAMESPACE_NAME;
        SOAPMessageContext context = 
            setUpContext(false, false, false, false, uri);
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
    
    public void testResponderOutboundPreExistingSOAPAction() throws Exception {
        SOAPMessageContext context = setUpContext(false, true, false, true);
        boolean proceed = codec.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        codec.close(context);
    }

    public void testResponderOutboundNonNative() throws Exception {
        String uri = VersionTransformer.Names200408.WSA_NAMESPACE_NAME;
        SOAPMessageContext context = 
            setUpContext(false, true, false, false, uri);
        boolean proceed = codec.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
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
    
    public void testRequestorInboundNonNative() throws Exception {
        String uri = VersionTransformer.Names200408.WSA_NAMESPACE_NAME;
        SOAPMessageContext context = 
            setUpContext(true, false, false, false, uri);
        boolean proceed = codec.handleMessage(context);
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
        return setUpContext(requestor, 
                            outbound,
                            invalidMAP,
                            false);
    }

    private SOAPMessageContext setUpContext(boolean requestor, 
                                            boolean outbound,
                                            boolean invalidMAP,
                                            boolean preExistingSOAPAction) 
        throws Exception {
        return setUpContext(requestor, 
                            outbound,
                            invalidMAP,
                            preExistingSOAPAction,
                            Names.WSA_NAMESPACE_NAME);
    }

    private SOAPMessageContext setUpContext(boolean requestor, 
                                            boolean outbound,
                                            boolean invalidMAP,
                                            boolean preExistingSOAPAction,
                                            String exposeAs) 
        throws Exception {
        SOAPMessageContext context =
            control.createMock(SOAPMessageContext.class);
        context.get(MESSAGE_OUTBOUND_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(outbound));
        context.get(REQUESTOR_ROLE_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(requestor));
        String mapProperty = getMAPProperty(requestor, outbound);
        AddressingPropertiesImpl maps = getMAPs(exposeAs, outbound);
        SOAPMessage message = control.createMock(SOAPMessage.class);
        context.getMessage();
        EasyMock.expectLastCall().andReturn(message);
        SOAPHeader header = setUpSOAPHeader(context, message, outbound);
        JAXBContext jaxbContext = control.createMock(JAXBContext.class);
        ContextUtils.setJAXBContext(jaxbContext);
        VersionTransformer.Names200408.setJAXBContext(jaxbContext);
        if (outbound) {
            setUpEncode(context,
                        message,
                        header,
                        maps,
                        mapProperty,
                        invalidMAP,
                        preExistingSOAPAction);
        } else {
            setUpDecode(context, header, maps, mapProperty, requestor);
        }
        control.replay();
        return context;
    }

    private SOAPHeader setUpSOAPHeader(SOAPMessageContext context, 
                                       SOAPMessage message,
                                       boolean outbound) 
        throws Exception {
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
                             SOAPMessage message,
                             SOAPHeader header,
                             AddressingPropertiesImpl maps,
                             String mapProperty,
                             boolean invalidMAP,
                             boolean preExistingSOAPAction) throws Exception { 
        context.get(mapProperty);
        EasyMock.expectLastCall().andReturn(maps);
        Iterator headerItr = control.createMock(Iterator.class);
        header.examineAllHeaderElements();
        EasyMock.expectLastCall().andReturn(headerItr);
        headerItr.hasNext();
        EasyMock.expectLastCall().andReturn(Boolean.FALSE);
        header.addNamespaceDeclaration(Names.WSA_NAMESPACE_PREFIX,
                                       maps.getNamespaceURI());
        EasyMock.expectLastCall().andReturn(null);        
        Marshaller marshaller = control.createMock(Marshaller.class);
        ContextUtils.getJAXBContext().createMarshaller();
        EasyMock.expectLastCall().andReturn(marshaller);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        EasyMock.expectLastCall();
        IArgumentMatcher matcher = new JAXBEltMatcher();
        for (int i = 0; i < expectedValues.length; i++) {
            EasyMock.reportMatcher(matcher);
            EasyMock.eq(header);
            marshaller.marshal(null, header);
            //marshaller.marshal(expectedJAXBElements[i], 
            //                   header);
            EasyMock.expectLastCall();
        }
        MimeHeaders mimeHeaders = control.createMock(MimeHeaders.class);
        message.getMimeHeaders();
        EasyMock.expectLastCall().andReturn(mimeHeaders);
        mimeHeaders.getHeader("SOAPAction");
        if (preExistingSOAPAction) {
            EasyMock.expectLastCall().andReturn(new String[] {"foobar"});
            String soapAction =
                "\"" + ((AttributedURIType)expectedValues[4]).getValue() + "\"";
            mimeHeaders.setHeader("SOAPAction", soapAction);
            EasyMock.expectLastCall();
        } else {
            EasyMock.expectLastCall().andReturn(null);
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
                             String mapProperty,
                             boolean requestor) throws Exception {
        Unmarshaller unmarshaller = control.createMock(Unmarshaller.class);
        ContextUtils.getJAXBContext().createUnmarshaller();
        EasyMock.expectLastCall().andReturn(unmarshaller);
        Iterator headerItr = control.createMock(Iterator.class);
        header.examineAllHeaderElements();
        EasyMock.expectLastCall().andReturn(headerItr);
        String uri = maps.getNamespaceURI();
        boolean exposedAsNative = Names.WSA_NAMESPACE_NAME.equals(uri);
        boolean exposedAs200408 = 
            VersionTransformer.Names200408.WSA_NAMESPACE_NAME.equals(uri);
        assertTrue("unexpected namescape URI: " + uri, 
                   exposedAsNative || exposedAs200408);
        setUpHeaderDecode(headerItr,
                          uri,
                          Names.WSA_MESSAGEID_NAME,
                          exposedAsNative
                          ? AttributedURIType.class
                          : AttributedURI.class,
                          0,
                          unmarshaller);
        setUpHeaderDecode(headerItr,
                          uri,
                          Names.WSA_TO_NAME,
                          exposedAsNative
                          ? AttributedURIType.class
                          : AttributedURI.class,
                          1,
                          unmarshaller);
        setUpHeaderDecode(headerItr,
                          uri,
                          Names.WSA_REPLYTO_NAME,
                          exposedAsNative
                          ? EndpointReferenceType.class
                          : VersionTransformer.Names200408.EPR_TYPE,
                          2,
                          unmarshaller);
        setUpHeaderDecode(headerItr,
                          uri,
                          Names.WSA_RELATESTO_NAME,
                          exposedAsNative
                          ? RelatesToType.class
                          : Relationship.class,
                          3,
                          unmarshaller);
        if (requestor) {
            context.put("org.objectweb.celtix.correlation.in",
                        exposedAsNative
                        ? ((RelatesToType)expectedValues[3]).getValue()
                        : ((Relationship)expectedValues[3]).getValue());
            EasyMock.expectLastCall().andReturn(null);
        }
        setUpHeaderDecode(headerItr,
                          uri,
                          Names.WSA_ACTION_NAME,
                          exposedAsNative
                          ? AttributedURIType.class
                          : AttributedURI.class,
                          4,
                          unmarshaller);
        EasyMock.eq(mapProperty);
        EasyMock.reportMatcher(new MAPMatcher());
        context.put(mapProperty, maps);
        EasyMock.expectLastCall().andReturn(null);
        context.setScope(mapProperty, MessageContext.Scope.HANDLER);
        EasyMock.expectLastCall();
    }

    private <T> void setUpHeaderDecode(Iterator headerItr,
                                       String uri,
                                       String name,
                                       Class<T> clz,
                                       int index,
                                       Unmarshaller unmarshaller) 
        throws Exception { 
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
        EasyMock.expectLastCall().andReturn(uri);
        headerName.getLocalName();
        EasyMock.expectLastCall().andReturn(name);
        Object v = expectedValues[index];
        JAXBElement<?> jaxbElement = 
            new JAXBElement<T>(new QName(uri, name), clz, clz.cast(v));
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

    private AddressingPropertiesImpl getMAPs(String uri, boolean outbound) {
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
        RelatesToType relatesTo = new RelatesToType(); 
        relatesTo.setValue("urn:uuid:67890");
        maps.setRelatesTo(relatesTo);
        AttributedURIType action = 
            ContextUtils.getAttributedURI("http://foo/bar/SEI/opRequest");
        maps.setAction(action);
        maps.exposeAs(uri);
        expectedNamespaceURI = uri;

        expectedNames = 
            new QName[] {new QName(uri, Names.WSA_MESSAGEID_NAME), 
                         new QName(uri, Names.WSA_TO_NAME), 
                         new QName(uri, Names.WSA_REPLYTO_NAME),
                         new QName(uri, Names.WSA_RELATESTO_NAME),
                         new QName(uri, Names.WSA_ACTION_NAME)};
        if (exposeAsNative) {
            expectedValues = new Object[] {id, to, replyTo, relatesTo, action};
            expectedDeclaredTypes = 
                new Class<?>[] {AttributedURIType.class,
                                AttributedURIType.class,
                                EndpointReferenceType.class,
                                RelatesToType.class, 
                                AttributedURIType.class};
        } else if (exposeAs200408) {
            expectedValues = new Object[] {VersionTransformer.convert(id),
                                           VersionTransformer.convert(to),
                                           VersionTransformer.convert(replyTo),
                                           VersionTransformer.convert(relatesTo),
                                           VersionTransformer.convert(action)};
            if (!outbound) {
                // conversion from 2004/08 to 2005/08 anonymous address
                // occurs transparently in VersionTransformer
                VersionTransformer.Names200408.EPR_TYPE.cast(expectedValues[2]).
                    getAddress().setValue(Names.WSA_ANONYMOUS_ADDRESS);
            }
            expectedDeclaredTypes = 
                new Class<?>[] {AttributedURI.class,
                                AttributedURI.class,
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
                String expectedRelatesTo = 
                    exposedAsNative
                    ? ((RelatesToType)expectedValues[3]).getValue()
                    : ((Relationship)expectedValues[3]).getValue();
                String expectedAction =                    
                    exposedAsNative
                    ? ((AttributedURIType)expectedValues[4]).getValue()
                    : ((AttributedURI)expectedValues[4]).getValue();
                ret = expectedMessageID.equals(other.getMessageID().getValue())
                      && expectedTo.equals(other.getTo().getValue())
                      && expectedReplyTo.equals(
                             other.getReplyTo().getAddress().getValue())
                      && expectedRelatesTo.equals(other.getRelatesTo().getValue())
                      && expectedAction.equals(other.getAction().getValue())
                      && expectedNamespaceURI.equals(other.getNamespaceURI());
            }
            return ret;
        }
    } 
}


