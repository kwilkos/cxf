package org.objectweb.celtix.systest.soapheader;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.header_test.SOAPHeaderService;
import org.objectweb.header_test.TestHeader;
import org.objectweb.header_test.types.TestHeader1;
import org.objectweb.header_test.types.TestHeader1Response;
import org.objectweb.header_test.types.TestHeader2;
import org.objectweb.header_test.types.TestHeader2Response;
import org.objectweb.header_test.types.TestHeader3;
import org.objectweb.header_test.types.TestHeader3Response;
import org.objectweb.header_test.types.TestHeader5;

public class ClientServerTest extends ClientServerTestBase {

    private final QName serviceName = new QName("http://objectweb.org/header_test",
                                                "SOAPHeaderService");    
    private final QName portName = new QName("http://objectweb.org/header_test",
                                             "SOAPHeaderPort");

    private TestHeader proxy;
    
    public void onetimeSetUp() { 
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }

    public void setUp() throws BusException {
        try { 
            super.setUp();
            
            URL wsdl = getClass().getResource("/wsdl/soapheader_test.wsdl");
            assertNotNull(wsdl);
            
            SOAPHeaderService service = new SOAPHeaderService(wsdl, serviceName);
            assertNotNull(service);
            proxy = (TestHeader) service.getPort(portName, TestHeader.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }
    
    public void testInHeader() throws Exception {
        try {
            TestHeader1 val = new TestHeader1();
            for (int idx = 0; idx < 2; idx++) {
                TestHeader1Response returnVal = proxy.testHeader1(val, val);
                assertNotNull(returnVal);
                assertEquals(TestHeader1.class.getSimpleName(), returnVal.getResponseType());
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    } 

    public void testOutHeader() throws Exception {
        try {
            TestHeader2 in = new TestHeader2();
            String val = new String(TestHeader2Response.class.getSimpleName());
            Holder<TestHeader2Response> out = new Holder<TestHeader2Response>();
            Holder<TestHeader2Response> outHeader = new Holder<TestHeader2Response>();
            for (int idx = 0; idx < 2; idx++) {
                val += idx;                
                in.setRequestType(val);
                proxy.testHeader2(in, out, outHeader);
                
                assertEquals(val, out.value.getResponseType());
                assertEquals(val, outHeader.value.getResponseType());
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    } 

    public void testInOutHeader() throws Exception {
        
        try {
            TestHeader3 in = new TestHeader3();
            String val = new String(TestHeader3.class.getSimpleName());
            Holder<TestHeader3> inoutHeader = new Holder<TestHeader3>();
            for (int idx = 0; idx < 2; idx++) {
                val += idx;                
                in.setRequestType(val);
                inoutHeader.value = new TestHeader3();
                TestHeader3Response returnVal = proxy.testHeader3(in, inoutHeader);
                //inoutHeader copied to return
                //in copied to inoutHeader
                assertNotNull(returnVal);
                assertNull(returnVal.getResponseType());
                assertEquals(val, inoutHeader.value.getRequestType());
                
                in.setRequestType(null);
                inoutHeader.value.setRequestType(val);
                returnVal = proxy.testHeader3(in, inoutHeader);
                assertNotNull(returnVal);
                assertEquals(val, returnVal.getResponseType());
                assertNull(inoutHeader.value.getRequestType());
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    } 

    public void testReturnHeader() throws Exception {
        
        try {
            TestHeader5 in = new TestHeader5();
            String val = new String(TestHeader5.class.getSimpleName());
            for (int idx = 0; idx < 2; idx++) {
                val += idx;                
                in.setRequestType(val);
                TestHeader5 returnVal = proxy.testHeader5(in);

                //in copied to return                
                assertNotNull(returnVal);
                assertEquals(val, returnVal.getRequestType());
            }
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
    } 
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ClientServerTest.class);
    }
}
