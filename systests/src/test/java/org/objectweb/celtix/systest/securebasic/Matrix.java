package org.objectweb.celtix.systest.securebasic;


import org.objectweb.hello_world_soap_http_secure.types.Result;


public class Matrix {
 
    protected static final boolean SUCCEED = true;
    protected static final boolean FAIL = false;
 
    
    protected static final ThreeTierTestItem[] THREE_TIER_TESTS = {
        //0 Inter and Target want and require pier authenticaion, 
        //all provided so all succeed                                                            
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSet",
                                            "InterSecureSOAPService",
                                            SUCCEED,
                                            null),
                              new InterData("hello_world_secure.wsdl",
                                            "SoapPortAllDataSet",
                                            "TargetSecureSOAPService",
                                            SUCCEED,
                                            null),
                              new TargetData(SUCCEED,
                                             null)),
        //1 Inter and Target want and require pier authenticaion, 
        //all provided by client but inter doesn't have anything set
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                             "SoapPortAllDataSet",
                                             "InterSecureSOAPService",
                                             SUCCEED,
                                             null),
                              new InterData("hello_world_secure.wsdl",
                                            "SoapPortNoDataSet",
                                            "TargetSecureSOAPService",
                                            FAIL,
                                            null),                                              
                              new TargetData(FAIL,
                                             null)), 
                               
        //2 Wrong Keystore and server wants and needs client auth so should fail
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSet",
                                            "InterSecureSOAPService",
                                            SUCCEED,
                                            null),
                              new InterData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSetWrongKeystore",
                                            "TargetSecureSOAPService",
                                            FAIL,
                                            null),
                               new TargetData(FAIL,
                                              null)),
        //3 Wrong Keystore server wants but doesn't need client auth so 
        // should pass                             
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                             "SoapPortAllDataSet",
                                             "InterSecureSOAPService",
                                             SUCCEED,
                                             null),
                               new InterData("hello_world_secure.wsdl", 
                                             "SoapPortAllDataSetWrongKeystore",
                                             "TargetSecureDontRequireClientAuthSOAPService",
                                             SUCCEED,
                                             null),
                               new TargetData(SUCCEED,
                                              null)),                             
                               
        //4 Wrong KeystoreType and server wants and needs client auth so should fail
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                             "SoapPortAllDataSet",
                                             "InterSecureSOAPService",
                                             SUCCEED,
                                             null),
                              new InterData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSetWrongKeystoreType",
                                            "TargetSecureSOAPService",
                                            FAIL,
                                            null),
                             new TargetData(FAIL,
                                            null)),                             
        
        //5 Wrong Keystore Password and server wants and needs client auth so should fail
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                             "SoapPortAllDataSet",
                                             "InterSecureSOAPService",
                                             SUCCEED,
                                             null),
                              new InterData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSetWrongKeystorePassword",
                                            "TargetSecureSOAPService",
                                             FAIL,
                                             null),
                              new TargetData(FAIL,
                                             null)),                             
                               
        //6 Wrong Keystore Password and server wants but does not need client auth so should SUCCEED
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                             "SoapPortAllDataSet",
                                             "InterSecureSOAPService",
                                             SUCCEED,
                                             null),
                              new InterData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSetWrongKeystorePassword",
                                            "TargetSecureDontRequireClientAuthSOAPService",
                                            SUCCEED,
                                            null),
                              new TargetData(SUCCEED,
                                             null)),      
                              
        //7 Wrong Client Password and server wants and needs client auth which 
        //would at first impl it would fail but due to limitation in JSSE the client password
        //and keystore need to be the same, so all that haapens is that the correct keystore 
        //password is used and a warning is logged to say that the client password is not being
        //used and the invocation succeeds
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                             "SoapPortAllDataSet",
                                             "InterSecureSOAPService",
                                             SUCCEED,
                                             null),
                              new InterData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSetWrongClientPassword",
                                            "TargetSecureSOAPService",
                                            SUCCEED,
                                            null),
                              new TargetData(SUCCEED,
                                             null)),  
                              
        //8 Wrong Truststore and client auth wants and needs server authentication so the invocation fails
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                             "SoapPortAllDataSet",
                                             "InterSecureSOAPService",
                                             SUCCEED,
                                             null),
                              new InterData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSetWrongTrustStore",
                                            "TargetSecureSOAPService",
                                            FAIL,
                                            null),
                              new TargetData(FAIL,
                                             null)),
        //9 Wrong Truststore Type and client auth wants and needs server 
        //authentication so the invocation fails
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                             "SoapPortAllDataSet",
                                             "InterSecureSOAPService",
                                             SUCCEED,
                                             null),
                              new InterData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSetWrongTrustStoreType",
                                            "TargetSecureSOAPService",
                                            FAIL,
                                            null),
                              new TargetData(FAIL,
                                             null)),
                              
        //10 Invalid Secure socket protocol used so will fail
        new ThreeTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                             "SoapPortAllDataSet",
                                             "InterSecureSOAPService",
                                             SUCCEED,
                                             null),
                              new InterData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSetWrongSecureSocketProtocol",
                                            "TargetSecureSOAPService",
                                            FAIL,
                                            null),
                              new TargetData(FAIL,
                                             null))                              
                                                           
    };
    
    protected static final TwoTierTestItem[] TWO_TIER_TESTS = {
        //0 Server wants and need client auth so must 
        //provide all data to succeed                                                           
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSet",
                                           "SecureSOAPService",
                                           SUCCEED,
                                           null),
                            new TargetData(SUCCEED,
                                           null)) ,
        //1 Server wants and need client auth but client auth data 
        //not provided so it will fail                            
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                            "SoapPortNoDataSet",
                                            "SecureSOAPService",
                                            FAIL,
                                            null),
                             new TargetData(FAIL,
                                            null)), 
                             
        //2 Wrong Keystore and server wants and needs client auth so should fail
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                            "SoapPortAllDataSetWrongKeystore",
                                            "SecureSOAPService",
                                            FAIL,
                                            null),
                             new TargetData(FAIL,
                                            null)),
        //3 Wrong Keystore server wants but doesn't need client auth so 
        // should pass                             
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetWrongKeystore",
                                           "SecureDontRequireClientAuthSOAPService",
                                           SUCCEED,
                                           null),
                             new TargetData(SUCCEED,
                                            null)),                             
                             
        //4 Wrong KeystoreType and server wants and needs client auth so should fail
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetWrongKeystoreType",
                                           "SecureSOAPService",
                                           FAIL,
                                           null),
                             new TargetData(FAIL,
                                            null)),                             

        //5 Wrong Keystore Password and server wants and needs client auth so should fail
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetWrongKeystorePassword",
                                           "SecureSOAPService",
                                           FAIL,
                                           null),
                            new TargetData(FAIL,
                                           null)),                             
                             
        //6 Wrong Keystore Password and server wants but does not need client auth so should SUCCEED
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetWrongKeystorePassword",
                                           "SecureDontRequireClientAuthSOAPService",
                                           SUCCEED,
                                           null),
                            new TargetData(SUCCEED,
                                           null)),      
                            
        //7 Wrong Client Password and server wants and needs client auth which 
        //would at first impl it would fail but due to limitation in JSSE the client password
        //and keystore need to be the same, so all that haapens is that the correct keystore 
        //password is used and a warning is logged to say that the client password is not being
        //used and the invocation succeeds
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetWrongClientPassword",
                                           "SecureSOAPService",
                                           SUCCEED,
                                           null),
                            new TargetData(SUCCEED,
                                           null)),  
                            
        //8 Wrong Truststore and client auth wants and needs server authentication so the invocation fails
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetWrongTrustStore",
                                           "SecureSOAPService",
                                           FAIL,
                                           null),
                            new TargetData(FAIL,
                                           null)),
        //9 Wrong Truststore Type and client auth wants and needs server 
        //authentication so the invocation fails
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetWrongTrustStoreType",
                                           "SecureSOAPService",
                                           FAIL,
                                           null),
                            new TargetData(FAIL,
                                           null)),
                            
        //10 Invalid Secure socket protocol used so will fail
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetWrongSecureSocketProtocol",
                                           "SecureSOAPService",
                                           FAIL,
                                           null),
                            new TargetData(FAIL,
                                           null)),    
        //11 No data set in client configuration file, all client data retrieved from data provider
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortNoDataSetProviderSetsAllData",
                                           "SecureSOAPService",
                                           SUCCEED,
                                           "org.objectweb.celtix.systest.securebasic."
                                               + "SetAllDataSecurityDataProvider"),
                            new TargetData(SUCCEED,
                                           null)),      
        //12 All wrong data set in client configuration file, all good client data 
        //retrieved from data provider, will succeed cause provider has precedence over config
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllWrongDataSetProviderSetsGoodData",
                                           "SecureSOAPService",
                                           SUCCEED,
                                           "org.objectweb.celtix.systest.securebasic."
                                               + "SetAllDataSecurityDataProvider"),
                            new TargetData(SUCCEED,
                                           null)), 
        //13 All good data set in client configuration file, but bad client data 
        //retrieved from data provider
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetProviderSetsBadData",
                                           "SecureSOAPService",
                                           FAIL,
                                           "org.objectweb.celtix.systest.securebasic."
                                               + "SetBadDataSecurityDataProvider"),
                            new TargetData(FAIL,
                                           null)),                            
        //14 All client data set in configuration file, all server data retrieved from data provider
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSet",
                                           "SecureSOAPServiceServerConfiguredByGoodProvider",
                                           SUCCEED,
                                           null),
                            new TargetData(SUCCEED,
                                           "org.objectweb.celtix.systest.securebasic."
                                               + "SetAllDataSecurityDataProvider")),
        //15 All client data set in configuration file, all server data retrieved from configuration but 
        //data provider changes truststore so the invocation will be rejected
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSet",
                                           "SecureSOAPServiceServerConfiguredByBadProvider",
                                           FAIL,
                                           null),
                            new TargetData(FAIL,
                                           "org.objectweb.celtix.systest.securebasic."
                                               + "SetBadDataSecurityDataProvider")),
        //16 All client data set in configuration file, all server data retrieved from configuration 
       // testing pkcs12 cerst specifically
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetP12Certs",
                                           "SecureSOAPServiceP12",
                                           SUCCEED,
                                           null),
                            new TargetData(SUCCEED,
                                           null)),  
                                           
        //17 All client data set in configuration file, all server data retrieved from configuration but 
        // but server has non default ciphersuite so handshake will fail
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSet",
                                           "SecureSOAPServiceDifferentCipherSuites",
                                           FAIL,
                                           null),
                            new TargetData(FAIL,
                                           null)),
        //18 All client data set in configuration file, all server data retrieved from configuration
        //server and client have non default ciphersuite so handshake will pass
        new TwoTierTestItem(new ClientData("hello_world_secure.wsdl", 
                                           "SoapPortAllDataSetDifferentCipherSuites",
                                           "SecureSOAPServiceDifferentCipherSuites",
                                           SUCCEED,
                                           null),
                            new TargetData(SUCCEED,
                                           null))                                            
    };
    
    public Matrix() {
        t();
    }
    
    
    protected static Result fail(String reason) {
        Result ret = new Result();
        ret.setDidPass(Matrix.FAIL);
        ret.setReturnString("");
        ret.setFailureReason(reason);
        return ret;
    }

    protected static Result dealWithResponse(String expectedResult, 
                                             Result res) {
        if (!res.isDidPass()) {
            return fail("The invocation from the server reported a failure = " 
                        + res.getFailureReason());
        }
        if (!res.getReturnString().equals(expectedResult)) {
            return fail("The expected return string and the actual return string didn't match");
        }
        
        return null;
    } 
    
    private void t() {
        //For compilation reasons we have to have a non static method, must check why
    }
    
}


class ClientData {
    String clientWsdl;
    String clientPortName;
    String clientServiceName;
    boolean clientExpectSuccess;
    String securityConfigurer;
    public ClientData(String clientWsdlParam,
                      String clientPortNameParam,
                      String clientServiceNameParam,
                      boolean clientExpectSuccessParam,
                      String securityConfigurerParam) {
        clientWsdl = clientWsdlParam;
        clientPortName = clientPortNameParam;
        clientServiceName = clientServiceNameParam;        
        clientExpectSuccess = clientExpectSuccessParam;
        securityConfigurer = securityConfigurerParam;
        
    }    
}

class InterData {
    String interWsdl;
    String interPortName;
    String interServiceName;
    boolean interExpectSuccess;
    String securityConfigurer;
    public InterData(String interWsdlParam,
                     String interPortNameParam,
                     String interServiceNameParam,
                     boolean interExpectSuccessParam,
                     String securityConfigurerParam) {
        interWsdl = interWsdlParam;
        interPortName = interPortNameParam;
        interServiceName = interServiceNameParam;
        interExpectSuccess = interExpectSuccessParam;
        securityConfigurer = securityConfigurerParam;
        
    }    
}

class TargetData {
    boolean targetExpectSuccess;  
    String securityConfigurer;
    public TargetData(boolean targetExpectSuccessParam,
                      String securityConfigurerParam) {
        targetExpectSuccess = targetExpectSuccessParam;
        securityConfigurer = securityConfigurerParam;
        
    }    
}

class ThreeTierTestItem {

    ClientData clientData;
    InterData interData;
    TargetData targetData;


    
    public ThreeTierTestItem(ClientData clientDataParam,
                             InterData interDataParam,
                             TargetData targetDataParam) {
        clientData = clientDataParam;
        interData = interDataParam;
        targetData = targetDataParam;        

        
    }
}

class TwoTierTestItem {
    ClientData clientData;
    TargetData targetData;
    
    public TwoTierTestItem(ClientData clientDataParam,
                             TargetData targetDataParam) {
        clientData = clientDataParam;
        targetData = targetDataParam;        

        
    }
}

