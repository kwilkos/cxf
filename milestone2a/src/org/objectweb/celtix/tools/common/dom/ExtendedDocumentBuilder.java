package org.objectweb.celtix.tools.common.dom;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;

import org.objectweb.celtix.common.logging.LogUtils;

/**
 * (not thread safe)
 * 
 */
public class ExtendedDocumentBuilder {

    private static final Logger LOG = LogUtils.getL7dLogger(ExtendedDocumentBuilder.class);

    private DOMParser parser;
    private final JavaResourceEntityResolver entityResolver = new JavaResourceEntityResolver();
    private final SchemaLocationMap schemaLocations = new SchemaLocationMap();

    public void setValidating(boolean validate) {

        try {
            getParser().setFeature("http://xml.org/sax/features/validation", validate);
        } catch (SAXNotRecognizedException e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("failed to set feature: " + e.getMessage());
            }
        } catch (SAXNotSupportedException e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("failed to set feature: " + e.getMessage());
            }
        }

    }

    public JavaResourceEntityResolver getJavaResourceEntityResolver() {
        return entityResolver;
    }

    private DOMParser getParser() {
        if (parser == null) {
            parser = new DOMParser();
            try {
                parser.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE, true);
                parser
                    .setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE, true);
                // parser.setProperty(Constants.XERCES_PROPERTY_PREFIX+Constants.ERROR_REPORTER_PROPERTY,
                // new Log4jErrorReporter(log)); // can't get this to work!!!
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "SET_FEATURE_FAILURE_MSG", ex);
            }
            setEntityResolver(entityResolver);
        }
        return parser;
    }

    private void setEntityResolver(XMLEntityResolver er) {
        try {
            getParser()
                .setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY, er);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "SET_RESOLVER_FAILURE_MSG", ex);
        }
    }

    public void mapDefaultNamespaceToSchemaResource(String path) {
        entityResolver.mapSystemIdentifierToResource(null, path);
    }

    public void mapNamespaceToSchemaResource(String publicId, String systemId, String path) {
        entityResolver.mapSystemIdentifierToResource(systemId, path);
        schemaLocations.add(publicId, systemId);
    }

    public Document parse(InputStream in) throws SAXException, IOException {
        if (in == null && LOG.isLoggable(Level.FINE)) {
            LOG.fine("ExtendedDocumentBuilder trying to parse a null inputstream");
        }
        if (schemaLocations.size() > 0) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Setting property " + Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_LOCATION
                         + " to " + schemaLocations);
            }
            getParser().setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_LOCATION,
                                    schemaLocations.toString());
        }
        getParser().parse(new InputSource(in));
        return getParser().getDocument();
    }

}
