package org.objectweb.celtix.bus.ws.rm;

import java.io.InputStream;
import java.math.BigInteger;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.objectweb.celtix.context.GenericMessageContext;

public class RMMessageImplTest extends TestCase {
    
    public void testContextConstructor() {
        MessageContext ctx = new GenericMessageContext();
        RMMessageImpl msg = new RMMessageImpl(BigInteger.TEN, ctx);
        assertEquals(BigInteger.TEN, msg.getMessageNr());
        assertSame(ctx, msg.getContext());
    }
    
    public void testInputStreamConstructor() throws SOAPException {
        MessageContext ctx = new GenericMessageContext();
        ctx.put("key", "value");
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage smsg = factory.createMessage();
        ctx.put("org.objectweb.celtix.bindings.soap.message", smsg);
        RMMessageImpl helperMsg = new RMMessageImpl(BigInteger.TEN, ctx);
        InputStream is = helperMsg.getContextAsStream();
        RMMessageImpl msg = new RMMessageImpl(BigInteger.TEN, is);
        assertEquals(BigInteger.TEN, msg.getMessageNr());
        assertEquals(BigInteger.TEN, msg.getMessageNr());
        assertEquals("value", msg.getContext().get("key"));
        assertTrue(msg.getContext().get("org.objectweb.celtix.bindings.soap.message") instanceof SOAPMessage);
    }
}
