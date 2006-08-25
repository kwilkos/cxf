package org.objectweb.celtix.wsdl11;

import javax.wsdl.Binding;

import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.ServiceInfo;

public interface WSDLBindingFactory extends BindingFactory {
    BindingInfo createBindingInfo(ServiceInfo serviceInfo, Binding binding);
}
