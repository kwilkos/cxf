package org.apache.cxf.transports.jms;

public final class JMSConstants {
    
    public static final String JMS_QUEUE = "queue";
    public static final String JMS_TOPIC = "topic";

    public static final String TEXT_MESSAGE_TYPE = "text";
    public static final String BINARY_MESSAGE_TYPE = "binary";


    public static final String JMS_SERVER_HEADERS = "org.apache.cxf.jms.server.headers";
    public static final String JMS_CLIENT_REQUEST_HEADERS = "org.apache.cxf.jms.client.request.headers";
    public static final String JMS_CLIENT_RESPONSE_HEADERS = 
        "org.apache.cxf.jms.client.response.headers";
    
    public static final String JMS_CLIENT_RECEIVE_TIMEOUT = "org.apache.cxf.jms.client.timeout";
    
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
    
    public static final String JMS_REBASED_REPLY_TO = "org.apache.cxf.jms.server.replyto";
    
}
