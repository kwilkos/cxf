package org.objectweb.celtix.bus.configuration;

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.InputSource;

import junit.framework.TestCase;

import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.impl.ConfigurationMetadataUtils;

public class ConfigurationMetadataUtilsTest extends TestCase {

    private static final String FRAGMENT = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
        + "<a xmlns:tt=\"http://celtix.objectweb.org/configuration/test/types\">" 
        + "<b1><c1 name=\"tt:un\">tt:one</c1></b1>"
        + "<b2><c2 xmlns:tt=\"http://celtix.objectweb.org/configuration/types\" name=\"tt:deux\">"
        + "tt:two</c2></b2>"
        + "<b3 xmlns:tt=\"http://celtix.objectweb.org/configuration/types\">" 
        + "<c3 xmlns:tt=\"http://celtix.objectweb.org/configuration/metadata\" name=\"tt:trois\">"
        + "tt:three</c3></b3>"
        + "<b4></b4>"
        + "<b5>BEFORE<c5></c5>AFTER</b5>"
        + "<b55><c5></c5>AFTER</b55>"
        + "<b6><c6>:noprefix</c6></b6>"
        + "<b7><c7>tt:</c7></b7>"
        + "<b8><c8>dd:unknownprefix</c8></b8>"
        + "</a>";
    
    private static Document document;

    public void testGetElementValue() throws Exception {
        Document doc = getDocument(FRAGMENT);
        Node node = doc.getElementsByTagName("c1").item(0);
        assertEquals("tt:one", ConfigurationMetadataUtils.getElementValue(node));
        node = doc.getElementsByTagName("b4").item(0);
        assertNull(ConfigurationMetadataUtils.getElementValue(node));
        node = doc.getElementsByTagName("b5").item(0);
        assertEquals("BEFORE", ConfigurationMetadataUtils.getElementValue(node));
        node = doc.getElementsByTagName("b55").item(0);
        assertEquals("AFTER", ConfigurationMetadataUtils.getElementValue(node));
    }
    
    public void testGetElementValueToQName() throws Exception {
        Document doc = getDocument(FRAGMENT);
        Element elem = (Element)(doc.getElementsByTagName("c1").item(0));
        QName qn = null;
        qn = new QName("http://celtix.objectweb.org/configuration/test/types", "one");
        assertEquals(qn, ConfigurationMetadataUtils.elementValueToQName(doc, elem));
        elem = (Element)(doc.getElementsByTagName("c2").item(0));
        qn = new QName("http://celtix.objectweb.org/configuration/types", "two");
        assertEquals(qn, ConfigurationMetadataUtils.elementValueToQName(doc, elem));
        elem = (Element)(doc.getElementsByTagName("c3").item(0));
        qn = new QName("http://celtix.objectweb.org/configuration/metadata", "three");
        assertEquals(qn, ConfigurationMetadataUtils.elementValueToQName(doc, elem));
        
        elem = (Element)(doc.getElementsByTagName("c6").item(0));
        try {
            ConfigurationMetadataUtils.elementValueToQName(doc, elem);
        } catch (ConfigurationException ex) {
            assertEquals("ILLEGAL_QNAME_EXC", ex.getCode());
        }
        elem = (Element)(doc.getElementsByTagName("c7").item(0));
        try {
            ConfigurationMetadataUtils.elementValueToQName(doc, elem);
        } catch (ConfigurationException ex) {
            assertEquals("ILLEGAL_QNAME_EXC", ex.getCode());
        }
        elem = (Element)(doc.getElementsByTagName("c8").item(0));
        try {
            ConfigurationMetadataUtils.elementValueToQName(doc, elem);
        } catch (ConfigurationException ex) {
            assertEquals("ILLEGAL_PREFIX_EXC", ex.getCode());
        }
    }
    
    public void testGetElementAttributeToQName() throws Exception {
        Document doc = getDocument(FRAGMENT);
        Element elem = (Element)(doc.getElementsByTagName("c1").item(0));
        QName qn = null;
        qn = new QName("http://celtix.objectweb.org/configuration/test/types", "un");
        assertEquals(qn, ConfigurationMetadataUtils.elementAttributeToQName(doc, elem, "name"));
        elem = (Element)(doc.getElementsByTagName("c2").item(0));
        qn = new QName("http://celtix.objectweb.org/configuration/types", "deux");
        assertEquals(qn, ConfigurationMetadataUtils.elementAttributeToQName(doc, elem, "name"));
        elem = (Element)(doc.getElementsByTagName("c3").item(0));
        qn = new QName("http://celtix.objectweb.org/configuration/metadata", "trois");
        assertEquals(qn, ConfigurationMetadataUtils.elementAttributeToQName(doc, elem, "name"));
    }
    
    private Document getDocument(String str) throws Exception { 
        if (null == document) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(new InputSource(new ByteArrayInputStream(str.getBytes())));
        }
        return document;
    }
}
