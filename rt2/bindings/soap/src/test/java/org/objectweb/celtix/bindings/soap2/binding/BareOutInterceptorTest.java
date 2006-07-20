package org.objectweb.celtix.bindings.soap2.binding;

import org.objectweb.celtix.bindings.soap2.TestBase;

public class BareOutInterceptorTest extends TestBase {
    public void testNothing() {
    }
}

/*
import java.io.*;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.bindings.soap2.TestBase;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxUtils;
import org.objectweb.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.objectweb.hello_world_doc_lit_bare.types.TradePriceData;

public class BareOutInterceptorTest extends TestBase {

    private ByteArrayOutputStream baos;
    private ObjectMessageContextImpl objCtx;
    
    public void setUp() throws Exception {
        super.setUp();
        
        BareOutInterceptor interceptor = new BareOutInterceptor();
        interceptor.setPhase("phase1");
        chain.add(interceptor);

        baos =  new ByteArrayOutputStream();

        soapMessage.setResult(XMLStreamWriter.class, getXMLStreamWriter(baos));
        soapMessage.put(MessageContext.WSDL_OPERATION, "sayHi");
        soapMessage.put("JAXB_CALLBACK", getTestCallback(PutLastTradedPricePortType.class, "sayHi"));
    }

    public void tearDown() throws Exception {
        baos.close();
    }

    public void testWriteOutbound() throws Exception {
        Holder<TradePriceData> holder = new Holder<TradePriceData>(getTestObject());
        soapMessage.put("PARAMETERS", Arrays.asList(holder));
        soapMessage.getInterceptorChain().doIntercept(soapMessage);

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

    public void testWriteInbound() throws Exception {
        Holder<TradePriceData> holder = new Holder<TradePriceData>(getTestObject());
        soapMessage.put("PARAMETERS", Arrays.asList(holder));
        soapMessage.put("message.inbound", "message.inbound");
        
        soapMessage.getInterceptorChain().doIntercept(soapMessage);

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
}
*/
