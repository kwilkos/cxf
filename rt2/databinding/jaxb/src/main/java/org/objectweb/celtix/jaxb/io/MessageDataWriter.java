package org.objectweb.celtix.jaxb.io;

import javax.xml.namespace.QName;

import org.objectweb.celtix.databinding.DataWriter;
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
        //if (output.getAttachments().size() > 0)
        if (obj != null) {
            JAXBEncoderDecoder.marshall(factory.getJAXBContext(),
                                        factory.getSchema(), obj,
                                        elName, output, null);
        }
    }

}
