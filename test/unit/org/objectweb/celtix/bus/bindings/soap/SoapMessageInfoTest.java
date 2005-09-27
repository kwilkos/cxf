package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Method;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.hello_world_soap_http.Greeter;

public class SoapMessageInfoTest extends TestCase {
    private SOAPMessageInfo msgInfo;
    
    public SoapMessageInfoTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SoapMessageInfoTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Method[] declMethods = Greeter.class.getDeclaredMethods();
        for (Method method : declMethods) {
            if (method.getName().equals("greetMe")) {
                msgInfo = new SOAPMessageInfo(method);
                break;
            }
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetSoapStyle() throws Exception {
        SOAPBinding.Style style = msgInfo.getSOAPStyle();
        assertEquals(SOAPBinding.Style.DOCUMENT, style);        
    }

    public void testGetSoapUse() throws Exception {
        SOAPBinding.Use use = msgInfo.getSOAPUse();
        assertEquals(SOAPBinding.Use.LITERAL, use);        
    }

    public void testGetSOAPParameterStyle() throws Exception {
        SOAPBinding.ParameterStyle paramStyle = msgInfo.getSOAPParameterStyle();
        assertEquals(SOAPBinding.ParameterStyle.WRAPPED, paramStyle);        
    }

    public void testGetWebResult() throws Exception {
        QName returnType = msgInfo.getWebResult();
        assertEquals(
                new QName("http://objectweb.org/hello_world_soap_http/types", "responseType"), 
                returnType);        
    }

    public void testGetWebParam() throws Exception {
        WebParam inParam = msgInfo.getWebParam(0);
        assertEquals(
                new QName("http://objectweb.org/hello_world_soap_http/types", "requestType"), 
                new QName(inParam.targetNamespace(), inParam.name()));
        assertEquals(WebParam.Mode.IN, inParam.mode());
        assertFalse(inParam.header());        
    }

    public void testGetRequestWrapperQName() throws Exception {
        QName reqWrapper = msgInfo.getRequestWrapperQName();
        assertNotNull(reqWrapper);
        assertEquals(
                new QName("http://objectweb.org/hello_world_soap_http/types", "greetMe"), 
                reqWrapper);
    }
    
    public void testGetResponseWrapperQName() throws Exception {
        QName respWrapper = msgInfo.getResponseWrapperQName();
        assertNotNull(respWrapper);
        assertEquals(
                new QName("http://objectweb.org/hello_world_soap_http/types", "greetMeResponse"), 
                respWrapper);
    }
    
    public void testGetResponseWrapperType() throws Exception {
        String respWrapperType = msgInfo.getResponseWrapperType();
        assertNotNull(respWrapperType);
        assertEquals(
                "org.objectweb.hello_world_soap_http.types.GreetMeResponse", 
                respWrapperType);
    }    
    
    public void testDefaults() throws Exception {
        SOAPMessageInfo info = null;
        
        Method[] declMethods = String.class.getDeclaredMethods();
        for (Method method : declMethods) {
            if (method.getName().equals("length")) {
                info = new SOAPMessageInfo(method);
                break;
            }
        }
        
        assertNotNull(info);
        assertEquals(SOAPBinding.Style.DOCUMENT, info.getSOAPStyle());
        assertEquals(SOAPBinding.Use.LITERAL, info.getSOAPUse());
        assertEquals(SOAPBinding.ParameterStyle.WRAPPED, info.getSOAPParameterStyle());
        assertEquals("", info.getOperationName());        
        assertEquals("", info.getSOAPAction());
        assertEquals(SOAPConstants.EMPTY_QNAME, info.getWebResult());
        assertNull(info.getWebParam(1));
        assertEquals(SOAPConstants.EMPTY_QNAME, info.getRequestWrapperQName());
        assertEquals(SOAPConstants.EMPTY_QNAME, info.getResponseWrapperQName());
        assertEquals("", info.getRequestWrapperType());
        assertEquals("", info.getResponseWrapperType());
    }
}
