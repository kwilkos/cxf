package org.objectweb.celtix.tools.generators.java2;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

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
        portType.setQName(new QName(wmodel.getTargetNameSpace(), this.portTypeName));
        portType.setUndefined(false);
        for (JavaMethod method : wmodel.getJavaMethods()) {
            Operation operation = definition.createOperation();
            operation.setName(method.getName());
            operation.setUndefined(false);
            Iterator ite1 = method.getObjectParameters().iterator();
            while (ite1.hasNext()) {
                Message message = definition.createMessage();
                Object obj = ite1.next();
                if (obj instanceof WSDLWrapperParameter) {
                    WSDLWrapperParameter wrapperPara = (WSDLWrapperParameter)obj;
                    message.setQName(new QName(wmodel.getTargetNameSpace(), wrapperPara.getName()));
                    String ns = null;
                    if (wrapperPara.getWrapperChildren().size() > 0) {
                        Iterator ite2 = wrapperPara.getWrapperChildren().iterator();
                        while (ite2.hasNext()) {
                            JavaParameter jp = (JavaParameter)ite2.next();
                            ns = jp.getTypeReference().tagName.getNamespaceURI();
                        }
                    } else {
                        ns = wrapperPara.getTypeReference().tagName.getNamespaceURI();

                    }
                    addPart(message, "parameter", new QName(ns, wrapperPara.getName()));
                    if (wrapperPara.getStyle() == JavaType.Style.IN) {
                        Input input = definition.createInput();
                        input.setMessage(message);
                        input.setName(wrapperPara.getName());
                        operation.setInput(input);

                    } else {
                        Output output = definition.createOutput();
                        output.setMessage(message);
                        output.setName(wrapperPara.getName());
                        operation.setOutput(output);
                    }

                }
                if (obj instanceof JavaParameter) {
                    JavaParameter jp = (JavaParameter)obj;
                    addPart(message, "parameter", new QName(wmodel.getTargetNameSpace(), jp.getName()));
                    if (jp.getStyle() == JavaType.Style.IN) {
                        Input input = definition.createInput();
                        input.setMessage(message);
                        input.setName(jp.getName());
                        operation.setInput(input);
                    } else {
                        Output output = definition.createOutput();
                        output.setMessage(message);
                        output.setName(jp.getName());
                        operation.setOutput(output);
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

    private void addPart(Message message, String partName, QName partElementName) {
        Part part = definition.createPart();
        part.setName(partName);
        part.setElementName(partElementName);
        message.addPart(part);
    }

}
