package org.objectweb.celtix.jaxb;

import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.interceptors.WrappedInInterceptor;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.service.model.BindingOperationInfo;

public class WrappedInInterceptorTest extends TestBase {

    public void testInterceptorInbound() throws Exception {
        WrappedInInterceptor interceptor = new WrappedInInterceptor();

        message.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
            .createXMLStreamReader(getTestStream(getClass(), "resources/GreetMeDocLiteralReq.xml")));

        interceptor.handleMessage(message);

        assertNull(message.getContent(Exception.class));
        BindingOperationInfo op = (BindingOperationInfo)message.getExchange().get(BindingOperationInfo.class
                                                                                      .getName());
        assertNotNull(op);

        List<?> objs = (List<?>)message.getContent(Object.class);
        assertTrue(objs != null && objs.size() > 0);
        Object obj = objs.get(0);
        assertTrue(obj instanceof String);

        assertEquals("TestSOAPInputPMessage", obj);
    }

    public void testInterceptorOutbound() throws Exception {
        WrappedInInterceptor interceptor = new WrappedInInterceptor();

        message.setContent(XMLStreamReader.class, XMLInputFactory.newInstance()
            .createXMLStreamReader(getTestStream(getClass(), "resources/GreetMeDocLiteralResp.xml")));
        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);

        interceptor.handleMessage(message);
        assertNull(message.getContent(Exception.class));

        List<?> objs = (List<?>)message.getContent(Object.class);
        assertTrue(objs != null && objs.size() > 0);

        Object retValue = objs.get(0);
        assertNotNull(retValue);

        assertTrue(retValue instanceof String);

        assertEquals("TestSOAPOutputPMessage", retValue);
    }
}
