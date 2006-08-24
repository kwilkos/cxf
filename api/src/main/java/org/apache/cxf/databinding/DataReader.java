package org.apache.cxf.databinding;

import javax.xml.namespace.QName;

public interface DataReader<T> {
    Object read(T input);
    Object read(QName name, T input);
    Object read(QName name, T input, Class<?> cls);
}
