package org.objectweb.celtix.bus.configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.xerces.parsers.SAXParser;

class ConfigurationTypesBuilder extends DefaultHandler {

    private static final Logger LOG = Logger.getLogger(ConfigurationMetadataBuilder.class.getName());
    
    private Collection<String> types;

    protected void build(Collection<String> t, InputSource is) throws SAXException, IOException {
        types = t;
        parseXML(is);
    }

    // ContentHandler interface

    public void startElement(String uri, String localName, String qName, Attributes attributes) 
        throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        /*
        System.out.println("startElement uri: " + uri + ", localName: " + localName
                           + ", qName: " + qName + ", #attributes: " + attributes.getLength());
                          */
        String typeName = attributes.getValue("name");
        if (null != typeName) {
            if ("complexType".equals(localName)) {
                types.add(typeName); 
            } else if ("simpleType".equals(localName)) {
                types.add(typeName);
            }
        }
    }

    // ErrorHandler interface

    private void parseXML(InputSource is) throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        parser.setContentHandler(this);
        parser.setErrorHandler(this);

        if (parser instanceof XMLReader) {
            ((XMLReader)parser).setFeature("http://xml.org/sax/features/validation", false);
            ((XMLReader)parser).setFeature("http://xml.org/sax/features/namespaces", true);
            ((XMLReader)parser).setFeature("http://apache.org/xml/features/validation/schema", true);
            ((XMLReader)parser).setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
                                           true);
            ((XMLReader)parser).setFeature("http://apache.org/xml/features/validation/schema-full-checking",
                                           false);
        }
        parser.parse(is);
    }

}
