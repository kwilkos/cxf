package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import org.objectweb.celtix.bindings.soap2.TestBase;
import org.objectweb.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.objectweb.hello_world_doc_lit_bare.types.TradePriceData;

public class BareInterceptorTest extends TestBase {
    
    public void setUp() throws Exception {
        super.setUp();

        BareInterceptor interceptor = new BareInterceptor();
        interceptor.setPhase("phase1");
        chain.add(interceptor);

        soapMessage.put("JAXB_CALLBACK", getTestCallback(PutLastTradedPricePortType.class, "sayHi"));
    }

    public void testInterceptorInbound() throws Exception {
        soapMessage.setSource(InputStream.class, getTestStream(getClass(),
                                                               "resources/sayHiDocLitBareReq.xml"));
        soapMessage.put("message.inbound", "message.inbound");

        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        List<?> parameters = (List<?>) soapMessage.get("PARAMETERS");
        assertEquals(1, parameters.size());

        Object obj = parameters.get(0);
        assertTrue(obj instanceof TradePriceData);
        TradePriceData s = (TradePriceData) obj;
        assertEquals("CELTIX", s.getTickerSymbol());
        assertEquals(1.0f, s.getTickerPrice());
    }

    public void testInterceptorOutbound() throws Exception {
        soapMessage.setSource(InputStream.class, getTestStream(getClass(),
                                                               "resources/sayHiDocLitBareResp.xml"));
        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        List<?> parameters = (List<?>) soapMessage.get("PARAMETERS");
        assertEquals(1, parameters.size());

        Object obj = parameters.get(0);
        assertTrue(obj instanceof TradePriceData);
        TradePriceData s = (TradePriceData) obj;
        assertEquals("OBJECTWEB", s.getTickerSymbol());
        assertEquals(4.5f, s.getTickerPrice());
    }
}
