package org.objectweb.celtix.jaxb.io;


import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.jaxb.JAXBDataReaderFactory;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;

public class XMLStreamDataReader implements DataReader<XMLStreamReader> {
    final JAXBDataReaderFactory factory;

    public XMLStreamDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }

    public Object read(int idx, XMLStreamReader input) {
        return read(null, idx, input);
    }

    public Object read(QName name, int idx, XMLStreamReader reader) {
        Class<?> cls = null;
        /*
         * REVISIT - this should be in jaxws layer
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
                                             reader,
                                             name,
                                             cls,
                                             null);
    }
}
