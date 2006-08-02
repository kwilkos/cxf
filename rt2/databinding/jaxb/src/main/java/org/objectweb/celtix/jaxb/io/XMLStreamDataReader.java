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

    public Object read(int idx, XMLStreamReader input) {
        return read(null, idx, input);
    }

    public Object read(QName name, int idx, XMLStreamReader reader) {
        Class<?> cls = null;
        return JAXBEncoderDecoder.unmarshall(factory.getJAXBContext(),
                                             factory.getSchema(),
                                             reader,
                                             name,
                                             cls,
                                             null);
    }
}
