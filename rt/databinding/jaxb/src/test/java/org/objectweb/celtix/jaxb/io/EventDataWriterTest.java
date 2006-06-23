package org.objectweb.celtix.jaxb.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;

import javax.jws.WebParam;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.jaxb.StaxEventFilter;
import org.objectweb.celtix.testutil.common.TestUtil;
import org.objectweb.hello_world_soap_http.Greeter;

public class EventDataWriterTest extends TestCase {

    private ByteArrayOutputStream baos;
    private XMLEventWriter evntWriter;
    private XMLInputFactory inFactory;

    public void setUp() throws Exception {
        baos =  new ByteArrayOutputStream();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        evntWriter = factory.createXMLEventWriter(baos);
        assertNotNull(evntWriter);
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
        
        DataWriter<XMLEventWriter> dw = cb.createWriter(XMLEventWriter.class);
        assertNotNull(dw);
        
        WebParam wp = cb.getWebParam(0);
        String val = new String("TESTOUTPUTMESSAGE");
        QName elName = new QName(wp.targetNamespace(), 
                                 wp.name());
        
        dw.write(val, elName, evntWriter);
        evntWriter.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLEventReader evntReader = inFactory.createXMLEventReader(bais);
        
        //To Filter START_DOCUMENT/END_DOCUMENT events use event filter
        evntReader = inFactory.createFilteredReader(evntReader, 
                                                    new StaxEventFilter(new QName[0]));

        XMLEvent evnt = evntReader.nextEvent();
        assertNotNull(evnt);
        checkStartElementEvent(evnt, elName);
        
        evnt = evntReader.nextEvent();
        assertNotNull(evnt);        
        checkCharacterEvent(evnt, val);
        
        evnt = evntReader.nextEvent();
        assertNotNull(evnt);        
        checkEndElementEvent(evnt, elName);
    }

    public void testWriteWrapper() throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(Greeter.class);
        Method m = TestUtil.getMethod(Greeter.class, "greetMe");
        JAXBDataBindingCallback cb = 
            new JAXBDataBindingCallback(m, Mode.PARTS, ctx);
        
        DataWriter<XMLEventWriter> dw = cb.createWriter(XMLEventWriter.class);
        assertNotNull(dw);

        String val = new String("TESTOUTPUTMESSAGE");
        
        ObjectMessageContextImpl objCtx = new ObjectMessageContextImpl();
        objCtx.setMessageObjects(new Object[] {val});
        
        dw.writeWrapper(objCtx, false, evntWriter);
        evntWriter.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLEventReader evntReader = inFactory.createXMLEventReader(bais);
        
        //To Filter START_DOCUMENT/END_DOCUMENT events use event filter
        evntReader = inFactory.createFilteredReader(evntReader, 
                                                    new StaxEventFilter(new QName[0]));

        XMLEvent evnt = evntReader.nextEvent();
        assertNotNull(evnt);
        checkStartElementEvent(evnt, cb.getRequestWrapperQName());
        
        WebParam wp = cb.getWebParam(0);
        QName elName = new QName(wp.targetNamespace(), 
                                 wp.name());
        evnt = evntReader.nextEvent();
        assertNotNull(evnt);
        checkStartElementEvent(evnt, elName);
        
        evnt = evntReader.nextEvent();
        assertNotNull(evnt);        
        checkCharacterEvent(evnt, val);
        
        evnt = evntReader.nextEvent();
        assertNotNull(evnt);        
        checkEndElementEvent(evnt, elName);
        
        evnt = evntReader.nextEvent();
        assertNotNull(evnt);        
        checkEndElementEvent(evnt, cb.getRequestWrapperQName());        
    }
    
    private void checkStartElementEvent(XMLEvent evnt, QName expectedTag) {
        assertTrue("Should be a start element tag", evnt.isStartElement());
        StartElement startEl = evnt.asStartElement();
        assertEquals(expectedTag, startEl.getName());        
    }

    private void checkEndElementEvent(XMLEvent evnt, QName expectedTag) {
        assertTrue("Should be a end element tag", evnt.isEndElement());
        EndElement endEl = evnt.asEndElement();
        assertEquals(expectedTag, endEl.getName());        
    }

    private void checkCharacterEvent(XMLEvent evnt, String expecetdVal) {
        assertTrue("Should be a character event", evnt.isCharacters());
        Characters actualVal = evnt.asCharacters();
        assertEquals(expecetdVal, actualVal.getData());        
    }
    
}
