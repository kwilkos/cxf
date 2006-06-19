package org.objectweb.celtix.jaxb.io;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.jws.WebParam;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bus.bindings.soap.SOAPConstants;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.jaxb.StaxEventFilter;
import org.objectweb.celtix.testutil.common.TestUtil;
import org.objectweb.hello_world_soap_http.Greeter;
 
public class EventDataReaderTest extends TestCase {

    private XMLInputFactory factory;
    private XMLEventReader evntReader;
    private InputStream is;

    public void setUp() throws Exception {
        is =  getClass().getResourceAsStream("../resources/GreetMeDocLiteralReq.xml");
        assertNotNull(is);
        factory = XMLInputFactory.newInstance();
        evntReader = factory.createXMLEventReader(is);
        assertNotNull(evntReader);
    }

    public void tearDown() throws IOException {
        is.close();
    }

    public void testRead() throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(Greeter.class);
        Method m = TestUtil.getMethod(Greeter.class, "greetMe");
        JAXBDataBindingCallback cb = 
            new JAXBDataBindingCallback(m, Mode.PARTS, ctx);

        QName[] tags = {SOAPConstants.SOAP_ENV, 
                        SOAPConstants.SOAP_BODY,
                        cb.getRequestWrapperQName()};
        
        StaxEventFilter filter = new StaxEventFilter(tags);
        XMLEventReader localReader = 
            factory.createFilteredReader(evntReader, filter);

        WebParam param = cb.getWebParam(0);
        QName elName = new QName(param.targetNamespace(),
                                 param.name());
        
        DataReader<XMLEventReader> reader = cb.createReader(XMLEventReader.class);
        assertNotNull(reader);
        Object val = reader.read(elName, 0, localReader);
        assertNotNull(val);
        assertTrue(val instanceof String);
        assertEquals("TestSOAPInputPMessage", (String)val);
    }
    
    public void testReadWrapper() throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(Greeter.class);
        Method m = TestUtil.getMethod(Greeter.class, "greetMe");
        JAXBDataBindingCallback cb = 
            new JAXBDataBindingCallback(m, Mode.PARTS, ctx);

        QName[] tags = {SOAPConstants.SOAP_ENV};
        
        StaxEventFilter filter = new StaxEventFilter(tags);
        XMLEventReader localReader = 
            factory.createFilteredReader(evntReader, filter);

        DataReader<XMLEventReader> reader = cb.createReader(XMLEventReader.class);
        assertNotNull(reader);
        ObjectMessageContextImpl objCtx = new ObjectMessageContextImpl();
        objCtx.setMessageObjects(new Object[1]);
        
        reader.readWrapper(objCtx, false, localReader);
        
        Object[] methodArgs = objCtx.getMessageObjects();
        assertEquals(1, methodArgs.length);
        assertTrue(methodArgs[0] instanceof String);
        assertEquals("TestSOAPInputPMessage", (String)methodArgs[0]);
    }
    
}
