package org.objectweb.celtix.bus.configuration.spring;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.objectweb.celtix.bus.configuration.TestConfigurationBuilder;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.impl.TypeSchema;
import org.objectweb.celtix.configuration.impl.TypeSchemaHelper;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.UrlResource;

public class CustomPropertyEditorsTest extends TestCase {   
    
    public void testJaxbPropertyEditorGetAsText() {
        PropertyEditor pe = new JaxbPropertyEditor();
        assertNull(pe.getValue());
        assertNull(pe.getAsText());
        pe.setValue("abc");
        assertEquals("abc", pe.getAsText());  
        Element element = EasyMock.createMock(Element.class);
        element.getTextContent();
        EasyMock.expectLastCall().andReturn("xyz");
        EasyMock.replay(element);
        pe.setValue(element);
        assertEquals("xyz", pe.getAsText());
        EasyMock.verify(element);
    }
    
    public void testJaxbPropertyEditorSetAsText() {
        PropertyEditor pe = new JaxbPropertyEditor();
        assertNull(pe.getValue());
        assertNull(pe.getAsText());
        pe.setAsText("abc");
        assertEquals("abc", pe.getAsText()); 
        pe.setValue(Boolean.TRUE);
        try {
            pe.setAsText("false");
            fail("Expected IllegalArgumentException not thrown.");
        } catch (IllegalArgumentException ex) {
            // ignore
        }
        pe.setValue("boolean");
        pe.setAsText("false");
        assertEquals("false", pe.getAsText());      
    }
    
    public void testJaxbPropertyEditorGetValue() throws JAXBException {
        PropertyEditor pe = new JaxbPropertyEditor();
        helpTestGetValue(pe, Boolean.TRUE, "address");
    }
    
    public void testJaxbBigIntegerEditorsetAsText() {
        PropertyEditor pe = new JaxbBigIntegerEditor();
        pe.setAsText("12345");
        Object o = pe.getValue();
        assertTrue(o instanceof BigInteger);
        assertEquals(12345, ((BigInteger)o).intValue());
    }
    
    public void testJaxbNumberEditorGetValue() throws JAXBException {
        Object value = null;
        PropertyEditor pe = null;
        
        value = new Byte(Byte.MAX_VALUE);
        pe = new JaxbNumberEditor(Byte.class);
        helpTestGetValue(pe, value, "Byte");
        
        value = new Short(Short.MAX_VALUE);
        pe = new JaxbNumberEditor(Short.class);
        helpTestGetValue(pe, value, "Short");
        
        value = new Integer(Integer.MAX_VALUE);
        pe = new JaxbNumberEditor(Integer.class);
        helpTestGetValue(pe, value, "Integer");
        
        value = new Long(Long.MAX_VALUE);
        pe = new JaxbNumberEditor(Long.class);
        helpTestGetValue(pe, value, "Long");
        
        value = new Double(Double.MAX_VALUE);
        pe = new JaxbNumberEditor(Double.class);
        helpTestGetValue(pe, value, "Double");
        
        value = new Float(Float.MAX_VALUE);
        pe = new JaxbNumberEditor(Float.class);
        helpTestGetValue(pe, value, "Float");      
    }
    
    public void testJaxbBooleanEditor() throws JAXBException {
        Object value = null;
        PropertyEditor pe = null;
        
        value = Boolean.TRUE;
        pe = new JaxbBooleanEditor();
        helpTestGetValue(pe, value, "Boolean");
    }
    
    public void testPropertyEditorConversionFailure() throws InvocationTargetException, 
        NoSuchMethodException, IllegalAccessException {
        URL url = CustomPropertyEditorsTest.class.getResource("resources/top3.xml");
        UrlResource urlRes = new UrlResource(url);
        CeltixXmlBeanFactory bf = new CeltixXmlBeanFactory(urlRes);
        Configuration top = new TestConfigurationBuilder().build("top3");
        bf.registerCustomEditors(top); 
      
        // the first form results in a BeanCreationException (caused by a 
        // PropertyAccessExceptionsException)
        
        try {
            bf.getBean("top3");
            fail("Expected BeanCreationException not thrown.");           
        } catch (BeanCreationException ex) {
            // ignore
        } 
        
        // the second form (preferrable because it performs schema validation)
        // results in a BeanCreationException (caused by a JAXBException)
       
        try {
            bf.getBean("top4");
            fail("Expected BeanCreationException not thrown.");           
        } catch (BeanCreationException ex) {
            ConfigurationException cause = (ConfigurationException)ex.getCause();
            assertEquals("JAXB_PROPERTY_EDITOR_EXC", cause.getCode());
        } 
    }
    
    private void helpTestGetValue(PropertyEditor pe, Object value, String typename) throws JAXBException {
        Element element = EasyMock.createMock(Element.class);
        String testURI = "http://celtix.objectweb.org/configuration/test/types";
        element.getNamespaceURI();
        EasyMock.expectLastCall().andReturn(testURI);
        element.getLocalName();
        EasyMock.expectLastCall().andReturn(typename); 
        TypeSchema ts = org.easymock.classextension.EasyMock.createMock(TypeSchema.class);
        TypeSchemaHelper tsh = new TypeSchemaHelper(true);
        tsh.put(testURI, ts);
        ts.unmarshal(new QName(testURI, typename), element);
        EasyMock.expectLastCall().andReturn(value); 
        EasyMock.replay(element);
        org.easymock.classextension.EasyMock.replay(ts);
       
        pe.setValue(element);
        Object o = pe.getValue();
        assertTrue(o == value); 
        
        EasyMock.reset(element);
        org.easymock.classextension.EasyMock.reset(ts);
        
        element.getNamespaceURI();
        EasyMock.expectLastCall().andReturn(testURI);
        element.getLocalName();
        EasyMock.expectLastCall().andReturn(typename); 
        ts.unmarshal(new QName(testURI, typename), element);
        EasyMock.expectLastCall().andThrow(new JAXBException("test"));
        EasyMock.replay(element);
        org.easymock.classextension.EasyMock.replay(ts);
        try {
            pe.getValue();
            fail("Expected ConfigurationException not thrown.");
        } catch (ConfigurationException ex) {
            assertEquals("JAXB_PROPERTY_EDITOR_EXC", ex.getCode());       
        }      
    }
    
       
}
