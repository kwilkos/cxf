package org.objectweb.celtix.bus.jaxws.io;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bus.jaxws.DynamicDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;

public class SOAPBodyDataReader<T> implements DataReader<T> {
    
    final DynamicDataBindingCallback callback;

    public SOAPBodyDataReader(DynamicDataBindingCallback cb) {
        callback = cb;
    }

    public Object read(int idx, T input) {
        Source obj = null;        
        SOAPBody src = (SOAPBody)input;    
        try {
            Document doc = src.extractContentAsDocument();
            assert doc != null;
    
            if (DOMSource.class.isAssignableFrom(callback.getSupportedFormats()[0])) {
                obj = new DOMSource();
                ((DOMSource)obj).setNode(doc);
            }
        } catch (SOAPException se) {
            se.printStackTrace();
        }
        return obj;
    }

    public Object read(QName name, int idx, T input) {
        // TODO Auto-generated method stub
        return null;
    }

    public void readWrapper(ObjectMessageContext objCtx, boolean isOutBound, T input) {
        // TODO Auto-generated method stub

    }

}
