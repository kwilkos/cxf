package org.objectweb.celtix.bus.ws.addressing;


import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.easymock.IMocksControl;

import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import static org.objectweb.celtix.bus.bindings.soap.SOAPConstants.SOAP_ENV_ENCSTYLE;
import static org.objectweb.celtix.context.ObjectMessageContext.REQUESTOR_ROLE_PROPERTY;
import static org.objectweb.celtix.context.OutputStreamMessageContext.ONEWAY_MESSAGE_TF;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_TRANSPORT_PROPERTY;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;


public class MAPAggregatorTest extends TestCase {

    private MAPAggregator aggregator;
    private IMocksControl control;
    private AddressingPropertiesImpl expectedMAPs;
    private String expectedTo;
    private String expectedReplyTo;
    private String expectedRelatesTo;


    public void setUp() {
        aggregator = new MAPAggregator();
        aggregator.init(null);
        control = EasyMock.createNiceControl();
    }

    public void tearDown() {
        aggregator.destroy();
        expectedMAPs = null;
        expectedTo = null;
        expectedReplyTo = null;
        expectedRelatesTo = null;
    }

    public void testRequestorOutboundUsingAddressingMAPsInContext() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, false, true, true);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundUsingAddressingMAPsInContextFault() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, false, true, true);
        boolean proceed = aggregator.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundUsingAddressingNoMAPsInContext() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, false, true, false);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundUsingAddressingNoMAPsInContextFault() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, false, true, false);
        boolean proceed = aggregator.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundNotUsingAddressing() throws Exception {
        LogicalMessageContext context = setUpContext(true, true, false, false);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundNotUsingAddressingFault() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, false, false);
        boolean proceed = aggregator.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundOnewayUsingAddressingMAPsInContext() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, true, true, true);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundOnewayUsingAddressingMAPsInContextFault() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, true, true, true);
        boolean proceed = aggregator.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundOnewayUsingAddressingNoMAPsInContext() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, true, true, false);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundOnewayUsingAddressingNoMAPsInContextFault() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, true, true, false);
        boolean proceed = aggregator.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundOnewayNotUsingAddressing() throws Exception {
        LogicalMessageContext context = setUpContext(true, true, true, false);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorOutboundOnewayNotUsingAddressingFault() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, true, false);
        boolean proceed = aggregator.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testResponderInboundValidMAPs() throws Exception {
        LogicalMessageContext context = setUpContext(false, false, false);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testResponderInboundValidMAPsFault() throws Exception {
        LogicalMessageContext context = setUpContext(false, false, false);
        boolean proceed = aggregator.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testResponderInboundInvalidMAPs() throws Exception {
        aggregator.messageIDs.put("urn:uuid:12345", "urn:uuid:12345");
        LogicalMessageContext context = setUpContext(false, false, false);
        boolean proceed = aggregator.handleMessage(context);
        assertFalse("expected dispatch not to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testResponderInboundInvalidMAPsFault() throws Exception {
        aggregator.messageIDs.put("urn:uuid:12345", "urn:uuid:12345");
        LogicalMessageContext context = setUpContext(false, false, false);
        boolean proceed = aggregator.handleFault(context);
        assertFalse("expected dispatch not to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testResponderOutbound() throws Exception {
        LogicalMessageContext context = setUpContext(false, true, false);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testResponderOutboundFault() throws Exception {
        LogicalMessageContext context = setUpContext(false, true, false);
        boolean proceed = aggregator.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorInbound() throws Exception {
        LogicalMessageContext context = setUpContext(true, true, false);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testRequestorInboundFault() throws Exception {
        LogicalMessageContext context = setUpContext(true, true, false);
        boolean proceed = aggregator.handleFault(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    private LogicalMessageContext setUpContext(boolean requestor, 
                                               boolean outbound,
                                               boolean oneway) {
        return setUpContext(requestor, outbound, oneway, false, false);
    }

    private LogicalMessageContext setUpContext(boolean requestor, 
                                               boolean outbound,
                                               boolean oneway,
                                               boolean usingAddressing) {
        return setUpContext(requestor, outbound, oneway, usingAddressing, false);
    }

    private LogicalMessageContext setUpContext(boolean requestor, 
                                               boolean outbound,
                                               boolean oneway,
                                               boolean usingAddressing,
                                               boolean mapsInContext) {
        LogicalMessageContext context =
            control.createMock(LogicalMessageContext.class);
        context.get(MESSAGE_OUTBOUND_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(outbound));
        context.get(REQUESTOR_ROLE_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(requestor));
        if (outbound && requestor) {
            setUpUsingAddressing(context, usingAddressing);
            if (usingAddressing) {
                context.get(REQUESTOR_ROLE_PROPERTY);
                EasyMock.expectLastCall().andReturn(Boolean.valueOf(requestor));
                context.get(CLIENT_ADDRESSING_PROPERTIES);
                AddressingPropertiesImpl maps = mapsInContext 
                                                ? new AddressingPropertiesImpl()
                                                : null;
                EasyMock.expectLastCall().andReturn(maps);
                context.get(CLIENT_TRANSPORT_PROPERTY);
                EasyMock.expectLastCall().andReturn(null);
                context.get(REQUESTOR_ROLE_PROPERTY);
                EasyMock.expectLastCall().andReturn(Boolean.valueOf(requestor));
                context.get(ONEWAY_MESSAGE_TF);
                EasyMock.expectLastCall().andReturn(Boolean.valueOf(oneway));
                EasyMock.eq(CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
                expectedMAPs = maps;
                expectedTo = Names.WSA_NONE_ADDRESS;
                expectedReplyTo = oneway 
                                  ? Names.WSA_NONE_ADDRESS
                                  : Names.WSA_ANONYMOUS_ADDRESS;
                EasyMock.reportMatcher(new MAPMatcher());
                context.put(CLIENT_ADDRESSING_PROPERTIES_OUTBOUND,
                            mapsInContext
                            ? maps
                            : new AddressingPropertiesImpl());
                EasyMock.expectLastCall().andReturn(null);
                context.setScope(CLIENT_ADDRESSING_PROPERTIES_OUTBOUND,
                                 MessageContext.Scope.HANDLER);
            } 
        } else if (!requestor) {
            context.get(REQUESTOR_ROLE_PROPERTY);
            EasyMock.expectLastCall().andReturn(Boolean.valueOf(requestor));
            context.get(SERVER_ADDRESSING_PROPERTIES_INBOUND);
            AddressingPropertiesImpl maps = new AddressingPropertiesImpl();
            EndpointReferenceType replyTo = new EndpointReferenceType();
            replyTo.setAddress(
                ContextUtils.getAttributedURI(Names.WSA_ANONYMOUS_ADDRESS));
            maps.setReplyTo(replyTo);
            AttributedURIType id = 
                ContextUtils.getAttributedURI("urn:uuid:12345");
            maps.setMessageID(id);
            EasyMock.expectLastCall().andReturn(maps);
            if (outbound || aggregator.messageIDs.size() > 0) {
                context.get(REQUESTOR_ROLE_PROPERTY);
                EasyMock.expectLastCall().andReturn(Boolean.valueOf(requestor));
                context.get(SERVER_ADDRESSING_PROPERTIES_INBOUND);
                EasyMock.expectLastCall().andReturn(maps);
                EasyMock.eq(SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
                expectedTo = Names.WSA_ANONYMOUS_ADDRESS;
                expectedRelatesTo = maps.getMessageID().getValue();
                EasyMock.reportMatcher(new MAPMatcher());
                context.put(SERVER_ADDRESSING_PROPERTIES_OUTBOUND,
                            new AddressingPropertiesImpl());
                EasyMock.expectLastCall().andReturn(null);
                context.setScope(SERVER_ADDRESSING_PROPERTIES_OUTBOUND,
                                 MessageContext.Scope.HANDLER);
            }
        }
        control.replay();
        return context;
    }

    private void setUpUsingAddressing(LogicalMessageContext context,
                                      boolean usingAddressing) {
        Port port = control.createMock(Port.class);
        ClientTransport transport = control.createMock(ClientTransport.class);
        context.get(CLIENT_TRANSPORT_PROPERTY);
        EasyMock.expectLastCall().andReturn(transport);
        transport.getPort();
        EasyMock.expectLastCall().andReturn(port);
        List portExts = control.createMock(List.class);
        port.getExtensibilityElements();
        EasyMock.expectLastCall().andReturn(portExts);
        Iterator portItr = control.createMock(Iterator.class);
        portExts.iterator();
        EasyMock.expectLastCall().andReturn(portItr);
        Binding binding = control.createMock(Binding.class);
        port.getBinding();
        EasyMock.expectLastCall().andReturn(binding);
        List bindingExts = control.createMock(List.class);
        binding.getExtensibilityElements();
        EasyMock.expectLastCall().andReturn(bindingExts);
        Iterator bindingItr = control.createMock(Iterator.class);
        bindingExts.iterator();
        EasyMock.expectLastCall().andReturn(bindingItr);
        portItr.hasNext();
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        ExtensibilityElement ext = 
            control.createMock(ExtensibilityElement.class);
        portItr.next();
        EasyMock.expectLastCall().andReturn(ext);
        QName elementType = usingAddressing 
            ? Names.WSAW_USING_ADDRESSING_QNAME 
            : SOAP_ENV_ENCSTYLE;
        ext.getElementType();
        EasyMock.expectLastCall().andReturn(elementType);
        if (!usingAddressing) {
            portItr.hasNext();
            EasyMock.expectLastCall().andReturn(Boolean.FALSE);
            bindingItr.hasNext();
            EasyMock.expectLastCall().andReturn(Boolean.FALSE);
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
            if (expectedMAPs == null || expectedMAPs == other) {
                ret = (expectedTo == null 
                       || expectedTo.equals(other.getTo().getValue()))
                      && (expectedReplyTo == null 
                          || expectedReplyTo.equals(other.getReplyTo().getAddress().getValue()))
                      && (expectedRelatesTo == null 
                          || expectedRelatesTo.equals(other.getRelatesTo().getValue()))
                      && other.getMessageID() != null;
            }
            return ret;
        }
    } 
}
