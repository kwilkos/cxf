package org.objectweb.celtix.jaxb;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Node;

import org.objectweb.celtix.jaxb.io.EventDataWriter;
import org.objectweb.celtix.jaxb.io.XMLStreamDataWriter;
import org.objectweb.celtix.servicemodel.DataWriter;
import org.objectweb.celtix.servicemodel.DataWriterFactory;

public class JAXBDataWriterFactory extends JAXBDataFactoryBase implements DataWriterFactory {
    private static final Class<?> SUPPORTED_FORMATS[] = new Class<?>[] {Node.class,
                                                                        XMLEventWriter.class,
                                                                        XMLStreamWriter.class};

    @SuppressWarnings("unchecked")
    public <T> DataWriter<T> createWriter(Class<T> cls) {
        if (cls == XMLStreamWriter.class) {
            return (DataWriter<T>)new XMLStreamDataWriter(this);
        } else if (cls == XMLEventWriter.class) {
            return (DataWriter<T>)new EventDataWriter(this);            
        }
        return null;
    }

    public Class<?>[] getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

}
