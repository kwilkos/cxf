package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.utils.FileWriterUtil;

public class WSDLToSoapProcessor
    extends WSDLToProcessor {

    private static final String NEW_FILE_NAME_MODIFIER = "-soapbinding";
    private static final String WSDL_FILE_NAME_EXT = ".wsdl";

    private ExtensionRegistry extReg;

    private Map portTypes;
    private PortType portType;
    private Binding binding;

    public void process() throws ToolException {
        init();
        if (isBindingExisted()) {
            throw new ToolException("Input binding already exist in imported contract.");
        }
        if (!isPortTypeExisted()) {
            throw new ToolException("Input port type does not exist in imported contract.");
        }
        if (!nameSpaceCheck()) {
            throw new ToolException("For rpc style binding, soap name space (-n) must be provided.");
        }
        extReg = this.wsdlReader.getExtensionRegistry();
        doAppendService();
    }

    private boolean isPortTypeExisted() {
        portTypes = wsdlDefinition.getPortTypes();
        if (portTypes == null) {
            return false;
        }
        Iterator it = portTypes.keySet().iterator();
        while (it.hasNext()) {
            QName existPortQName = (QName)it.next();
            String existPortName = existPortQName.getLocalPart();
            if (existPortName.equals(env.get(ToolConstants.CFG_PORTTYPE))) {
                portType = (PortType)portTypes.get(existPortQName);
                break;
            }
        }
        return (portType == null) ? false : true;
    }

    private boolean isBindingExisted() {
        Map bindings = wsdlDefinition.getBindings();
        if (bindings == null) {
            return false;
        }
        Iterator it = bindings.keySet().iterator();
        while (it.hasNext()) {
            QName existBindingQName = (QName)it.next();
            String existBindingName = existBindingQName.getLocalPart();
            String bindingName = (String)env.get(ToolConstants.CFG_BINDING);
            if (bindingName.equals(existBindingName)) {
                binding = (Binding)bindings.get(existBindingQName);
            }
        }
        return (binding == null) ? false : true;
    }

    private boolean nameSpaceCheck() {
        if (WSDLConstants.RPC.equalsIgnoreCase((String)env.get(ToolConstants.CFG_STYLE))
            && !env.optionSet(ToolConstants.CFG_NAMESPACE)) {
            return false;
        }
        return true;
    }

    protected void init() throws ToolException {
        parseWSDL((String)env.get(ToolConstants.CFG_WSDLURL));
    }

    private void doAppendService() throws ToolException {
        if (binding == null) {
            binding = wsdlDefinition.createBinding();
            binding.setQName(new QName(wsdlDefinition.getTargetNamespace(), (String)env
                .get(ToolConstants.CFG_BINDING)));
            binding.setUndefined(false);
            binding.setPortType(portType);
        }
        setSoapBindingExtElement();
        addBindingOperation();
        wsdlDefinition.addBinding(binding);

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
            throw new ToolException("close wsdl output file failed, due to " + ioe.getMessage(),
                                    ioe);
        }
    }

    private void setSoapBindingExtElement() throws ToolException {
        if (extReg == null) {
            extReg = wsdlFactory.newPopulatedExtensionRegistry();
        }
        SOAPBinding soapBinding = null;
        try {
            soapBinding = (SOAPBinding)extReg.createExtension(Binding.class,
                                                              WSDLConstants.NS_SOAP_BINDING);
        } catch (WSDLException wse) {
            throw new ToolException("Create soap binding ext element failed, due to " + wse);
        }
        soapBinding.setStyle((String)env.get(ToolConstants.CFG_STYLE));
        soapBinding.setTransportURI(WSDLConstants.NS_SOAP11_HTTP_BINDING);
        binding.addExtensibilityElement(soapBinding);
    }

    @SuppressWarnings("unchecked")
    private void addBindingOperation() throws ToolException {
        /**
         * This method won't do unique operation name checking on portType The
         * WS-I Basic Profile[17] R2304 requires that operations within a
         * wsdl:portType have unique values for their name attribute so mapping
         * of WS-I compliant WSDLdescriptions will not generate Java interfaces
         * with overloaded methods. However, for backwards compatibility, JAX-WS
         * supports operation name overloading provided the overloading does not
         * cause conflicts (as specified in the Java Language Specification[25])
         * in the mapped Java service endpoint interface declaration.
         */
        List<Operation> ops = portType.getOperations();
        for (Operation op : ops) {
            BindingOperation bindingOperation = wsdlDefinition.createBindingOperation();
            setSoapOperationExtElement(bindingOperation);
            bindingOperation.setName(op.getName());
            if (op.getInput() != null) {
                bindingOperation.setBindingInput(getBindingInput(op.getInput()));
            }
            if (op.getOutput() != null) {
                bindingOperation.setBindingOutput(getBindingOutput(op.getOutput()));
            }
            if (op.getFaults() != null && op.getFaults().size() > 0) {
                addSoapFaults(op, bindingOperation);
            }
            bindingOperation.setOperation(op);
            binding.addBindingOperation(bindingOperation);
        }
    }

    private void setSoapOperationExtElement(BindingOperation bo) throws ToolException {
        if (extReg == null) {
            extReg = wsdlFactory.newPopulatedExtensionRegistry();
        }
        SOAPOperation soapOperation = null;
        try {
            soapOperation = (SOAPOperation)extReg.createExtension(BindingOperation.class,
                                                                  WSDLConstants.NS_SOAP_OPERATION);
        } catch (WSDLException wse) {
            throw new ToolException("Create soap operation ext element failed, due to " + wse);
        }
        soapOperation.setStyle((String)env.get(ToolConstants.CFG_STYLE));
        soapOperation.setSoapActionURI("");
        bo.addExtensibilityElement(soapOperation);
    }

    private BindingInput getBindingInput(Input input) throws ToolException {
        BindingInput bi = wsdlDefinition.createBindingInput();
        bi.setName(input.getName());
        // As command line won't specify the details of body/header for message
        // parts
        // All input message's parts will be added into one soap body element
        bi.addExtensibilityElement(getSoapBody(BindingInput.class));
        return bi;
    }

    private BindingOutput getBindingOutput(Output output) throws ToolException {
        BindingOutput bo = wsdlDefinition.createBindingOutput();
        bo.setName(output.getName());
        // As command line won't specify the details of body/header for message
        // parts
        // All output message's parts will be added into one soap body element
        bo.addExtensibilityElement(getSoapBody(BindingOutput.class));
        return bo;
    }

    private SOAPBody getSoapBody(Class parent) throws ToolException {
        if (extReg == null) {
            extReg = wsdlFactory.newPopulatedExtensionRegistry();
        }
        SOAPBody soapBody = null;
        try {
            soapBody = (SOAPBody)extReg.createExtension(parent, WSDLConstants.NS_SOAP_BODY);
        } catch (WSDLException wse) {
            throw new ToolException("Create soap body ext element failed, due to " + wse);
        }
        soapBody.setUse((String)env.get(ToolConstants.CFG_USE));
        if (WSDLConstants.RPC.equalsIgnoreCase((String)env.get(ToolConstants.CFG_STYLE))
            && env.optionSet(ToolConstants.CFG_NAMESPACE)) {
            soapBody.setNamespaceURI((String)env.get(ToolConstants.CFG_NAMESPACE));
        }
        return soapBody;
    }

    private void addSoapFaults(Operation op, BindingOperation bindingOperation)
        throws ToolException {
        Map faults = op.getFaults();
        Iterator it = faults.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            Fault fault = (Fault)faults.get(key);
            BindingFault bf = wsdlDefinition.createBindingFault();
            bf.setName(fault.getName());
            setSoapFaultExtElement(bf);
            bindingOperation.addBindingFault(bf);
        }
    }

    private void setSoapFaultExtElement(BindingFault bf) throws ToolException {
        if (extReg == null) {
            extReg = wsdlFactory.newPopulatedExtensionRegistry();
        }
        SOAPFault soapFault = null;
        try {
            soapFault = (SOAPFault)extReg.createExtension(BindingFault.class,
                                                          WSDLConstants.NS_SOAP_FAULT);
        } catch (WSDLException wse) {
            throw new ToolException("Create soap fault ext element failed, due to " + wse);
        }
        soapFault.setName(bf.getName());
        soapFault.setUse((String)env.get(ToolConstants.CFG_USE));
        if (WSDLConstants.RPC.equalsIgnoreCase((String)env.get(ToolConstants.CFG_STYLE))
            && env.optionSet(ToolConstants.CFG_NAMESPACE)) {
            soapFault.setNamespaceURI((String)env.get(ToolConstants.CFG_NAMESPACE));
        }
        bf.addExtensibilityElement(soapFault);
    }

    private Writer getOutputWriter() throws ToolException {
        Writer writer = null;
        String newName = null;
        String outputDir;

        if (env.get(ToolConstants.CFG_OUTPUTFILE) != null) {
            newName = (String)env.get(ToolConstants.CFG_OUTPUTFILE);
        } else {
            String oldName = (String)env.get(ToolConstants.CFG_WSDLURL);
            int position = oldName.lastIndexOf("/");
            if (position < 0) {
                position = oldName.lastIndexOf("\\");
            }
            if (position >= 0) {
                oldName = oldName.substring(position + 1, oldName.length());
            }
            if (oldName.toLowerCase().indexOf(WSDL_FILE_NAME_EXT) >= 0) {
                newName = oldName.substring(0, oldName.length() - 5) + NEW_FILE_NAME_MODIFIER
                          + WSDL_FILE_NAME_EXT;
            } else {
                newName = oldName + NEW_FILE_NAME_MODIFIER;
            }
        }
        if (env.get(ToolConstants.CFG_OUTPUTDIR) != null) {
            outputDir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);
            if (!("/".equals(outputDir.substring(outputDir.length() - 1))
                  || "\\".equals(outputDir.substring(outputDir.length() - 1)))) {
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
