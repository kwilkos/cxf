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
        is =  getClass().getResourceAsStream("../resources/GreetMeDocLiteralReq.xml");
        assertNotNull(is);
        factory = XMLInputFactory.newInstance();
        reader = factory.createXMLStreamReader(is);
        assertNotNull(reader);
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
        
        StaxStreamFilter filter = new StaxStreamFilter(tags);
        XMLStreamReader localReader = factory.createFilteredReader(reader, filter);

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
}
