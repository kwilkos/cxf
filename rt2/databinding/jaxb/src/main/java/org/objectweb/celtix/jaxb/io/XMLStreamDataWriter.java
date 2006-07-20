package org.objectweb.celtix.jaxb.io;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.jaxb.JAXBDataWriterFactory;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;

public class XMLStreamDataWriter implements DataWriter<XMLStreamWriter> {
    final JAXBDataWriterFactory factory;

    public XMLStreamDataWriter(JAXBDataWriterFactory cb) {
        factory = cb;
    }
    
    public void write(Object obj, XMLStreamWriter output) {
        write(obj, null, output);
    }
    
    public void write(Object obj, QName elName, XMLStreamWriter output) {
        if (obj != null) {
            JAXBEncoderDecoder.marshall(factory.getJAXBContext(),
                                        factory.getSchema(), obj,
                                        elName, output);
        }
    }
    
    /*
    public void writeWrapper(ObjectMessageContext objCtx, boolean isOutBound, T output) {
        // TODO.
    }
    */
}
