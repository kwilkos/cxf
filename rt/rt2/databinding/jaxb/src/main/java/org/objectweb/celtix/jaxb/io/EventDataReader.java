package org.objectweb.celtix.jaxb.io;


import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.jaxb.JAXBDataReaderFactory;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;

public class EventDataReader implements DataReader<XMLEventReader> {
    final JAXBDataReaderFactory factory;

    public EventDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }

    public Object read(XMLEventReader input) {
        return read(null, input);
    }

    public Object read(QName name, XMLEventReader reader) {
        return read(name, reader, null);
    }
    
    public Object read(QName name, XMLEventReader reader, Class cls) {
        return JAXBEncoderDecoder.unmarshall(factory.getJAXBContext(),
                                             factory.getSchema(),
                                             reader,
                                             name,
                                             cls, 
                                             null);
    }    
}
