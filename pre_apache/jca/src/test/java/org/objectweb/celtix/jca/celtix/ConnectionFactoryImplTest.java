package org.objectweb.celtix.jca.celtix;



import java.io.Serializable;
import java.net.URL;

import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.easymock.classextension.EasyMock;

public class ConnectionFactoryImplTest extends TestCase {
    ManagedConnectionFactory mockConnectionFactory;

    ConnectionManager mockConnectionManager;

    public ConnectionFactoryImplTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        mockConnectionFactory = EasyMock.createMock(ManagedConnectionFactory.class);
        mockConnectionManager = EasyMock.createMock(ConnectionManager.class);
    }

    public void testInstanceOfSerializable() throws Exception {
        ConnectionFactoryImpl cf = new ConnectionFactoryImpl(mockConnectionFactory,
                                                             mockConnectionManager);
        assertTrue("instance of serializable", cf instanceof Serializable);
    }

    public void testInstanceOfReferencable() throws Exception {
        ConnectionFactoryImpl cf = new ConnectionFactoryImpl(mockConnectionFactory,
                                                             mockConnectionManager);
        assertTrue("instance of Referencable", cf instanceof Referenceable);

        assertNull("no ref set", cf.getReference());
        Reference ref = EasyMock.createMock(Reference.class);
        cf.setReference(ref);
        assertEquals("got back what was set", ref, cf.getReference());
    }

    public void testGetConnectionReturnsConnectionWithRightManager() throws Exception {
        EasyMock.reset(mockConnectionManager);
        
        CeltixConnectionRequestInfo reqInfo = 
            new CeltixConnectionRequestInfo(Runnable.class, 
                                            new URL("file:/tmp/foo"), 
                                            new QName(""), 
                                            new QName(""));
        
        mockConnectionManager.allocateConnection(EasyMock.eq(mockConnectionFactory),
                                                 EasyMock.eq(reqInfo));
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.replay(mockConnectionManager);
         
       
        ConnectionFactoryImpl cf = new ConnectionFactoryImpl((ManagedConnectionFactory)mockConnectionFactory,
                                                             (ConnectionManager)mockConnectionManager);
        
        Object o = cf.getConnection(Runnable.class, new URL("file:/tmp/foo"), new QName(""), new QName(""));
        assertNull("got the result (the passed in ConnectionRequestInfo) from out mock manager",
                   o);
        EasyMock.verify(mockConnectionManager); 
    }

    public void testGetConnectionWithNoPortReturnsConnectionWithRightManager() throws Exception {
        
        EasyMock.reset(mockConnectionManager);
        
        CeltixConnectionRequestInfo reqInfo = 
            new CeltixConnectionRequestInfo(Runnable.class, 
                                            new URL("file:/tmp/foo"), 
                                            new QName(""), 
                                            null);
        
        mockConnectionManager.allocateConnection(EasyMock.eq(mockConnectionFactory),
                                                 EasyMock.eq(reqInfo));
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.replay(mockConnectionManager);

        ConnectionFactoryImpl cf = new ConnectionFactoryImpl((ManagedConnectionFactory)mockConnectionFactory,
                                                             (ConnectionManager)mockConnectionManager);
        
        Object o = cf.getConnection(Runnable.class, new URL("file:/tmp/foo"), new QName(""));
        
        EasyMock.verify(mockConnectionManager);
        
        assertNull("got the result (the passed in ConnectionRequestInfo) from out mock manager",
                   o);
        
        
    }

    public void testGetConnectionWithNoWsdlLocationReturnsConnectionWithRightManager() throws Exception {
        
        EasyMock.reset(mockConnectionManager);
        
        CeltixConnectionRequestInfo reqInfo = 
            new CeltixConnectionRequestInfo(Runnable.class, 
                                            null, 
                                            new QName(""), 
                                            new QName(""));
        
        mockConnectionManager.allocateConnection(EasyMock.eq(mockConnectionFactory),
                                                 EasyMock.eq(reqInfo));
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.replay(mockConnectionManager);

        ConnectionFactoryImpl cf = new ConnectionFactoryImpl((ManagedConnectionFactory)mockConnectionFactory,
                                                             (ConnectionManager)mockConnectionManager);

        Object o = cf.getConnection(Runnable.class, new QName(""), new QName(""));
        EasyMock.verify(mockConnectionManager);
        
        assertNull("got the result (the passed in ConnectionRequestInfo) from out mock manager",
                   o);
        
    }

    public void testGetConnectionWithNoWsdlLocationAndNoPortReturnsConnectionWithRightManager()
        throws Exception {
        EasyMock.reset(mockConnectionManager);
        
        CeltixConnectionRequestInfo reqInfo = 
            new CeltixConnectionRequestInfo(Runnable.class, 
                                            null, 
                                            new QName(""), 
                                            null);
        
        mockConnectionManager.allocateConnection(EasyMock.eq(mockConnectionFactory),
                                                 EasyMock.eq(reqInfo));
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.replay(mockConnectionManager);
        
        ConnectionFactoryImpl cf = new ConnectionFactoryImpl((ManagedConnectionFactory)mockConnectionFactory,
                                                             (ConnectionManager)mockConnectionManager);

        Object o = cf.getConnection(Runnable.class, new QName(""));
        assertNull("got the result (the passed in ConnectionRequestInfo) from out mock manager",
                   o);
        

    }

    public void testGetConnectionWithNonInterface() throws Exception {
        ConnectionFactoryImpl cf = new ConnectionFactoryImpl(mockConnectionFactory,
                                                             mockConnectionManager);

        try {
            cf.getConnection(Object.class, new URL("file:/tmp/foo"), new QName(""), new QName(""));
            fail("expect exception on use of non interface class");
        } catch (ResourceException re) {
            assertTrue("nested ex is invalid arg", re.getCause() instanceof IllegalArgumentException);
        }
    }

    public static Test suite() {
        return new TestSuite(ConnectionFactoryImplTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {ConnectionFactoryImplTest.class.getName()});
    }
}
