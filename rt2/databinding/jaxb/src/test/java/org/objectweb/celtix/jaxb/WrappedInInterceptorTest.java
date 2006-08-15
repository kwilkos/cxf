package org.objectweb.celtix.jaxb;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.interceptors.WrappedInInterceptor;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;


import org.objectweb.celtix.service.model.OperationInfo;

import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.types.GreetMe;
import org.objectweb.hello_world_soap_http.types.GreetMeResponse;

public class WrappedInInterceptorTest extends TestBase {

    public void setUp() throws Exception {
        super.setUp();
        message.put(Message.INVOCATION_OPERATION, "greetMe");
        message.getExchange().put(Message.DATAREADER_FACTORY_KEY, "test.reader.factory");
        message.put(Message.BINDING_INFO,
                    getTestService("resources/wsdl/hello_world.wsdl", "SoapPort"));
    }

    public void testInterceptorInbound() throws Exception {
        WrappedInInterceptor interceptor = new WrappedInInterceptor();

        message.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
            .createXMLStreamReader(getTestStream(getClass(), "resources/GreetMeDocLiteralReq.xml")));

        message.put(Message.INBOUND_MESSAGE, Message.INBOUND_MESSAGE);

        interceptor.handleMessage(message);

        assertNull(message.getContent(Exception.class));

        assertEquals("greetMe", (String) message.get(Message.INVOCATION_OPERATION));

        List<?> objs = (List<?>) message.get(Message.INVOCATION_OBJECTS);
        assertTrue(objs != null && objs.size() > 0);
        Object obj = objs.get(0);

        assertTrue(obj instanceof GreetMe);
        
        assertEquals("TestSOAPInputPMessage", ((GreetMe)obj).getRequestType());
    }

    public void testInterceptorOutbound() throws Exception {
        WrappedInInterceptor interceptor = new WrappedInInterceptor();

        message.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
            .createXMLStreamReader(getTestStream(getClass(), "resources/GreetMeDocLiteralResp.xml")));
        
        interceptor.handleMessage(message);
        assertNull(message.getContent(Exception.class));

        List<?> objs = (List<?>) message.get(Message.INVOCATION_OBJECTS);
        assertTrue(objs != null && objs.size() > 0);
        
        Object retValue = objs.get(0);
        assertNotNull(retValue);

        assertTrue(retValue instanceof GreetMeResponse);
        
        assertEquals("TestSOAPOutputPMessage", ((GreetMeResponse)retValue).getResponseType());
    }
    
    protected BindingInfo getTestService(String wsdlUrl, String port) throws Exception {
        BindingInfo binding = super.getTestService(wsdlUrl, port);

        assertEquals(6, binding.getOperations().size());

        BindingOperationInfo op = binding.getOperation(new QName("http://objectweb.org/hello_world_soap_http",
                                                                 "greetMe"));
        assertNotNull(op);

        OperationInfo oi = op.getOperationInfo();
        oi.setProperty("test.reader.factory", getTestReaderFactory(Greeter.class));

        return binding;
    }
    
}
