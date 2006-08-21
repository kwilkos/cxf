package org.objectweb.celtix.jaxb;

import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.interceptors.BareInInterceptor;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.hello_world_soap_http.types.GreetMe;
import org.objectweb.hello_world_soap_http.types.GreetMeResponse;

public class BareInInterceptorTest extends TestBase {

    public void testInterceptorInbound() throws Exception {
        BareInInterceptor interceptor = new BareInInterceptor();
        message.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
            .createXMLStreamReader(getTestStream(getClass(), "resources/GreetMeDocLiteralReq.xml")));

        message.put(Message.INBOUND_MESSAGE, Message.INBOUND_MESSAGE);

        interceptor.handleMessage(message);

        assertNull(message.getContent(Exception.class));

        List<?> parameters = (List<?>)message.getContent(Object.class);
        assertEquals(1, parameters.size());

        Object obj = parameters.get(0);

        assertTrue(obj instanceof GreetMe);
        GreetMe greet = (GreetMe)obj;
        assertEquals("TestSOAPInputPMessage", greet.getRequestType());
        
        BindingOperationInfo bop = message.getExchange().get(BindingOperationInfo.class);
        assertNotNull(bop);
        // this doesn't pass yet as we can't introspect the message part type classes to find
        // the correct operation. One possibility is to try to match QNames instead.
        // assertEquals("greetMe", bop.getName().getLocalPart());
    }

    public void testInterceptorOutbound() throws Exception {
        BareInInterceptor interceptor = new BareInInterceptor();

        message.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
            .createXMLStreamReader(getTestStream(getClass(), "resources/GreetMeDocLiteralResp.xml")));

        interceptor.handleMessage(message);

        List<?> parameters = (List<?>)message.getContent(Object.class);
        assertEquals(1, parameters.size());

        Object obj = parameters.get(0);

        assertTrue(obj instanceof GreetMeResponse);
        GreetMeResponse greet = (GreetMeResponse)obj;
        assertEquals("TestSOAPOutputPMessage", greet.getResponseType());

        BindingOperationInfo bop = message.getExchange().get(BindingOperationInfo.class);
        assertNotNull(bop);
    }
}
