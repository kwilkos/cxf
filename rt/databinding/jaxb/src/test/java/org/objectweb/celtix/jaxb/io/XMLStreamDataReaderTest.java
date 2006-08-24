package org.apache.cxf.jaxb.io;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.jaxb.JAXBDataReaderFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;
import org.apache.cxf.staxutils.StaxStreamFilter;
import org.apache.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.apache.hello_world_doc_lit_bare.types.TradePriceData;
import org.apache.hello_world_rpclit.GreeterRPCLit;
import org.apache.hello_world_rpclit.types.MyComplexStruct;
import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.types.GreetMe;
import org.apache.hello_world_soap_http.types.GreetMeResponse;
 
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

    public void testReadWrapper() throws Exception {
        JAXBDataReaderFactory rf = getTestReaderFactory(Greeter.class);

        reader = getTestReader("../resources/GreetMeDocLiteralReq.xml");
        assertNotNull(reader);
        
        DataReader<XMLStreamReader> dr = rf.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        Object val = dr.read(reader);
        assertNotNull(val);
        assertTrue(val instanceof GreetMe);
        assertEquals("TestSOAPInputPMessage", ((GreetMe)val).getRequestType());
    }

    public void testReadWrapperReturn() throws Exception {
        JAXBDataReaderFactory rf = getTestReaderFactory(Greeter.class);

        reader = getTestReader("../resources/GreetMeDocLiteralResp.xml");
        assertNotNull(reader);

        DataReader<XMLStreamReader> dr = rf.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        
        Object retValue = dr.read(reader);
        
        assertNotNull(retValue);
        assertTrue(retValue instanceof GreetMeResponse);
        assertEquals("TestSOAPOutputPMessage", ((GreetMeResponse)retValue).getResponseType());
    }

    public void testReadRPC() throws Exception {
        JAXBDataReaderFactory rf = getTestReaderFactory(GreeterRPCLit.class);

        QName[] tags = {new QName("http://apache.org/hello_world_rpclit", "sendReceiveData")};

        reader = getTestReader("../resources/greetMeRpcLitReq.xml");
        assertNotNull(reader);
        
        XMLStreamReader localReader = getTestFilteredReader(reader, tags);

        DataReader<XMLStreamReader> dr = rf.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        Object val = dr.read(new QName("http://apache.org/hello_world_rpclit", "in"),
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

        reader = getTestReader("../resources/sayHiDocLitBareReq.xml");
        assertNotNull(reader);
        
        DataReader<XMLStreamReader> dr = rf.createReader(XMLStreamReader.class);
        assertNotNull(dr);
        Object val = dr.read(new QName("http://apache.org/hello_world_doc_lit_bare/types", "inout"),
                             reader,
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
