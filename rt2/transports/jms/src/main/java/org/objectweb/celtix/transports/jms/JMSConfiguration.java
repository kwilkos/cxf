package org.objectweb.celtix.transports.jms;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.CompoundName;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.transports.jms.jms_conf.JMSSessionPoolConfigPolicy;

public class JMSConfiguration {
    protected String address;
    protected Configuration configuration;
    protected JMSAddressPolicyType jmsAddressPolicy;
    protected JMSSessionPoolConfigPolicy jmsSessionPoolConfig;
    
    public JMSConfiguration(Bus bus, EndpointInfo endpointInfo, boolean isServer) {
        address = endpointInfo.getAddress();
        configuration = createConfiguration(bus, endpointInfo, isServer);
        jmsAddressPolicy = getAddressPolicy();
        jmsSessionPoolConfig = getSessionPoolPolicy();
    }
    
    public String getAddress() {
        return address;
    }
    
    public final JMSAddressPolicyType getJmsAddressDetails() {
        return jmsAddressPolicy;
    }
    
    public final JMSSessionPoolConfigPolicy getSessionPoolConfig() {
        return jmsSessionPoolConfig;
    }
    
    private JMSAddressPolicyType getAddressPolicy() {
        JMSAddressPolicyType pol = configuration.getObject(JMSAddressPolicyType.class, "jmsAddress");
        if (pol == null) {
            pol = new JMSAddressPolicyType();
        }
        return pol;
    }
    
    private JMSSessionPoolConfigPolicy getSessionPoolPolicy() {
        JMSSessionPoolConfigPolicy sessionPoolCfg = 
            configuration.getObject(JMSSessionPoolConfigPolicy.class, "jmsSessionPool");
        if (sessionPoolCfg == null) {
            sessionPoolCfg = new JMSSessionPoolConfigPolicy();
        }
        return sessionPoolCfg;
    }  
    
    private Configuration createConfiguration(Bus bus, EndpointInfo endpointInfo, boolean isServer) {
        String configURI; 
        CompoundName configID;
        ConfigurationBuilder cb = bus.getExtension(ConfigurationBuilder.class);
        if (isServer) {
            configURI = JMSConstants.JMS_SERVER_CONFIGURATION_URI;
            configID = new CompoundName(
                bus.getId(),
                endpointInfo.getService().getName().toString()
                + "/" + endpointInfo.getName().getLocalPart(),
                JMSConstants.JMS_SERVER_CONFIG_ID
            );
        } else {
            configURI = JMSConstants.JMS_CLIENT_CONFIGURATION_URI;
            configID = new CompoundName(
                bus.getId(),
                endpointInfo.getService().getName().toString()
                + "/" + endpointInfo.getName().getLocalPart(),
                JMSConstants.JMS_CLIENT_CONFIG_ID
            );
        }       
               
        Configuration cfg = cb.getConfiguration(configURI, configID);
        // register the additional provider

        //TODO need to avoid add more configuration provider
        cfg.getProviders().add(new ServiceModelJMSConfigurationProvider(endpointInfo));

        return cfg;
    }
}
