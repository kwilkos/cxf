package org.objectweb.celtix.tools.generators.java2;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.schema.Schema;
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
    private String wsdlFile;
    private String portTypeName;
    private String targetNameSpace;

    public WSDLGenerator(WSDLModel model, ProcessorEnvironment penv) {
        wmodel = model;
        env = penv;
        definition = model.getDefinition();
    }

    public void generate() {
        preGenerate();
        generateTypes();
        generateMessageAndPortType();
        writeDefinition();

    }

    private void preGenerate() {
        Object obj = env.get(ToolConstants.CFG_OUTPUTFILE);
        wsdlFile = obj == null ? "./" + wmodel.getServiceName() + ".wsdl" : (String)obj;
        obj = env.get(ToolConstants.CFG_TNS);
        targetNameSpace = obj == null ? wmodel.getTargetNameSpace() : (String)obj;
        obj = env.get(ToolConstants.CFG_PORTTYPE);
        portTypeName = obj == null ? wmodel.getPortyTypeName() : (String)obj;

    }

    private boolean writeDefinition() {

        try {
            wsdlFactory = WSDLFactory.newInstance();
        } catch (javax.wsdl.WSDLException e) {
            throw new ToolException("Generate definition error ", e);
        }
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

    @SuppressWarnings("unchecked")
    private void generateTypes() {

        JAXBRIContext jxbContext = wmodel.getJaxbContext();

        SchemaOutputResolver resolver = new WSDLOutputResolver(env, wmodel);

        try {
            jxbContext.generateSchema(resolver);
        } catch (IOException e) {
            throw new ToolException("Generate types error ", e);
        }

        Types types = definition.createTypes();

        try {
            wsdlFactory = WSDLFactory.newInstance();

            ExtensionRegistry extensionRegistry = wsdlFactory.newPopulatedExtensionRegistry();
            Schema schema;

            schema = (Schema)extensionRegistry.createExtension(Types.class,
                                                               new QName("http://www.w3.org/2001/XMLSchema",
                                                                         "schema"));

            DocumentBuilder docBuilder;
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element element = doc.createElement("xsd:schema");
            java.util.Map schemafileMap = wmodel.getSchemaNSFileMap();

            Set<java.util.Map.Entry<String, String>> entryset = schemafileMap.entrySet();
            Iterator ite = entryset.iterator();
            while (ite.hasNext()) {
                java.util.Map.Entry<String, String> entry = (java.util.Map.Entry<String, String>)ite.next();

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

        definition.addNamespace(WSDLConstants.WSDL_PREFIX, WSDLConstants.NS_WSDL);
        definition.addNamespace(WSDLConstants.XSD_PREFIX, WSDLConstants.XSD_NAMESPACE);
        definition.addNamespace(WSDLConstants.SOAP_PREFIX, WSDLConstants.SOAP11_NAMESPACE);
        definition.addNamespace(WSDLConstants.TNS_PREFIX, wmodel.getTargetNameSpace());
        int i = 0;
        for (String s : wmodel.getJaxbContext().getKnownNamespaceURIs()) {
            definition.addNamespace("ns" + (++i), s);
        }

    }

    private void generateMessageAndPortType() {
        PortType portType = definition.createPortType();
        portType.setQName(new QName(wmodel.getTargetNameSpace(), portTypeName));
        portType.setUndefined(false);
        for (JavaMethod method : wmodel.getJavaMethods()) {
            Operation operation = definition.createOperation();
            operation.setName(method.getName());
            operation.setUndefined(false);
            Iterator ite1 = method.getObjectParameters().iterator();
            while (ite1.hasNext()) {
                Object obj = ite1.next();
                Message message = definition.createMessage();
                if (obj instanceof WSDLWrapperParameter) {
                    WSDLWrapperParameter wrapperPara = (WSDLWrapperParameter)obj;
                    message.setQName(new QName(wmodel.getTargetNameSpace(), wrapperPara.getName()));
                    String ns = wrapperPara.getTargetNamespace();
                    // Doc-Lit Wrapped
                    if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && method.isWrapperStyle()) {
                        addPartByElementName(message, wrapperPara.getName(), new QName(ns, wrapperPara
                            .getName()));
                    }
                    // RPC
                    if (method.getSoapStyle() == SOAPBinding.Style.RPC
                        && wrapperPara.getWrapperChildren().size() > 0) {
                        JAXBRIContext jxbcontext = wmodel.getJaxbContext();
                        Iterator ite2 = wrapperPara.getWrapperChildren().iterator();
                        while (ite2.hasNext()) {
                            JavaParameter jp = (JavaParameter)ite2.next();
                            ns = jp.getTypeReference().tagName.getNamespaceURI();

                            QName qname = jxbcontext.getTypeName(jp.getTypeReference());
                            addPartByTypeName(message, jp.getName(), qname);

                        }
                    }
                    if (wrapperPara.getStyle() == JavaType.Style.IN) {
                        addInputToMessage(operation, message, wrapperPara.getName());
                    } else {
                        addOutputToMessage(operation, message, wrapperPara.getName());
                    }

                }
                // Doc_Bare
                if (obj instanceof JavaParameter) {
                    JavaParameter jp = (JavaParameter)obj;
                    addPartByElementName(message, jp.getName(), jp.getTypeReference().tagName);
                    if (jp.getStyle() == JavaType.Style.IN) {
                        addInputToMessage(operation, message, jp.getName());
                    } else {
                        addOutputToMessage(operation, message, jp.getName());

                    }

                }
                message.setUndefined(false);
                definition.addMessage(message);
                generateFault(method, operation);
            }
            portType.addOperation(operation);
            definition.addPortType(portType);

        }
    }

    private void generateFault(JavaMethod method, Operation operation) {
        for (org.objectweb.celtix.tools.common.model.WSDLException exception : method.getWSDLExceptions()) {
            String exceptionName = exception.getExcpetionClass().getSimpleName();
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

    private void addPartByElementName(Message message, String partName, QName partElementName) {
        Part part = definition.createPart();
        part.setName(partName);
        part.setElementName(partElementName);
        message.addPart(part);
    }

    private void addPartByTypeName(Message message, String partName, QName typeName) {
        Part part = definition.createPart();
        part.setName(partName);
        part.setTypeName(typeName);
        message.addPart(part);
    }

    private void addInputToMessage(Operation operation, Message msg, String inputName) {
        Input input = definition.createInput();
        input.setMessage(msg);
        input.setName(inputName);
        operation.setInput(input);
    }

    private void addOutputToMessage(Operation operation, Message msg, String outputName) {
        Output output = definition.createOutput();
        output.setMessage(msg);
        output.setName(outputName);
        operation.setOutput(output);
    }

}
