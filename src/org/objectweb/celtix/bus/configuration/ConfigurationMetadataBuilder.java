package org.objectweb.celtix.bus.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.xerces.parsers.SAXParser;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata.LifecyclePolicy;

class ConfigurationMetadataBuilder extends DefaultHandler {

    private static final Logger LOG = Logger.getLogger(ConfigurationMetadataBuilder.class.getName());
    private SAXException firstWarning;
    private SAXException firstError;
    private ConfigurationMetadataImpl model;
    private URL url;

    private String itemName;
    private QName itemType;
    private LifecyclePolicy itemPolicy;
    private String itemDescription;
    private Map<String, String> prefixMappings;
    private char[] valueChars;
    private int valueStart;
    private int valueLength;
    
    private List<String[]> schemaLocations;
    
    public ConfigurationMetadataBuilder() {
        prefixMappings = new HashMap<String, String>();
        schemaLocations = new ArrayList<String[]>();    
        
   
        
        URL u = ConfigurationException.class.getResource("config-metadata.xsd");
        if (null != u) {
            addSchemaLocation("http://celtix.objectweb.org/config-metadata",
                              u.toString());
        } else {
            LOG.severe("Could not find configuration metadata schema resource");
        }
        
    }

    public void build(ConfigurationMetadataImpl m, URL u) throws ConfigurationException {
        model = m;
        url = u;
        prefixMappings = new HashMap<String, String>();
        String path = url.getFile();
        if (null == path) {
            throw new ConfigurationException(new ConfigurationMessage("IO_ERROR", url.toString()));
        }
        try {
            parseXML(new InputSource(new FileInputStream(path)));
        } catch (IOException ex) {
            throw new ConfigurationException(new ConfigurationMessage("IO_ERROR", path));
        }
    }

    public void build(ConfigurationMetadataImpl m, InputSource is) 
        throws ConfigurationException, IOException {
        model = m;
        parseXML(is);
    }
    
    public void addSchemaLocation(String namespace, String location) {
        schemaLocations.add(new String[] {namespace, location });        
    }

    // ContentHandler interface

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
        prefixMappings.put(prefix, uri);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if ("configImport".equals(localName) && "http://celtix.objectweb.org/config-metadata".equals(uri)) {
            try {
                importTypes(attributes.getValue("location"), attributes.getValue("namespace"));
            } catch (SAXException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if ("name".equals(localName)) {
            itemName = lastValue();
        } else if ("type".equals(localName)) {
            itemType = stringToQName(lastValue());
            if (!model.getTypes().contains(itemType)) {
                throw new SAXException(new ConfigurationMessage("UNKNOWN_TYPE", 
                                                                itemType.toString()).toString());
            }
        } else if ("description".equals(localName)) {
            itemDescription = lastValue();
        } else if ("lifecyclePolicy".equals(localName)) {
            String policy = lastValue();
            if ("static".equals(policy)) {
                itemPolicy = LifecyclePolicy.STATIC;
            } else if ("process".equals(policy)) {
                itemPolicy = LifecyclePolicy.PROCESS;
            } else if ("bus".equals(policy)) {
                itemPolicy = LifecyclePolicy.BUS;    
            } else {
                itemPolicy = LifecyclePolicy.DYNAMIC; 
            }
        } else if ("configItem".equals(localName)) {
            assert null != itemName;
            assert null != itemType;
            ConfigurationItemMetadataImpl item = new ConfigurationItemMetadataImpl();
            item.setName(itemName);
            item.setType(itemType);
            if (null != itemPolicy) {
                item.setLifecyclePolicy(itemPolicy);
            }
            item.setDescription(itemDescription);
 
            model.addItem(item);
            
            itemName = null;
            itemType = null;
            itemPolicy = null;
            itemDescription = null;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        valueChars = ch;
        valueStart = start;
        valueLength = length;
    }

    // ErrorHandler interface

    public void warning(SAXParseException ex) {
        // LOG.severe(getLocationString(ex) + ": " + ex.getMessage());
        if (null == firstWarning) {
            firstWarning = ex;
        }
    }

    public void error(SAXParseException ex) {
        // LOG.severe(getLocationString(ex) + ": " + ex.getMessage());
        if (null == firstError) {
            firstError = ex;
        }
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        // LOG.severe(getLocationString(ex) + ": " + ex.getMessage());
        throw ex;
    }

    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(index + 1);
            }
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();
    }

    private void parseXML(InputSource is) throws ConfigurationException, IOException {
        SAXException exception = null;
        try {
            SAXParser parser = new SAXParser();
            parser.setContentHandler(this);
            parser.setErrorHandler(this);
            /*
             * String parserName = "org.apache.xerces.parsers.SAXParser";
             * SAXParser parser =
             * (SAXParser)ParserFactory.makeParser(parserName);
             * System.out.println("Using SAX parser of type: " +
             * parser.getClass().getName()); parser.setContentHandler(handler);
             * parser.setErrorHandler(errorHandler);
             */

            if (parser instanceof XMLReader) {
                ((XMLReader)parser).setFeature("http://xml.org/sax/features/validation", true);
                ((XMLReader)parser).setFeature("http://xml.org/sax/features/namespaces", true);
                ((XMLReader)parser).setFeature("http://apache.org/xml/features/validation/schema", true);
                ((XMLReader)parser)
                    .setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
                ((XMLReader)parser)
                    .setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
            }
            String externalSchemaLocations = getExternalSchemaLocations();
            if (null != externalSchemaLocations) {
                parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
                    externalSchemaLocations);
                LOG.fine("Using external schema locations:\n"
                                   + externalSchemaLocations);
            } else {
                LOG.fine("Using schema location hints in document.");
            }
            parser.parse(is);
        } catch (SAXException ex) {
            exception = ex;
        }
        if (null != firstError) {
            exception = firstError;
        }
        if (null != exception) {
            throw new ConfigurationException(new ConfigurationMessage("PARSE_ERROR"), exception);
        }
        if (null != firstWarning) {
            LOG.warning(exception.getMessage());
        }

        // dump();
    }

    private void importTypes(String location, String namespace) throws SAXException {

        LOG.fine("importing types from: " + location + ", using namespace: " + namespace);

        if (!prefixMappings.containsValue(namespace)) {
            throw new SAXException(new ConfigurationMessage("MISSING_PREFIX", namespace).toString());
        }

        ConfigurationTypesBuilder ctBuilder = new ConfigurationTypesBuilder();

        URI typesURI = null;
        try {
            typesURI = new URI(location);
        } catch (URISyntaxException ex) {
            throw new SAXException(new ConfigurationMessage("TYPES_URI_ERROR").toString(), ex);
        }
        if (!typesURI.isAbsolute()) {
            try {
                typesURI = url.toURI().resolve(typesURI);
            } catch (URISyntaxException ex) {
                throw new SAXException(new ConfigurationMessage("TYPES_URI_ERROR").toString(), ex);
            }
            LOG.fine("Effective import location: " + typesURI.toString());
        }

        Collection<String> types = new ArrayList<String>();

        try {
            InputSource is = new InputSource(new FileInputStream(typesURI.getPath()));
            ctBuilder.build(types, is);
        } catch (IOException ex) {
            throw new SAXException(new ConfigurationMessage("IO_ERROR", typesURI.toString()).toString());
        }

        for (String type : types) {
            model.getTypes().add(new QName(namespace, type));
        }
    }

    private String lastValue() {
        return new String(valueChars, valueStart, valueLength);
    }

    private void dump() {

        System.out.println("Types list:");
        System.out.println("===========");
        for (QName qn : model.getTypes()) {
            System.out.println(qn);
        }
        System.out.println("Item list:");
        System.out.println("===========");
        for (ConfigurationItemMetadata item : model.getDefinitions()) {
            System.out.println("name:" + item.getName());
            System.out.println("     type:" + item.getType());
            System.out.println("     lifecyclePolicy:" + item.getLifecyclePolicy());
            System.out.println("     description:" + item.getDescription());
        }
    }
    
    private QName stringToQName(String s) throws SAXException {
        
        int index = s.indexOf(":");
        if (index < 0) {
            return new QName(s);
        } else if (index == 0) {
            throw new SAXException(new ConfigurationMessage("ILLEGAL_QNAME", s).toString());
        }
        String uri = prefixMappings.get(s.substring(0, index));
        if (null == uri) {            
            throw new SAXException(new ConfigurationMessage("ILLEGAL_PREFIX", s).toString());
        }
        if (index >= (s.length() - 1)) {
            throw new SAXException(new ConfigurationMessage("ILLEGAL_QNAME", s).toString());
        }
        String localPart = s.substring(index + 1); 
        
        return new QName(uri, localPart); 
    }
    
    private String getExternalSchemaLocations() {
        if (0 == schemaLocations.size()) {
            return null;
        }

        StringBuffer buf = new StringBuffer();
        for (String[] locationPairs : schemaLocations) {
            if (buf.length() > 0) {
                buf.append(" ");
            }
            buf.append(locationPairs[0]);
            buf.append(" ");
            buf.append(locationPairs[1]);
        }

        return buf.toString();
    }

}
