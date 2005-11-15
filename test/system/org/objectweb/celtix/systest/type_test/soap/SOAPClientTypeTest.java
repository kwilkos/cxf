package org.objectweb.celtix.systest.type_test.soap;

import javax.xml.namespace.QName;
import org.objectweb.celtix.systest.type_test.AbstractTypeTestClient;

public class SOAPClientTypeTest extends AbstractTypeTestClient {
    static final String WSDL_PATH = "/wsdl/type_test/type_test_soap.wsdl";
    static final QName SERVICE_NAME = new QName("http://objectweb.org/type_test", "SOAPService");
    static final QName PORT_NAME = new QName("http://objectweb.org/type_test", "SOAPPort");
    static String[] args = new String[] {};

    public SOAPClientTypeTest(String name) {
        super(name, SERVICE_NAME, PORT_NAME, WSDL_PATH);
    }

    public void onetimeSetUp()  { 
        try { 
            initBus(); 
            boolean ok = launchServer(SOAPServerImpl.class); 
            assertTrue("failed to launch server", ok);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    } 


/*
    public void testStructWithAnyStrict() throws Exception {
        failed = true;
        
        StructWithAnyStrict x = new StructWithAnyStrict();
        x.setName("Name x");
        x.setAddress("Some Address x");

        SOAPFactory factory = SOAPFactory.newInstance();
        SOAPElement elem = factory.createElement("StringElementQualified",
            "tns", "http://objectweb.org/type_test");
        elem.addNamespaceDeclaration("tns",
            "http://objectweb.org/type_test");
        elem.addTextNode("This is the text of the node");

        x.set_any(elem);
        
        StructWithAnyStrict yOrig = new StructWithAnyStrict();
        yOrig.setName("Name y");
        yOrig.setAddress("Some Address y");
        
        elem = factory.createElement("StringElementQualified",
            "tns", "http://objectweb.org/type_test");
        elem.addNamespaceDeclaration("tns",
            "http://objectweb.org/type_test");
        elem.addTextNode("This is the text of the second node");
       
        yOrig.set_any(elem);

        StructWithAnyStrictHolder y = new StructWithAnyStrictHolder(yOrig);
        StructWithAnyStrictHolder z = new StructWithAnyStrictHolder();

        StructWithAnyStrict ret = client.testStructWithAnyStrict(x,y,z);
        if (!perfTestOnly) {
            assertEqualsStructWithAnyStrictSimple(x, y.value);
            assertEqualsStructWithAnyStrictSimple(yOrig, z.value);
            assertEqualsStructWithAnyStrictSimple(x, ret);
        }

        x = new StructWithAnyStrict();
        x.setName("Name x");
        x.setAddress("Some Address x");

        elem = factory.createElement("AnonTypeElementQualified",
            "tns", "http://objectweb.org/type_test");
        elem.addNamespaceDeclaration("tns",
            "http://objectweb.org/type_test");
        
        SOAPElement floatElem = factory.createElement("varFloat", "tns",
            "http://objectweb.org/type_test");
        floatElem.addTextNode("12.5");
        elem.addChildElement(floatElem);
        
        SOAPElement intElem = factory.createElement("varInt", "tns",
            "http://objectweb.org/type_test");
        intElem.addTextNode("34");
        elem.addChildElement(intElem);
        
        SOAPElement stringElem = factory.createElement("varString", "tns",
            "http://objectweb.org/type_test");
        stringElem.addTextNode("test string within any");
        elem.addChildElement(stringElem);

        x.set_any(elem);
        
        yOrig = new StructWithAnyStrict();
        yOrig.setName("Name y");
        yOrig.setAddress("Some Address y");
        
        elem = factory.createElement("AnonTypeElementQualified", "tns",
            "http://objectweb.org/type_test");
        elem.addNamespaceDeclaration("tns", "http://objectweb.org/type_test");
        
        floatElem = factory.createElement("varFloat", "tns",
            "http://objectweb.org/type_test");
        floatElem.addTextNode("12.76");
        elem.addChildElement(floatElem);
        
        intElem = factory.createElement("varInt", "tns", 
            "http://objectweb.org/type_test");
        intElem.addTextNode("56");
        elem.addChildElement(intElem);
        
        stringElem = factory.createElement("varString", "tns",
            "http://objectweb.org/type_test");
        stringElem.addTextNode("test string");
        elem.addChildElement(stringElem);

       
        yOrig.set_any(elem);

        y = new StructWithAnyStrictHolder(yOrig);
        z = new StructWithAnyStrictHolder();
        
        ret = client.testStructWithAnyStrict(x,y,z);
        
        if (!perfTestOnly) {
            assertEqualsStructWithAnyStrictComplex(x, y.value);
            assertEqualsStructWithAnyStrictComplex(yOrig, z.value);
            assertEqualsStructWithAnyStrictComplex(x, ret);
        }

        failed = false;
    }

    public void testStructWithAnyArrayLax() throws Exception {
        failed = true;
        
        StructWithAnyArrayLax x = new StructWithAnyArrayLax();
        x.setName("Name x");
        x.setAddress("Some Address x");

        StructWithAnyArrayLax yOrig = new StructWithAnyArrayLax();
        yOrig.setName("Name y");
        yOrig.setAddress("Some Other Address y");

        SOAPFactory factory = SOAPFactory.newInstance();
        SOAPElement elem = factory.createElement("StringElementQualified", 
            "tns", "http://objectweb.org/type_test");
        elem.addNamespaceDeclaration("tns","http://objectweb.org/type_test");
        elem.addTextNode("This is the text of the node");

        x.set_any(new SOAPElement[] {elem});

        elem = factory.createElement("StringElementQualified", "tns",
            "http://objectweb.org/type_test");
        elem.addNamespaceDeclaration("tns","http://objectweb.org/type_test");
        elem.addTextNode("This is the text of the node for the second struct");

        yOrig.set_any(new SOAPElement[] {elem});

        StructWithAnyArrayLaxHolder y = new StructWithAnyArrayLaxHolder(yOrig);
        StructWithAnyArrayLaxHolder z = new StructWithAnyArrayLaxHolder();

        StructWithAnyArrayLax ret = client.testStructWithAnyArrayLax(x,y,z);
        if (!perfTestOnly) {
            assertEqualsStructWithAnyArrayLaxSimple(x, y.value);
            assertEqualsStructWithAnyArrayLaxSimple(yOrig, z.value);
            assertEqualsStructWithAnyArrayLaxSimple(x, ret);
        }

        x = new StructWithAnyArrayLax();
        x.setName("Name x");
        x.setAddress("Some Address x");

        yOrig = new StructWithAnyArrayLax();
        yOrig.setName("Name y");
        yOrig.setAddress("Some Other Address y");

        factory = SOAPFactory.newInstance();
        elem = factory.createElement("AnonTypeElementQualified", "tns",
            "http://objectweb.org/type_test");
        elem.addNamespaceDeclaration("tns","http://objectweb.org/type_test");

        SOAPElement floatElem = factory.createElement("varFloat", "tns",
            "http://objectweb.org/type_test");
        floatElem.addTextNode("12.76");
        elem.addChildElement(floatElem);

        SOAPElement intElem = factory.createElement("varInt", "tns",
            "http://objectweb.org/type_test");       
        intElem.addTextNode("56");
        elem.addChildElement(intElem);

        SOAPElement stringElem = factory.createElement("varString", "tns",
            "http://objectweb.org/type_test");             
        stringElem.addTextNode("test string");
        elem.addChildElement(stringElem);

        x.set_any(new SOAPElement[] {elem});
        
        yOrig.set_any(new SOAPElement[] {elem});

        y = new StructWithAnyArrayLaxHolder(yOrig);
        z = new StructWithAnyArrayLaxHolder();

        ret = client.testStructWithAnyArrayLax(x,y,z);
        if (!perfTestOnly) {
            assertEqualsStructWithAnyArrayLaxComplex(x, y.value);
            assertEqualsStructWithAnyArrayLaxComplex(yOrig, z.value);
            assertEqualsStructWithAnyArrayLaxComplex(x, ret);
        }

        failed = false;
    }

    public void assertEqualsStructWithAnyStrictSimple(StructWithAnyStrict a,
            StructWithAnyStrict b) throws Exception {
        assertEquals("StructWithAnyStrict names don't match",a.getName(),b.getName());
        assertEquals("StructWithAnyStrict addresses don't match",a.getAddress(),b.getAddress());
        SOAPElement ae = a.get_any();
        SOAPElement be = b.get_any();       
        assertEquals("StructWithAnyStrict soapelement names don't match",
                     ae.getElementName(), be.getElementName());
        assertEquals("StructWithAnyStrict soapelement text node don't match",
                     ae.getValue(), be.getValue());
    }

    public void assertEqualsStructWithAnyArrayLaxSimple(StructWithAnyArrayLax a,
            StructWithAnyArrayLax b) throws Exception {
        assertEquals("StructWithAnyLax names don't match",a.getName(),b.getName());
        assertEquals("StructWithAnyLax addresses don't match",a.getAddress(),b.getAddress());

        SOAPElement ae[] = a.get_any();
        SOAPElement be[] = b.get_any();

        assertEquals("StructWithAnyArrayLax soapelement lengths don't match",ae.length,be.length);
        for (int x = 0; x < ae.length; x++) {
            assertEquals("StructWithAnyArrayLax soapelement names don't match",
                         ae[x].getElementName(),be[x].getElementName());
            assertEquals("StructWithAnyArrayLax soapelement text node don't match",
                         ae[x].getValue(),be[x].getValue());
        }
    }

    public void assertEqualsStructWithAnyStrictComplex(StructWithAnyStrict a,
            StructWithAnyStrict b) throws Exception {
        assertEquals("StructWithAnyStrict names don't match",a.getName(),b.getName());
        assertEquals("StructWithAnyStrict addresses don't match",a.getAddress(),b.getAddress());
        SOAPElement ae = a.get_any();
        SOAPElement be = b.get_any();       
        assertEquals("StructWithAnyStrict soapelement names don't match",
            ae.getElementName(), be.getElementName());
        
        // REVISIT bug in the xmlstream node, appends newline characters at the
        // start of the xml, so need to skip the new lines at the start of the
        // soap element.

        Iterator itExp = ae.getChildElements();
        Iterator itGen = be.getChildElements();
        while (itExp.hasNext()) {
            if (!itGen.hasNext()) {
                fail("Incorrect number of child elements inside any");
            }           
            SOAPElement elema = (SOAPElement) itExp.next();         
            Object obj = itGen.next();
            while (!(obj instanceof SOAPElement)) {
                if (!itGen.hasNext()) {
                    fail("Incorrect number of child elements inside any");
                }
                obj = itGen.next();
            }
            SOAPElement elemb = (SOAPElement) obj;

            assertEquals("StructWithAnyStrict soapelement names don't match",
                elema.getElementName(), elemb.getElementName());
            assertEquals("StructWithAnyStrict soapelement text node don't match",
                elema.getValue(), elemb.getValue());
        }
    }

    public void assertEqualsStructWithAnyArrayLaxComplex(
            StructWithAnyArrayLax a, StructWithAnyArrayLax b) throws Exception {
        assertEquals("StructWithAnyLax names don't match",a.getName(),b.getName());
        assertEquals("StructWithAnyLax addresses don't match",a.getAddress(),b.getAddress());

        SOAPElement ae[] = a.get_any();
        SOAPElement be[] = b.get_any();

        assertEquals("StructWithAnyArrayLax soapelement lengths don't match",
            ae.length, be.length);
        for (int x = 0; x < ae.length; x++) {
            assertEquals("StructWithAnyArrayLax soapelement names don't match",
                         ae[x].getElementName(), be[x].getElementName());
            Iterator itExp = ae[x].getChildElements();
            Iterator itGen = be[x].getChildElements();
            while (itExp.hasNext()) {
                if (!itGen.hasNext()) {
                    fail("Incorrect number of child elements inside any");
                }
                SOAPElement elema = (SOAPElement) itExp.next();         
                Object obj = itGen.next();
                while (!(obj instanceof SOAPElement)) {
                    if (!itGen.hasNext()) {
                        fail("Incorrect number of child elements inside any");
                    }
                    obj = itGen.next();
                }
                SOAPElement elemb = (SOAPElement) obj;
                assertEquals("StructWithAnyStrict soapelement names don't match",
                    elema.getElementName(), elemb.getElementName());
                assertEquals("StructWithAnyStrict soapelement text node don't match",
                    elema.getValue(), elemb.getValue());
            }
        }
    }
*/

}
