package org.objectweb.celtix.jaxb;

import java.io.*;
import java.util.*;

import javax.xml.namespace.QName;

import org.objectweb.celtix.interceptors.BareInInterceptor;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.OperationInfo;

import org.objectweb.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.objectweb.hello_world_doc_lit_bare.types.TradePriceData;

public class BareInInterceptorTest extends TestBase {


    public void setUp() throws Exception {
        super.setUp();

        message.put(Message.INVOCATION_OPERATION, "SayHi");
        message.getExchange().put(Message.DATAREADER_FACTORY_KEY, "test.reader.factory");
        message.put(Message.BINDING_INFO,
                    getTestService("resources/wsdl/doc_lit_bare.wsdl", "SoapPort"));
    }

    public void testInterceptorInbound() throws Exception {
        BareInInterceptor interceptor = new BareInInterceptor();
        message.setContent(InputStream.class,
                           getClass().getResourceAsStream("resources/sayHiDocLitBareReq.xml"));
        message.put(Message.INBOUND_MESSAGE, Message.INBOUND_MESSAGE);

        interceptor.handleMessage(message);

        assertNull(message.getContent(Exception.class));

        List<?> parameters = (List<?>) message.get(Message.INVOCATION_OBJECTS);
        assertEquals(1, parameters.size());

        Object obj = parameters.get(0);

        assertTrue(obj instanceof TradePriceData);
        TradePriceData s = (TradePriceData) obj;
        assertEquals("CELTIX", s.getTickerSymbol());
        assertEquals(1.0f, s.getTickerPrice());
    }

    public void testInterceptorOutbound() throws Exception {
        BareInInterceptor interceptor = new BareInInterceptor();
        message.setContent(InputStream.class,
                           getClass().getResourceAsStream("resources/sayHiDocLitBareResp.xml"));

        interceptor.handleMessage(message);

        List<?> parameters = (List<?>) message.get(Message.INVOCATION_OBJECTS);
        assertEquals(1, parameters.size());

        Object obj = parameters.get(0);
        assertTrue(obj instanceof TradePriceData);
        TradePriceData s = (TradePriceData) obj;
        assertEquals("OBJECTWEB", s.getTickerSymbol());
        assertEquals(4.5f, s.getTickerPrice());
    }

    protected BindingInfo getTestService(String wsdlUrl, String port) throws Exception {
        BindingInfo binding = super.getTestService(wsdlUrl, port);

        assertEquals(3, binding.getOperations().size());

        QName qname = new QName("http://objectweb.org/hello_world_doc_lit_bare", "SayHi");
        BindingOperationInfo op = binding.getOperation(qname);
        assertNotNull(op);

        OperationInfo oi = op.getOperationInfo();
        oi.setProperty("test.reader.factory", getTestReaderFactory(PutLastTradedPricePortType.class));

        return binding;
    }
}
