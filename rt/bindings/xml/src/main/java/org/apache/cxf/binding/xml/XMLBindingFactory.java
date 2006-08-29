package org.apache.cxf.binding.xml;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.xml.interceptor.XMLMessageInInterceptor;
import org.apache.cxf.binding.xml.interceptor.XMLMessageOutInterceptor;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.service.model.BindingInfo;

public class XMLBindingFactory extends AbstractBindingFactory {

    private Map cachedBinding = new HashMap<BindingInfo, Binding>();

    public Binding createBinding(BindingInfo binding) {

        if (cachedBinding.get(binding) != null) {
            return (Binding) cachedBinding.get(binding);
        }

        XMLBinding xb = new XMLBinding();
        
        xb.getInInterceptors().add(new StaxInInterceptor());
        xb.getInInterceptors().add(new XMLMessageInInterceptor());
        xb.getInFaultInterceptors().add(xb.getInFaultInterceptor());
        
        xb.getOutInterceptors().add(new StaxOutInterceptor());
        xb.getOutInterceptors().add(new XMLMessageOutInterceptor());
        xb.getOutFaultInterceptors().add(xb.getOutFaultInterceptor());

        return xb;
    }

}
