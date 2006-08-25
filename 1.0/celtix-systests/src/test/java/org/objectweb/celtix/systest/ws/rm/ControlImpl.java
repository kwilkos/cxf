package org.objectweb.celtix.systest.ws.rm;

import java.net.URL;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Response;

import org.objectweb.celtix.greeter_control.Control;
import org.objectweb.celtix.greeter_control.types.StartGreeterResponse;
import org.objectweb.celtix.greeter_control.types.StopGreeterResponse;


@WebService(serviceName = "ControlService", 
            portName = "ControlPort", 
            endpointInterface = "org.objectweb.celtix.greeter_control.Control", 
            targetNamespace = "http://celtix.objectweb.org/greeter_control")
public class ControlImpl implements Control {
    
    private static final Logger LOG = Logger.getLogger(ControlImpl.class.getName());
    private static QName serviceName = new QName("http://celtix.objectweb.org/greeter_control", 
                                                 "GreeterService");
    private Endpoint endpoint;
    
    public boolean startGreeter(String configuration) {
        
        if (!(null == configuration || "".equals(configuration))) {
            setConfigFileProperty(configuration);
        }
        
        TestConfigurator tc = new TestConfigurator();        
        tc.configureServer(serviceName);
        
        GreeterImpl implementor = new GreeterImpl();
        String address = "http://localhost:9000/SoapContext/GreeterPort";
        endpoint = Endpoint.publish(address, implementor);
        
        return true;        
    }

    public boolean stopGreeter() {  
        
        if (null != endpoint) {
            LOG.info("Stopping Greeter endpoint");
            endpoint.stop();
        } else {
            LOG.info("No endpoint active.");
        }
        endpoint = null;
        return true;
    }
    
    public static void setConfigFileProperty(String cfgName) {
        URL url = ControlImpl.class.getResource(cfgName + ".xml"); 
        if (null == url) {
            LOG.severe("cannot find test resource " +  cfgName + ".xml");
            return;
        }
        String configFileName = url.toString();             
        System.setProperty("celtix.config.file", configFileName);
    }

    public Future<?> startGreeterAsync(String requestType, AsyncHandler<StartGreeterResponse> asyncHandler) {
        // never called
        return null;
    }

    public Response<StartGreeterResponse> startGreeterAsync(String requestType) {
        // never called
        return null;
    }

    public Response<StopGreeterResponse> stopGreeterAsync() {
        // never called
        return null;
    }

    public Future<?> stopGreeterAsync(AsyncHandler<StopGreeterResponse> asyncHandler) {
        // never called
        return null;
    }
    
    
    
}
