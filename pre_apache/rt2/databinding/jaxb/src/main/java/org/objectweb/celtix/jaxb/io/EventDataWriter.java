package org.objectweb.celtix.jaxb.io;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;

import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.jaxb.JAXBDataWriterFactory;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;

public class EventDataWriter implements DataWriter<XMLEventWriter> {
    final JAXBDataWriterFactory factory;

    public EventDataWriter(JAXBDataWriterFactory cb) {
        factory = cb;
    }
    
    public void write(Object obj, XMLEventWriter output) {
        write(obj, null, output);
    }
    
    public void write(Object obj, QName elName, XMLEventWriter output) {
        if (obj != null) {
            JAXBEncoderDecoder.marshall(factory.getJAXBContext(),
                                        factory.getSchema(), obj,
                                        elName, output, null);
        }
    }
    /*
    public void writeWrapper(ObjectMessageContext objCtx, boolean isOutBound, T output) {
        Object obj = callback.createWrapperType(objCtx, isOutBound);
        QName elName = isOutBound ? callback.getResponseWrapperQName()
                                  : callback.getRequestWrapperQName();
        write(obj, elName, output);
    }
    */
}
