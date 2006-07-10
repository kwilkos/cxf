package org.objectweb.celtix.jaxb.io;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import org.objectweb.celtix.jaxb.JAXBDataWriterFactory;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.servicemodel.DataWriter;

public class NodeDataWriter implements DataWriter<Node> {
    final JAXBDataWriterFactory factory;
    
    public NodeDataWriter(JAXBDataWriterFactory cb) {
        factory = cb;
    }
    public void write(Object obj, Node output) {
        write(obj, null, output);
    }
    public void write(Object obj, QName elName, Node output) {
        if (obj != null) {
            JAXBEncoderDecoder.marshall(factory.getJAXBContext(),
                                        factory.getSchema(), obj,
                                        elName, output);
        }
    }

}
