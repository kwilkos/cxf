package org.objectweb.celtix.bus.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata.LifecyclePolicy;
import org.objectweb.celtix.configuration.ConfigurationMetadata;


public class ConfigurationMetadataBuilder  {

    final class ValidatorErrorHandler implements ErrorHandler {

        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }

        public void warning(SAXParseException exception) throws SAXException {
            throw exception;
        }
    }
    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationMetadataBuilder.class);
    private static final String MEATADATA_NAMESPACE_URI = 
        "http://celtix.objectweb.org/configuration/metadata";
    private static Schema metadataSchema;
    private static Validator metadataValidator;
    
    private static ErrorHandler validatorErrorHandler;
    
    private final ConfigurationMetadataImpl model;
    private boolean doValidate;

    public ConfigurationMetadataBuilder() {
        model = new ConfigurationMetadataImpl();
    }
    
    public void setValidation(boolean onOff) {
        doValidate = onOff;   
    }

    public ConfigurationMetadata build(InputSource is) throws IOException {
        parseXML(is);
        return model;
    }

    public ConfigurationMetadata build(InputStream is) throws IOException {    
        return build(new InputSource(is));
    }
    
    private void deserializeConfig(Document document) {
        Element root = document.getDocumentElement();
        model.setNamespaceURI(root.getAttribute("namespace"));
    }

    private void deserializeConfigItem(Document document, Element configItemElement) {
        
        ConfigurationItemMetadataImpl item = new ConfigurationItemMetadataImpl();

        for (Node nd = configItemElement.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE != nd.getNodeType()) {
                continue;
            } else if ("name".equals(nd.getLocalName())) {
                item.setName(ConfigurationMetadataUtils.getElementValue(nd));
            } else if ("type".equals(nd.getLocalName())) {
                QName type = ConfigurationMetadataUtils.elementValueToQName(document, 
                                                                           (Element)nd);
                item.setType(type);
                if (doValidate) {
                    if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getNamespaceURI())) {
                        continue;
                    }
                    TypeSchema ts = new TypeSchemaHelper().get(type.getNamespaceURI());
                    if (ts == null) {
                        throw new ConfigurationException(new Message("NO_TYPESCHEMA_FOR_NAMESPACE_EXC", LOG,
                                                                     type.getNamespaceURI()));
                    }
                    if (!ts.hasType(type.getLocalPart())) {
                        throw new ConfigurationException(new Message("TYPE_NOT_DEFINED_IN_NAMESPACE_EXC",
                                                                     LOG, type.getLocalPart(), type
                                                                         .getNamespaceURI()));
                    }
                }
            } else if ("description".equals(nd.getLocalName())) {
                // item.setDescription(getElementValue(nd));
            } else if ("lifecyclePolicy".equals(nd.getLocalName())) {
                String value = ConfigurationMetadataUtils.getElementValue(nd);
                if (null != value) {
                    if ("static".equals(value)) {
                        item.setLifecyclePolicy(LifecyclePolicy.STATIC);
                    } else if ("process".equals(value)) {
                        item.setLifecyclePolicy(LifecyclePolicy.PROCESS);
                    } else if ("bus".equals(value)) {
                        item.setLifecyclePolicy(LifecyclePolicy.BUS);
                    } else {
                        item.setLifecyclePolicy(LifecyclePolicy.DYNAMIC);
                    }
                }
            } else {
                // this must be the extension element holding the default value
                deserializeDefaultValue(item, (Element)nd);
            }
        }

        model.addItem(item);
    }

    private void deserializeConfigItems(Document document) {
        for (Node nd = document.getDocumentElement().getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE == nd.getNodeType()
                && "configItem".equals(nd.getLocalName())
                && MEATADATA_NAMESPACE_URI.equals(nd.getNamespaceURI())) {
                Element configItemElement = (Element)nd;
                deserializeConfigItem(document, configItemElement);
            }
        }
    }

    private void deserializeDefaultValue(ConfigurationItemMetadataImpl item, Element data) {
        /*
        String namespaceURI = data.getNamespaceURI();
        System.out.println("deserializeDefaultValue: \n" 
                           + "    data namespaceURI: " + namespaceURI + "\n"
                           + "    data localName: " + data.getLocalName() + "\n"
                           + "    item type: " + item.getType());
        
        if (!namespaceURI.equals(item.getType().getNamespaceURI())) {
            Message msg = new Message("INVALID_ELEMENT_FOR_DEFAULT_VALUE_EXC", 
                                      LOG, item.getName(), item.getType());
            throw new ConfigurationException(msg); 
        }
        TypeSchema ts = new TypeSchemaHelper().get(namespaceURI);
        assert ts != null;
        String name = data.getLocalName();
        QName type = ts.getDeclaredType(name);
        if (null == type || !type.equals(item.getType().getLocalPart())) {
            Message msg = new Message("INVALID_ELEMENT_FOR_DEFAULT_VALUE_EXC",
                                      LOG, item.getName(), item.getType());
            throw new ConfigurationException(msg);
        }                
        unmarshalDefaultValue(item, data);
        */
        String elementName = data.getLocalName();
        String namespaceURI = data.getNamespaceURI();   
        TypeSchema ts = new TypeSchemaHelper().get(namespaceURI);
        QName type = null;
        if (null != ts) {
            type = ts.getDeclaredType(elementName);
        }
        if (null == ts || null == type) {
            System.err.println(elementName);
            System.err.println(namespaceURI);
            System.err.println(ts);
            System.err.println(type);
            throw new ConfigurationException(new Message("INVALID_ELEMENT_FOR_DEFAULT_VALUE_EXC", LOG,
                                                         item.getName(), item.getType()));
        }
        if (!type.equals(item.getType())) {
            throw new ConfigurationException(new Message("INVALID_TYPE_FOR_DEFAULT_VALUE_EXC", LOG, 
                                                       item.getName(), item.getType()));
        }                
        unmarshalDefaultValue(item, data);
    } 
    
    private void deserializeImports(Document document) {
        TypeSchemaHelper tsh = new TypeSchemaHelper();
        for (Node nd = document.getDocumentElement().getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE == nd.getNodeType()
                && "configImport".equals(nd.getLocalName())
                && MEATADATA_NAMESPACE_URI.equals(nd.getNamespaceURI())) {             
                Element importElement = (Element)nd;
                String location = importElement.getAttribute("location");
                String namespaceURI = importElement.getAttribute("namespace");     
                if (null == tsh.get(namespaceURI)) {
                    tsh.get(namespaceURI, location);
                }
            }
        } 
    }
    
    /**
     * The configuration metadata schema is obtained system resource
     * "schemas/configuration/metadata.xsd".
     * It requires that either the resources directory is on the classpath or that
     * the resources is listed in the classpath specified in the manifest of celtix.jar.
     * 
     * @return the metadata schema
     */
    
    private Schema getMetadataSchema() {
        if (null == metadataSchema) {
            InputStream is = ClassLoader.getSystemResourceAsStream("schemas/configuration/metadata.xsd");
            if (null == is) {
                throw new ConfigurationException(new Message("CANNOT_FIND_CONFIG_METADATA_SCHEMA_MSG", LOG));
            }

            try {
                metadataSchema = getSchema(is);
            } catch (ConfigurationException ex) {
                // should never happen as metadata schema is immutable
                LOG.log(Level.SEVERE, "CANNOT_CREATE_CONFIG_METADATA_SCHEMA_MSG", ex);
            }
        }
        return metadataSchema;
    }

    private Validator getMetadataValidator() {
        if (null == metadataValidator) {
            Schema schema = getMetadataSchema();
            // assert null != schema;
            metadataValidator = schema.newValidator();
            if (null == validatorErrorHandler) {
                validatorErrorHandler = new ValidatorErrorHandler();
            }
            metadataValidator.setErrorHandler(validatorErrorHandler);
            // assert null != metadataValidator;
        }
        return metadataValidator;
    }

    private Schema getSchema(InputStream is) {
        Source schemaFile = new StreamSource(is);
        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = factory.newSchema(schemaFile);
        } catch (SAXException ex) {
            throw new ConfigurationException(new Message("SCHEMA_CREATION_ERROR_EXC", LOG), ex);
        }
        return schema;
    }

    private void parseXML(InputSource is) throws IOException {

        // parse
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); 
            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(is);
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException(new Message("PARSER_CONFIGURATION_ERROR_EXC", LOG), ex);
        } catch (SAXException ex) {
            throw new ConfigurationException(new Message("PARSE_ERROR_EXC", LOG), ex);
        } 
        
        if (doValidate) {
            try {
                Validator v = getMetadataValidator();
                v.validate(new DOMSource(document));
            } catch (SAXException ex) {
                Message msg = new Message("METADATA_VALIDATION_ERROR_EXC", LOG);
                throw new ConfigurationException(msg, ex);
            }
        }
        
        deserializeImports(document);
        deserializeConfig(document);
        deserializeConfigItems(document);
    }

    private void unmarshalDefaultValue(ConfigurationItemMetadataImpl item, Element data) {
        TypeSchema ts = new TypeSchemaHelper().get(data.getNamespaceURI());
        Object obj = ts.unmarshalDefaultValue(item, data, doValidate);
        if (null != obj) {
            item.setDefaultValue(obj);        
        }
    }
}
