package org.objectweb.celtix.systest.securebasic;


import javax.jws.WebService;

import org.objectweb.hello_world_soap_http_secure.Greeter;
import org.objectweb.hello_world_soap_http_secure.types.Result;

@WebService(serviceName = "SecureSOAPService", 
            portName = "SoapPort",
            endpointInterface = "org.objectweb.hello_world_soap_http_secure.Greeter",
            targetNamespace = "http://objectweb.org/hello_world_soap_http_secure")
public class GreeterImpl implements Greeter {

    public Result greetMeTwoTier(String me, int testIndex) {
        Result ret = new Result();
        if (Matrix.TWO_TIER_TESTS[testIndex].targetData.targetExpectSuccess) {
            ret.setDidPass(Matrix.SUCCEED);
            ret.setReturnString("Hello " + me);
            ret.setFailureReason("");
        } else {
            ret.setDidPass(Matrix.FAIL);
            ret.setReturnString("");
            ret.setFailureReason("Shouldn't have been able to contact GreeterImpl.greetme, testIndex = " 
                                 + testIndex);  
        }
        return ret;
    } 

    public Result greetMeThreeTier(String me, int testIndex) {
        Result ret = new Result();
        if (Matrix.THREE_TIER_TESTS[testIndex].targetData.targetExpectSuccess) {
            ret.setDidPass(Matrix.SUCCEED);
            ret.setReturnString("Hello " + me);
            ret.setFailureReason("");
        } else {
            ret.setDidPass(Matrix.FAIL);
            ret.setReturnString("");
            ret.setFailureReason("Shouldn't have been able to contact GreeterImpl.greetme, testIndex = " 
                                 + testIndex);  
        }
        return ret;
    } 

    
}
