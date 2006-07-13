package org.objectweb.celtix.jaxb.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;

import javax.jws.WebParam;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxUtils;
import org.objectweb.celtix.testutil.common.TestUtil;
import org.objectweb.hello_world_soap_http.Greeter;

public class XMLStreamDataWriterTest extends TestCase {

    private ByteArrayOutputStream baos;
    private XMLStreamWriter streamWriter;
    private XMLInputFactory inFactory;

    public void setUp() throws Exception {
        baos =  new ByteArrayOutputStream();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        streamWriter = factory.createXMLStreamWriter(baos);
        assertNotNull(streamWriter);
        inFactory = XMLInputFactory.newInstance();
    }

    public void tearDown() throws Exception {
        baos.close();
    }

    public void testWrite() throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(Greeter.class);
        Method m = TestUtil.getMethod(Greeter.class, "greetMe");
        JAXBDataBindingCallback cb = 
            new JAXBDataBindingCallback(m, Mode.PARTS, ctx);
        
        DataWriter<XMLStreamWriter> dw = cb.createWriter(XMLStreamWriter.class);
        assertNotNull(dw);
        
        WebParam wp = cb.getWebParam(0);
        String val = new String("TESTOUTPUTMESSAGE");
        QName elName = new QName(wp.targetNamespace(), 
                                 wp.name());
        
        dw.write(val, elName, streamWriter);
        streamWriter.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = inFactory.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_soap_http/types", "requestType"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("TESTOUTPUTMESSAGE", reader.getText());
    }

    public void testWriteWrapper() throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(Greeter.class);
        Method m = TestUtil.getMethod(Greeter.class, "greetMe");
        JAXBDataBindingCallback cb = 
            new JAXBDataBindingCallback(m, Mode.PARTS, ctx);
        
        DataWriter<XMLStreamWriter> dw = cb.createWriter(XMLStreamWriter.class);
        assertNotNull(dw);

        String val = new String("TESTOUTPUTMESSAGE");
        
        ObjectMessageContextImpl objCtx = new ObjectMessageContextImpl();
        objCtx.setMessageObjects(new Object[] {val});
        
        dw.writeWrapper(objCtx, false, streamWriter);
        streamWriter.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = inFactory.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_soap_http/types", "greetMe"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_soap_http/types", "requestType"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("TESTOUTPUTMESSAGE", reader.getText());
    }

    public void testWriteWrapperReturn() throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(Greeter.class);
        Method m = TestUtil.getMethod(Greeter.class, "greetMe");
        JAXBDataBindingCallback cb = 
            new JAXBDataBindingCallback(m, Mode.PARTS, ctx);
        
        DataWriter<XMLStreamWriter> dw = cb.createWriter(XMLStreamWriter.class);
        assertNotNull(dw);

        String retVal = new String("TESTOUTPUTMESSAGE");
        
        ObjectMessageContextImpl objCtx = new ObjectMessageContextImpl();
        objCtx.setReturn(retVal);
        
        dw.writeWrapper(objCtx, true, streamWriter);
        streamWriter.flush();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = inFactory.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_soap_http/types", "greetMeResponse"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_soap_http/types", "responseType"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("TESTOUTPUTMESSAGE", reader.getText());
    }
}
