package org.objectweb.celtix.jaxb;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import org.objectweb.celtix.jaxb.io.EventDataReader;
import org.objectweb.celtix.jaxb.io.XMLStreamDataReader;
import org.objectweb.celtix.servicemodel.DataReader;
import org.objectweb.celtix.servicemodel.DataReaderFactory;


public class JAXBDataReaderFactory extends JAXBDataFactoryBase implements DataReaderFactory {
    private static final Class<?> SUPPORTED_FORMATS[] = new Class<?>[] {Node.class,
                                                                        XMLEventReader.class,
                                                                        XMLStreamReader.class};
    
    

    
    @SuppressWarnings("unchecked")
    public <T> DataReader<T> createReader(Class<T> cls) {
        if (cls == XMLEventReader.class) {
            return (DataReader<T>)new XMLStreamDataReader(this);
        } else if (cls == XMLEventReader.class) {
            return (DataReader<T>)new EventDataReader(this);            
        }
        // TODO Auto-generated method stub
        return null;
    }

    public Class<?>[] getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }


}
