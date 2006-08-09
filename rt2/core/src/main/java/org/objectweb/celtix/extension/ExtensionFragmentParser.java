package org.objectweb.celtix.extension;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

public class ExtensionFragmentParser {

    Extension getExtension(InputStream is) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(is);
        } catch (ParserConfigurationException ex) {
            throw new ExtensionException(ex);
        } catch (SAXException ex) {
            throw new ExtensionException(ex);
        } catch (IOException ex) {
            throw new ExtensionException(ex);
        }
        
        return deserialiseExtension(document);
    }
    
    Extension deserialiseExtension(Document document) {
        Extension e = new Extension();
        
        // TODO
        
        return e;
    }
    
    
}
