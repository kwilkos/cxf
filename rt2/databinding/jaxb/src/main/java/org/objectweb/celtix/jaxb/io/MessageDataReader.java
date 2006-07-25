package org.objectweb.celtix.jaxb.io;

import javax.xml.namespace.QName;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.jaxb.JAXBAttachmentUnmarshaller;
import org.objectweb.celtix.jaxb.JAXBDataReaderFactory;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.message.Message;

public class MessageDataReader implements DataReader<Message> {
    
    final JAXBDataReaderFactory factory;

    public MessageDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }

    public Object read(int idx, Message input) {
        return read(null, idx, input);
    }

    public Object read(QName name, int idx, Message input) {
        Class<?> cls = null;
        
        JAXBAttachmentUnmarshaller au = null;        
        if (input.getAttachments().size() > 0) {
            au = new JAXBAttachmentUnmarshaller(input); 
        }
        
        return JAXBEncoderDecoder.unmarshall(factory.getJAXBContext(),
                                             factory.getSchema(), input,
                                             name,
                                             cls, 
                                             au);
    }

}
