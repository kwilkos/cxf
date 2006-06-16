package org.objectweb.celtix.bus.ws.addressing;


import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.easymock.IMocksControl;

import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

import static org.objectweb.celtix.bus.bindings.soap.SOAPConstants.SOAP_ENV_ENCSTYLE;
import static org.objectweb.celtix.context.ObjectMessageContext.METHOD_OBJ;
import static org.objectweb.celtix.context.ObjectMessageContext.REQUESTOR_ROLE_PROPERTY;
import static org.objectweb.celtix.context.OutputStreamMessageContext.ONEWAY_MESSAGE_TF;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;

public class MAPAggregatorTest extends TestCase {

    private MAPAggregator aggregator;
    private IMocksControl control;
    private AddressingPropertiesImpl expectedMAPs;
    private String expectedTo;
    private String expectedReplyTo;
    private String expectedRelatesTo;
    private String expectedAction;
    
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
        expectedAction = null;
    }

    public void testRequestorOutboundUsingAddressingMAPsInContext() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, false, true, true);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }
    
    public void testRequestorOutboundUsingAddressingMAPsInContextZeroLengthAction() 
        throws Exception {
        LogicalMessageContext context = setUpContext(true, true, false, true, true, true);
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
    
    public void testResponderInboundDecoupled() throws Exception {
        LogicalMessageContext context = 
            setUpContext(false, false, false, true, false, true);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }
    
    public void testResponderInboundOneway() throws Exception {
        LogicalMessageContext context = 
            setUpContext(false, false, true, true, false, true);
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
    
    public void testResponderOutboundZeroLengthAction() throws Exception {
        LogicalMessageContext context = 
            setUpContext(false, true, false, false, false, false, true);
        boolean proceed = aggregator.handleMessage(context);
        assertTrue("expected dispatch to proceed", proceed);
        control.verify();
        aggregator.close(context);
    }

    public void testResponderOutboundFault() throws Exception {
        LogicalMessageContext context = setUpContext(new boolean[] {false,
                                                                    true,
                                                                    false,
                                                                    false,
                                                                    false,
                                                                    true,
                                                                    false,
                                                                    true});
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
                                               boolean oneway) 
        throws Exception {
        return setUpContext(requestor, outbound, oneway, false, false, false);
    }

    private LogicalMessageContext setUpContext(boolean requestor, 
                                               boolean outbound,
                                               boolean oneway,
                                               boolean usingAddressing)
        throws Exception {
        return setUpContext(requestor,
                            outbound,
                            oneway,
                            usingAddressing,
                            false,
                            false);
    }

    private LogicalMessageContext setUpContext(boolean requestor, 
                                               boolean outbound,
                                               boolean oneway,
                                               boolean usingAddressing,
                                               boolean mapsInContext) 
        throws Exception {
        return setUpContext(requestor,
                            outbound,
                            oneway,
                            usingAddressing,
                            mapsInContext,
                            false);
    }

    private LogicalMessageContext setUpContext(boolean requestor, 
                                               boolean outbound,
                                               boolean oneway,
                                               boolean usingAddressing,
                                               boolean mapsInContext,
                                               boolean decoupled) 
        throws Exception {
        return setUpContext(requestor, 
                            outbound,
                            oneway,
                            usingAddressing,
                            mapsInContext,
                            decoupled,
                            false);
    }

    
    private LogicalMessageContext setUpContext(boolean requestor, 
                                               boolean outbound,
                                               boolean oneway,
                                               boolean usingAddressing,
                                               boolean mapsInContext,
                                               boolean decoupled,
                                               boolean zeroLengthAction) 
        throws Exception {
        boolean[] params = {requestor, 
                            outbound,
                            oneway,
                            usingAddressing,
                            mapsInContext,
                            decoupled,
                            zeroLengthAction,
                            false};  
        return setUpContext(params);
    }

    /**
     * Boolean array is used to work around checkstyle rule limiting
     * parameter cardinality to 7. 
     */
    private LogicalMessageContext setUpContext(boolean[] params)
        throws Exception {
        boolean requestor = params[0]; 
        boolean outbound = params[1];
        boolean oneway = params[2];
        boolean usingAddressing = params[3];
        boolean mapsInContext = params[4];
        boolean decoupled = params[5];
        boolean zeroLengthAction = params[6];
        boolean fault = params[7];
        LogicalMessageContext context =
            control.createMock(LogicalMessageContext.class);
        context.get(MESSAGE_OUTBOUND_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(outbound));
        context.get(REQUESTOR_ROLE_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(requestor));
        if (outbound && requestor) {
            setUpUsingAddressing(context, usingAddressing);
            if (usingAddressing) {
                setUpRequestor(context, 
                               oneway, 
                               mapsInContext,
                               decoupled,
                               zeroLengthAction);
            } 
        } else if (!requestor) {
            setUpResponder(context,
                           oneway,
                           outbound,
                           decoupled,
                           zeroLengthAction,
                           fault);
        }
        control.replay();
        return context;
    }

    private void setUpUsingAddressing(LogicalMessageContext context,
                                      boolean usingAddressing) {
        Port port = control.createMock(Port.class);
        aggregator.clientTransport = control.createMock(ClientTransport.class);
        aggregator.clientTransport.getPort();
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
    
    private void setUpRequestor(LogicalMessageContext context,
                                boolean oneway,
                                boolean mapsInContext,
                                boolean decoupled,
                                boolean zeroLengthAction) throws Exception {
        context.get(REQUESTOR_ROLE_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        AddressingPropertiesImpl maps = mapsInContext 
                                        ? new AddressingPropertiesImpl()
                                        : null;
        if (zeroLengthAction) {
            maps.setAction(ContextUtils.getAttributedURI(""));
        }
        context.get(CLIENT_ADDRESSING_PROPERTIES);
        EasyMock.expectLastCall().andReturn(maps);
        Method method = SEI.class.getMethod("op", new Class[0]);
        if (!zeroLengthAction) {
            context.get(METHOD_OBJ);     
            EasyMock.expectLastCall().andReturn(method);
            context.get(REQUESTOR_ROLE_PROPERTY);
            EasyMock.expectLastCall().andReturn(Boolean.TRUE);
            expectedAction = "http://foo/bar/SEI/opRequest";
        }
        context.get(REQUESTOR_ROLE_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
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

    private void setUpResponder(LogicalMessageContext context,
                                boolean oneway,
                                boolean outbound,
                                boolean decoupled,
                                boolean zeroLengthAction,
                                boolean fault) throws Exception {
        context.get(REQUESTOR_ROLE_PROPERTY);
        EasyMock.expectLastCall().andReturn(Boolean.FALSE);
        AddressingPropertiesImpl maps = new AddressingPropertiesImpl();
        EndpointReferenceType replyTo = new EndpointReferenceType();
        replyTo.setAddress(
            ContextUtils.getAttributedURI(decoupled
                                          ? "http://localhost:9999/decoupled"
                                          : Names.WSA_ANONYMOUS_ADDRESS));
        maps.setReplyTo(replyTo);
        EndpointReferenceType faultTo = new EndpointReferenceType();
        faultTo.setAddress(
            ContextUtils.getAttributedURI(decoupled
                                          ? "http://localhost:9999/fault"
                                          : Names.WSA_ANONYMOUS_ADDRESS));
        maps.setFaultTo(faultTo);
        AttributedURIType id = 
            ContextUtils.getAttributedURI("urn:uuid:12345");
        maps.setMessageID(id);
        if (zeroLengthAction) {
            maps.setAction(ContextUtils.getAttributedURI(""));
        }
        context.get(SERVER_ADDRESSING_PROPERTIES_INBOUND);
        EasyMock.expectLastCall().andReturn(maps);
        aggregator.serverBinding = control.createMock(ServerBinding.class);
        aggregator.serverTransport = control.createMock(ServerTransport.class);
        if (!outbound && (oneway || decoupled)) {
            context.get(ONEWAY_MESSAGE_TF);
            EasyMock.expectLastCall().andReturn(Boolean.valueOf(oneway));
            OutputStreamMessageContext outputContext = 
                control.createMock(OutputStreamMessageContext.class);
            aggregator.serverTransport.rebase(context, replyTo);
            EasyMock.expectLastCall().andReturn(outputContext);
            aggregator.serverBinding.partialResponse(
                              EasyMock.isA(OutputStreamMessageContext.class),
                              EasyMock.isA(JAXBDataBindingCallback.class));
            EasyMock.expectLastCall();
        }
        if (outbound || aggregator.messageIDs.size() > 0) {
            if (!zeroLengthAction) {
                Method method = SEI.class.getMethod("op", new Class[0]);
                context.get(METHOD_OBJ);     
                EasyMock.expectLastCall().andReturn(method);
                context.get(REQUESTOR_ROLE_PROPERTY);
                EasyMock.expectLastCall().andReturn(Boolean.FALSE);
                expectedAction = "http://foo/bar/SEI/opResponse";
            }
            context.get(REQUESTOR_ROLE_PROPERTY);
            EasyMock.expectLastCall().andReturn(Boolean.FALSE);
            context.get(SERVER_ADDRESSING_PROPERTIES_INBOUND);
            EasyMock.expectLastCall().andReturn(maps);
            if (fault) {
                aggregator.serverTransport.rebase(EasyMock.same(context),
                                                  EasyMock.same(faultTo));
                EasyMock.expectLastCall().andReturn(null);
            }
            EasyMock.eq(SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
            expectedTo = decoupled
                         ? "http://localhost:9999/decoupled"
                         : Names.WSA_ANONYMOUS_ADDRESS;
            expectedRelatesTo = maps.getMessageID().getValue();
            EasyMock.reportMatcher(new MAPMatcher());
            context.put(SERVER_ADDRESSING_PROPERTIES_OUTBOUND,
                        new AddressingPropertiesImpl());
            EasyMock.expectLastCall().andReturn(null);
            context.setScope(SERVER_ADDRESSING_PROPERTIES_OUTBOUND,
                             MessageContext.Scope.HANDLER);
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
                boolean toOK = 
                    expectedTo == null 
                    || expectedTo.equals(other.getTo().getValue());
                boolean replyToOK = 
                    expectedReplyTo == null 
                    || expectedReplyTo.equals(
                           other.getReplyTo().getAddress().getValue());
                boolean relatesToOK =
                    expectedRelatesTo == null 
                    || expectedRelatesTo.equals(
                           other.getRelatesTo().getValue());
                boolean actionOK =
                    expectedAction == null
                    || expectedAction.equals(other.getAction().getValue());
                boolean messageIdOK = other.getMessageID() != null;
                ret = toOK 
                      && replyToOK 
                      && relatesToOK 
                      && actionOK
                      && messageIdOK;
            }
            return ret;
        }
    } 
    
    private static interface SEI {
        @RequestWrapper(targetNamespace = "http://foo/bar", className = "SEI", localName = "opRequest")
        @ResponseWrapper(targetNamespace = "http://foo/bar", className = "SEI", localName = "opResponse")
        String op();
    }
}
