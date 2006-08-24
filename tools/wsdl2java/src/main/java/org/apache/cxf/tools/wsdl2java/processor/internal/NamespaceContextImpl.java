package org.apache.cxf.tools.wsdl2java.processor.internal;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

public class NamespaceContextImpl implements NamespaceContext {
    private Element element;

    public NamespaceContextImpl(File file) {
        Document document = null;
        DocumentBuilderFactory docFact = DocumentBuilderFactory.newInstance();
        docFact.setNamespaceAware(true);
        try {
            document = docFact.newDocumentBuilder().parse(file);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        element = document.getDocumentElement();
    }

    public String getNamespaceURI(String prefix) {
        return element.lookupNamespaceURI(prefix);

    }

    public String getPrefix(String namespaceURI) {
        return element.lookupPrefix(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }

}
