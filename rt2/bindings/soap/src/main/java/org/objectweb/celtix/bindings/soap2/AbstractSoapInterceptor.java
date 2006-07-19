package org.objectweb.celtix.bindings.soap2;

import java.net.URI;
import java.util.Set;

import javax.xml.namespace.QName;

import org.objectweb.celtix.phase.AbstractPhaseInterceptor;

public abstract class AbstractSoapInterceptor extends AbstractPhaseInterceptor implements SoapInterceptor {

    public Set<URI> getRoles() {
        return null;
    }

    public Set<QName> getUnderstoodHeaders() {
        return null;
    }

}
