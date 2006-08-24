package org.apache.cxf.jaxb.io;


import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.jaxb.JAXBDataReaderFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;

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
