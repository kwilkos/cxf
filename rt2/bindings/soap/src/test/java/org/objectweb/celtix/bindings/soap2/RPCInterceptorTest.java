package org.objectweb.celtix.bindings.soap2;

import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessageInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;

public class RPCInterceptorTest extends TestBase {
    
    public void setUp() throws Exception {
        super.setUp();

        RPCInterceptor interceptor = new RPCInterceptor();
        interceptor.setPhase("phase1");
        chain.add(interceptor);

        soapMessage.getExchange().put(SoapMessage.DATAREADER_FACTORY_KEY, "test.reader.factory");
        soapMessage.put(SoapMessage.BINDING_INFO,
                        getTestService("resources/wsdl/hello_world_rpc_lit.wsdl", "SoapPortRPCLit"));
        soapMessage.put("IMPLEMENTOR_METHOD", getTestMethod(GreeterRPCLit.class, "sendReceiveData"));
    }

    public void testInterceptorRPCLitInbound() throws Exception {
        soapMessage.setContent(InputStream.class, getTestStream(getClass(),
                                                                "resources/greetMeRpcLitReq.xml"));
        soapMessage.put("message.inbound", "message.inbound");

        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        List<?> parameters = (List<?>) soapMessage.get("OBJECTS");
        assertEquals(1, parameters.size());

        assertEquals("sendReceiveData", (String) soapMessage.get(MessageContext.WSDL_OPERATION));
        
        Object obj = parameters.get(0);
        assertTrue(obj instanceof MyComplexStruct);
        MyComplexStruct s = (MyComplexStruct) obj;
        assertEquals("this is element 2", s.getElem2());
    }

    public void testInterceptorRPCLitOutbound() throws Exception {
        soapMessage.setContent(InputStream.class, getTestStream(getClass(),
                                                               "resources/greetMeRpcLitResp.xml"));
        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        List<?> objs = (List<?>) soapMessage.get("OBJECTS");
        assertEquals(1, objs.size());
        
        Object retValue = objs.get(0);
        assertNotNull(retValue);
        
        assertTrue(retValue instanceof MyComplexStruct);
        MyComplexStruct s = (MyComplexStruct) retValue;
        assertEquals("return is element 2", s.getElem2());
    }

    protected BindingInfo getTestService(String wsdlUrl, String port) throws Exception {
        BindingInfo binding = super.getTestService(wsdlUrl, port);

        assertEquals(3, binding.getOperations().size());

        BindingOperationInfo op = binding.getOperation(new QName("http://objectweb.org/hello_world_rpclit",
                                                                 "sendReceiveData"));
        assertNotNull(op);
        assertEquals(1, op.getInput().getMessageInfo().size());
        assertEquals(1, op.getOutput().getMessageInfo().size());
        MessageInfo msg = op.getInput().getMessageInfo();
        assertEquals(1, msg.getMessageParts().size());
        MessagePartInfo part = msg.getMessageParts().get(0);
        assertEquals(new QName("http://objectweb.org/hello_world_rpclit", "in"), part.getName());

        OperationInfo oi = op.getOperationInfo();
        oi.setProperty("test.reader.factory", getTestReaderFactory(GreeterRPCLit.class));

        return binding;
    }

}

