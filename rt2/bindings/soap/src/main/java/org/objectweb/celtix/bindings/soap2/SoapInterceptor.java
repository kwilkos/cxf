package org.objectweb.celtix.bindings.soap2;

import java.util.List;

import javax.xml.namespace.QName;

public interface SoapInterceptor {
    List<QName> getRoles();
    List<QName> getUnderstoodHeaders();
}
