package org.objectweb.celtix.bus.bindings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPBinding;


public class WSDLMetaDataCache {
    
    Definition definition;
    Port port;
    Binding binding;
    
    Map<String, WSDLOperationInfo> allOperationInfo;

    
    
    public WSDLMetaDataCache(Definition def, Port p) {
        definition = def;
        port = p;

        binding = port.getBinding();
        if (binding == null) {
            throw new IllegalArgumentException("WSDL binding cannot be found for " + port.getName());
        }
    }
    
    public Definition getDefinition() {
        return definition;
    }
    public String getTargetNamespace() {
        return binding.getPortType().getQName().getNamespaceURI();
    }

    public javax.jws.soap.SOAPBinding.Style getStyle() {

        // Find the binding style
        javax.jws.soap.SOAPBinding.Style style = null;
        if (binding != null) {
            SOAPBinding soapBinding = getExtensibilityElement(binding.getExtensibilityElements(),
                                                              SOAPBinding.class);
            if (soapBinding != null) {
                style = javax.jws.soap.SOAPBinding.Style.valueOf(soapBinding.getStyle().toUpperCase());
            }
        }
        // Default to document
        return (style == null) ? javax.jws.soap.SOAPBinding.Style.DOCUMENT : style;
    }
    
    public Map<String, WSDLOperationInfo> getAllOperationInfo() {
        if (allOperationInfo == null) {
            allOperationInfo = new HashMap<String, WSDLOperationInfo>();
            for (Iterator<?> it = binding.getBindingOperations().iterator(); it.hasNext();) {
                final BindingOperation bindingOperation = (BindingOperation)it.next();
                if (bindingOperation.getOperation() != null) {
                    WSDLOperationInfo data = new WSDLOperationInfo(this,
                                                                   bindingOperation);
                    allOperationInfo.put(data.getName(), data);
                }
            }
        }
        return allOperationInfo;
    } 
    public WSDLOperationInfo getOperationInfo(String operation) {
        return getAllOperationInfo().get(operation);
    }

    
    
    private static <T> T getExtensibilityElement(List<?> elements, Class<T> type) {
        for (Iterator<?> i = elements.iterator(); i.hasNext();) {
            Object element = i.next();
            if (type.isInstance(element)) {
                return type.cast(element);
            }
        }
        return null;
    }    
    
}
