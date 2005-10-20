package org.objectweb.celtix.bus.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
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

public class TypeSchema /* implements ErrorHandler */ {

    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationMetadataBuilder.class);
    private static Map<TypeSchemaInfo, TypeSchema> map = new HashMap<TypeSchemaInfo, TypeSchema>();

    private Schema schema;
    private Validator validator;
    private String packageName;
    private Map<String, String> types;

    /**
     * prevent instantiation
     */
    protected TypeSchema(String namespaceURI, String location) {

        types = new HashMap<String, String>();

        URI uri = null;
        try {
            uri = new URI(location);
        } catch (URISyntaxException ex) {
            Message msg = new Message("SCHEMA_LOCATION_ERROR_EXC", LOG, location);
            throw new ConfigurationException(msg, ex);
        }

        URL url = null;

        if (uri.isAbsolute()) {
            try {
                url = uri.toURL();
            } catch (MalformedURLException ex) {
                Message msg = new Message("SCHEMA_LOCATION_ERROR_EXC", LOG, location);
                throw new ConfigurationException(msg, ex);
            }
        } else {
            url = getClass().getResource(uri.getPath());
        }
        if (null == url) {
            throw new ConfigurationException(new Message("SCHEMA_LOCATION_ERROR_EXC", LOG, location));
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Effective location for schema: " + url.getPath());
        }

        String effectiveLocation = url.getPath();
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(new InputSource(new FileInputStream(effectiveLocation)));
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException(new Message("PARSER_CONFIGURATION_ERROR_EXC", LOG,
                                                         effectiveLocation), ex);
        } catch (SAXException ex) {
            throw new ConfigurationException(new Message("PARSE_ERROR_EXC", LOG), ex);
        } catch (IOException ex) {
            throw new ConfigurationException(new Message("FILE_OPEN_ERROR_EXC", LOG, effectiveLocation), ex);
        }

        deserialize(document, namespaceURI);

        Source src = new DOMSource(document);

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schema = factory.newSchema(src);
        } catch (SAXException ex) {
            throw new ConfigurationException(
                                             new Message("SCHEMA_CREATION_ERROR_EXC", LOG, effectiveLocation),
                                             ex);
        }
        document = null;
    }

    public static TypeSchema get(String namespaceURI, String location) {
        TypeSchemaInfo tsi = new TypeSchemaInfo(namespaceURI, location);
        TypeSchema ts = map.get(tsi);
        if (null == ts) {
            ts = new TypeSchema(namespaceURI, location);
            map.put(tsi, ts);
        }
        return ts;
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
    
    public String getTypeType(String typeName) {
        return types.get(typeName);
    }

    public String getPackageName() {
        return packageName;
    }

    public Schema getSchema() {
        return schema;
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
                    QName name = ConfigurationMetadataUtils.elementAttributeToQName(document,
                                                                                   (Element)nd, "name");
                    String localName = name.getLocalPart();
                    /*
                    if (!isJavaIdentifier(localName)) {
                        throw new ConfigurationException(new Message("ELEMENT_NAME_NOT_AN_IDENTIFIER_EXC", 
                                                                     LOG, localName));
                    } 
                    */                                                               
                    QName type = ConfigurationMetadataUtils.elementAttributeToQName(document,
                                                                                    (Element)nd, "type");
                    String localType = type.getLocalPart();
                    
                    /*
                    if (!isJavaIdentifier(localType)) {
                        throw new ConfigurationException(new Message("ELEMENT_TYPE_NOT_AN_IDENTIFIER_EXC", 
                                                                     LOG, localType));
                    }
                    */
                    
                    types.put(localName, localType);
                }
            }
        }
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
    
    private final class TypeSchemaErrorHandler implements ErrorHandler {

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
