package org.objectweb.celtix.bindings.soap2.binding;

import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.soap2.TestBase;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessageInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.ServiceInfo;
import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;


public abstract class AbstractRPCInterceptorTest extends TestBase {

    
    public void setUp() throws Exception {
        super.setUp();

        RPCInterceptor interceptor = new RPCInterceptor();
        interceptor.setPhase("phase1");
        chain.add(interceptor);


        soapMessage.put("service.model.binding", getTestService());
        soapMessage.put("JAXB_CALLBACK", getTestCallback(GreeterRPCLit.class, "sendReceiveData"));
    }

    public void testInterceptorRPCLitInbound() throws Exception {
        soapMessage.setSource(InputStream.class, getTestStream(getClass(), "resources/greetMeRpcLitReq.xml"));
        soapMessage.put("message.inbound", "message.inbound");

        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        List<?> parameters = (List<?>) soapMessage.get("PARAMETERS");
        assertEquals(1, parameters.size());

        assertEquals("sendReceiveData", (String) soapMessage.get(MessageContext.WSDL_OPERATION));
        
        Object obj = parameters.get(0);
        assertTrue(obj instanceof MyComplexStruct);
        MyComplexStruct s = (MyComplexStruct) obj;
        assertEquals("this is element 2", s.getElem2());
    }

    public void testInterceptorRPCLitOutbound() throws Exception {
        soapMessage.setSource(InputStream.class, getTestStream(getClass(),
                                                               "resources/greetMeRpcLitResp.xml"));
        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        Object retValue = soapMessage.get("RETURN");
        assertNotNull(retValue);
        assertTrue(retValue instanceof MyComplexStruct);
        MyComplexStruct s = (MyComplexStruct) retValue;
        assertEquals("return is element 2", s.getElem2());
    }
    
    public BindingInfo getTestService() {
        ServiceInfo service = getTestService(GreeterRPCLit.class);
        assertNotNull(service);
        
        BindingInfo binding = service.getEndpoint("GreeterRPCLitPort").getBinding();
        assertNotNull(binding);
        BindingOperationInfo op = binding.getOperation("sendReceiveData");
        assertNotNull(op);
        assertEquals(1, op.getInput().getMessageInfo().size());
        assertEquals(1, op.getOutput().getMessageInfo().size());
        MessageInfo msg = op.getInput().getMessageInfo();
        assertEquals(1, msg.getMessageParts().size());
        MessagePartInfo part = msg.getMessageParts().get(0);
        assertEquals(new QName("in"), part.getName());
        return binding;
    }
}
