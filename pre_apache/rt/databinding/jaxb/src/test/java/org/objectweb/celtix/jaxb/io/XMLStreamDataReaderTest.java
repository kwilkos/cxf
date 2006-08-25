package org.objectweb.celtix.jaxb.io;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.jws.WebParam;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.datamodel.soap.SOAPConstants;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.staxutils.StaxStreamFilter;
import org.objectweb.celtix.testutil.common.TestUtil;
import org.objectweb.hello_world_soap_http.Greeter;
 
public class XMLStreamDataReaderTest extends TestCase {

    private XMLInputFactory factory;
    private XMLStreamReader reader;
    private InputStream is;
    
    public void setUp() throws Exception {
        factory = XMLInputFactory.newInstance();
    }

    public void tearDown() throws IOException {
        is.close();
    }

    public void testRead() throws Exception {
        JAXBDataBindingCallback cb = getTestCallback();

        QName[] tags = {SOAPConstants.SOAP_ENV, 
                        SOAPConstants.SOAP_BODY,
                        cb.getRequestWrapperQName()};

        reader = getTestReader("../resources/GreetMeDocLiteralReq.xml");
        assertNotNull(reader);
        
        XMLStreamReader localReader = getTestFilteredReader(reader, tags);

        WebParam param = cb.getWebParam(0);
        QName elName = new QName(param.targetNamespace(),
                                 param.name());
        
        DataReader<XMLStreamReader> dr = cb.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        Object val = dr.read(elName, 0, localReader);
        assertNotNull(val);
        assertTrue(val instanceof String);
        assertEquals("TestSOAPInputPMessage", (String)val);
    }

    public void testReadWrapper() throws Exception {
        JAXBDataBindingCallback cb = getTestCallback();

        QName[] tags = {SOAPConstants.SOAP_ENV, 
                        SOAPConstants.SOAP_BODY};

        reader = getTestReader("../resources/GreetMeDocLiteralReq.xml");
        assertNotNull(reader);
        
        XMLStreamReader localReader = getTestFilteredReader(reader, tags);

        DataReader<XMLStreamReader> dr = cb.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        
        ObjectMessageContextImpl objCtx = new ObjectMessageContextImpl();
        objCtx.setMessageObjects(new Object[1]);
        
        dr.readWrapper(objCtx, false, localReader);
        
        Object[] methodArgs = objCtx.getMessageObjects();
        assertEquals(1, methodArgs.length);
        assertTrue(methodArgs[0] instanceof String);
        assertEquals("TestSOAPInputPMessage", (String)methodArgs[0]);
    }

    public void testReadWrapperReturn() throws Exception {
        JAXBDataBindingCallback cb = getTestCallback();

        QName[] tags = {SOAPConstants.SOAP_ENV, 
                        SOAPConstants.SOAP_BODY};

        reader = getTestReader("../resources/GreetMeDocLiteralResp.xml");
        assertNotNull(reader);
        XMLStreamReader localReader = getTestFilteredReader(reader, tags);

        DataReader<XMLStreamReader> dr = cb.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        
        ObjectMessageContextImpl objCtx = new ObjectMessageContextImpl();
        objCtx.setMessageObjects(new Object[1]);
        
        dr.readWrapper(objCtx, true, localReader);
        
        Object retValue = objCtx.getReturn();
        assertNotNull(retValue);
        assertTrue(retValue instanceof String);
        assertEquals("TestSOAPOutputPMessage", (String)retValue);
    }

    private JAXBDataBindingCallback getTestCallback() throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(Greeter.class);
        Method m = TestUtil.getMethod(Greeter.class, "greetMe");
        return new JAXBDataBindingCallback(m, Mode.PARTS, ctx);
    }

    private XMLStreamReader getTestFilteredReader(XMLStreamReader r, QName[] q) throws Exception {
        StaxStreamFilter filter = new StaxStreamFilter(q);
        return factory.createFilteredReader(r, filter);
    }

    private XMLStreamReader getTestReader(String resource) throws Exception {
        is = getTestStream(resource);
        assertNotNull(is);
        return factory.createXMLStreamReader(is);
    }

    private InputStream getTestStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }
}
