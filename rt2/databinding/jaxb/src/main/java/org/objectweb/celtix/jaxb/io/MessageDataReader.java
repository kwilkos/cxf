package org.objectweb.celtix.jaxb.io;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

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
        Object source = null;
        XMLStreamReader xsr = (XMLStreamReader)input.getContent(XMLStreamReader.class);
        if (xsr != null) {
            source = xsr;
        } else {
            XMLEventReader xer = (XMLEventReader)input.getContent(XMLEventReader.class);
            if (xer != null) {
                source = xer;
            } else {
                Node node = (Node)input.getContent(Node.class);
                source = node;
            }
        }
        if (source == null) {
            return null;
        }
        return JAXBEncoderDecoder.unmarshall(factory.getJAXBContext(),
                                             factory.getSchema(), source,
                                             name,
                                             cls, 
                                             au);
    }

}
