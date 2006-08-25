package org.apache.cxf.binding.soap;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.phase.AbstractPhaseInterceptor;

public abstract class AbstractSoapInterceptor extends AbstractPhaseInterceptor<SoapMessage> 
    implements SoapInterceptor {

    public Set<URI> getRoles() {
        return Collections.emptySet();
    }

    public Set<QName> getUnderstoodHeaders() {
        return Collections.emptySet();
    }
}
