package org.apache.cxf.tools.common.dom;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
    
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.apache.cxf.common.logging.LogUtils;

/**
 * (not thread safe)
 * 
 */
public class ExtendedDocumentBuilder {

    private static final Logger LOG = LogUtils.getL7dLogger(ExtendedDocumentBuilder.class);

    private final DocumentBuilderFactory parserFactory;
    private DocumentBuilder parser;

    private SchemaFactory schemaFactory;
    private Schema schema;

    public ExtendedDocumentBuilder() {
        parserFactory = DocumentBuilderFactory.newInstance();
        parserFactory.setNamespaceAware(true);
    }

    private InputStream getSchemaLocation() {
        String toolspec = "/org/objectweb/celtix/tools/common/toolspec/tool-specification.xsd";
        return getClass().getResourceAsStream(toolspec);
    }

    public void setValidating(boolean validate) {
        if (validate) {
            this.schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                this.schema = schemaFactory.newSchema(new StreamSource(getSchemaLocation()));
            } catch (org.xml.sax.SAXException e) {
                LOG.log(Level.SEVERE, "SCHEMA_FACTORY_EXCEPTION_MSG");
            }
            this.parserFactory.setSchema(this.schema);
        }
    }


    private DocumentBuilder getParser() {
        if (parser == null) {
            try {
                parser = parserFactory.newDocumentBuilder();
            } catch (javax.xml.parsers.ParserConfigurationException e) {
                LOG.log(Level.SEVERE, "NEW_DOCUMENT_BUILDER_EXCEPTION_MSG");
            }
        }
        return parser;
    }
    
    public Document parse(InputStream in) throws SAXException, IOException {
        if (in == null && LOG.isLoggable(Level.FINE)) {
            LOG.fine("ExtendedDocumentBuilder trying to parse a null inputstream");
        }
        return getParser().parse(in);
    }

}
