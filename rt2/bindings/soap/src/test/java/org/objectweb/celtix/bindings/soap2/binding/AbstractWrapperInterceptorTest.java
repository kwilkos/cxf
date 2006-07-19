package org.objectweb.celtix.bindings.soap2.binding;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.soap2.TestBase;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.servicemodel.BindingInfo;
import org.objectweb.celtix.servicemodel.BindingOperationInfo;
import org.objectweb.celtix.servicemodel.MessageInfo;
import org.objectweb.celtix.servicemodel.MessagePartInfo;
import org.objectweb.celtix.servicemodel.ServiceInfo;
import org.objectweb.hello_world_soap_http.Greeter;

public abstract class AbstractWrapperInterceptorTest extends TestBase {

    ObjectMessageContextImpl objCtx;
    
    public void setUp() throws Exception {
        super.setUp();

        WrapperInterceptor interceptor = new WrapperInterceptor();
        interceptor.setPhase("phase1");
        chain.add(interceptor);

        soapMessage.put("service.model.binding", getTestService());
        soapMessage.put("JAXB_CALLBACK", getTestCallback(Greeter.class, "greetMe"));

        objCtx = new ObjectMessageContextImpl();
        objCtx.setMessageObjects(new Object[1]);
        soapMessage.put("OBJECTCONTEXT", objCtx);
    }

    public void testInterceptorInbound() throws Exception {
        soapMessage.setSource(InputStream.class, getTestStream(getClass(),
                                                               "resources/GreetMeDocLiteralReq.xml"));
        soapMessage.put("message.inbound", "message.inbound");

        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        assertEquals("greetMe", (String) soapMessage.get(MessageContext.WSDL_OPERATION));
        
        Object[] methodArgs = objCtx.getMessageObjects();
        assertEquals(1, methodArgs.length);
        assertTrue(methodArgs[0] instanceof String);
        assertEquals("TestSOAPInputPMessage", (String)methodArgs[0]);
    }

    public void testInterceptorOutbound() throws Exception {
        soapMessage.setSource(InputStream.class, getTestStream(getClass(),
                                                               "resources/GreetMeDocLiteralResp.xml"));
        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        Object retValue = soapMessage.get("RETURN");
        assertNotNull(retValue);
        assertTrue(retValue instanceof String);
        String s = (String) retValue;
        assertEquals("TestSOAPOutputPMessage", s);
    }
    
    public BindingInfo getTestService() {
        ServiceInfo service = getTestService(Greeter.class);
        assertNotNull(service);
        
        BindingInfo binding = service.getEndpoint("GreeterPort").getBinding();
        assertNotNull(binding);
        BindingOperationInfo op = binding.getOperation("greetMe");
        assertNotNull(op);
        assertEquals(1, op.getInput().getMessageInfo().size());
        assertEquals(1, op.getOutput().getMessageInfo().size());
        MessageInfo msg = op.getInput().getMessageInfo();
        assertEquals(1, msg.getMessageParts().size());
        MessagePartInfo part = msg.getMessageParts().get(0);
        assertEquals(new QName("http://objectweb.org/hello_world_soap_http/types", "requestType"),
                     part.getName());
        return binding;
    }
}
