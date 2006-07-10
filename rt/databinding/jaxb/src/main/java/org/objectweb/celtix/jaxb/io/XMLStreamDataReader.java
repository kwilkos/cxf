package org.objectweb.celtix.jaxb.io;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.Holder;

import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;

public class XMLStreamDataReader<T> implements DataReader<T> {
    final JAXBDataBindingCallback callback;

    public XMLStreamDataReader(JAXBDataBindingCallback cb) {
        callback = cb;
    }

    public Object read(int idx, T input) {
        return read(null, idx, input);
    }

    public Object read(QName name, int idx, T input) {
        Class<?> cls;
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
        
        XMLStreamReader reader = (XMLStreamReader)input;

        return JAXBEncoderDecoder.unmarshall(callback.getJAXBContext(),
                                             callback.getSchema(),
                                             reader,
                                             name,
                                             cls);
    }
    public void readWrapper(ObjectMessageContext objCtx, boolean isOutBound, T input) {
        // TODO.
    }
}
