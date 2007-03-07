package org.apache.cxf.aegis.type.java5;

import javax.mail.MessageContext;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.type.Configuration;
import org.apache.cxf.aegis.type.CustomTypeMapping;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.java5.CurrencyService.Currency;
import org.apache.cxf.aegis.util.XmlConstants;
import org.apache.cxf.aegis.xml.jdom.JDOMReader;
import org.apache.cxf.aegis.xml.jdom.JDOMWriter;
import org.apache.cxf.binding.soap.SoapConstants;
import org.jdom.Element;
import org.jdom.output.DOMOutputter;

public class EnumTypeTest
    extends AbstractAegisTest
{
    private CustomTypeMapping tm;
    
    private enum smallEnum { VALUE1, VALUE2 };
    
    public void setUp() throws Exception
    {
        super.setUp();
        
        tm = new CustomTypeMapping();
        Java5TypeCreator creator = new Java5TypeCreator();
        creator.setConfiguration(new Configuration());
        tm.setTypeCreator(creator);
    }

    public void testType() throws Exception
    {
        EnumType type = new EnumType();
        type.setTypeClass(smallEnum.class);
        type.setSchemaType(new QName("urn:test", "test"));

        tm.register(type);
        
        Element root = new Element("root");
        JDOMWriter writer = new JDOMWriter(root);
        
        type.writeObject(smallEnum.VALUE1, writer, new Context());
        
        assertEquals("VALUE1", root.getValue());
        
        JDOMReader reader = new JDOMReader(root);
        Object value = type.readObject(reader, new Context());
        
        assertEquals(smallEnum.VALUE1, value);
    }

    public void testAutoCreation() throws Exception
    {
        Type type = (Type) tm.getTypeCreator().createType(smallEnum.class);
        
        assertTrue( type instanceof EnumType );
    }
    
    public void testTypeAttributeOnEnum() throws Exception
    {
        Type type = (Type) tm.getTypeCreator().createType(TestEnum.class);
        
        assertEquals("urn:xfire:foo", type.getSchemaType().getNamespaceURI());
        
        assertTrue( type instanceof EnumType );
    }

    public void testWSDL() throws Exception
    {
        EnumType type = new EnumType();
        type.setTypeClass(smallEnum.class);
        type.setSchemaType(new QName("urn:test", "test"));

        Element root = new Element("root");
        org.jdom.Document wsdl = new org.jdom.Document(root);
        type.writeSchema(root);

        Document doc = new DOMOutputter().output(wsdl);
        addNamespace("xsd", XmlConstants.XSD);
        assertValid("//xsd:simpleType[@name='test']/xsd:restriction[@base='xsd:string']", doc);
        assertValid("//xsd:restriction[@base='xsd:string']/xsd:enumeration[@value='VALUE1']", doc);
        assertValid("//xsd:restriction[@base='xsd:string']/xsd:enumeration[@value='VALUE2']", doc);
    }
    
    public void testCurrencyService() throws Exception
    {
        createService(CurrencyService.class, null);
        
        Document wsdl = getWSDLDocument("CurrencyService");

        assertValid("//xsd:element[@name='inputCurrency'][@nillable='true']", wsdl);
        assertValid("//xsd:simpleType[@name='Currency']/xsd:restriction[@base='xsd:string']", wsdl);
        assertValid("//xsd:restriction[@base='xsd:string']/xsd:enumeration[@value='USD']", wsdl);
        assertValid("//xsd:restriction[@base='xsd:string']/xsd:enumeration[@value='EURO']", wsdl);
        assertValid("//xsd:restriction[@base='xsd:string']/xsd:enumeration[@value='POUNDS']", wsdl);
    }
    
    
    public void testNillable() throws Exception
    {
        Type type = tm.getTypeCreator().createType(EnumBean.class);

        tm.register(type);
        
        Element root = new Element("root");
        JDOMWriter writer = new JDOMWriter(root);
        
        type.writeObject(new EnumBean(), writer, new Context());

        JDOMReader reader = new JDOMReader(root);
        Object value = type.readObject(reader, new Context());
        
        assertTrue(value instanceof EnumBean);
        EnumBean bean = (EnumBean) value;
        assertNull(bean.getCurrency());
    }
    
    public static class EnumBean {
        private Currency currency;

        public Currency getCurrency()
        {
            return currency;
        }

        public void setCurrency(Currency currency)
        {
            this.currency = currency;
        }
        
        public Currency[] getSomeCurrencies()
        {
            return new Currency[] { Currency.EURO, null };
        }
        
        public void setSomeCurrencies(Currency[] currencies)
        {
            
        }
    }
}
