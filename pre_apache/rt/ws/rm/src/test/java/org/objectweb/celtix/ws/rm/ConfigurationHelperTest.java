package org.objectweb.celtix.ws.rm;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bus.configuration.wsrm.DeliveryAssuranceType;
import org.objectweb.celtix.configuration.CompoundName;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.impl.ConfigurationBuilderImpl;
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
        ConfigurationBuilder builder = new ConfigurationBuilderImpl();
        CompoundName id = null;
        if (server) {
            id = new CompoundName(
                "celtix",
                SERVICE_NAME.toString(),
                ConfigurationHelper.RM_CONFIGURATION_ID
            );
        } else {
            id = new CompoundName(
                "celtix",
                SERVICE_NAME.toString(),
                ConfigurationHelper.RM_CONFIGURATION_ID + "/" + PORT_NAME
            );
        }
        configuration = builder.getConfiguration(ConfigurationHelper.RM_CONFIGURATION_URI, id);
        
        control = EasyMock.createNiceControl();
        AbstractBindingBase binding = control.createMock(AbstractBindingBase.class);
        Bus bus = control.createMock(Bus.class);
        Configuration busCfg = control.createMock(Configuration.class);
        expect(binding.getBus()).andReturn(bus).times(2);
        expect(bus.getConfiguration()).andReturn(busCfg);
        CompoundName bcn = new CompoundName("celtix"); 
        expect(busCfg.getId()).andReturn(bcn);
        expect(bus.getConfigurationBuilder()).andReturn(builder);
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
