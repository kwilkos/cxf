package org.objectweb.celtix.bus;

import java.util.Properties;

import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.spi.Provider;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.jaxws.spi.ProviderImpl;
import org.objectweb.hello_world_soap_http.AnnotatedGreeterImpl;
import org.objectweb.hello_world_soap_http.NotAnnotatedGreeterImpl;

public class EndpointFactoryImplTest extends TestCase {
    
    private Bus bus;
    private String epfClassName;
    
    protected void setUp() throws Exception {
        super.setUp();
        epfClassName = System.getProperty(Provider.JAXWSPROVIDER_PROPERTY);
        System.setProperty(Provider.JAXWSPROVIDER_PROPERTY,
                           ProviderImpl.JAXWS_PROVIDER);
        bus = Bus.init();
    }

    protected void tearDown() throws Exception {
        bus.shutdown(true);
        if (null == epfClassName) {
            Properties properties = System.getProperties();
            properties.remove(Provider.JAXWSPROVIDER_PROPERTY);
            System.setProperties(properties);
        } else {
            System.setProperty(Provider.JAXWSPROVIDER_PROPERTY, epfClassName);
        }
      
        super.tearDown();
    }

    public void testCreateWithNotAnnotatedImplementor() throws Exception {
        Object implementor = new NotAnnotatedGreeterImpl(); 
        Endpoint ep = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, implementor);     
        assertNotNull(ep);
    }
    
    public void testCreateWithCorrectlyAnnotatedImplementor() throws Exception {
        Object implementor = new AnnotatedGreeterImpl(); 
        Endpoint ep = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, implementor);     
        assertNotNull(ep);
    }
   
}
