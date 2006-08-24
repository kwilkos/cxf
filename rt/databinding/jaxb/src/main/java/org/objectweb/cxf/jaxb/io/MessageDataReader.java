package org.apache.cxf.jaxb.io;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.jaxb.JAXBDataReaderFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;
import org.apache.cxf.jaxb.attachments.JAXBAttachmentUnmarshaller;
import org.apache.cxf.message.Message;

public class MessageDataReader implements DataReader<Message> {
    
    final JAXBDataReaderFactory factory;

    public MessageDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }

    public Object read(Message input) {
        return read(null, input);
    }
    
    public Object read(QName name, Message input) {
        return read(name, input, null);
    }
    
    public Object read(QName name, Message input, Class cls) {
        JAXBAttachmentUnmarshaller au = null;        
        if (input.get(Message.ATTACHMENT_DESERIALIZER) != null) {
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
