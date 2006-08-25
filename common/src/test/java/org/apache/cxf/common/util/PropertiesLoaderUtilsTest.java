package org.apache.cxf.common.util;

import java.util.*;

import junit.framework.TestCase;

public class PropertiesLoaderUtilsTest extends TestCase {

    Properties properties;
    String soapBindingFactory = "org.apache.cxf.bindings.soap.SOAPBindingFactory";
    
    public void setUp() throws Exception {
        properties = PropertiesLoaderUtils.
            loadAllProperties("org/apache/cxf/common/util/resources/bindings.properties.xml",
                              Thread.currentThread().getContextClassLoader());
        assertNotNull(properties);        
        
    }
    public void testLoadBindings() throws Exception {

        assertEquals(soapBindingFactory,
                     properties.getProperty("http://schemas.xmlsoap.org/wsdl/soap/"));

        assertEquals(soapBindingFactory,
                     properties.getProperty("http://schemas.xmlsoap.org/wsdl/soap/http"));

        assertEquals(soapBindingFactory,
                     properties.getProperty("http://cxf.apache.org/transports/jms"));
        

    }

    public void testGetPropertyNames() throws Exception {
        Collection<String> names = PropertiesLoaderUtils.getPropertyNames(properties, soapBindingFactory);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("http://schemas.xmlsoap.org/wsdl/soap/"));
        assertTrue(names.contains("http://schemas.xmlsoap.org/wsdl/soap/http"));
        assertTrue(names.contains("http://cxf.apache.org/transports/jms"));
    }
}
