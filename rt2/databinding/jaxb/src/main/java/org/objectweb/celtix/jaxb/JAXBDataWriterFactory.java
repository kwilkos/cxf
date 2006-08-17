package org.objectweb.celtix.jaxb;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Node;

import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.databinding.DataWriterFactory;
import org.objectweb.celtix.jaxb.io.EventDataWriter;
import org.objectweb.celtix.jaxb.io.NodeDataWriter;
import org.objectweb.celtix.jaxb.io.XMLStreamDataWriter;

public final class JAXBDataWriterFactory extends JAXBDataFactoryBase implements DataWriterFactory {
    private static final Class<?> SUPPORTED_FORMATS[] = new Class<?>[] {Node.class,
                                                                        XMLEventWriter.class,
                                                                        XMLStreamWriter.class};
    private static JAXBDataWriterFactory dataWriterFactory;
    
    private JAXBDataWriterFactory() {
        
    }
    
    public static synchronized JAXBDataWriterFactory getInstance() {
        if (dataWriterFactory == null) {
            dataWriterFactory = new JAXBDataWriterFactory();
        }
        return dataWriterFactory;
    }
    
    @SuppressWarnings("unchecked")
    public <T> DataWriter<T> createWriter(Class<T> cls) {
        
        if (cls == XMLStreamWriter.class) {
            return (DataWriter<T>)new XMLStreamDataWriter(this);
        } else if (cls == XMLEventWriter.class) {
            return (DataWriter<T>)new EventDataWriter(this);            
        } else if (cls == Node.class) {
            return (DataWriter<T>)new NodeDataWriter(this);
        }
        
        return null;
    }

    public Class<?>[] getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

}
