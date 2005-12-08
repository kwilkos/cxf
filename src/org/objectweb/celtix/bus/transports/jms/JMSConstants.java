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
    

    public static final String JMS_RESPONSE_HEADERS = "org.objectweb.celtix.jms.response.headers";
    public static final String JMS_REQUEST_HEADERS = "org.objectweb.celtix.jms.request.headers";
}
