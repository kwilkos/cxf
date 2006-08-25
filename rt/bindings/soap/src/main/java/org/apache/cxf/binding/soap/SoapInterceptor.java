package org.apache.cxf.binding.soap;

import java.net.URI;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.interceptor.Interceptor;

public interface SoapInterceptor extends Interceptor<SoapMessage> {
    Set<URI> getRoles();
    Set<QName> getUnderstoodHeaders();
}
