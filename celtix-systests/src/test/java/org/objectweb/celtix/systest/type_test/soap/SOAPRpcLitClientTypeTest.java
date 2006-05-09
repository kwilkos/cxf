package org.objectweb.celtix.systest.type_test.soap;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.Holder;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.type_test.AbstractTypeTestClient5;
import org.objectweb.type_test.types2.StructWithAnyArrayLax;
import org.objectweb.type_test.types2.StructWithAnyStrict;

public class SOAPRpcLitClientTypeTest extends AbstractTypeTestClient5 {
    static final String WSDL_PATH = "/wsdl/type_test/type_test_rpclit_soap.wsdl";
    static final QName SERVICE_NAME =
        new QName("http://objectweb.org/type_test/rpc", "SOAPService");
    static final QName PORT_NAME = 
        new QName("http://objectweb.org/type_test/rpc", "SOAPPort");

    public SOAPRpcLitClientTypeTest(String name) {
        super(name, SERVICE_NAME, PORT_NAME, WSDL_PATH);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SOAPRpcLitClientTypeTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                boolean ok = launchServer(SOAPRpcLitServerImpl.class); 
                assertTrue("failed to launch server", ok);
            }
        };
    }  

    public void testStructWithAnyStrict() throws Exception {
        SOAPFactory factory = SOAPFactory.newInstance();
        SOAPElement elem = factory.createElement("StringElementQualified",
            "tns", "http://objectweb.org/type_test/types1");
        elem.addNamespaceDeclaration("tns", "http://objectweb.org/type_test/types1");
        elem.addTextNode("This is the text of the node");

        StructWithAnyStrict x = new StructWithAnyStrict();
        x.setName("Name x");
        x.setAddress("Some Address x");
        x.setAny(elem);
        
        elem = factory.createElement("StringElementQualified",
            "tns", "http://objectweb.org/type_test/types1");
        elem.addNamespaceDeclaration("tns", "http://objectweb.org/type_test/types1");
        elem.addTextNode("This is the text of the second node");
                                
        StructWithAnyStrict yOrig = new StructWithAnyStrict();
        yOrig.setName("Name y");
        yOrig.setAddress("Some Address y");
        yOrig.setAny(elem);

        Holder<StructWithAnyStrict> y = new Holder<StructWithAnyStrict>(yOrig);
        Holder<StructWithAnyStrict> z = new Holder<StructWithAnyStrict>();
        StructWithAnyStrict ret = rpcClient.testStructWithAnyStrict(x, y, z);
        if (!perfTestOnly) {
            assertEqualsStructWithAnyStrict(x, y.value);
            assertEqualsStructWithAnyStrict(yOrig, z.value);
            assertEqualsStructWithAnyStrict(x, ret);
        }
    }

    public void testStructWithAnyStrictComplex() throws Exception {
        SOAPFactory factory = SOAPFactory.newInstance();
        SOAPElement elem = factory.createElement("AnonTypeElementQualified",
            "tns", "http://objectweb.org/type_test/types1");
        elem.addNamespaceDeclaration("tns", "http://objectweb.org/type_test/types1");
        SOAPElement floatElem = factory.createElement("varFloat", "tns",
            "http://objectweb.org/type_test/types1");
        floatElem.addTextNode("12.5");
        elem.addChildElement(floatElem);
        SOAPElement intElem = factory.createElement("varInt", "tns",
            "http://objectweb.org/type_test/types1");
        intElem.addTextNode("34");
        elem.addChildElement(intElem);
        SOAPElement stringElem = factory.createElement("varString", "tns",
            "http://objectweb.org/type_test/types1");
        stringElem.addTextNode("test string within any");
        elem.addChildElement(stringElem);
                                 
        StructWithAnyStrict x = new StructWithAnyStrict();
        x.setName("Name x");
        x.setAddress("Some Address x");
        x.setAny(elem);
        
        elem = factory.createElement("AnonTypeElementQualified", "tns",
            "http://objectweb.org/type_test/types1");
        elem.addNamespaceDeclaration("tns", "http://objectweb.org/type_test/types1");
        floatElem = factory.createElement("varFloat", "tns",
            "http://objectweb.org/type_test/types1");
        floatElem.addTextNode("12.76");
        elem.addChildElement(floatElem);
        intElem = factory.createElement("varInt", "tns",
            "http://objectweb.org/type_test/types1");
        intElem.addTextNode("56");
        elem.addChildElement(intElem);
        stringElem = factory.createElement("varString", "tns",
            "http://objectweb.org/type_test/types1");
        stringElem.addTextNode("test string");
        elem.addChildElement(stringElem);
        
        StructWithAnyStrict yOrig = new StructWithAnyStrict();
        yOrig.setName("Name y");
        yOrig.setAddress("Some Address y");
        yOrig.setAny(elem);

        Holder<StructWithAnyStrict> y = new Holder<StructWithAnyStrict>(yOrig);
        Holder<StructWithAnyStrict> z = new Holder<StructWithAnyStrict>();
        StructWithAnyStrict ret = rpcClient.testStructWithAnyStrict(x, y, z);
        if (!perfTestOnly) {
            assertEqualsStructWithAnyStrict(x, y.value);
            assertEqualsStructWithAnyStrict(yOrig, z.value);
            assertEqualsStructWithAnyStrict(x, ret);
        }
    }

    public void testStructWithAnyArrayLax() throws Exception {
        SOAPFactory factory = SOAPFactory.newInstance();
        SOAPElement elem = factory.createElement("StringElementQualified", 
            "tns", "http://objectweb.org/type_test/types1");
        elem.addNamespaceDeclaration("tns", "http://objectweb.org/type_test/types1");
        elem.addTextNode("This is the text of the node");

        StructWithAnyArrayLax x = new StructWithAnyArrayLax();
        x.setName("Name x");
        x.setAddress("Some Address x");
        x.getAny().add(elem);

        elem = factory.createElement("StringElementQualified", "tns",
            "http://objectweb.org/type_test/types1");
        elem.addNamespaceDeclaration("tns", "http://objectweb.org/type_test/types1");
        elem.addTextNode("This is the text of the node for the second struct");

        StructWithAnyArrayLax yOrig = new StructWithAnyArrayLax();
        yOrig.setName("Name y");
        yOrig.setAddress("Some Other Address y");
        yOrig.getAny().add(elem);

        Holder<StructWithAnyArrayLax> y = new Holder<StructWithAnyArrayLax>(yOrig);
        Holder<StructWithAnyArrayLax> z = new Holder<StructWithAnyArrayLax>();
        StructWithAnyArrayLax ret = rpcClient.testStructWithAnyArrayLax(x, y, z);
        if (!perfTestOnly) {
            assertEqualsStructWithAnyArrayLax(x, y.value);
            assertEqualsStructWithAnyArrayLax(yOrig, z.value);
            assertEqualsStructWithAnyArrayLax(x, ret);
        }
    }
    
    public void testStructWithAnyArrayLaxComplex() throws Exception {
        SOAPFactory factory = SOAPFactory.newInstance();
        SOAPElement elem = factory.createElement("AnonTypeElementQualified", "tns",
            "http://objectweb.org/type_test/types1");
        elem.addNamespaceDeclaration("tns", "http://objectweb.org/type_test/types1");
        SOAPElement floatElem = factory.createElement("varFloat", "tns",
            "http://objectweb.org/type_test/types1");
        floatElem.addTextNode("12.76");
        elem.addChildElement(floatElem);
        SOAPElement intElem = factory.createElement("varInt", "tns",
            "http://objectweb.org/type_test/types1");
        intElem.addTextNode("56");
        elem.addChildElement(intElem);
        SOAPElement stringElem = factory.createElement("varString", "tns",
            "http://objectweb.org/type_test/types1");
        stringElem.addTextNode("test string");
        elem.addChildElement(stringElem);

        StructWithAnyArrayLax x = new StructWithAnyArrayLax();
        x.setName("Name x");
        x.setAddress("Some Address x");
        x.getAny().add(elem);
        StructWithAnyArrayLax yOrig = new StructWithAnyArrayLax();
        yOrig.setName("Name y");
        yOrig.setAddress("Some Other Address y");
        yOrig.getAny().add(elem);

        Holder<StructWithAnyArrayLax> y = new Holder<StructWithAnyArrayLax>(yOrig);
        Holder<StructWithAnyArrayLax> z = new Holder<StructWithAnyArrayLax>();
        StructWithAnyArrayLax ret = rpcClient.testStructWithAnyArrayLax(x, y, z);
        if (!perfTestOnly) {
            assertEqualsStructWithAnyArrayLax(x, y.value);
            assertEqualsStructWithAnyArrayLax(yOrig, z.value);
            assertEqualsStructWithAnyArrayLax(x, ret);
        }
    }

    public void assertEqualsStructWithAnyStrict(StructWithAnyStrict a,
            StructWithAnyStrict b) throws Exception {
        assertEquals("StructWithAnyStrict names don't match", a.getName(), b.getName());
        assertEquals("StructWithAnyStrict addresses don't match", a.getAddress(), b.getAddress());
        if (a.getAny() instanceof SOAPElement && b.getAny() instanceof SOAPElement) {
            assertEquals((SOAPElement)a.getAny(), (SOAPElement)b.getAny());
        }
    }

    public void assertEqualsStructWithAnyArrayLax(StructWithAnyArrayLax a,
            StructWithAnyArrayLax b) throws Exception {
        assertEquals("StructWithAnyArrayLax names don't match", a.getName(), b.getName());
        assertEquals("StructWithAnyArrayLax addresses don't match", a.getAddress(), b.getAddress());

        List<Object> ae = a.getAny();
        List<Object> be = b.getAny();
        assertEquals("StructWithAnyArrayLax soap element lengths don't match", ae.size(), be.size());
        for (int i = 0; i < ae.size(); i++) {
            if (ae.get(i) instanceof SOAPElement && be.get(i) instanceof SOAPElement) {
                assertEquals(ae.get(i), be.get(i));
            }
        }
    }
}
