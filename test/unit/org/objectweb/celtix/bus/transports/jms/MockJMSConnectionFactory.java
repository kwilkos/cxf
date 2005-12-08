package org.objectweb.celtix.bus.transports.jms;

import java.util.Hashtable;

//import javax.jms.Connection;
import javax.jms.QueueConnection;
import javax.jms.TopicConnection;
import javax.naming.Context;
//import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.easymock.classextension.EasyMock;

public class MockJMSConnectionFactory implements InitialContextFactory {
    
    private static boolean isQueue = true;
    private static Context returnContext;
    
    public MockJMSConnectionFactory() {
        //
    }

    public static void setQueueDestinationStyle(boolean isQueueDestination) {
        isQueue = isQueueDestination;
    }
    
    public static boolean isQueue() {
        return isQueue;
    }
    public static void setReturnContext(Context ctx) {
        returnContext = ctx;
    }
    public  QueueConnection createQueueConnection() {
        return  EasyMock.createMock(QueueConnection.class);
    }
    
    public  TopicConnection createTopicConnection() {
        return  EasyMock.createMock(TopicConnection.class);
    }
    
    public Context getInitialContext(Hashtable arg) throws NamingException {
        return returnContext;
    }

}
