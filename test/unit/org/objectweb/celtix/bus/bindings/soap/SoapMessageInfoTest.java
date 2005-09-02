package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Method;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.ObjectMessageContextImpl;
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
        
        ObjectMessageContextImpl msgContext = new ObjectMessageContextImpl();
        Method[] declMethods = Greeter.class.getDeclaredMethods();
        for (Method method : declMethods) {
            if (method.getName().equals("greetMe")) {
                msgContext.put(ObjectMessageContextImpl.METHOD_INVOKED, method);
            }
        }
        
        msgInfo = new SOAPMessageInfo(msgContext);
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
                new QName("responseType", "http://objectweb.org/hello_world_soap_http"), 
                returnType);        
    }

    public void testGetWebParam() throws Exception {
        WebParam inParam = msgInfo.getWebParam(0);
        assertEquals(
                new QName("requestType", "http://objectweb.org/hello_world_soap_http"), 
                new QName(inParam.name(), inParam.targetNamespace()));
        assertEquals(WebParam.Mode.IN, inParam.mode());
        assertFalse(inParam.header());        
    }
    
}
