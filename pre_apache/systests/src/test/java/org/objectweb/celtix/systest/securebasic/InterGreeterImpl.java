package org.objectweb.celtix.systest.securebasic;


import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.objectweb.hello_world_soap_http_secure.Greeter;

import org.objectweb.hello_world_soap_http_secure.SecureSOAPService;
import org.objectweb.hello_world_soap_http_secure.types.Result;

@WebService(serviceName = "SecureSOAPService", 
            portName = "SoapPort",
            endpointInterface = "org.objectweb.hello_world_soap_http_secure.Greeter",
            targetNamespace = "http://objectweb.org/hello_world_soap_http_secure")
public class InterGreeterImpl implements Greeter {

    public Result greetMeTwoTier(String me, int testIndex) {
        
        Result ret = new Result();
        ret.setDidPass(Matrix.FAIL);
        ret.setReturnString("");
        ret.setFailureReason("Should only be called as part of three tier test");
        return ret;
    }
    
    public Result greetMeThreeTier(String me, int testIndex) {
        String response1 = new String("Hello Milestone-");

        URL wsdl = getClass().getResource("/wsdl/" + Matrix.THREE_TIER_TESTS[testIndex].interData.interWsdl);
        QName secureServiceName = new QName("http://objectweb.org/hello_world_soap_http_secure",
                                            Matrix.THREE_TIER_TESTS[testIndex].interData.interServiceName); 
        SecureSOAPService service = new SecureSOAPService(wsdl, secureServiceName);

        
        QName portName = new QName("http://objectweb.org/hello_world_soap_http_secure",
                                   Matrix.THREE_TIER_TESTS[testIndex].interData.interPortName);
        Greeter greeter = service.getPort(portName, Greeter.class);
        try {       
            Result res = greeter.greetMeThreeTier("Milestone-" + testIndex, testIndex);
            if (!Matrix.THREE_TIER_TESTS[testIndex].interData.interExpectSuccess) {
                return Matrix.fail("Expected to fail but didn't");
            }
            String exResponse = response1 + testIndex;
            Result failResult = Matrix.dealWithResponse(exResponse, res);
            if (failResult != null) {
                return failResult;
            }
               

        } catch (UndeclaredThrowableException ex) {
            if (Matrix.THREE_TIER_TESTS[testIndex].interData.interExpectSuccess) {
                ex.printStackTrace();
                return Matrix.fail("Caught unexpected ex = " + ex.getMessage());
            } 
            
            
        }
        Result ret = new Result();
        ret.setDidPass(Matrix.SUCCEED);
        ret.setReturnString("Hello " + me);
        ret.setFailureReason("");
        return ret;
    }


 
    
}
