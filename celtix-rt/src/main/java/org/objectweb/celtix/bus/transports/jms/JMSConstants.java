package org.objectweb.celtix.bus.transports.jms;

public final class JMSConstants {

    // JMS Attribute Names
    public static final String DESTINATION_STYLE = "destinationStyle";
    public static final String JNDI_PROVIDER_URL = "jndiProviderURL";
    public static final String INITIAL_CONTEXT_FACTORY = "initialContextFactory";
    public static final String JNDI_CONNECTION_FACTORY_NAME = "jndiConnectionFactoryName";
    public static final String JNDI_DESTINATION_NAME = "jndiDestinationName";
    public static final String JNDI_REPLY_DESTINATION_NAME = "jndiReplyDestinationName";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String USE_MESSAGE_ID_AS_CORRELATION_ID = "useMessageIDAsCorrelationID";

    public static final String MESSAGE_SELECTOR = "messageSelector";
    public static final String DURABLE_SUBSCRIBER_NAME = "durableSubscriberName";
    
    public static final String JMS_QUEUE = "queue";
    public static final String JMS_TOPIC = "topic";

    public static final String TEXT_MESSAGE_TYPE = "text";
    public static final String BINARY_MESSAGE_TYPE = "binary";

    public static final String IS_TRANSACTIONAL = "transactional";

    public static final String JMS_CONNECTION_USERNAME = "connectionUserName";
    public static final String JMS_CONNECTION_PASSWORD = "connectionPassword";
    

    public static final String JMS_SERVER_HEADERS = "org.objectweb.celtix.jms.server.headers";
    public static final String JMS_CLIENT_REQUEST_HEADERS = "org.objectweb.celtix.jms.client.request.headers";
    public static final String JMS_CLIENT_RESPONSE_HEADERS = 
        "org.objectweb.celtix.jms.client.response.headers";
    public static final String JMS_CLIENT_RECEIVE_TIMEOUT = "org.objectweb.celtix.jms.client.timeout";
    public static final String JMS_SERVER_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/transports/jms/jms-server-config";
    public static final String JMS_CLIENT_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/transports/jms/jms-client-config";
    public static final String ENDPOINT_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/jaxws/endpoint-config";
    public static final String SERVICE_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/jaxws/service-config";
    public static final String PORT_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/jaxws/port-config";
    public static final String JMS_CLIENT_CONFIG_ID = "jms-client";
    public static final String JMS_SERVER_CONFIG_ID = "jms-server";
    
}
