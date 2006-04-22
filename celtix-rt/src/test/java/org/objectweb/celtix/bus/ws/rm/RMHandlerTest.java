package org.objectweb.celtix.bus.ws.rm;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Future;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Response;
import javax.xml.ws.handler.Handler;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.greeter_control.Greeter;
import org.objectweb.celtix.greeter_control.GreeterService;
import org.objectweb.celtix.greeter_control.PingMeFault;
import org.objectweb.celtix.greeter_control.types.GreetMeResponse;
import org.objectweb.celtix.greeter_control.types.PingMeResponse;
import org.objectweb.celtix.greeter_control.types.SayHiResponse;

public class RMHandlerTest extends TestCase {
    private static final String APP_NAMESPACE = "http://celtix.objectweb.org/greeter_control";
    private static final String CFG_FILE_PROPERTY = "celtix.config.file";
    private static final QName SERVICE_NAME = new QName(APP_NAMESPACE, "GreeterService");
    private static final QName PORT_NAME = new QName(APP_NAMESPACE, "GreeterPort");
    
    private String celtixCfgFile;
    private Bus bus;
    
    public void setUp() throws Exception {
        
        celtixCfgFile = System.getProperty(CFG_FILE_PROPERTY);
        URL url = RMHandlerTest.class.getResource("resources/RMHandlerTest.xml"); 
        assert null != url;
        String configFileName = url.toString();             
        System.setProperty(CFG_FILE_PROPERTY, configFileName);
        bus = Bus.init();
    }
    
    public void tearDown() throws Exception {
        bus.shutdown(true);
        if (null != celtixCfgFile) {
            System.setProperty(CFG_FILE_PROPERTY, celtixCfgFile);
        } else {
            System.clearProperty(CFG_FILE_PROPERTY);
        }
    }
    
    
    public void testInitialisationClientSide() throws Exception {
        
        URL wsdl = getClass().getResource("/wsdl/greeter_control.wsdl");
        GreeterService greeterService = new GreeterService(wsdl, SERVICE_NAME);         
        Greeter greeter = greeterService.getPort(PORT_NAME, Greeter.class);
        
        BindingProvider provider = (BindingProvider)greeter;
        AbstractBindingImpl abi = (AbstractBindingImpl)provider.getBinding();
        List<Handler> handlerChain = abi.getPreLogicalSystemHandlers();
        
        assertEquals(1, handlerChain.size());
        RMHandler handler = (RMHandler)handlerChain.get(0);
        
        assertEquals(bus, handler.getBus());
        assertNotNull(handler.getClientBinding());
        assertNull(handler.getServerBinding());
        assertNotNull(handler.getBinding());
        
        assertNotNull(handler.getClientTransport());
        assertNull(handler.getServerTransport());
        assertNotNull(handler.getTransport());

    }
     
    public void testInitialisationServerSide() {
   
        String address = "http://localhost:9000/SoapContext/GreeterPort";
        Endpoint ep = Endpoint.publish(address, new GreeterImpl());

        AbstractBindingImpl abi = (AbstractBindingImpl)ep.getBinding();
        List<Handler> handlerChain = abi.getPreLogicalSystemHandlers();
        
        assertEquals(1, handlerChain.size());
        RMHandler handler = (RMHandler)handlerChain.get(0);
        
        assertEquals(bus, handler.getBus());
        assertNull(handler.getClientBinding());
        assertNotNull(handler.getServerBinding());
        assertNotNull(handler.getBinding());
        
        assertNull(handler.getClientTransport());
        assertNotNull(handler.getServerTransport());
        assertNotNull(handler.getTransport());
    }
    
    @WebService(serviceName = "GreeterService", portName = "GreeterPort", 
                name = "Greeter", 
                targetNamespace = "http://celtix.objectweb.org/greeter_control")
    class GreeterImpl implements Greeter {

        public String greetMe(String requestType) {
            return null;
        }

        public Future<?> greetMeAsync(String requestType, AsyncHandler<GreetMeResponse> asyncHandler) {
            return null;
        }

        public Response<GreetMeResponse> greetMeAsync(String requestType) {
            return null;
        }

        public void greetMeOneWay(String requestType) {         
        }

        public void pingMe() throws PingMeFault {              
        }

        public Response<PingMeResponse> pingMeAsync() {
            return null;
        }

        public Future<?> pingMeAsync(AsyncHandler<PingMeResponse> asyncHandler) {
            return null;
        }

        public String sayHi() {
            return null;
        }

        public Response<SayHiResponse> sayHiAsync() {
            return null;
        }

        public Future<?> sayHiAsync(AsyncHandler<SayHiResponse> asyncHandler) {
            return null;
        }
    }
}
