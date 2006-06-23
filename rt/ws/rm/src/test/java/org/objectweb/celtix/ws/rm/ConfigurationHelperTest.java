package org.objectweb.celtix.ws.rm;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bus.busimpl.BusConfigurationBuilder;
import org.objectweb.celtix.bus.configuration.wsrm.DeliveryAssuranceType;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.bus.jaxws.ServiceImpl;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

import static org.easymock.EasyMock.expect;

public class ConfigurationHelperTest extends TestCase {
    
    private static final QName SERVICE_NAME = 
        new QName("http://celtix.objectweb.org/greeter_control", "GreeterService");
    private static final String PORT_NAME = "GreeterPort";
    
    private ConfigurationHelper ch;
    private IMocksControl control;
    private Configuration configuration;
    
    public void setUp() {
        doSetUp(true);
    }
    
    public void tearDown() {
        
    }
    
    public void testGetAcksPolicy() {
        assertNotNull(ch.getAcksPolicy());
    }
    
    public void testGetConfiguration() {
        assertNotNull(ch.getConfiguration());
    }
    
    public void testGetDestinationPolicies() {
        assertNotNull(ch.getDestinationPolicies());
    }
    
    public void testGetEndpointId() {
        assertEquals("celtix." + SERVICE_NAME.toString(), ch.getEndpointId());
    }
    
    public void testGetDeliveryAssurance() {
        DeliveryAssuranceType da = ch.getDeliveryAssurance();
        assertTrue(da.isSetAtLeastOnce());
        assertTrue(!da.isSetAtMostOnce());
        assertTrue(!da.isSetInOrder());
    }
      
    public void testGetRMAssertion() {
        RMAssertionType rma = ch.getRMAssertion();
        assertNotNull(rma.getBaseRetransmissionInterval());
        assertNotNull(rma.getExponentialBackoff());
    }
    
    public void testGetSequenceTerminationPolicy() {
        assertNotNull(ch.getSequenceTerminationPolicy());
    }
    
    public void testGetSourcePolicies() {
        assertNotNull(ch.getSourcePolicies());
    }
    
    private void doSetUp(boolean server) {
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
        Configuration busCfg = builder.buildConfiguration(
            BusConfigurationBuilder.BUS_CONFIGURATION_URI, "celtix");
        Configuration parent = null;
        if (server) {
            parent = builder.buildConfiguration(EndpointImpl.ENDPOINT_CONFIGURATION_URI, 
                                                SERVICE_NAME.toString(), busCfg);
        } else {
            String id = SERVICE_NAME.toString() + "/" + PORT_NAME;
            parent = builder.buildConfiguration(ServiceImpl.PORT_CONFIGURATION_URI, id, busCfg);
        }
        configuration = builder.buildConfiguration(ConfigurationHelper.RM_CONFIGURATION_URI, 
                                                   ConfigurationHelper.RM_CONFIGURATION_ID, parent);
        
        control = EasyMock.createNiceControl();
        AbstractBindingBase binding = control.createMock(AbstractBindingBase.class);
        Bus bus = control.createMock(Bus.class);
        expect(binding.getBus()).andReturn(bus);
        expect(bus.getConfiguration()).andReturn(busCfg);
        EndpointReferenceType epr = EndpointReferenceUtils.getEndpointReference(
            "http://localhost:9000/SoapContext/GreeterPort");
        EndpointReferenceUtils.setServiceAndPortName(epr, SERVICE_NAME, PORT_NAME);
        expect(binding.getEndpointReference()).andReturn(epr);
        RMPolicyProvider pp = control.createMock(RMPolicyProvider.class);
        configuration.getProviders().add(pp);
        
        control.replay();
        ch = new ConfigurationHelper(binding, server);
        control.verify();       
        control.reset();
        
        configuration.getProviders().remove(pp);
    }
      
}
