package org.apache.cxf.transport.jms;

import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import junit.framework.TestCase;

import org.apache.cxf.transport.jms.PooledSession;
import org.easymock.classextension.EasyMock;

public class PooledSessionTest extends TestCase {

    public PooledSessionTest(String arg0) {
        super(arg0);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PooledSessionTest.class);
    }

    public void testPooledSession() throws Exception {
            
        Session sess =  EasyMock.createMock(Session.class);
        Destination dest = EasyMock.createMock(Destination.class);
        MessageProducer mproducer = EasyMock.createMock(MessageProducer.class);
        MessageConsumer mconsumer = EasyMock.createMock(MessageConsumer.class);
       
        PooledSession ps = new PooledSession(sess, dest, mproducer, mconsumer);
       
        assertTrue(ps.session().equals(sess));
        assertTrue(ps.destination().equals(dest));
        assertTrue(ps.consumer().equals(mconsumer));
        assertTrue(ps.producer().equals(mproducer));    
         
        MessageConsumer mcons = EasyMock.createMock(MessageConsumer.class);
        assertFalse(mconsumer.equals(mcons));
         
        ps.consumer(mcons);
         
        assertTrue(ps.consumer().equals(mcons));
         
        Destination mdest = EasyMock.createMock(Destination.class);
        assertFalse(dest.equals(mdest));
        
        ps.destination(mdest);
        assertTrue(mdest.equals(ps.destination()));
    }    
}
