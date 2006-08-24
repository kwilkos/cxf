package org.apache.cxf.bindings.soap;

import java.net.URI;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.interceptors.Interceptor;

public interface SoapInterceptor extends Interceptor<SoapMessage> {
    Set<URI> getRoles();
    Set<QName> getUnderstoodHeaders();
}
