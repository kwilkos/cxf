package org.objectweb.celtix.ws.rm;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.xml.ws.handler.Handler;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bus.handlers.HandlerChainInvoker;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class CreateSequenceResponseTest extends TestCase {
    
    private ObjectMessageContext objectCtx;
    private AbstractBindingBase binding;
    private Transport transport;
    private HandlerChainInvoker hci;
    private IMocksControl control;
    private AddressingProperties inMAPs;
    private CreateSequenceResponseType inCSR;
    
    public void setUp() {
        objectCtx = new ObjectMessageContextImpl(); 
        control = EasyMock.createNiceControl();
        binding = control.createMock(AbstractBindingBase.class);
        transport = control.createMock(Transport.class);
        hci = new HandlerChainInvoker(new ArrayList<Handler>());
        
        binding.createObjectContext();
        EasyMock.expectLastCall().andReturn(objectCtx);
        binding.createHandlerInvoker();
        EasyMock.expectLastCall().andReturn(hci);

        inMAPs = new AddressingPropertiesImpl();
        EndpointReferenceType replyTo = TestUtils.getEPR("response1");
        inMAPs.setReplyTo(replyTo);
        AttributedURIType messageID =
            ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
        messageID.setValue("msg1");
        inMAPs.setMessageID(messageID);
        
        inCSR = RMUtils.getWSRMFactory().createCreateSequenceResponseType();
    }

    public void testStatic() {
        control.reset();
        assertNotNull(CreateSequenceResponse.createDataBindingCallback());
        Method method = CreateSequenceResponse.getMethod();
        assertNotNull("expected method", method);
        assertEquals("expected method name",
                     "createSequenceResponse",
                     method.getName());
        assertEquals("unexpected operation name",
                     "CreateSequenceResponse",
                     CreateSequenceResponse.getOperationName());        
    }

    public void testConstruction() {     
        control.replay();
                        
        CreateSequenceResponse resp =
            new CreateSequenceResponse(binding,
                                       transport,
                                       inMAPs,
                                       inCSR);
        
        verifyContext();
        assertFalse("unexpected related request",
                    resp.isRelatedRequestExpected());
                
        Object[] params = resp.getObjectMessageContext().getMessageObjects();
        assertEquals(1, params.length);
        CreateSequenceResponseType outCSR = (CreateSequenceResponseType)params[0];
        assertSame("unexpected CSR", inCSR, outCSR);
        
        control.verify();
    }

    private void verifyContext() {
        EndpointReferenceType to =
            (EndpointReferenceType)objectCtx.get("org.objectweb.celtix.ws.addressing.to");
        assertNotNull("unexpectedly null To", to);
        assertSame("unexpected To",
                   inMAPs.getReplyTo(),
                   to);
        
        EndpointReferenceType replyTo =
            (EndpointReferenceType)objectCtx.get("org.objectweb.celtix.ws.addressing.replyto");
        assertNotNull("unexpectedly null ReplyTo", replyTo);
        assertEquals("unexpected ReplyTo",
                     "http://schemas.xmlsoap.org/ws/2004/08/addressing/anonymous",
                     replyTo.getAddress().getValue());
        
        Boolean usingAddressing = 
            (Boolean)objectCtx.get("org.objectweb.celtix.ws.addressing.using");
        assertNotNull("unexpectedly null usingAddressing", usingAddressing);
        assertTrue("expected usingAddressing", usingAddressing.booleanValue());
        
        assertTrue("expected requestor", objectCtx.isRequestorRole());
        
        AddressingProperties outMAPs =
            (AddressingProperties)objectCtx.get("javax.xml.ws.addressing.context");
        assertNotNull("unexpectedly null MAPs", outMAPs);
        assertEquals("unexpected action", 
                     "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse",
                     outMAPs.getAction().getValue());
        assertEquals("expected RelatesTo set to messageID",
                     inMAPs.getMessageID().getValue(),
                     outMAPs.getRelatesTo().getValue());
                
        Boolean isOneway = 
            (Boolean)objectCtx.get(OutputStreamMessageContext.ONEWAY_MESSAGE_TF);
        assertNotNull("unexpectedly null isOneway", isOneway);
        assertTrue("expected isOneway", isOneway.booleanValue());
    }
}
