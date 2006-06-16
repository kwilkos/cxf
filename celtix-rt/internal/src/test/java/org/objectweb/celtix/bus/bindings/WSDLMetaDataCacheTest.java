package org.objectweb.celtix.bus.bindings;

import java.lang.reflect.Method;
import java.net.URL;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;

import org.xml.sax.InputSource;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.objectweb.hello_world_rpclit.GreeterRPCLit;

public class WSDLMetaDataCacheTest extends TestCase {

    public WSDLMetaDataCacheTest(String name) {
        super(name);
    }
    
    public void testRpcLit() throws Exception {
        Class<?> cls = GreeterRPCLit.class;
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(cls);
        
        URL url = getClass().getResource("/wsdl/hello_world_rpc_lit.wsdl");
        assertNotNull("Could not find wsdl /wsdl/hello_world_rpc_lit.wsdl", url);
        
        
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        InputSource input = new InputSource(url.openStream());
        Definition def = reader.readWSDL(null, input);
        Port port = def.getService(new QName("http://objectweb.org/hello_world_rpclit",
                                             "SOAPServiceRPCLit1")).getPort("SoapPortRPCLit1");
        WSDLMetaDataCache wsdl = new WSDLMetaDataCache(def, port);
        
        for (Method method : cls.getDeclaredMethods()) {
            DataBindingCallback c1 = new JAXBDataBindingCallback(method, Mode.PARTS, ctx);
            WSDLOperationInfo info = wsdl.getOperationInfo(c1.getOperationName());
            assertNotNull("Could not find operation info in wsdl: " + c1.getOperationName(), info);
            DataBindingCallback c2 = new WSDLOperationDataBindingCallback(info);
            compareDataBindingCallbacks(c1, c2);
        }
    }
    
    /*REVISIT - the OperationInfo thing doesn't support wrapped doc/lit yet
    public void testDocLit() throws Exception {
        Class<?> cls = org.objectweb.hello_world_doc_lit.Greeter.class;
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(cls);
        
        URL url = getClass().getResource("/wsdl/hello_world_doc_lit.wsdl");
        assertNotNull("Could not find wsdl /wsdl/hello_world_doc_lit.wsdl", url);
        
        
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        InputSource input = new InputSource(url.openStream());
        Definition def = reader.readWSDL(null, input);
        Port port = def.getService(new QName("http://objectweb.org/hello_world_doc_lit",
                                             "SOAPService")).getPort("SoapPort");
        WSDLMetaDataCache wsdl = new WSDLMetaDataCache(def, port);
        
        for (Method method : cls.getDeclaredMethods()) {
            DataBindingCallback c1 = new JAXBDataBindingCallback(method, Mode.PARTS, ctx);
            WSDLOperationInfo info = wsdl.getOperationInfo(c1.getOperationName());
            assertNotNull("Could not find operation info in wsdl: " + c1.getOperationName(), info);
            DataBindingCallback c2 = new WSDLOperationDataBindingCallback(info);
            compareDataBindingCallbacks(c1, c2);
        }
    }
    */
    
    public void testDocLitBare() throws Exception {
        Class<?> cls = PutLastTradedPricePortType.class;
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(cls);
        
        URL url = getClass().getResource("/wsdl/doc_lit_bare.wsdl");
        assertNotNull("Could not find wsdl /wsdl/doc_lit_bare.wsdl", url);
        
        
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        InputSource input = new InputSource(url.openStream());
        Definition def = reader.readWSDL(null, input);
        Port port = def.getService(new QName("http://objectweb.org/hello_world_doc_lit_bare",
                                             "SOAPService")).getPort("SoapPort");
        WSDLMetaDataCache wsdl = new WSDLMetaDataCache(def, port);
        
        for (Method method : cls.getDeclaredMethods()) {
            DataBindingCallback c1 = new JAXBDataBindingCallback(method, Mode.PARTS, ctx);
            WSDLOperationInfo info = wsdl.getOperationInfo(c1.getOperationName());
            assertNotNull("Could not find operation info in wsdl: " + c1.getOperationName(), info);
            DataBindingCallback c2 = new WSDLOperationDataBindingCallback(info);
            compareDataBindingCallbacks(c1, c2);
        }
    }
    
    private void compareDataBindingCallbacks(DataBindingCallback c1, DataBindingCallback c2) {
        assertEquals(c1.getSOAPAction(), c2.getSOAPAction());
        
        assertEquals(c1.getSOAPStyle(), c2.getSOAPStyle());
        assertEquals(c1.getSOAPUse(), c2.getSOAPUse());
        assertEquals(c1.getSOAPParameterStyle(), c2.getSOAPParameterStyle());
        
        assertEquals(c1.getOperationName(), c2.getOperationName());
        assertEquals(c1.getTargetNamespace(), c2.getTargetNamespace());
        assertEquals(c1.getRequestWrapperQName(), c2.getRequestWrapperQName());
        assertEquals(c1.getResponseWrapperQName(), c2.getResponseWrapperQName());
        
        assertEquals(c1.getParamsLength(), c2.getParamsLength());
        assertEquals(c1.getWebResultQName(), c2.getWebResultQName());
        
        WebResult w1 = c1.getWebResult();
        WebResult w2 = c2.getWebResult();
        if (w1 != null || w2 != null) {
            assertNotNull(w1);
            assertNotNull(w2);
            assertEquals(w1.name(), w2.name());
            assertEquals(w1.header(), w2.header());
            assertEquals(w1.partName(), w2.partName());
            assertEquals(w1.targetNamespace(), w2.targetNamespace());
        } 
        
        for (int x = 0; x < c1.getParamsLength(); x++) {
            WebParam wp1 =  c1.getWebParam(x);
            WebParam wp2 =  c2.getWebParam(x);
            if (wp1 != null || wp2 != null) {
                assertNotNull(wp1);
                assertNotNull(wp2);
                assertEquals(wp1.name(), wp2.name());
                assertEquals(wp1.header(), wp2.header());
                assertEquals(wp1.partName(), wp2.partName());
                assertEquals(wp1.targetNamespace(), wp2.targetNamespace());
            } 
        }
    }
    
    
    class WSDLOperationDataBindingCallback extends AbstractWSDLOperationDataBindingCallback {
        public WSDLOperationDataBindingCallback(WSDLOperationInfo info) {
            super(info);
        }

        public Mode getMode() {
            return null;
        }
        public Class<?>[] getSupportedFormats() {
            return null;
        }
        public <T> DataWriter<T> createWriter(Class<T> cls) {
            return null;
        }
        public <T> DataReader<T> createReader(Class<T> cls) {
            return null;
        }

        public void initObjectContext(ObjectMessageContext octx) {
            // TODO Auto-generated method stub
            
        }
    }
}
