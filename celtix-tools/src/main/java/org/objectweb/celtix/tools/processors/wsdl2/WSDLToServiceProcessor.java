package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.xml.WSDLWriter;

import javax.xml.namespace.QName;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.extensions.jms.JMSAddress;
import org.objectweb.celtix.tools.extensions.jms.JMSAddressSerializer;

import org.objectweb.celtix.tools.utils.FileWriterUtil;

public class WSDLToServiceProcessor extends WSDLToProcessor {

    private static final String NEW_FILE_NAME_MODIFIER = "-service";
    private static final String WSDL_FILE_NAME_EXT = ".wsdl";
    private static final String HTTP_PREFIX = "http://localhost:9000";
    
    private Map services;
    private Service service;
    private Map ports;
    private Port port;
    private Binding binding;
    
    public void process() throws ToolException {
        init();
        if (isServicePortExisted()) {
            throw new ToolException("Input server and port already exist in imported contract.");
        }        
        if (!isBindingExisted()) {
            throw new ToolException("Input binding does not exist in imported contract.");
        }
        doAppendService();
    }
    
    private boolean isServicePortExisted() {
        return isServiceExisted() && isPortExisted();
    }
    
    private boolean isServiceExisted() {
        services = wsdlDefinition.getServices();
        if (services == null) {
            return false;
        }
        Iterator it = services.keySet().iterator();
        while (it.hasNext()) {
            QName serviceQName = (QName) it.next();
            String serviceName = serviceQName.getLocalPart();  
            if (serviceName.equals(env.get(ToolConstants.CFG_SERVICE))) {
                service = (Service) services.get(serviceQName);
                break;
            }
        }          
        return (service == null) ? false : true;        
    }
    
    private boolean isPortExisted() {
        ports = service.getPorts(); 
        if (ports == null) {            
            return false;
        }
        Iterator it = ports.keySet().iterator();
        while (it.hasNext()) {
            String portName = (String) it.next();               
            if (portName.equals(env.get(ToolConstants.CFG_PORT))) {
                port = (Port) ports.get(portName);
                break;
            }
        }      
        return (port == null) ? false : true;
    }
    
    private boolean isBindingExisted() {
        Map bindings = wsdlDefinition.getBindings();
        if (bindings == null) {
            return false;
        }
        Iterator it = bindings.keySet().iterator();
        while (it.hasNext()) {
            QName bindingQName = (QName) it.next();
            String bindingName = bindingQName.getLocalPart();
            String attrBinding = (String) env.get(ToolConstants.CFG_BINDING_ATTR);
            if (attrBinding.equals(bindingName)) {
                binding = (Binding) bindings.get(bindingQName);
            }
        }      
        return (binding == null) ? false : true;
    }
    
    protected void init() throws ToolException {
        parseWSDL((String)env.get(ToolConstants.CFG_WSDLURL));        
    }
    
    private void doAppendService() throws ToolException {
        if (service == null) {
            service = wsdlDefinition.createService();
            service.setQName(new QName(WSDLConstants.WSDL_PREFIX, 
                                       (String) env.get(ToolConstants.CFG_SERVICE)));
        }
        if (port == null) {
            port = wsdlDefinition.createPort();
            port.setName((String) env.get(ToolConstants.CFG_PORT));
            port.setBinding(binding);
        }        
        setAddrElement();
        service.addPort(port);
        wsdlDefinition.addService(service);        

        WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
        Writer outputWriter = getOutputWriter();
        try {            
            wsdlWriter.writeWSDL(wsdlDefinition, outputWriter);
        } catch (WSDLException wse) {
            throw new ToolException("can not write modified wsdl, due to " + wse.getMessage(), wse);
        }
        try {
            outputWriter.close();
        } catch (IOException ioe) {
            throw new ToolException("close wsdl output file failed, due to " + ioe.getMessage(), ioe);
        }
        
    }
    
    private void setAddrElement() throws ToolException {
        ExtensionRegistry extReg = this.wsdlReader.getExtensionRegistry();
        if (extReg == null) {
            extReg = wsdlFactory.newPopulatedExtensionRegistry();
        }
        if ("http".equalsIgnoreCase((String) env.get(ToolConstants.CFG_TRANSPORT))) {
            SOAPAddress soapAddress = null;
            try {
                soapAddress = (SOAPAddress) extReg.createExtension(Port.class,
                                                                   WSDLConstants.NS_SOAP_BINDING_ADDRESS);
            } catch (WSDLException wse) {
                throw new ToolException("Create soap address ext element failed, due to " + wse);
            }
            if (env.get(ToolConstants.CFG_ADDRESS) != null) {
                soapAddress.setLocationURI((String) env.get(ToolConstants.CFG_ADDRESS));
            } else {
                soapAddress.setLocationURI(HTTP_PREFIX + "/" + env.get(ToolConstants.CFG_SERVICE) 
                                           + "/" + env.get(ToolConstants.CFG_PORT));
            }   
            port.addExtensibilityElement(soapAddress);
        } else if ("jms".equalsIgnoreCase((String) env.get(ToolConstants.CFG_TRANSPORT))) {
            JMSAddress jmsAddress = null;
            JMSAddressSerializer jmsAddressSerializer = new JMSAddressSerializer(); 
            try {
                extReg.registerSerializer(JMSAddress.class, ToolConstants.JMS_ADDRESS,
                                          jmsAddressSerializer);
                extReg.registerDeserializer(JMSAddress.class, ToolConstants.JMS_ADDRESS,
                                            jmsAddressSerializer);
                jmsAddress = (JMSAddress) extReg.createExtension(SOAPAddress.class,
                                                                 ToolConstants.JMS_ADDRESS);
            } catch (WSDLException wse) {
                throw new ToolException("Create soap address ext element failed, due to " + wse);
            }
            port.addExtensibilityElement(jmsAddress);    
        }
    }
    
    private Writer getOutputWriter() throws ToolException {        
        Writer writer = null;        
        String newName = null;
        String outputDir;
        
        if (env.get(ToolConstants.CFG_OUTPUTFILE) != null) {
            newName = (String) env.get(ToolConstants.CFG_OUTPUTFILE);
        } else {
            String oldName = (String) env.get(ToolConstants.CFG_WSDLURL);
            int position = oldName.lastIndexOf("/"); 
            if (position < 0) {
                position = oldName.lastIndexOf("\\");
            }
            if (position >= 0) {
                oldName = oldName.substring(position + 1, oldName.length());
            }
            if (oldName.toLowerCase().indexOf(WSDL_FILE_NAME_EXT) >= 0) {
                newName = oldName.substring(0, oldName.length() - 5)
                        + NEW_FILE_NAME_MODIFIER + WSDL_FILE_NAME_EXT;               
            } else {
                newName = oldName + NEW_FILE_NAME_MODIFIER;
            }
        }
        if (env.get(ToolConstants.CFG_OUTPUTDIR) != null) {
            outputDir = (String) env.get(ToolConstants.CFG_OUTPUTDIR);
            if (!(outputDir.substring(outputDir.length() - 1).equals("/") 
                        || outputDir.substring(outputDir.length() - 1).equals("\\"))) {
                outputDir = outputDir + "/";
            }            
        } else {
            outputDir = "./";
        }        
        FileWriterUtil fw = new FileWriterUtil(outputDir);
        try {
            writer = fw.getWriter("", newName);
        } catch (IOException ioe) {
            throw new ToolException("Failed to write " + env.get(ToolConstants.CFG_OUTPUTDIR) 
                                    + System.getProperty("file.seperator") + newName, ioe);
        }        
        return writer;
    }
}
