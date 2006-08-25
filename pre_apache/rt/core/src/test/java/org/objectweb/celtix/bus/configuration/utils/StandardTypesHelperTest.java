package org.objectweb.celtix.bus.configuration.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingType;
import org.objectweb.celtix.configuration.types.ExtensionType;

import static org.easymock.EasyMock.expect;

public class StandardTypesHelperTest extends TestCase {
    
    private static final String TEST_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/http/";
    
    public void testSupportsNamespace() {
        IMocksControl control = EasyMock.createNiceControl();
       
        ClassNamespaceMappingType mapping = control.createMock(ClassNamespaceMappingType.class);
        List<String> namespaces = new ArrayList<String>();
        expect(mapping.getNamespace()).andReturn(namespaces);
        control.replay();
        assertTrue("Namespace should not be supported.", 
                   !StandardTypesHelper.supportsNamespace(mapping, TEST_NAMESPACE));
        control.verify();
        control.reset();
        namespaces.add(TEST_NAMESPACE);
        expect(mapping.getNamespace()).andReturn(namespaces);
        control.replay();
        assertTrue("Namespace should not be supported.", 
                   StandardTypesHelper.supportsNamespace(mapping, TEST_NAMESPACE));
        control.verify();
    }
    
    public void testParseClassNamespaceMappingsFragment() throws IOException, JAXBException {        
        InputStream is = getClass().getResourceAsStream("invalid-fragment.xml");
        try {
            StandardTypesHelper.parseClassNamespaceMappingsFragment(is, getClass().getClassLoader());
            fail("Expected JAXBException not thrown.");
        } catch (JAXBException ex) {
            // expected
        }
        
        is = getClass().getResourceAsStream("valid-class-namespace-mappings.xml");
        List<ClassNamespaceMappingType> mappings = null;
        mappings = StandardTypesHelper.parseClassNamespaceMappingsFragment(is, getClass().getClassLoader());
        assertNotNull(mappings);
        assertEquals(1, mappings.size());
        assertTrue(StandardTypesHelper.supportsNamespace(mappings.get(0), TEST_NAMESPACE)); 
    }
    
    public void testParseExtensionsFragment() throws IOException, JAXBException {        
        InputStream is = getClass().getResourceAsStream("invalid-fragment.xml");
        try {
            StandardTypesHelper.parseExtensionsFragment(is, getClass().getClassLoader());
            fail("Expected JAXBException not thrown.");
        } catch (JAXBException ex) {
            // expected
        }
        
        is = getClass().getResourceAsStream("valid-extensions.xml");
        List<ExtensionType> extensions = null;
        extensions = StandardTypesHelper.parseExtensionsFragment(is, getClass().getClassLoader());
        assertNotNull(extensions);
        assertEquals(2, extensions.size());
    }
}
