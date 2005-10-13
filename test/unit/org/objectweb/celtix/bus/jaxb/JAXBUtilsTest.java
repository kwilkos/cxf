package org.objectweb.celtix.bus.jaxb;

import junit.framework.TestCase;

public class JAXBUtilsTest extends TestCase {

    public void testPackageNames() {
        assertEquals("org.objectweb.celtix.configuration.types",
                     JAXBUtils.namespaceURIToPackage("http://celtix.objectweb.org/configuration/types"));
        assertEquals("org.objectweb.celtix.configuration.types",
                     JAXBUtils.namespaceURIToPackage("http://celtix.objectweb.org/configuration/types.xsd"));
        assertEquals("org.objectweb.celtix.config_types",
                     JAXBUtils.namespaceURIToPackage("http://celtix.objectweb.org/config-types"));
        assertEquals("org.objectweb.celtix._default.types",
                     JAXBUtils.namespaceURIToPackage("http://celtix.objectweb.org/default/types"));
        assertEquals("org.objectweb.celtix.config._4types",
                     JAXBUtils.namespaceURIToPackage("http://celtix.objectweb.org/config/4types."));
        assertEquals("com.iona.configuration.types",
                     JAXBUtils.namespaceURIToPackage("http://www.iona.com/configuration/types"));
        assertEquals("org.objectweb.celtix.config.types",
                     JAXBUtils.namespaceURIToPackage("urn://celtix-objectweb-org/config/types"));
        
    }   
}
