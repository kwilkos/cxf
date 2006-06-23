package org.objectweb.celtix.systest.basicDOCBare;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.namespace.QName;
//    import javax.xml.ws.AsyncHandler;

import javax.xml.ws.Holder;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;

import org.objectweb.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.objectweb.hello_world_doc_lit_bare.SOAPService;
import org.objectweb.hello_world_doc_lit_bare.types.TradePriceData;

public class DOCBareClientServerTest extends ClientServerTestBase {

    private final QName serviceName = new QName("http://objectweb.org/hello_world_doc_lit_bare",
                                                "SOAPService");
    private final QName portName = new QName("http://objectweb.org/hello_world_doc_lit_bare", "SoapPort");

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(DOCBareClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(Server.class));
            }
        };
    }

    public void testBasicConnection() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/doc_lit_bare.wsdl");
        assertNotNull("WSDL is null", wsdl);

        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull("Service is ull ", service);

        PutLastTradedPricePortType putLastTradedPrice = service.getPort(portName,
                                                                        PutLastTradedPricePortType.class);
        TradePriceData priceData = new TradePriceData();
        priceData.setTickerPrice(1.0f);
        priceData.setTickerSymbol("CELTIX");

        Holder<TradePriceData> holder = new Holder<TradePriceData>(priceData);

        for (int i = 0; i < 5; i++) {
            putLastTradedPrice.sayHi(holder);
            assertEquals(4.5f, holder.value.getTickerPrice());
            assertEquals("OBJECTWEB", holder.value.getTickerSymbol());
            putLastTradedPrice.putLastTradedPrice(priceData);
        }

    }

    public void testAnnotation() throws Exception {
        Class claz = PutLastTradedPricePortType.class;
        TradePriceData priceData = new TradePriceData();
        Holder<TradePriceData> holder = new Holder<TradePriceData>(priceData);
        Method method = claz.getMethod("sayHi", holder.getClass());
        assertNotNull("Can not find SayHi method in generated class ", method);
        Annotation ann = method.getAnnotation(WebMethod.class);
        WebMethod webMethod = (WebMethod)ann;
        assertEquals(webMethod.operationName(), "SayHi");
        Annotation[][] paraAnns = method.getParameterAnnotations();
        for (Annotation[] paraType : paraAnns) {
            for (Annotation an : paraType) {
                if (an.annotationType() == WebParam.class) {
                    WebParam webParam = (WebParam)an;
                    assertNotSame("", webParam.targetNamespace());
                }
            }
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DOCBareClientServerTest.class);
    }

    /*
     * public static void main(String[] args) { ClientServerTest cst = new
     * ClientServerTest(); if ("client".equals(args[0])) { try {
     * cst.testAsyncPollingCall(); } catch (Exception ex) {
     * ex.printStackTrace(); } System.err.println("Exiting...........");
     * System.exit(0); } else if ("server".equals(args[0])) { try { //
     * cst.setUp(); cst.onetimeSetUp(); } catch (Exception ex) {
     * ex.printStackTrace(); } } else { System.out.println("Invaid arg"); } }
     */

}
