package org.objectweb.celtix.bus.jaxws.io;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bus.jaxws.DynamicDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;

public class SOAPMessageDataReader<T> implements DataReader<T> {
    
    final DynamicDataBindingCallback callback;

    public SOAPMessageDataReader(DynamicDataBindingCallback cb) {
        callback = cb;
    }

    public Object read(int idx, T input) {
        SOAPMessage src = (SOAPMessage) input;
        Source obj = null;
        try {
            if (DOMSource.class.isAssignableFrom(callback.getSupportedFormats()[0])) {
                obj = new DOMSource(src.getSOAPPart());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }       
        return obj;        
    }

    public Object read(QName name, int idx, T input) {
        return null;
    }

    public void readWrapper(ObjectMessageContext objCtx, boolean isOutBound, T input) {
        //Complete
    }

}
