package org.apache.cxf.aegis.type.java5;

import javax.xml.namespace.QName;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.type.Configuration;
import org.apache.cxf.aegis.type.CustomTypeMapping;
import org.apache.cxf.aegis.type.DefaultTypeMappingRegistry;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.XMLTypeCreator;
import org.apache.cxf.aegis.type.basic.BeanType;
import org.apache.cxf.aegis.type.basic.BeanTypeInfo;

/**
 * @author adam
 * 
 */
public class ConfigurationTest
    extends AbstractAegisTest
{

    CustomTypeMapping tm;

    Configuration config = null;

    public void setUp()
        throws Exception
    {
        super.setUp();

        DefaultTypeMappingRegistry reg = new DefaultTypeMappingRegistry();
        config = reg.getConfiguration();
        XMLTypeCreator creator = new XMLTypeCreator();
        creator.setConfiguration(reg.getConfiguration());
        Java5TypeCreator next = new Java5TypeCreator();
        next.setConfiguration(reg.getConfiguration());
        creator.setNextCreator(next);
        reg.createDefaultMappings();
        tm = (CustomTypeMapping) reg.getDefaultTypeMapping();
        tm.setTypeCreator(creator);
    }
    
    public void testNillableDefaultTrue() throws Exception
    {
        config.setDefaultNillable( true );

        Type type = tm.getTypeCreator().createType(AnnotatedBean1.class);
        BeanTypeInfo info = ((BeanType) type).getTypeInfo();

        assertTrue(info.isNillable(new QName(info.getDefaultNamespace(), "bogusProperty")));
    }
    
    public void testNillableDefaultFalse() throws Exception
    {
        config.setDefaultNillable( false );
        Type type = tm.getTypeCreator().createType(AnnotatedBean1.class);
        BeanTypeInfo info = ((BeanType) type).getTypeInfo();

        assertFalse(info.isNillable(new QName(info.getDefaultNamespace(), "bogusProperty")));
    }
    
    public void testMinOccursDefault0() throws Exception
    {
        config.setDefaultMinOccurs( 0 );
        Type type = tm.getTypeCreator().createType(AnnotatedBean1.class);
        BeanTypeInfo info = ((BeanType) type).getTypeInfo();

        assertEquals(info.getMinOccurs(new QName(info.getDefaultNamespace(), "bogusProperty")), 0);
    }
    
    public void testMinOccursDefault1() throws Exception
    {
        config.setDefaultMinOccurs( 1 );
        Type type = tm.getTypeCreator().createType(AnnotatedBean1.class);
        BeanTypeInfo info = ((BeanType) type).getTypeInfo();

        assertEquals(info.getMinOccurs(new QName(info.getDefaultNamespace(), "bogusProperty")), 1);
    }
    
    public void testExtensibleDefaultTrue() throws Exception
    {
        config.setDefaultExtensibleElements( true );
        config.setDefaultExtensibleAttributes( true );
        Type type = tm.getTypeCreator().createType(AnnotatedBean1.class);
        BeanTypeInfo info = ((BeanType) type).getTypeInfo();
        assertTrue(info.isExtensibleElements());
        assertTrue(info.isExtensibleAttributes());
    }
    
    public void testExtensibleDefaultFalse() throws Exception
    {
        config.setDefaultExtensibleElements( false );
        config.setDefaultExtensibleAttributes( false );
        Type type = tm.getTypeCreator().createType(AnnotatedBean1.class);
        BeanTypeInfo info = ((BeanType) type).getTypeInfo();
        assertFalse(info.isExtensibleElements());
        assertFalse(info.isExtensibleAttributes());
    }
    
}