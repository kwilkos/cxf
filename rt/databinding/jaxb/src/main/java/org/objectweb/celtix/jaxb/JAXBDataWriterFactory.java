package org.apache.cxf.jaxb;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Node;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.databinding.DataWriterFactory;
import org.apache.cxf.jaxb.io.EventDataWriter;
import org.apache.cxf.jaxb.io.NodeDataWriter;
import org.apache.cxf.jaxb.io.XMLStreamDataWriter;

public final class JAXBDataWriterFactory extends JAXBDataFactoryBase implements DataWriterFactory {
    private static final Class<?> SUPPORTED_FORMATS[] = new Class<?>[] {Node.class,
                                                                        XMLEventWriter.class,
                                                                        XMLStreamWriter.class};
    
    public JAXBDataWriterFactory() {
        
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
