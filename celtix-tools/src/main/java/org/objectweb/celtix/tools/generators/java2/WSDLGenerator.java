package org.objectweb.celtix.tools.generators.java2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.xml.bind.api.JAXBRIContext;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.model.JavaType;
import org.objectweb.celtix.tools.common.model.WSDLModel;
import org.objectweb.celtix.tools.common.model.WSDLWrapperParameter;

public class WSDLGenerator {
    private final WSDLModel wmodel;

    private final ProcessorEnvironment env;

    private final Definition definition;

    private WSDLFactory wsdlFactory;

    private ExtensionRegistry extensionRegistry;

    private String wsdlFile;

    private String portTypeName;

    private String targetNameSpace;

    public WSDLGenerator(WSDLModel model, ProcessorEnvironment penv) {
        wmodel = model;
        env = penv;
        definition = model.getDefinition();

        try {
            wsdlFactory = WSDLFactory.newInstance();
        } catch (javax.wsdl.WSDLException e) {
            throw new ToolException("Generate definition error ", e);
        }

        extensionRegistry = wsdlFactory.newPopulatedExtensionRegistry();
    }

    public void generate() {
        preGenerate();
        generateTypes();
        generateMessageAndPortType();
        generateBinding();
        generateService();
        writeDefinition();

    }

    private void preGenerate() {
        Object obj = env.get(ToolConstants.CFG_OUTPUTFILE);
        wsdlFile = obj == null ? "./" + wmodel.getServiceName() + ".wsdl"
                : (String) obj;
        obj = env.get(ToolConstants.CFG_TNS);
        targetNameSpace = obj == null ? wmodel.getTargetNameSpace()
                : (String) obj;
        obj = env.get(ToolConstants.CFG_PORTTYPE);
        portTypeName = obj == null ? wmodel.getPortName() : (String) obj;
        wmodel.setPortName(portTypeName);

    }

    private boolean writeDefinition() {

        WSDLWriter writer = wsdlFactory.newWSDLWriter();

        java.io.File file = new java.io.File(wsdlFile);
        java.io.OutputStream outstream = null;
        try {
            outstream = new java.io.FileOutputStream(file);
        } catch (java.io.FileNotFoundException e) {
            throw new ToolException("Create WSDL file error", e);
        }

        try {
            writer.writeWSDL(this.definition, outstream);

        } catch (javax.wsdl.WSDLException e) {
            throw new ToolException("Generate definition error ", e);
        }
        return true;
    }

    private void generateTypes() {
        try {
            wmodel.createJAXBContext();
        } catch (Exception e) {
            System.out.println("Generate Types Error : " + e.getMessage());
        }

        SchemaOutputResolver resolver = new WSDLOutputResolver(env, wmodel);

        try {
            wmodel.getJaxbContext().generateSchema(resolver);             
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        Types types = definition.createTypes();

        try {
            Schema schema;

            schema = (Schema) extensionRegistry.createExtension(Types.class,
                    new QName("http://www.w3.org/2001/XMLSchema", "schema"));

            DocumentBuilder docBuilder;
            docBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element element = doc.createElement("xsd:schema");
            Map<String, String> schemafileMap = wmodel.getSchemaNSFileMap();

            Set<Map.Entry<String, String>> entryset = schemafileMap.entrySet();
            Iterator<Map.Entry<String, String>> ite = entryset.iterator();
            while (ite.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) ite.next();
                Element importelement = doc.createElement("xsd:import");
                importelement.setAttribute("namespace", entry.getKey());
                importelement.setAttribute("schemaLocation", entry.getValue());
                element.appendChild(importelement);
            }
            schema.setElement(element);
            types.addExtensibilityElement(schema);
            definition.setTypes(types);
        } catch (javax.wsdl.WSDLException e1) {
            throw new ToolException("Generate types error ", e1);
        } catch (ParserConfigurationException e) {
            throw new ToolException("Generate types error ", e);
        }

        definition.setTargetNamespace(targetNameSpace);

        definition.addNamespace(WSDLConstants.WSDL_PREFIX,
                WSDLConstants.NS_WSDL);
        definition.addNamespace(WSDLConstants.XSD_PREFIX,
                WSDLConstants.XSD_NAMESPACE);
        definition.addNamespace(WSDLConstants.SOAP_PREFIX,
                WSDLConstants.SOAP11_NAMESPACE);
        definition.addNamespace(WSDLConstants.TNS_PREFIX, wmodel
                .getTargetNameSpace());
        int i = 0;
        for (String s : wmodel.getJaxbContext().getKnownNamespaceURIs()) {
            definition.addNamespace("ns" + (++i), s);
        }

    }

    private void generateMessageAndPortType() {
        PortType portType = definition.createPortType();
        portType.setQName(new QName(wmodel.getTargetNameSpace(), wmodel
                .getPortTypeName()));
        portType.setUndefined(false);
        for (JavaMethod method : wmodel.getJavaMethods()) {
            Operation operation = definition.createOperation();
            operation.setName(method.getName());
            operation.setUndefined(false);

            Message inputMessage = null;
            inputMessage = definition.createMessage();
            inputMessage.setQName(new QName(wmodel.getTargetNameSpace(), method
                    .getName()));

            Message outputMessage = null;
            if (!method.isOneWay()) {
                outputMessage = definition.createMessage();
                outputMessage.setQName(new QName(wmodel.getTargetNameSpace(),
                        method.getName() + "Response"));
            }

            for (WSDLWrapperParameter wrapperPara : method
                    .getWSDLWrapperParameters()) {
                String ns = wrapperPara.getTargetNamespace();
                // Doc-Lit-wrapped
                if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT
                        && method.isWrapperStyle()) {
                    if (wrapperPara.getStyle() == JavaType.Style.IN) {
                        addPartByElementName(inputMessage, wrapperPara
                                .getName(),
                                new QName(ns, wrapperPara.getName()));
                    } else {
                        addPartByElementName(outputMessage, wrapperPara
                                .getName(),
                                new QName(ns, wrapperPara.getName()));
                    }
                }
                // Doc-Lit-Bare
                if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT
                        && !method.isWrapperStyle()) {
                    if (wrapperPara.getStyle() == JavaType.Style.IN) {
                        for (JavaParameter jp : wrapperPara
                                .getWrapperChildren()) {
                            addPartByElementName(inputMessage,
                                    jp.getPartName(),
                                    new QName(jp.getTargetNamespace(), jp
                                            .getName()));

                        }
                    } else {
                        /*JavaParameter jp = wrapperPara.getWrapperChildren()
                                .get(0);*/
                        for (JavaParameter jp : wrapperPara.getWrapperChildren()) { 
                            addPartByElementName(
                                outputMessage,
                                jp.getPartName(),
                                new QName(jp.getTargetNamespace(), jp.getName()));
                        }
                    }
                }

                // RPC
                if (method.getSoapStyle() == SOAPBinding.Style.RPC
                        && wrapperPara.getWrapperChildren().size() > 0) {
                    JAXBRIContext jxbcontext = wmodel.getJaxbContext();
                    Iterator ite2 = wrapperPara.getWrapperChildren().iterator();
                    while (ite2.hasNext()) {
                        JavaParameter jp = (JavaParameter) ite2.next();
                        ns = jp.getTypeReference().tagName.getNamespaceURI();

                        QName qname = jxbcontext.getTypeName(jp
                                .getTypeReference());
                        if (wrapperPara.getStyle() == JavaType.Style.IN) {
                            addPartByTypeName(inputMessage, jp.getName(), qname);
                        } else {
                            addPartByTypeName(outputMessage, jp.getName(),
                                    qname);
                        }

                    }
                }

                if (wrapperPara.getStyle() == JavaType.Style.IN) {
                    addInputToMessage(operation, inputMessage, wrapperPara
                            .getName());
                } else {
                    addOutputToMessage(operation, outputMessage, wrapperPara
                            .getName());
                }

            }

            inputMessage.setUndefined(false);
            definition.addMessage(inputMessage);

            if (outputMessage != null) {
                outputMessage.setUndefined(false);
                definition.addMessage(outputMessage);
            }

            generateFault(method, operation);

            portType.addOperation(operation);
            definition.addPortType(portType);
        }

    }

    private void generateBinding() {
        Binding binding = definition.createBinding();

        binding.setQName(new QName(WSDLConstants.NS_WSDL, wmodel
                .getPortTypeName()
                + "Binding"));
        binding.setPortType(definition.getPortType(new QName(wmodel
                .getTargetNameSpace(), wmodel.getPortTypeName())));

        // genearte the soap binding

        javax.wsdl.extensions.soap.SOAPBinding soapBinding;
        try {
            soapBinding = (javax.wsdl.extensions.soap.SOAPBinding) extensionRegistry
                    .createExtension(Binding.class, new QName(
                            WSDLConstants.SOAP11_NAMESPACE, "binding"));
            soapBinding.setTransportURI("http://schemas.xmlsoap.org/soap/http");
            soapBinding.setStyle(wmodel.getStyle().toString().toLowerCase());
            binding.addExtensibilityElement(soapBinding);
        } catch (WSDLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        generateBindingOperation(binding);
        binding.setUndefined(false);
        definition.addBinding(binding);

    }

    private void generateBindingOperation(Binding binding) {
        for (JavaMethod jmethod : wmodel.getJavaMethods()) {
            BindingOperation bindOperation = definition
                    .createBindingOperation();
            bindOperation.setName(jmethod.getName());
            generateBindingOperationInputOutPut(bindOperation, jmethod);
            binding.addBindingOperation(bindOperation);
        }

    }

    private void generateBindingOperationInputOutPut(
            BindingOperation operation, JavaMethod jmethod) {
        // generate soap binding action
        SOAPOperation soapOperation = generateSoapAction();
        soapOperation.setStyle(jmethod.getSoapStyle().name().toLowerCase());
        soapOperation.setSoapActionURI(jmethod.getSoapAction());
        operation.addExtensibilityElement(soapOperation);

        for (WSDLWrapperParameter param : jmethod.getWSDLWrapperParameters()) {
            if (param.getStyle() == JavaType.Style.IN) {
                generateInputSoapBody(jmethod, operation, param);
            } else {
                generateOutputSoapBody(jmethod, operation, param);
            }
        }

        for (org.objectweb.celtix.tools.common.model.WSDLException ex : jmethod
                .getWSDLExceptions()) {

            BindingFault bindingFault = definition.createBindingFault();
            bindingFault.setName(ex.getExcpetionClass().getSimpleName());
            operation.addBindingFault(bindingFault);
            javax.wsdl.extensions.soap.SOAPFault soapFault = null;
            try {
                soapFault = (javax.wsdl.extensions.soap.SOAPFault) extensionRegistry
                        .createExtension(BindingFault.class, new QName(
                                WSDLConstants.SOAP11_NAMESPACE, "fault"));
                soapFault.setUse("literal");
                soapFault.setName(ex.getExcpetionClass().getSimpleName());
            } catch (WSDLException e) {
                throw new ToolException("Error " + e.getMessage(), e);
            }
            bindingFault.addExtensibilityElement(soapFault);

        }

    }

    private void splitSoapHeaderBodyParams(WSDLWrapperParameter param,
            List<JavaParameter> bodyList, List<JavaParameter> headerList) {
        for (JavaParameter jpara : param.getWrapperChildren()) {
            if (jpara.isHeader()) {
                headerList.add(jpara);
            } else {
                bodyList.add(jpara);
            }
        }

    }

    private SOAPOperation generateSoapAction() {
        SOAPOperation soapOperation = null;
        try {
            soapOperation = (SOAPOperation) extensionRegistry.createExtension(
                    BindingOperation.class, new QName(
                            WSDLConstants.SOAP11_NAMESPACE, "operation"));
        } catch (WSDLException e) {
            throw new ToolException("Error " + e.getMessage(), e);
        }

        return soapOperation;
    }

    private void generateOutputSoapBody(JavaMethod jmethod,
            BindingOperation operation, WSDLWrapperParameter param) {

        SOAPBody body = null;

        BindingOutput bindingOutput = definition.createBindingOutput();
        bindingOutput.setName(param.getName());

        operation.setBindingOutput(bindingOutput);

        try {
            body = (SOAPBody) extensionRegistry.createExtension(
                    BindingOutput.class, new QName(
                            WSDLConstants.SOAP11_NAMESPACE, "body"));
        } catch (WSDLException e1) {
            throw new ToolException("Error " + e1.getMessage(), e1);
        }

        if (jmethod.getSoapUse() == SOAPBinding.Use.LITERAL) {
            body.setUse("literal");
        } else {
            throw new ToolException("Encoded use is not supported");
        }

        List<JavaParameter> bodyParams = new ArrayList<JavaParameter>();
        List<JavaParameter> headerParams = new ArrayList<JavaParameter>();

        splitSoapHeaderBodyParams(param, bodyParams, headerParams);
        // if exists soap header,then generate soap body parts

        if (headerParams.size() > 0) {
            List<String> parts = new ArrayList<String>();
            for (JavaParameter parameter : bodyParams) {
                parts.add(parameter.getPartName());
            }
            body.setParts(parts);
            SOAPHeader soapHeader = null;
            for (JavaParameter jp : headerParams) {

                try {
                    soapHeader = (SOAPHeader) extensionRegistry
                            .createExtension(BindingOutput.class, new QName(
                                    WSDLConstants.SOAP11_NAMESPACE, "header"));
                    soapHeader.setMessage(new QName(this.targetNameSpace, param
                            .getName()));
                    soapHeader.setPart(jp.getPartName());
                    soapHeader.setUse("literal");

                } catch (WSDLException e) {
                    throw new ToolException("Error " + e.getMessage(), e);
                }
            }

            if (jmethod.getSoapStyle() == SOAPBinding.Style.RPC) {
                body.setNamespaceURI(param.getTargetNamespace());
            }
            bindingOutput.addExtensibilityElement(soapHeader);

        }
        bindingOutput.addExtensibilityElement(body);

    }

    private void generateInputSoapBody(JavaMethod jmethod,
            BindingOperation operation, WSDLWrapperParameter param) {

        SOAPBody body = null;

        BindingInput bindingInput = definition.createBindingInput();
        bindingInput.setName(param.getName());

        operation.setBindingInput(bindingInput);

        try {
            body = (SOAPBody) extensionRegistry.createExtension(
                    BindingInput.class, new QName(
                            WSDLConstants.SOAP11_NAMESPACE, "body"));
        } catch (WSDLException e1) {
            throw new ToolException("Error " + e1.getMessage(), e1);
        }

        if (jmethod.getSoapUse() == SOAPBinding.Use.LITERAL) {
            body.setUse("literal");
        } else {
            throw new ToolException("Encoded use is not supported");
        }

        List<JavaParameter> bodyParams = new ArrayList<JavaParameter>();
        List<JavaParameter> headerParams = new ArrayList<JavaParameter>();

        splitSoapHeaderBodyParams(param, bodyParams, headerParams);

        // if exists soap header,then generate soap body parts

        if (headerParams.size() > 0) {
            List<String> parts = new ArrayList<String>();
            for (JavaParameter parameter : bodyParams) {
                parts.add(parameter.getPartName());
            }
            body.setParts(parts);
            SOAPHeader soapHeader = null;
            for (JavaParameter jp : headerParams) {

                try {
                    soapHeader = (SOAPHeader) extensionRegistry
                            .createExtension(BindingInput.class, new QName(
                                    WSDLConstants.SOAP11_NAMESPACE, "header"));

                    soapHeader.setMessage(new QName(this.targetNameSpace, param
                            .getName()));
                    soapHeader.setPart(jp.getPartName());
                    soapHeader.setUse("literal");

                } catch (WSDLException e) {
                    throw new ToolException("Error " + e.getMessage(), e);
                }
            }

            if (jmethod.getSoapStyle() == SOAPBinding.Style.RPC) {
                body.setNamespaceURI(param.getTargetNamespace());
            }
            bindingInput.addExtensibilityElement(soapHeader);

        }
        bindingInput.addExtensibilityElement(body);

    }

    private void generateService() {
        Service service = definition.createService();
        service.setQName(new QName(WSDLConstants.WSDL_PREFIX, wmodel
                .getServiceName()));
        Port port = definition.createPort();
        port.setName(wmodel.getPortName());
        Binding binding = definition.createBinding();
        binding.setQName(new QName(targetNameSpace, wmodel.getPortTypeName()
                + "Binding"));
        port.setBinding(binding);
        SOAPAddress soapAddress = null;
        try {
            soapAddress = (SOAPAddress) extensionRegistry.createExtension(
                    Port.class, new QName(WSDLConstants.SOAP11_NAMESPACE,
                            "address"));
            soapAddress.setLocationURI("http://localhost/changme");

        } catch (WSDLException e) {
            throw new ToolException("Error " + e.getMessage(), e);
        }
        port.addExtensibilityElement(soapAddress);
        service.addPort(port);
        definition.addService(service);
    }

    private void generateFault(JavaMethod method, Operation operation) {
        for (org.objectweb.celtix.tools.common.model.WSDLException exception : method
                .getWSDLExceptions()) {
            String exceptionName = exception.getExcpetionClass()
                    .getSimpleName();
            Message msg = definition.createMessage();
            msg.setQName(new QName(wmodel.getTargetNameSpace(), exceptionName));
            Part part = definition.createPart();
            part.setName(exception.getDetailType().getSimpleName());
            part.setElementName(exception.getDetailTypeReference().tagName);
            msg.addPart(part);
            msg.setUndefined(false);
            definition.addMessage(msg);
            Fault fault = definition.createFault();
            fault.setMessage(msg);
            fault.setName(exceptionName);
            operation.addFault(fault);
        }
    }

    private void addPartByElementName(Message message, String partName,
            QName partElementName) {
        Part part = definition.createPart();
        part.setName(partName);
        part.setElementName(partElementName);
        message.addPart(part);
    }

    private void addPartByTypeName(Message message, String partName,
            QName typeName) {
        Part part = definition.createPart();
        part.setName(partName);
        part.setTypeName(typeName);
        message.addPart(part);
    }

    private void addInputToMessage(Operation operation, Message msg,
            String inputName) {
        Input input = definition.createInput();
        input.setMessage(msg);
        input.setName(inputName);
        operation.setInput(input);
    }

    private void addOutputToMessage(Operation operation, Message msg,
            String outputName) {
        Output output = definition.createOutput();
        output.setMessage(msg);
        output.setName(outputName);
        operation.setOutput(output);
    }
    
    /*protected class JAXWSOutputSchemaResolver extends SchemaOutputResolver {
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return createOutputFile(namespaceUri, suggestedFileName);
        }
    }*/
    
}
