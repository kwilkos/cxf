package org.objectweb.celtix.jaxb.io;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.jaxb.JAXBDataReaderFactory;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.staxutils.StaxStreamFilter;
import org.objectweb.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.objectweb.hello_world_doc_lit_bare.types.TradePriceData;
import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.types.GreetMe;
import org.objectweb.hello_world_soap_http.types.GreetMeResponse;
 
public class XMLStreamDataReaderTest extends TestCase {

    public static final QName  SOAP_ENV = 
        new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
    public static final QName  SOAP_BODY = 
            new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body");

    private XMLInputFactory factory;
    private XMLStreamReader reader;
    private InputStream is;
    
    public void setUp() throws Exception {
        factory = XMLInputFactory.newInstance();
    }

    public void tearDown() throws IOException {
        is.close();
    }

    public void testReadWrapper() throws Exception {
        JAXBDataReaderFactory rf = getTestReaderFactory(Greeter.class);

        QName[] tags = {SOAP_ENV, 
                        SOAP_BODY};

        reader = getTestReader("../resources/GreetMeDocLiteralReq.xml");
        assertNotNull(reader);
        
        XMLStreamReader localReader = getTestFilteredReader(reader, tags);

        DataReader<XMLStreamReader> dr = rf.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        Object val = dr.read(localReader);
        assertNotNull(val);
        assertTrue(val instanceof GreetMe);
        assertEquals("TestSOAPInputPMessage", ((GreetMe)val).getRequestType());
    }

    public void testReadWrapperReturn() throws Exception {
        JAXBDataReaderFactory rf = getTestReaderFactory(Greeter.class);

        QName[] tags = {SOAP_ENV, 
                        SOAP_BODY};

        reader = getTestReader("../resources/GreetMeDocLiteralResp.xml");
        assertNotNull(reader);
        XMLStreamReader localReader = getTestFilteredReader(reader, tags);

        DataReader<XMLStreamReader> dr = rf.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        
        Object retValue = dr.read(localReader);
        
        assertNotNull(retValue);
        assertTrue(retValue instanceof GreetMeResponse);
        assertEquals("TestSOAPOutputPMessage", ((GreetMeResponse)retValue).getResponseType());
    }

    public void testReadRPC() throws Exception {
        JAXBDataReaderFactory rf = getTestReaderFactory(GreeterRPCLit.class);

        QName[] tags = {SOAP_ENV, 
                        SOAP_BODY,
                        new QName("http://objectweb.org/hello_world_rpclit", "sendReceiveData")};

        reader = getTestReader("../resources/greetMeRpcLitReq.xml");
        assertNotNull(reader);
        
        XMLStreamReader localReader = getTestFilteredReader(reader, tags);

        DataReader<XMLStreamReader> dr = rf.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        Object val = dr.read(new QName("http://objectweb.org/hello_world_rpclit", "in"),
                             localReader,
                             MyComplexStruct.class);
        assertNotNull(val);
        assertTrue(val instanceof MyComplexStruct);
        assertEquals("this is element 1", ((MyComplexStruct)val).getElem1());
        assertEquals("this is element 2", ((MyComplexStruct)val).getElem2());
        assertEquals(42, ((MyComplexStruct)val).getElem3());
    }


    public void testReadBare() throws Exception {
        JAXBDataReaderFactory rf = getTestReaderFactory(PutLastTradedPricePortType.class);

        QName[] tags = {SOAP_ENV, 
                        SOAP_BODY};

        reader = getTestReader("../resources/sayHiDocLitBareReq.xml");
        assertNotNull(reader);
        
        XMLStreamReader localReader = getTestFilteredReader(reader, tags);

        DataReader<XMLStreamReader> dr = rf.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        Object val = dr.read(new QName("http://objectweb.org/hello_world_doc_lit_bare/types", "inout"),
                             localReader,
                             null);

        assertNotNull(val);
        assertTrue(val instanceof TradePriceData);
        assertEquals("CELTIX", ((TradePriceData)val).getTickerSymbol());
        assertEquals(1.0f, ((TradePriceData)val).getTickerPrice());
    }

    private JAXBDataReaderFactory getTestReaderFactory(Class clz) throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(clz);
        JAXBDataReaderFactory readerFacotry = new JAXBDataReaderFactory();
        readerFacotry.setJAXBContext(ctx);
        return readerFacotry;
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
