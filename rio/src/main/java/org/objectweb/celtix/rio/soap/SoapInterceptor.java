package org.objectweb.celtix.rio.soap;

import java.util.List;

import javax.xml.namespace.QName;

public interface SoapInterceptor {
    List<QName> getRoles();
    List<QName> getUnderstoodHeaders();
}
