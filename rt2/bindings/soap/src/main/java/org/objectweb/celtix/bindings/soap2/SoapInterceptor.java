package org.objectweb.celtix.bindings.soap2;

import java.net.URI;
import java.util.Set;

import javax.xml.namespace.QName;

import org.objectweb.celtix.interceptors.Interceptor;

public interface SoapInterceptor extends Interceptor {
    Set<URI> getRoles();
    Set<QName> getUnderstoodHeaders();
}
