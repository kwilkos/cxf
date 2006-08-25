package org.objectweb.celtix.transports.jms;

import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.spring.ConfigurationProviderImpl;
import org.objectweb.celtix.configuration.CompoundName;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.impl.TypeSchemaHelper;
//import org.objectweb.celtix.transports.jms.JMSAddressPolicyType;
//import org.objectweb.celtix.transports.jms.JMSClientBehaviorPolicyType;
//import org.objectweb.celtix.transports.jms.JMSServerBehaviorPolicyType;
import org.objectweb.celtix.transports.jms.jms_conf.JMSClientConfig;
import org.objectweb.celtix.transports.jms.jms_conf.JMSServerConfig;
import org.objectweb.celtix.transports.jms.jms_conf.JMSSessionPoolConfigPolicy;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;


public class JMSConfigTest extends TestCase {

    Bus bus;

    public JMSConfigTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JMSConfigTest.class);
    }

    public void setUp() throws Exception {
        TypeSchemaHelper.clearCache();
        ConfigurationProviderImpl.clearBeanFactoriesMap();
        bus = Bus.init();
    }

    public void tearDown() throws Exception {
        bus.shutdown(true);
        if (System.getProperty("celtix.config.file") != null) {
            System.clearProperty("celtix.config.file");
        }

        TypeSchemaHelper.clearCache();
        ConfigurationProviderImpl.clearBeanFactoriesMap();
    }

    private void createNecessaryConfig(URL wsdlUrl, QName serviceName,
                                       String portName) throws Exception {
        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName,
                                                                             portName);
        Configuration busCfg = bus.getConfiguration();
        assertNotNull(busCfg);
        Configuration endpointCfg = null;
        Configuration portCfg = null;

        String id = EndpointReferenceUtils.getServiceName(ref).toString();
        ConfigurationBuilder cb = bus.getConfigurationBuilder();

        // Server Endpoint Config
        CompoundName ecn = new CompoundName("celtix", id);
        endpointCfg = cb.getConfiguration(JMSConstants.ENDPOINT_CONFIGURATION_URI, ecn);
        assertNotNull(endpointCfg);

        // Client Service Endpoint  Port config.
        CompoundName pcn = new CompoundName("celtix",
            id + "/" + EndpointReferenceUtils.getPortName(ref).toString());
        portCfg = cb.getConfiguration(JMSConstants.PORT_CONFIGURATION_URI, pcn);
        assertNotNull(portCfg);

        CompoundName cn = new CompoundName(ecn, JMSConstants.JMS_SERVER_CONFIG_ID);
        // Server Transport Config.
        cb.getConfiguration(JMSConstants.JMS_SERVER_CONFIGURATION_URI, cn);

        cn = new CompoundName(pcn, JMSConstants.JMS_CLIENT_CONFIG_ID);
        //Client Transport Config.
        cb.getConfiguration(JMSConstants.JMS_CLIENT_CONFIGURATION_URI, cn);
    }

    public void testDefaultClientConfig() throws Exception {

        QName serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms", "HelloWorldService");
        URL wsdlUrl = getClass().getResource("/wsdl/jms_test.wsdl");
        assertNotNull(wsdlUrl);

        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName,
                                                                                "HelloWorldPort");

        createNecessaryConfig(wsdlUrl, serviceName, "HelloWorldPort");

        JMSClientTransport clientTransport = new JMSClientTransport(bus, ref, null);

        checkDefaultAddressFields(clientTransport.getJmsAddressDetails());

        JMSClientBehaviorPolicyType clientPolicy = clientTransport.getJMSClientBehaviourPolicy();
        assertTrue("JMSClientBehaviourPolicy cannot be null ", null != clientPolicy);

        assertTrue("JMSClientPolicy messageType should be text ",
                   JMSConstants.TEXT_MESSAGE_TYPE.equals(clientPolicy.getMessageType().value()));
        
        JMSClientConfig cltConf = clientTransport.getClientConfiguration();
        assertTrue("clientConfig should not be null ...", cltConf  != null);
        assertTrue("Client receive timeout should be 0", cltConf.getClientReceiveTimeout() == 0);
        assertTrue("Client message time-to-live should be 0", cltConf.getMessageTimeToLive() == 0);
        
        JMSSessionPoolConfigPolicy sessConf = clientTransport.getSessionPoolConfig();
        assertTrue("JMS Session pool config cannot be null", sessConf != null);
        assertTrue("JMS Session pool lowWaterMark should be 20...", sessConf.getLowWaterMark() == 20);
        assertTrue("JMS Session pool HighWaterMark should be 500...", sessConf.getHighWaterMark() == 500);
    }

    public void testDefaultServerConfig() throws Exception {

        QName serviceName =  new QName("http://celtix.objectweb.org/hello_world_jms", "HelloWorldService");
        URL wsdlUrl = getClass().getResource("/wsdl/jms_test.wsdl");
        assertNotNull(wsdlUrl);

        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName,
                                                                                "HelloWorldPort");

        createNecessaryConfig(wsdlUrl, serviceName, "HelloWorldPort");

        JMSServerTransport serverTransport = new JMSServerTransport(bus, ref);

        checkDefaultAddressFields(serverTransport.getJmsAddressDetails());

        JMSServerBehaviorPolicyType serverPolicy = serverTransport.getJMSServerBehaviourPolicy();

        assertTrue("JMSServerPolicy messageSelector should be null ",
                   serverPolicy.getMessageSelector() == null);
        assertTrue("JMSServerPolicy useMessageIDAsCorrelationID should be false ",
                   !serverPolicy.isUseMessageIDAsCorrelationID());
        assertTrue("JMSServerPolicy useMessageIDAsCorrelationID should be false ",
                   !serverPolicy.isTransactional());
        assertTrue("JMSServerPolicy durableSubscriberName should be null ",
                   serverPolicy.getDurableSubscriberName() == null);
        
        JMSServerConfig  srvConf = serverTransport.getServerConfiguration();
        assertTrue("serverConfig should not be null ...", srvConf  != null);
        assertTrue("Server message time-to-live should be 0", srvConf.getMessageTimeToLive() == 0);
        assertTrue("durableSubscriptionClientId should be null", 
                   srvConf.getDurableSubscriptionClientId() == null);
        
        JMSSessionPoolConfigPolicy sessConf = serverTransport.getSessionPoolConfig();
        assertTrue("JMS Session pool config cannot be null", sessConf != null);
        assertTrue("JMS Session pool lowWaterMark should be 20...", sessConf.getLowWaterMark() == 20);
        assertTrue("JMS Session pool HighWaterMark should be 500...", sessConf.getHighWaterMark() == 500);
        
    }

    public void testClientConfig() throws Exception {


        bus.shutdown(true);
        URL clientConfigFileUrl = getClass().getResource("/wsdl/jms_test_config.xml");
        System.setProperty("celtix.config.file", clientConfigFileUrl.toString());

        bus = Bus.init();
        QName serviceName =  new QName("http://celtix.objectweb.org/jms_conf_test",
                                       "HelloWorldQueueBinMsgService");
        URL wsdlUrl = getClass().getResource("/wsdl/jms_test_no_addr.wsdl");
        assertNotNull(wsdlUrl);

        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName,
                                                                                "HelloWorldQueueBinMsgPort");

        createNecessaryConfig(wsdlUrl, serviceName, "HelloWorldQueueBinMsgPort");

        JMSClientTransport clientTransport = new JMSClientTransport(bus, ref, null);

        checkNonDefaultAddressFields(clientTransport.getJmsAddressDetails());

        JMSClientBehaviorPolicyType clientPolicy = clientTransport.getJMSClientBehaviourPolicy();
        assertTrue("JMSClientBehaviourPolicy cannot be null ", null != clientPolicy);
        assertTrue("JMSClientPolicy messageType should be text ",
                   JMSConstants.BINARY_MESSAGE_TYPE.equals(clientPolicy.getMessageType().value()));
        
        JMSClientConfig cltConf = clientTransport.getClientConfiguration();
        assertTrue("clientConfig should not be null ...", cltConf  != null);
        assertTrue("Client receive timeout should be 500", cltConf.getClientReceiveTimeout() == 500);
        assertTrue("Client message time-to-live should be 500", cltConf.getMessageTimeToLive() == 500);
        
        JMSSessionPoolConfigPolicy sessConf = clientTransport.getSessionPoolConfig();
        assertTrue("JMS Session pool config cannot be null", sessConf != null);
        assertTrue("JMS Session pool lowWaterMark should be 10...", sessConf.getLowWaterMark() == 10);
        assertTrue("JMS Session pool HighWaterMark should be 5000...", sessConf.getHighWaterMark() == 5000);

        System.setProperty("celtix.config.file", "");
    }

    public void testServerConfig() throws Exception {

        URL clientConfigFileUrl = getClass().getResource("/wsdl/jms_test_config.xml");
        System.setProperty("celtix.config.file", clientConfigFileUrl.toString());

        QName serviceName =  new QName("http://celtix.objectweb.org/jms_conf_test",
                                       "HelloWorldQueueBinMsgService");
        URL wsdlUrl = getClass().getResource("/wsdl/jms_test_no_addr.wsdl");
        assertNotNull(wsdlUrl);

        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName,
                                                                                "HelloWorldQueueBinMsgPort");

        createNecessaryConfig(wsdlUrl, serviceName, "HelloWorldQueueBinMsgPort");

        JMSServerTransport serverTransport = new JMSServerTransport(bus, ref);

        checkNonDefaultAddressFields(serverTransport.getJmsAddressDetails());

        JMSServerBehaviorPolicyType serverPolicy = serverTransport.getJMSServerBehaviourPolicy();

        assertTrue("JMSServerPolicy messageSelector should be Celtix_message_selector ",
                   "Celtix_message_selector".equals(serverPolicy.getMessageSelector()));
        assertTrue("JMSServerPolicy useMessageIDAsCorrelationID should be true ",
                   serverPolicy.isUseMessageIDAsCorrelationID());
        assertTrue("JMSServerPolicy useMessageIDAsCorrelationID should be true ",
                   serverPolicy.isTransactional());
        assertTrue("JMSServerPolicy durableSubscriberName should be Celtix_subscriber ",
                   "Celtix_subscriber".equals(serverPolicy.getDurableSubscriberName()));
        
        JMSServerConfig  srvConf = serverTransport.getServerConfiguration();
        assertTrue("serverConfig should not be null ...", srvConf  != null);
        assertTrue("Server message time-to-live should be 500", srvConf.getMessageTimeToLive() == 500);
        assertTrue("durableSubscriptionClientId should not be null", 
                   srvConf.getDurableSubscriptionClientId() != null);
        
        JMSSessionPoolConfigPolicy sessConf = serverTransport.getSessionPoolConfig();
        assertTrue("JMS Session pool config cannot be null", sessConf != null);
        assertTrue("JMS Session pool lowWaterMark should be 10...", sessConf.getLowWaterMark() == 10);
        assertTrue("JMS Session pool HighWaterMark should be 5000...", sessConf.getHighWaterMark() == 5000);
    }

    public void checkDefaultAddressFields(JMSAddressPolicyType addrPolicy) throws Exception {
        assertNotNull("JMSAddress cannot be null ", addrPolicy);

        assertNull("JMSAddress: connectionUserName should be null",
                   addrPolicy.getConnectionUserName());
        assertNull("JMSAddress: connectionPassword should be null",
                   addrPolicy.getConnectionPassword());
        assertNull("JMSAddress: JndiReplyDestinationName should be null",
                   addrPolicy.getJndiReplyDestinationName());
        assertNotNull("JMSAddress: JndiDestinationName should not be null",
                   addrPolicy.getJndiDestinationName());
        assertNotNull("JMSAddress: jndiConnectionFactoryName should not be null",
                   addrPolicy.getJndiConnectionFactoryName());
        assertEquals(addrPolicy.getDestinationStyle().value(), JMSConstants.JMS_QUEUE);


        assertNotNull("JMSAddress:  jmsNaming should not be null ",
                   addrPolicy.getJMSNamingProperty());
    }

    public void checkNonDefaultAddressFields(JMSAddressPolicyType addrPolicy) throws Exception {

        assertNotNull("JMSAddress cannot be null ", addrPolicy);

        assertEquals("JMSAddress: connectionUserName should be testUser",
                   "testUser", addrPolicy.getConnectionUserName());
        assertEquals("JMSAddress: connectionPassword should be testPassword",
                   "testPassword", addrPolicy.getConnectionPassword());
        assertEquals("JMSAddress: JndiReplyDestinationName should be myOwnReplyDestination",
                   "myOwnReplyDestination", addrPolicy.getJndiReplyDestinationName());
        assertEquals("JMSAddress: JndiDestinationName should be myOwnDestination",
                   "myOwnDestination", addrPolicy.getJndiDestinationName());
        assertEquals("JMSAddress: jndiConnectionFactoryName should be MockConnectionFactory",
                   "MockConnectionFactory", addrPolicy.getJndiConnectionFactoryName());
        assertEquals("JMSAddress: destinationStyle should be queue",
                   addrPolicy.getDestinationStyle().value(), JMSConstants.JMS_QUEUE);

        assertNotNull("JMSAddress:  jmsNaming should not be null ",
                   addrPolicy.getJMSNamingProperty());
    }

}
