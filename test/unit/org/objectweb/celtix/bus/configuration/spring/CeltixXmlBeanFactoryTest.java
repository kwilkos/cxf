package org.objectweb.celtix.bus.configuration.spring;

import java.beans.PropertyEditor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bus.configuration.TopConfiguration;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.types.StringListType;
import org.springframework.core.io.UrlResource;

public class CeltixXmlBeanFactoryTest extends TestCase {

    public void testEntityResolver() throws SAXException, IOException, NoSuchMethodException {
        EntityResolver resolver = new CeltixBeansDtdResolver();
        assertNull(resolver.resolveEntity("-//SPRING//DTD BEAN//EN", 
            "http://www.springframework.org/dtd/spring-beans.dtd"));
        assertNotNull(resolver.resolveEntity(null, 
            "http://celtix.objectweb.org/configuration/spring/celtix-spring-beans.dtd"));
        
        
        CeltixBeansDtdResolver cer  = EasyMock.createMock(CeltixBeansDtdResolver.class,
            new Method[] {CeltixBeansDtdResolver.class.getDeclaredMethod("getDtdFile", (Class[])null)});
               
        cer.getDtdFile();
        org.easymock.EasyMock.expectLastCall().andReturn("celtixx-spring-beans.dtd");
        EasyMock.replay(cer);
        try {
            cer.resolveEntity(null, 
                "http://celtix.objectweb.org/configuration/spring/celtix-spring-beans.dtd");
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("COULD_NOT_RESOLVE_BEANS_DTD_EXC", ex.getCode());
        }      
    }
    
    public void testConstructor() {
       
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top2.xml");   
        UrlResource urlRes = new UrlResource(url);
        CeltixXmlBeanFactory bf = new CeltixXmlBeanFactory(urlRes);
        
        assertNotNull(bf);
       
    }
    
    public void testCustomEditorRegistration() {
        
        URL url = ConfigurationProviderImplTest.class.getResource("resources/top2.xml");
        UrlResource urlRes = new UrlResource(url);
        CeltixXmlBeanFactory bf = new CeltixXmlBeanFactory(urlRes);
        Configuration top = new TopConfiguration("top22");
        
        Map map = bf.getCustomEditors();
        
        // no editor for complex types registered yet
        
        assertNull(map.get(StringListType.class));
        
        // all editors for primitive types registered
        
        PropertyEditor pe = null;
        PropertyEditor pe2 = null;
        
        pe = (PropertyEditor)map.get(String.class);
        assertTrue(pe instanceof JaxbPropertyEditor);
        
        pe = (PropertyEditor)map.get(BigInteger.class);
        assertTrue(pe instanceof JaxbBigIntegerEditor);
        
        pe = (PropertyEditor)map.get(Boolean.class);
        assertTrue(pe instanceof JaxbBooleanEditor);
        pe2 = (PropertyEditor)map.get(boolean.class);
        assertTrue(pe == pe2);
        
        pe = (PropertyEditor)map.get(Byte.class);
        assertTrue(pe instanceof JaxbNumberEditor);
        pe2 = (PropertyEditor)map.get(byte.class);
        assertTrue(pe == pe2);
        
        /* 
         * string
         * big integer
         * boolean (2 x) 
         * byte (2 x)
         * short (2 x)
         * int (2 x)
         * long (2 x)
         * float (2 x)
         * double (2 x)
         * 
         */
        
        assertEquals(16, map.size());
       
        bf.registerCustomEditors(top);
        
        assertEquals(17, map.size());
        
        pe = (PropertyEditor)map.get(StringListType.class);
        assertNotNull(pe);
        assertTrue(pe instanceof JaxbPropertyEditor);
        assertTrue(pe == map.get(String.class));
    }
}
