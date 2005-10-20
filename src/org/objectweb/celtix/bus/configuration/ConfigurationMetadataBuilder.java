package org.objectweb.celtix.bus.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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

    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationMetadataBuilder.class);
    private static final String MEATADATA_NAMESPACE_URI = 
        "http://celtix.objectweb.org/configuration/metadata";
    private static Validator metadataValidator;
    private static Schema metadataSchema;
    private static ErrorHandler validatorErrorHandler;
    
    private ConfigurationMetadataImpl model;
    private URL url;
    
    public ConfigurationMetadataBuilder() {
        model = new ConfigurationMetadataImpl();
    }

    public ConfigurationMetadata build(URL u) {
        url = u;
        String path = url.getFile();
        if (null == path) {
            throw new ConfigurationException(new Message("IO_ERROR_EXC", LOG, url));
        }
        try {
            parseXML(new InputSource(new FileInputStream(path)));
        } catch (IOException ex) {
            throw new ConfigurationException(new Message("IO_ERROR_EXC", LOG, path));
        }
        return model;
    }

    public ConfigurationMetadata build(InputSource is) throws IOException {
        parseXML(is);
        return model;
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
            throw new ConfigurationException(new Message("PARSER_CONFIGURATION_ERROR_EXC", LOG, url), ex);
        } catch (SAXException ex) {
            throw new ConfigurationException(new Message("PARSE_ERROR_EXC", LOG), ex);
        }        
       
        try {
            Validator v = getMetadataValidator();
            v.validate(new DOMSource(document));
        } catch (SAXException ex) {
            Message msg = new Message("METADATA_VALIDATION_ERROR_EXC", LOG, url);
            throw new ConfigurationException(msg, ex);
        }
        
        deserializeImports(document);
        deserializeConfig(document);
        deserializeConfigItems(document);
    }
    
    private void deserializeConfig(Document document) {
        Element root = document.getDocumentElement();
        model.setNamespaceURI(root.getAttribute("namespace"));
    }

    private void deserializeImports(Document document) {
    
        for (Node nd = document.getDocumentElement().getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE == nd.getNodeType()
                && "configImport".equals(nd.getLocalName())
                && MEATADATA_NAMESPACE_URI.equals(nd.getNamespaceURI())) {             
                Element importElement = (Element)nd;
                String location = importElement.getAttribute("location");
                String namespaceURI = importElement.getAttribute("namespace");
                if (null == model.getTypeSchema(namespaceURI)) {
                    TypeSchema ts = TypeSchema.get(namespaceURI, location);
                    model.addTypeSchema(namespaceURI, ts);
                }
            }
        } 
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
                TypeSchema ts = model.getTypeSchema(type.getNamespaceURI());
                if (null == ts || !ts.hasType(type.getLocalPart())) {
                    throw new ConfigurationException(new Message("UNKNOWN_TYPE_EXC", LOG, 
                                                                 type.getLocalPart()));
                }
                item.setType(type);
                /*
                String packageName = ts.getPackageName();
                String className = ts.getTypeType(type.getLocalPart());
                item.setDeclaredClass(packageName + "." + className);
                */
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
    
    private void deserializeDefaultValue(ConfigurationItemMetadataImpl item, Element data) {
        
        String localName = data.getLocalName();
        String namespaceURI = data.getNamespaceURI();
        QName type = new QName(namespaceURI, localName);
        if (!type.equals(item.getType())) {
            Message msg = new Message("INVALID_TYPE_FOR_DEFAULT_VALUE_EXC", LOG, item.getType());
            throw new ConfigurationException(msg);
        }                
        unmarshalDefaultValue(item, data);
    }
    
    private void unmarshalDefaultValue(ConfigurationItemMetadataImpl item, Element data) {
        JAXBContext context = null;
        TypeSchema ts = model.getTypeSchema(data.getNamespaceURI());
        String packageName = ts.getPackageName();
        Object obj = null;
        try {
            context = JAXBContext.newInstance(packageName);
            Unmarshaller u = context.createUnmarshaller();
            u.setSchema(model.getTypeSchema(data.getNamespaceURI()).getSchema());
            obj = u.unmarshal(data);
            if (obj instanceof JAXBElement<?>) {
                JAXBElement<?> el = (JAXBElement<?>)obj;
                if (el.getName().equals(item.getType())) {
                    obj = el.getValue();
                }
            }    
        } catch (JAXBException ex) {
            Message msg = new Message("DEFAULT_VALUE_UNMARSHAL_ERROR_EXC", LOG, item.getName());
            throw new ConfigurationException(msg, ex);
        }
        if (null != obj) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Unmarshaled default value into object of type: " + obj.getClass().getName() 
                         + "    value: " + obj);
            }
            item.setDefaultValue(obj);        
        }
    }

    private String getSchemaLocation() {
        URL u = ConfigurationException.class.getResource("metadata.xsd");
        if (null != u) {
            return u.getFile();
        }
        LOG.severe("CANNOT_FIND_CONFIG_METADATA_SCHEMA_MSG");
        return null;
    }

    private Schema getMetadataSchema() {
        if (null == metadataSchema) {
            String path = getSchemaLocation();
            try {
                metadataSchema = getSchema(path);
            } catch (ConfigurationException ex) {
                // should never happen as metadata schema is immutable
                LOG.log(Level.SEVERE, "CANNOT_CREATE_CONFIG_METADATA_SCHEMA_MSG", ex);
            }
        }
        return metadataSchema;
    }

    private Schema getSchema(String path) {
        Source schemaFile = new StreamSource(new File(path));
        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = factory.newSchema(schemaFile);
        } catch (SAXException ex) {
            throw new ConfigurationException(new Message("SCHEMA_CREATION_ERROR_EXC", LOG, path), ex);
        }
        return schema;
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
}
