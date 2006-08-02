package org.objectweb.celtix.common.util;

import java.util.*;

import junit.framework.TestCase;

public class PropertiesLoaderUtilsTest extends TestCase {

    Properties properties;
    String soapBindingFactory = "org.objectweb.celtix.bindings.soap.SOAPBindingFactory";
    
    public void setUp() throws Exception {
        properties = PropertiesLoaderUtils.
            loadAllProperties("org/objectweb/celtix/common/util/resources/bindings.properties",
                              Thread.currentThread().getContextClassLoader());
        assertNotNull(properties);        
        
    }
    public void testLoadBindings() throws Exception {

        assertEquals(soapBindingFactory,
                     properties.getProperty("http://schemas.xmlsoap.org/wsdl/soap/"));

        assertEquals(soapBindingFactory,
                     properties.getProperty("http://schemas.xmlsoap.org/wsdl/soap/http"));

        assertEquals(soapBindingFactory,
                     properties.getProperty("http://celtix.objectweb.org/transports/jms"));
        

    }

    public void testGetPropertyNames() throws Exception {
        Collection names = PropertiesLoaderUtils.getPropertyNames(properties, soapBindingFactory);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("http://schemas.xmlsoap.org/wsdl/soap/"));
        assertTrue(names.contains("http://schemas.xmlsoap.org/wsdl/soap/http"));
        assertTrue(names.contains("http://celtix.objectweb.org/transports/jms"));
    }
}
