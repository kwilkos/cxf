package org.apache.cxf.aegis.type.java5;

import org.w3c.dom.Document;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.util.XmlConstants;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

public class DualOutServiceTest
    extends AbstractAegisTest
{
    public void testWSDL() throws Exception
    {
        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
        sf.setServiceClass(DualOutService.class);
        sf.setAddress("DualOutService");
        sf.setBus(getBus());
        setupAegis(sf);
        sf.create();
        
        Document wsdl = getWSDLDocument("DualOutService");
        assertNotNull(wsdl);
        
        addNamespace("xsd", XmlConstants.XSD);
        assertValid("//xsd:element[@name='getValuesResponse']//xsd:element[@name='return'][@type='xsd:string']", wsdl);
        assertValid("//xsd:element[@name='getValuesResponse']//xsd:element[@name='out2'][@type='xsd:string']", wsdl);
    }
}
