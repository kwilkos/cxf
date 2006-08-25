package org.objectweb.celtix.tools.common.dom;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

import org.objectweb.celtix.common.logging.LogUtils;

public class JavaResourceEntityResolver implements XMLEntityResolver, Cloneable, EntityResolver {

    private static final Logger LOG = LogUtils.getL7dLogger(JavaResourceEntityResolver.class);
    private Map<String, String> systemIdResourceMappings = new HashMap<String, String>();
    private Map<String, String> systemIdResourceMappingPrefixes = new HashMap<String, String>();

    class LocalEntityResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
            return null;
        }
    }

    public void mapSystemIdentifierToResource(String systemId, String resource) {
        // Needs parameters to set the location of a local resource.
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Mapping " + systemId + " to " + resource);
        }
        systemIdResourceMappings.put(systemId, resource);
    }

    public void mapSystemIdentifierPrefixToResourcePrefix(String systemIdPrefix, String resourcePrefix) {
        if (systemIdPrefix == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Argument for systemIdPrefix cannot be null");
            }
            throw new IllegalArgumentException("Argument for systemIdPrefix cannot be null");
        } else {
            systemIdResourceMappingPrefixes.put(systemIdPrefix, resourcePrefix);
        }
    }

    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) {
        String resource = null;

        // Go through direct mappings first, these take priority
        if (systemIdResourceMappings.containsKey(resourceIdentifier.getLiteralSystemId())) {
            resource = (String)systemIdResourceMappings.get(resourceIdentifier.getLiteralSystemId());
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("SystemId maps to resource " + resource);
            }
        } else {
            for (Iterator it = systemIdResourceMappingPrefixes.keySet().iterator(); it.hasNext();) {
                String key = (String)it.next();
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("checking " + resourceIdentifier.getExpandedSystemId() + " for prefix " + key);
                }
                if (resourceIdentifier.getExpandedSystemId().startsWith(key)) {
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("prefix is true, setting resource");
                    }
                    resource = (String)systemIdResourceMappingPrefixes.get(key)
                               + resourceIdentifier.getExpandedSystemId().substring(key.length());
                }
            }
        }

        if (resource == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(resourceIdentifier.getLiteralSystemId()
                         + " does not map to a local resource, set one with "
                         + "mapSystemIdentifierToResource(). Trying to get the systemId " + "as a resource");
            }
            resource = resourceIdentifier.getLiteralSystemId();
        }

        if (resource == null) {
            return null;
        }

        InputStream in = getClass().getResourceAsStream(resource);

        if (in == null) {
            LOG.log(Level.WARNING, "FOUND_RESOURCE_FAILURE_MSG", resource);
            return null;
        }

        XMLInputSource xis = new XMLInputSource(resourceIdentifier);

        xis.setByteStream(in);
        return xis;
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        String resource = null;

        // Go through direct mappings first, these take priority
        if (systemIdResourceMappings.containsKey(systemId)) {
            resource = (String)systemIdResourceMappings.get(systemId);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.info("SystemId maps to resource " + resource);
            }
        } else {
            for (Iterator it = systemIdResourceMappingPrefixes.keySet().iterator(); it.hasNext();) {
                String key = (String)it.next();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.info("checking " + systemId + " for prefix " + key);
                }
                if (systemId.startsWith(key)) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.info("prefix is true, setting resource");
                    }
                    resource = (String)systemIdResourceMappingPrefixes.get(key)
                               + systemId.substring(key.length());
                }
            }
        }

        if (resource == null) {
            LOG.log(Level.WARNING, "CANNOT_MAP_LOCAL_RESOURCE_MSG", systemId);
            resource = systemId;
        }

        if (resource == null) {
            return null;
        }

        InputStream in = getClass().getResourceAsStream(resource);

        if (in == null) {
            LOG.log(Level.WARNING, "CANNOT_FIND_RESOURCE_MSG", resource);
            return null;
        }

        InputSource xis = new InputSource(systemId);

        xis.setByteStream(in);
        return xis;
    }

    public Object clone() {
        try {
            super.clone();
        } catch (Exception e) {
            // intently do nothing
        }
        JavaResourceEntityResolver res = new JavaResourceEntityResolver();

        res.systemIdResourceMappings = new HashMap<String, String>(systemIdResourceMappings);
        res.systemIdResourceMappingPrefixes = new HashMap<String, String>(systemIdResourceMappingPrefixes);
        return res;
    }
}
