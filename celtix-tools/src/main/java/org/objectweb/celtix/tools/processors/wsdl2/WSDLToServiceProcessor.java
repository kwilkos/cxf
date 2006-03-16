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

public class WSDLToServiceProcessor extends WSDLToProcessor {

    private static final String NEW_FILE_NAME_MODIFIER = "-service";
    private static final String HTTP_PREFIX = "http://localhost:9000";

    private Map services;
    private Service service;
    private Map ports;
    private Port port;
    private Binding binding;

    public void process() throws ToolException {
        init();
        if (isServicePortExisted()) {
            throw new ToolException("Input service and port already exist in imported contract.");
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
            QName serviceQName = (QName)it.next();
            String serviceName = serviceQName.getLocalPart();
            if (serviceName.equals(env.get(ToolConstants.CFG_SERVICE))) {
                service = (Service)services.get(serviceQName);
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
            String portName = (String)it.next();
            if (portName.equals(env.get(ToolConstants.CFG_PORT))) {
                port = (Port)ports.get(portName);
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
            QName bindingQName = (QName)it.next();
            String bindingName = bindingQName.getLocalPart();
            String attrBinding = (String)env.get(ToolConstants.CFG_BINDING_ATTR);
            if (attrBinding.equals(bindingName)) {
                binding = (Binding)bindings.get(bindingQName);
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
            service
                .setQName(new QName(WSDLConstants.WSDL_PREFIX, (String)env.get(ToolConstants.CFG_SERVICE)));
        }
        if (port == null) {
            port = wsdlDefinition.createPort();
            port.setName((String)env.get(ToolConstants.CFG_PORT));
            port.setBinding(binding);
        }
        setAddrElement();
        service.addPort(port);
        wsdlDefinition.addService(service);

        WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
        Writer outputWriter = getOutputWriter(NEW_FILE_NAME_MODIFIER);
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
        if ("http".equalsIgnoreCase((String)env.get(ToolConstants.CFG_TRANSPORT))) {
            SOAPAddress soapAddress = null;
            try {
                soapAddress = (SOAPAddress)extReg.createExtension(Port.class,
                                                                  WSDLConstants.NS_SOAP_BINDING_ADDRESS);
            } catch (WSDLException wse) {
                throw new ToolException("Create soap address ext element failed, due to " + wse);
            }
            if (env.get(ToolConstants.CFG_ADDRESS) != null) {
                soapAddress.setLocationURI((String)env.get(ToolConstants.CFG_ADDRESS));
            } else {
                soapAddress.setLocationURI(HTTP_PREFIX + "/" + env.get(ToolConstants.CFG_SERVICE) + "/"
                                           + env.get(ToolConstants.CFG_PORT));
            }
            port.addExtensibilityElement(soapAddress);
        } else if ("jms".equalsIgnoreCase((String)env.get(ToolConstants.CFG_TRANSPORT))) {
            JMSAddress jmsAddress = null;
            JMSAddressSerializer jmsAddressSerializer = new JMSAddressSerializer();
            try {
                extReg.registerSerializer(JMSAddress.class, ToolConstants.JMS_ADDRESS, jmsAddressSerializer);
                extReg
                    .registerDeserializer(JMSAddress.class, ToolConstants.JMS_ADDRESS, jmsAddressSerializer);
                jmsAddress = (JMSAddress)extReg.createExtension(Port.class, ToolConstants.JMS_ADDRESS);
                if (env.optionSet(ToolConstants.JMS_ADDR_DEST_STYLE)) {
                    jmsAddress.setDestinationStyle((String)env.get(ToolConstants.JMS_ADDR_DEST_STYLE));
                }
                if (env.optionSet(ToolConstants.JMS_ADDR_INIT_CTX)) {
                    jmsAddress.setInitialContextFactory((String)env.get(ToolConstants.JMS_ADDR_INIT_CTX));
                }
                if (env.optionSet(ToolConstants.JMS_ADDR_JNDI_DEST)) {
                    jmsAddress.setJndiDestinationName((String)env.get(ToolConstants.JMS_ADDR_JNDI_DEST));
                }
                if (env.optionSet(ToolConstants.JMS_ADDR_JNDI_FAC)) {
                    jmsAddress.setJndiConnectionFactoryName((String)env.get(ToolConstants.JMS_ADDR_JNDI_FAC));
                }
                if (env.optionSet(ToolConstants.JMS_ADDR_JNDI_URL)) {
                    jmsAddress.setJndiProviderURL((String)env.get(ToolConstants.JMS_ADDR_JNDI_URL));
                }
                if (env.optionSet(ToolConstants.JMS_ADDR_MSGID_TO_CORRID)) {
                    jmsAddress.setUseMessageIDAsCorrelationID(Boolean.getBoolean((String)env
                        .get(ToolConstants.JMS_ADDR_MSGID_TO_CORRID)));
                }
                if (env.optionSet(ToolConstants.JMS_ADDR_SUBSCRIBER_NAME)) {
                    jmsAddress.setDurableSubscriberName((String)env
                        .get(ToolConstants.JMS_ADDR_SUBSCRIBER_NAME));
                }
            } catch (WSDLException wse) {
                throw new ToolException("Create soap address ext element failed, due to " + wse);
            }
            port.addExtensibilityElement(jmsAddress);
        }
    }

}
