package org.objectweb.celtix.systest.schema_validation;

import java.net.URL;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.systest.common.TestServerBase;

public class ValidationServer extends TestServerBase {
    
    private String oldConfig;
    
    public ValidationServer() {
        oldConfig = System.getProperty("celtix.config.file");
        URL url = getClass().getResource("celtix-config.xml"); 
        if (url != null) {
            System.setProperty("celtix.config.file", url.toString());
        }
    }
    
    protected void run()  {
        Object implementor = new SchemaValidationImpl();
        String address = "http://localhost:9900/SoapContext/SoapPort";
        Endpoint.publish(address, implementor);
    }
    
    public boolean stopInProcess() throws Exception {
        System.setProperty("celtix.config.file", oldConfig);
        return super.stopInProcess();
    }

    public static void main(String[] args) {
        try { 
            ValidationServer s = new ValidationServer(); 
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally { 
            System.out.println("done!");
        }
    }
}
