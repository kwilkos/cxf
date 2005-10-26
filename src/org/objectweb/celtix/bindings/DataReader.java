package org.objectweb.celtix.bindings;

import javax.xml.namespace.QName;

public interface DataReader<T> {

    Object read(QName name, T input);
    
}
