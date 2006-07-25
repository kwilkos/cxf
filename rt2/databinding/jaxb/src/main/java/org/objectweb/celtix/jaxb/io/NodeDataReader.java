package org.objectweb.celtix.jaxb.io;


import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.jaxb.JAXBDataReaderFactory;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;

public class NodeDataReader implements DataReader<Node> {
    final JAXBDataReaderFactory factory;
    
    public NodeDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }
    
    public Object read(int idx, Node input) {
        return read(null, idx, input);
    }
    
    public Object read(QName name, int idx, Node xmlNode) {
        Class<?> cls = null;
        /*
        if (idx == -1) {
            cls = callback.getMethod().getReturnType();
        } else {
            cls = callback.getMethod().getParameterTypes()[idx];
            if (cls.isAssignableFrom(Holder.class)) {
                //INOUT and OUT Params are mapped to Holder<T>. 
                Type[] genericParameterTypes = callback.getMethod().getGenericParameterTypes();
                //ParameterizedType represents Holder<?>
                ParameterizedType paramType = (ParameterizedType)genericParameterTypes[idx];
                cls = JAXBEncoderDecoder.getClassFromType(
                                         paramType.getActualTypeArguments()[0]);                
            }
        }
        */
        
        return JAXBEncoderDecoder.unmarshall(factory.getJAXBContext(),
                                             factory.getSchema(),
                                             xmlNode, name, cls, null);
    }
    
   
}
