package org.objectweb.celtix.jaxb.io;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.jaxb.JAXBDataReaderFactory;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;

public class XMLStreamDataReader implements DataReader<XMLStreamReader> {
    final JAXBDataReaderFactory factory;

    public XMLStreamDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }

    public Object read(XMLStreamReader input) {
        return read(null, input);
    }

    public Object read(QName name, XMLStreamReader reader) {
        return read(name, reader, null);
    }

    public Object read(QName name, XMLStreamReader reader, Class cls) {
        return JAXBEncoderDecoder.unmarshall(factory.getJAXBContext(),
                                             factory.getSchema(),
                                             reader,
                                             name,
                                             cls,
                                             null);
    }
}
