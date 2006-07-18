package org.objectweb.celtix.bindings.soap2;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

public interface SoapInterceptor {
    List<URI> getRoles();
    List<QName> getUnderstoodHeaders();
}
