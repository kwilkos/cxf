package org.objectweb.celtix.tools.generators.java2;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.common.model.WSDLModel;

public class TypesGenerator {
    private WSDLModel wmodel;
    private Definition definition;
    private ExtensionRegistry extensionRegistry;
    private ProcessorEnvironment env;
    
    public TypesGenerator(WSDLModel model , ProcessorEnvironment penv) {
        this.definition = model.getDefinition();
        this.wmodel = model;
        env = penv;
        extensionRegistry = definition.getExtensionRegistry();
        
    }
    public void generate() {
       
        try {
            wmodel.createJAXBContext();
        } catch (Exception e) {
            System.out.println("Generate Types Error : " + e.getMessage());
            e.printStackTrace();
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

            schema = (Schema)extensionRegistry.createExtension(Types.class,
                                                               new QName("http://www.w3.org/2001/XMLSchema",
                                                                         "schema"));

            DocumentBuilder docBuilder;
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element element = doc.createElement("xsd:schema");
            Map<String, String> schemafileMap = wmodel.getSchemaNSFileMap();

            Set<Map.Entry<String, String>> entryset = schemafileMap.entrySet();
            Iterator<Map.Entry<String, String>> ite = entryset.iterator();
            while (ite.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>)ite.next();
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

        definition.setTargetNamespace(wmodel.getTargetNameSpace());

        definition.addNamespace(WSDLConstants.WSDL_PREFIX, WSDLConstants.NS_WSDL);
        definition.addNamespace(WSDLConstants.XSD_PREFIX, WSDLConstants.XSD_NAMESPACE);
        definition.addNamespace(WSDLConstants.SOAP_PREFIX, WSDLConstants.SOAP11_NAMESPACE);
        definition.addNamespace(WSDLConstants.TNS_PREFIX, wmodel.getTargetNameSpace());
        int i = 0;
        for (String s : wmodel.getJaxbContext().getKnownNamespaceURIs()) {
            definition.addNamespace("ns" + (++i), s);
        }

    }


}
