package org.apache.cxf.jaxb.io;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.jaxb.JAXBDataWriterFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;

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
                                        factory.getSchema(),
                                        obj,
                                        elName,
                                        output,
                                        null);
        }
    }
}
