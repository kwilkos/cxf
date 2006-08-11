package org.objectweb.celtix.jaxb;

import java.io.*;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.interceptors.BareOutInterceptor;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxUtils;

import org.objectweb.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.objectweb.hello_world_doc_lit_bare.types.TradePriceData;


public class BareOutInterceptorTest extends TestBase {

    private ByteArrayOutputStream baos;
    
    public void setUp() throws Exception {
        super.setUp();
        
        baos =  new ByteArrayOutputStream();

        message.setContent(XMLStreamWriter.class, getXMLStreamWriter(baos));
        message.put(Message.INVOCATION_OPERATION, "SayHi");
        message.getExchange().put(Message.DATAWRITER_FACTORY_KEY, "test.writer.factory");
        message.put(Message.BINDING_INFO,
                    getTestService("resources/wsdl/doc_lit_bare.wsdl", "SoapPort"));
    }

    public void tearDown() throws Exception {
        baos.close();
    }

    public void testWriteOutbound() throws Exception {
        BareOutInterceptor interceptor = new BareOutInterceptor();
        // Holder<TradePriceData> holder = new Holder<TradePriceData>();
        message.put(Message.INVOCATION_OBJECTS, Arrays.asList(getTestObject()));

        interceptor.handleMessage(message);

        assertNull(message.getContent(Exception.class));

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        //System.err.println(baos.toString());
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_doc_lit_bare/types", "inout"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_doc_lit_bare/types", "tickerSymbol"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("this is elem1", reader.getText());
    }

    public void testWriteInbound() throws Exception {
        BareOutInterceptor interceptor = new BareOutInterceptor();
        message.put(Message.INVOCATION_OBJECTS, Arrays.asList(getTestObject()));
        message.put(Message.INBOUND_MESSAGE, Message.INBOUND_MESSAGE);
        
        interceptor.handleMessage(message);
        assertNull(message.getContent(Exception.class));

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_doc_lit_bare/types", "inout"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_doc_lit_bare/types", "tickerSymbol"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("this is elem1", reader.getText());
    }

    public TradePriceData getTestObject() {
        TradePriceData obj = new TradePriceData();
        obj.setTickerSymbol("this is elem1");
        obj.setTickerPrice(5);
        return obj;
    }

    public BindingInfo getTestService(String wsdlUrl, String port) throws Exception {
        BindingInfo binding = super.getTestService(wsdlUrl, port);
        QName qname = new QName("http://objectweb.org/hello_world_doc_lit_bare", "SayHi");
        BindingOperationInfo op = binding.getOperation(qname);
        OperationInfo oi = op.getOperationInfo();
        oi.setProperty("test.writer.factory", getTestWriterFactory(PutLastTradedPricePortType.class));
        return binding;
    }
}
