package org.objectweb.celtix.bus.configuration;

import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


// import org.apache.xerces.parsers.SAXParser;

class ConfigurationTypesBuilder extends DefaultHandler {
    
    private Collection<String> types;


    protected void build(Collection<String> t, InputSource is) throws SAXException, 
        IOException, ParserConfigurationException {
        types = t;
        parseXML(is);
    }
    

    // ContentHandler interface

    public void startElement(String uri, String localName, String qName, Attributes attributes) 
        throws SAXException {
        super.startElement(uri, localName, qName, attributes);
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

    private void parseXML(InputSource is) throws SAXException, IOException, 
        ParserConfigurationException {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/namespaces", true);
        SAXParser parser = factory.newSAXParser();

        parser.parse(is, this);
    }

}
