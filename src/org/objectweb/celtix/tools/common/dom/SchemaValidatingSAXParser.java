package org.objectweb.celtix.tools.common.dom;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.parsers.SAXParser;

import org.objectweb.celtix.common.logging.LogUtils;

/**
 * (not thread safe)
 * 
 */
public final class SchemaValidatingSAXParser {

    private static final Logger LOG = LogUtils.getL7dLogger(SchemaValidatingSAXParser.class);

    private final JavaResourceEntityResolver entityResolver = new JavaResourceEntityResolver();
    private final SAXParser parser = new SAXParser();
    private final SchemaLocationMap schemaLocations = new SchemaLocationMap();

    public SchemaValidatingSAXParser() {
        try {
            setValidating(true);
            parser.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE, true);
            parser.setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY,
                               entityResolver);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "SET_FEATURE_FAILURE_MSG");
        }
    }

    public void setValidating(boolean validate) {
        try {
            parser.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE, validate);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "SET_FEATURE_FAILURE_MSG");
        }
    }

    public SAXParser getSAXParser() {
        return parser;
    }

    public void mapDefaultNamespaceToSchemaResource(String path) {
        entityResolver.mapSystemIdentifierToResource(null, path);
    }

    public void mapNamespaceToSchemaResource(String publicId, String systemId, String path) {
        entityResolver.mapSystemIdentifierToResource(systemId, path);
        schemaLocations.add(publicId, systemId);
        if (schemaLocations.size() > 0) {
            try {
                parser.setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_LOCATION,
                                   schemaLocations.toString());
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "FAIL_SET_SAX_PARSER_MSG", Constants.XERCES_PROPERTY_PREFIX
                                                              + Constants.SCHEMA_LOCATION);
            }
        }
    }

}
