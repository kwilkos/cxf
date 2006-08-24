package org.apache.cxf.jaxb.io;


import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.jaxb.JAXBDataReaderFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;

public class NodeDataReader implements DataReader<Node> {
    final JAXBDataReaderFactory factory;
    
    public NodeDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }
    
    public Object read(Node input) {
        return read(null, input);
    }

    public Object read(QName name, Node xmlNode) {
        return read(name, xmlNode, null);
    }
    
    public Object read(QName name, Node xmlNode, Class cls) {
        return JAXBEncoderDecoder.unmarshall(factory.getJAXBContext(),
                                             factory.getSchema(),
                                             xmlNode,
                                             name,
                                             cls,
                                             null);
    }
    
   
}
