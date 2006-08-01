package org.objectweb.celtix.jaxb.io;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Node;

import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.jaxb.JAXBAttachmentMarshaller;
import org.objectweb.celtix.jaxb.JAXBDataWriterFactory;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.message.Message;

public class MessageDataWriter implements DataWriter<Message> {

    final JAXBDataWriterFactory factory;

    public MessageDataWriter(JAXBDataWriterFactory cb) {
        factory = cb;
    }
    
    public void write(Object obj, Message output) {
        write(obj, null, output);
    }
    
    public void write(Object obj, QName elName, Message output) {
        //if the mtom is enabled, we need to create the attachment mashaller
        JAXBAttachmentMarshaller am = new JAXBAttachmentMarshaller(output); 
        Object source = null;        
        XMLStreamWriter xsw = (XMLStreamWriter)output.getContent(XMLStreamWriter.class);
        if (xsw != null) {
            source = xsw;
        } else {
            XMLEventWriter xew = (XMLEventWriter)output.getContent(XMLEventWriter.class);
            if (xew != null) {
                source = xew;
            } else {
                Node node = (Node)output.getContent(Node.class);
                source = node;
            }
        }
        if (source == null) {
            return;
        }

        if (obj != null) {
            JAXBEncoderDecoder.marshall(factory.getJAXBContext(),
                                        factory.getSchema(), obj,
                                        elName, source, am);
        }
    }

}
