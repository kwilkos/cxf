package org.objectweb.celtix.bus.configuration.wsdl;

import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationProvider;

public class WsdlPortProvider implements ConfigurationProvider {
    
    private final Port port;
    
    public WsdlPortProvider(Port p) {
        port = p;
    }
    
    
    public void init(Configuration configuration) {
        // not needed
    }
    
    public Object getObject(String name) {
        if (null == port) {
            return null;
        }
        if ("bindingId".equals(name)) {
            return getBinding();
        } else if ("address".equals(name)) {
            return getAddress();
        } 
        return null;
    }
    
    public boolean setObject(String name, Object value) {
        // TODO Auto-generated method stub
        return false;
    }


    private Object getBinding() {
        Binding binding = port.getBinding();
        if (null == binding) {
            // TODO
            return null;
        }
        
        List list = binding.getExtensibilityElements();     
        if (list.isEmpty()) {
            // TODO
            // throw new WebServiceException("Could not get the extension element URI");
            return null;
        }
      
        return ((ExtensibilityElement)list.get(0)).getElementType().getNamespaceURI();
    }
    
    private Object getAddress() {
        List<?> list = port.getExtensibilityElements();
        for (Object ep : list) {
            ExtensibilityElement ext = (ExtensibilityElement)ep;
            if (ext instanceof SOAPAddress) {
                return ((SOAPAddress)ext).getLocationURI();
            }
        }
        return null;
    }

}
