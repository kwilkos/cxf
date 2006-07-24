package demo.servlet;

import javax.xml.ws.WebFault;



@WebFault(name = "faultDetail", targetNamespace = "http://objectweb.org/hello_world_soap_http/types")

public class PingMeFault extends Exception {
    
    private org.objectweb.hello_world_soap_http.types.FaultDetail faultDetail;

    public PingMeFault(String message) {
        super(message);
    }

    public PingMeFault(String message, org.objectweb.hello_world_soap_http.types.FaultDetail faultDetail) {
        super(message);
        this.faultDetail = faultDetail;
    }

    public PingMeFault(String message, 
        org.objectweb.hello_world_soap_http.types.FaultDetail faultDetail,
        Throwable cause) {
        super(message, cause);
        this.faultDetail = faultDetail;
    }

    public org.objectweb.hello_world_soap_http.types.FaultDetail getFaultInfo() {
        return this.faultDetail;
    }
}
