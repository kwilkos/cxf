package org.objectweb.celtix.jaxb;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.databinding.DataReaderFactory;
import org.objectweb.celtix.jaxb.io.EventDataReader;
import org.objectweb.celtix.jaxb.io.NodeDataReader;
import org.objectweb.celtix.jaxb.io.XMLStreamDataReader;


public final class JAXBDataReaderFactory extends JAXBDataFactoryBase implements DataReaderFactory {
    private static final Class<?> SUPPORTED_FORMATS[] = new Class<?>[] {Node.class,
                                                                        XMLEventReader.class,
                                                                        XMLStreamReader.class};
    
    private static JAXBDataReaderFactory dataReaderFactory;
    
    private JAXBDataReaderFactory() {
        
    }

    public static synchronized JAXBDataReaderFactory getInstance() {
        if (dataReaderFactory == null) {
            dataReaderFactory = new JAXBDataReaderFactory();
        }
        return dataReaderFactory;
    }
    
    @SuppressWarnings("unchecked")
    public <T> DataReader<T> createReader(Class<T> cls) {
        if (cls == XMLStreamReader.class) {
            return (DataReader<T>)new XMLStreamDataReader(this);
        } else if (cls == XMLEventReader.class) {
            return (DataReader<T>)new EventDataReader(this);            
        } else if (cls == Node.class) {
            return (DataReader<T>)new NodeDataReader(this);
        }
        // TODO Auto-generated method stub
        return null;
    }

    public Class<?>[] getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }


}
