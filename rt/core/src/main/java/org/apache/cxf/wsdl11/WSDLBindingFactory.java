package org.apache.cxf.wsdl11;

import javax.wsdl.Binding;

import org.apache.cxf.binding.BindingFactory;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.ServiceInfo;

public interface WSDLBindingFactory extends BindingFactory {
    BindingInfo createBindingInfo(ServiceInfo serviceInfo, Binding binding);
}
