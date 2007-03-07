package org.apache.cxf.aegis.type.java5;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.TypeMapping;
import org.apache.cxf.aegis.type.XMLTypeCreator;
import org.apache.cxf.aegis.type.basic.BeanType;
import org.apache.cxf.aegis.util.XmlConstants;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.service.Service;

public class AnnotatedTypeTest
    extends AbstractAegisTest
{
    private TypeMapping tm;
    private Service service;
    
    public void setUp() throws Exception
    {
        super.setUp();
       
        Server s = createService(AnnotatedService.class, null);
        service = s.getEndpoint().getService();
        
        tm = (TypeMapping) service.get(TypeMapping.class.getName());
    }

    public void testTM()
    {
        assertTrue( tm.getTypeCreator() instanceof XMLTypeCreator );
    }
    
    public void testType()
    {
        AnnotatedTypeInfo info = new AnnotatedTypeInfo(tm, AnnotatedBean1.class, "urn:foo");
        
        Iterator elements = info.getElements();
        assertTrue(elements.hasNext());
        QName element = (QName) elements.next();
        assertTrue(elements.hasNext());
        
        element = (QName) elements.next();
        assertFalse(elements.hasNext());
        
        Type custom = info.getType(element);

        assertTrue(custom instanceof CustomStringType);
        
        Iterator atts = info.getAttributes();
        assertTrue(atts.hasNext());
        atts.next();
        assertFalse(atts.hasNext());
        
        assertTrue ( info.isExtensibleElements() );
        assertTrue( info.isExtensibleAttributes() );
    }

    public void testAegisType()
    {
        BeanType type = (BeanType) tm.getTypeCreator().createType(AnnotatedBean3.class);

        assertFalse(type.getTypeInfo().getAttributes().hasNext());
        
        Iterator itr = type.getTypeInfo().getElements();
        assertTrue(itr.hasNext());
        QName q = (QName) itr.next();
        assertEquals("attProp", q.getLocalPart());
    }
    
    public void testExtensibilityOff()
    {
        BeanType type = (BeanType) tm.getTypeCreator().createType(AnnotatedBean4.class);
        
        assertFalse ( type.getTypeInfo().isExtensibleElements() );
        assertFalse ( type.getTypeInfo().isExtensibleAttributes() );
    }
    
    public void testNillableAndMinOccurs()
    {
        BeanType type = (BeanType) tm.getTypeCreator().createType(AnnotatedBean4.class);
        AnnotatedTypeInfo info = (AnnotatedTypeInfo) type.getTypeInfo();
        Iterator elements = info.getElements();
        assertTrue(elements.hasNext());
        // nillable first
        QName element = (QName) elements.next();
        if ( "minOccursProperty".equals( element.getLocalPart() ) )
        {
            assertEquals(1, info.getMinOccurs( element ) );
        }
        else
        {
            assertFalse( info.isNillable( element ) );
        }
        
        assertTrue(elements.hasNext());
        // minOccurs = 1 second
        element = (QName) elements.next();
        if ( "minOccursProperty".equals( element.getLocalPart() ) )
        {
            assertEquals(1, info.getMinOccurs( element ) );
        }
        else
        {
            assertFalse( info.isNillable( element ) );
        }        
    }

    public void testWSDL() throws Exception
    {
        Document wsdl = getWSDLDocument("AnnotatedService");

        addNamespace("xsd", XmlConstants.XSD);
        assertValid("//xsd:complexType[@name='AnnotatedBean1']/xsd:sequence/xsd:element[@name='elementProperty']", wsdl);
        assertValid("//xsd:complexType[@name='AnnotatedBean1']/xsd:attribute[@name='attributeProperty']", wsdl);
        assertValid("//xsd:complexType[@name='AnnotatedBean1']/xsd:sequence/xsd:element[@name='bogusProperty']", wsdl);

        assertValid("//xsd:complexType[@name='AnnotatedBean2']/xsd:sequence/xsd:element[@name='element'][@type='xsd:string']", wsdl);
        assertValid("//xsd:complexType[@name='AnnotatedBean2']/xsd:attribute[@name='attribute'][@type='xsd:string']", wsdl);
    }
    
    public void testGetSetRequired() throws Exception
    {
        BeanType type = new BeanType(new AnnotatedTypeInfo(tm, BadBean.class, "urn:foo"));
        type.setSchemaType(new QName("urn:foo", "BadBean"));
        
        assertFalse(type.getTypeInfo().getElements().hasNext());
    }
    
    public static class BadBean
    {
        public void setString(String string)
        {
        }
    }
}
