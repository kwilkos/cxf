package org.objectweb.celtix.tools.java2wsdl.generator;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.common.model.WSDLModel;

public class ServiceGenerator {
    private static final String ADDRESS_URI = "http://localhost/changme";
    private WSDLModel wmodel;
    private Definition definition;
    private ExtensionRegistry extensionRegistry;
    
    public ServiceGenerator(WSDLModel model) {
        this.definition = model.getDefinition();
        this.wmodel = model;
        extensionRegistry = definition.getExtensionRegistry();
        
    }
    
    public void generate() {
        Service service = definition.createService();
        service.setQName(new QName(WSDLConstants.WSDL_PREFIX, wmodel.getServiceName()));
        Port port = definition.createPort();
        port.setName(wmodel.getPortName());
        Binding binding = definition.createBinding();
        String targetNameSpace = wmodel.getTargetNameSpace();
        binding.setQName(new QName(targetNameSpace, wmodel.getPortTypeName() + "Binding"));
        port.setBinding(binding);
        SOAPAddress soapAddress = null;
        try {
            soapAddress = (SOAPAddress)extensionRegistry
                .createExtension(Port.class, new QName(WSDLConstants.SOAP11_NAMESPACE, "address"));
            soapAddress.setLocationURI(ADDRESS_URI);
        } catch (WSDLException e) {
            throw new ToolException(e.getMessage(), e);
        }
        port.addExtensibilityElement(soapAddress);
        service.addPort(port);
        definition.addService(service);
    }
}
