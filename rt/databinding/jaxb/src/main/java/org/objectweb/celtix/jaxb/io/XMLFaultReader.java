package org.objectweb.celtix.jaxb.io;

import java.lang.reflect.Constructor;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.datamodel.xml.XMLFault;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;

public class XMLFaultReader<T> implements DataReader<T> {
    final JAXBDataBindingCallback callback;
    
    public XMLFaultReader(JAXBDataBindingCallback cb) {
        callback = cb;
    }

    public Object read(QName name, int idx, T input) {
        XMLFault fault = (XMLFault)input;
        if (fault.getFaultDetail() != null) {
            NodeList list = fault.getFaultDetail().getChildNodes();

            QName faultName;
            for (int i = 0; i < list.getLength(); i++) {
                Node entry = list.item(i);
                if (entry.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                
                faultName = new QName(entry.getNamespaceURI(),
                                      entry.getLocalName());
        
                Class<?> clazz = callback.getWebFault(faultName);
                try {
                    if (clazz != null) {
                        Class<?> faultInfo = clazz.getMethod("getFaultInfo").getReturnType();
                        Object obj = JAXBEncoderDecoder.unmarshall(callback.getJAXBContext(), 
                                                                   callback.getSchema(),
                                                                   entry,
                                                                   faultName,
                                                                   faultInfo);
                        Constructor<?> ctor = clazz.getConstructor(String.class,
                                                                   obj.getClass());
                        return ctor.newInstance(fault.getFaultString(), obj);
                    }
                } catch (Exception ex) {
                    throw new WebServiceException("error in unmarshal of XMLFault", ex);
                }
            }
        }
        return new WebServiceException("Unknow XMLFault exception");
    }

    public Object read(int idx, T input) {
        return read(null, idx, input);
    }

    public void readWrapper(ObjectMessageContext objCtx, boolean isOutBound, T input) {
        throw new UnsupportedOperationException();
    }
}
