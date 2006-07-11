package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.bindings.soap2.TestBase;
import org.objectweb.celtix.jaxb.utils.DepthXMLStreamReader;
import org.objectweb.celtix.servicemodel.MessageInfo;
import org.objectweb.celtix.servicemodel.MessagePartInfo;
import org.objectweb.celtix.servicemodel.OperationInfo;
import org.objectweb.celtix.servicemodel.Service;
import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;


public class RPCInterceptorTest extends TestBase {

    private DepthXMLStreamReader reader;
    
    public void setUp() throws Exception {
        super.setUp();

        RPCInterceptor interceptor = new RPCInterceptor();
        interceptor.setPhase("phase1");
        chain.add(interceptor);


        soapMessage.put("service.model", getTestService());
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
    
    public Service getTestService() {
        Service service = getTestService(GreeterRPCLit.class);
        assertNotNull(service);
        OperationInfo op = service.getOperation("sendReceiveData");
        assertNotNull(op);
        assertEquals(1, op.getInput().size());
        assertEquals(1, op.getOutput().size());
        MessageInfo msg = op.getInput();
        assertEquals(1, msg.getMessageParts().size());
        MessagePartInfo part = msg.getMessageParts().get(0);
        assertEquals(new QName("in"), part.getName());
        return service;
    }
}
