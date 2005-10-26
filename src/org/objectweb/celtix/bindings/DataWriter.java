package org.objectweb.celtix.bindings;

import javax.xml.namespace.QName;

public interface DataWriter<T> {
    
    void write(Object obj, T output);
    void write(Object obj, QName elName, T output);

}
