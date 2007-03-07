package org.apache.cxf.aegis.type.java5;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.type.Configuration;
import org.apache.cxf.aegis.type.CustomTypeMapping;
import org.apache.cxf.aegis.type.DefaultTypeCreator;
import org.apache.cxf.aegis.type.Type;

public class XmlParamTypeTest
    extends AbstractAegisTest
{
    private CustomTypeMapping tm;
    private Java5TypeCreator creator;

    public void setUp() throws Exception
    {
        super.setUp();
        
        tm = new CustomTypeMapping();
        creator = new Java5TypeCreator();
        creator.setNextCreator(new DefaultTypeCreator());
        creator.setConfiguration(new Configuration());
        tm.setTypeCreator(creator);
    }

    public void testType() throws Exception
    {
        Method m = CustomTypeService.class.getMethod("doFoo", new Class[] { String.class });
        
        Type type = creator.createType(m, 0);
        tm.register(type);
        assertTrue( type instanceof CustomStringType );
        assertEquals( new QName("urn:xfire:foo", "custom"), type.getSchemaType());
        
        type = creator.createType(m, -1);
        tm.register(type);
        assertTrue( type instanceof CustomStringType );
        assertEquals( new QName("urn:xfire:foo", "custom"), type.getSchemaType());
    }
    
    public void testMapServiceWSDL() throws Exception
    {
        createService(CustomTypeService.class, null);
        
        Document wsdl = getWSDLDocument("CustomTypeService");
        assertValid("//xsd:element[@name='s'][@type='ns0:custom']", wsdl);
    }
    
    public class CustomTypeService
    {
        
        @XmlReturnType(type=CustomStringType.class, 
                       namespace="urn:xfire:foo",
                       name="custom")
        public String doFoo(@XmlParamType(type=CustomStringType.class, 
                                        namespace="urn:xfire:foo",
                                        name="custom") String s) {
        	return null;
        }
    }
}
