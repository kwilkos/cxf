package org.objectweb.celtix.bus.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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


import org.objectweb.celtix.bus.jaxb.JAXBUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.ConfigurationException;

public class TypeSchema {

    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationMetadataBuilder.class);
    private static Map<String, TypeSchema> map = new HashMap<String, TypeSchema>();

    private Schema schema;
    private Validator validator;
    private String packageName;
    private Map<String, String> types;
    private Map<String, String> baseTypes;

    /**
     * prevent instantiation
     */
    protected TypeSchema(String namespaceURI, String location) {

        types = new HashMap<String, String>();
        baseTypes = new HashMap<String, String>();

        URI uri = null;
        try {
            uri = new URI(location);
        } catch (URISyntaxException ex) {
            Message msg = new Message("SCHEMA_LOCATION_ERROR_EXC", LOG, location);
            throw new ConfigurationException(msg, ex);
        }

        InputStream is = null;

        if (uri.isAbsolute()) {
            String path = uri.getPath();
            if (null == path) {
                Message msg = new Message("FILE_OPEN_ERROR_EXC", LOG, location); 
                throw new ConfigurationException(msg);
            }  
            try {
                is = new FileInputStream(path);
            } catch (IOException ex) {
                Message msg = new Message("FILE_OPEN_ERROR_EXC", LOG, location);
                throw new ConfigurationException(msg, ex);
            }
        } else {
            is = getClass().getResourceAsStream(uri.getPath());
            if (null == is) {
                throw new ConfigurationException(new Message("SCHEMA_LOCATION_ERROR_EXC", LOG, location));
            }      
        }
        
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(new InputSource(is));
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException(new Message("PARSER_CONFIGURATION_ERROR_EXC", LOG,
                                                         location), ex);
        } catch (SAXException ex) {
            throw new ConfigurationException(new Message("PARSE_ERROR_EXC", LOG), ex);
        } catch (IOException ex) {
            throw new ConfigurationException(new Message("FILE_OPEN_ERROR_EXC", LOG, location), ex);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }

        deserialize(document, namespaceURI);

        Source src = new DOMSource(document);

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schema = factory.newSchema(src);
        } catch (SAXException ex) {
            throw new ConfigurationException(new Message("SCHEMA_CREATION_ERROR_EXC", LOG, location), ex);
        }
        document = null;
    }

    public static TypeSchema get(String namespaceURI, String location) {
        TypeSchema ts = map.get(namespaceURI);
        if (null == ts) {
            ts = new TypeSchema(namespaceURI, location);
            map.put(namespaceURI, ts);
        }
        return ts;
    }
    
    public static TypeSchema get(String namespaceURI) {
        return map.get(namespaceURI);
    }

    public Validator getValidator() {
        if (null == validator) {
            Schema s = getSchema();
            validator = s.newValidator();
            validator.setErrorHandler(new TypeSchemaErrorHandler());
        }
        return validator;
    }

    public Collection<String> getTypes() {
        return types.keySet();
    }
    
    public boolean hasType(String typeName) {
        return types.containsKey(typeName);
    }
    
    public String getDeclaredType(String typeName) {
        return types.get(typeName);
    }
    
    public String getXMLSchemaBaseType(String typeName) {
        return baseTypes.get(typeName);
    }

    public String getPackageName() {
        return packageName;
    }

    public Schema getSchema() {
        return schema;
    }
    
    public Object unmarshalDefaultValue(ConfigurationItemMetadataImpl item, Element data) {
        JAXBContext context = null;
        Object obj = null;
        try {
            context = JAXBContext.newInstance(packageName);
            Unmarshaller u = context.createUnmarshaller();
            u.setSchema(schema);
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
        }
        return obj;
    }

    private void deserialize(Document document, String namespaceURI) {
        deseralizePackageName(document, namespaceURI);
        deserializeTypes(document);
    }

    private void deserializeTypes(Document document) {
        Element root = document.getDocumentElement();
        for (Node nd = root.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE == nd.getNodeType()) {
                if ("element".equals(nd.getLocalName())) {
                    QName name = ConfigurationMetadataUtils.elementAttributeToQName(
                        document, (Element)nd, "name");
                    String localName = name.getLocalPart();
                    /*
                    if (!isJavaIdentifier(localName)) {
                        throw new ConfigurationException(new Message("ELEMENT_NAME_NOT_AN_IDENTIFIER_EXC", 
                                                                     LOG, localName));
                    } 
                    */                                                               
                    QName type = ConfigurationMetadataUtils.elementAttributeToQName(
                        document, (Element)nd, "type");                    
                    String localType = type.getLocalPart();
                    
                    /*
                    if (!isJavaIdentifier(localType)) {
                        throw new ConfigurationException(new Message("ELEMENT_TYPE_NOT_AN_IDENTIFIER_EXC", 
                                                                     LOG, localType));
                    }
                    */
                    
                    types.put(localName, localType);
                    
                    String baseType = getBaseType(document, type);
                    if (null != baseType) {
                        baseTypes.put(localName, baseType);
                    }
                }
            }
        }
    }
    
    private String getBaseType(Document document, QName type) {
        QName currentType = type;
        QName baseType;
        do {
            baseType = getBaseTypeInternal(document, currentType);
            if (null == baseType) {
                return null;
            } else if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(baseType.getNamespaceURI())) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Base type for " + type + ": " + baseType);
                }
                return baseType.getLocalPart();                
            }  
            currentType = baseType;
        } while (true);
    }
    
    private QName getBaseTypeInternal(Document document, QName type) {
        Element root = document.getDocumentElement();
        Element simpleTypeElement = null;
        for (Node nd = root.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE == nd.getNodeType()
                && "simpleType".equals(nd.getLocalName())
                && type.getLocalPart().equals(((Element)nd).getAttribute("name"))) { 
                simpleTypeElement = (Element)nd;                
            }
        }
        if (null == simpleTypeElement) {
            return null;
        }
        
        for (Node nd = simpleTypeElement.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE == nd.getNodeType()
                && "restriction".equals(nd.getLocalName())) { 
                // TODO restriction element can have base attribute OR simpleType
                // child element. Currently we only handle the base attribute. 
                
                return ConfigurationMetadataUtils.elementAttributeToQName(document, (Element)nd, "base");
            }
        }       
        return  null;
    }

    private void deseralizePackageName(Document document, String namespaceURI) {
        Element root = document.getDocumentElement();
        Element annotationElement = null;
        for (Node nd = root.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE == nd.getNodeType() && "annotation".equals(nd.getLocalName())) {
                annotationElement = (Element)nd;
                break;
            }
        }
        Element appInfoElement = null;
        if (null != annotationElement) {
            for (Node nd = annotationElement.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
                if (Node.ELEMENT_NODE == nd.getNodeType() && "appinfo".equals(nd.getLocalName())) {
                    appInfoElement = (Element)nd;
                    break;
                }
            }
        }
        Element schemaBindingsElement = null;
        if (null != appInfoElement) {
            for (Node nd = appInfoElement.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
                if (Node.ELEMENT_NODE == nd.getNodeType() && "schemaBindings".equals(nd.getLocalName())) {
                    schemaBindingsElement = (Element)nd;
                    break;
                }
            }
        }
        Element packageElement = null;
        if (null != schemaBindingsElement) {
            for (Node nd = schemaBindingsElement.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
                if (Node.ELEMENT_NODE == nd.getNodeType() && "package".equals(nd.getLocalName())) {
                    packageElement = (Element)nd;
                    break;
                }
            }
        }

        if (null != packageElement) {
            packageName = packageElement.getAttribute("name");
        } else {
            packageName = JAXBUtils.namespaceURIToPackage(namespaceURI);
        }

        if (null == packageName) {
            throw new ConfigurationException(new Message("MISSING_PACKAGE_NAME_EXC", LOG, namespaceURI));
        }

    }

    // ErrorHandler interface
    
    final class TypeSchemaErrorHandler implements ErrorHandler {

        public void error(SAXParseException exception) throws SAXParseException {
            throw exception;
        }

        public void fatalError(SAXParseException exception) throws SAXParseException {
            throw exception;
        }

        public void warning(SAXParseException exception) throws SAXParseException {
            throw exception;
        }
    }
}
