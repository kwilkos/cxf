package org.objectweb.celtix.bus.ws.rm.persistence.jdbc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bus.ws.rm.Names;
import org.objectweb.celtix.bus.ws.rm.RMUtils;
import org.objectweb.celtix.bus.ws.rm.persistence.RMStoreException;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;

public class RMTxStoreTest extends TestCase {
    
    private static File root = new File("rmdb");  
    private static RMTxStore store;
    private static final String CLIENT_ENDPOINT_ID = 
        "celtix.{http://celtix.objectweb.org/greeter_control}GreeterService/GreeterPort";
    private static final String SERVER_ENDPOINT_ID = 
        "celtix.{http://celtix.objectweb.org/greeter_control}GreeterService";
    private static final String NON_ANON_ACKS_TO = 
        "http://localhost:9999/decoupled_endpoint";
    
    public static Test suite() throws Exception {
        
        TestSuite suite = new TestSuite(RMTxStoreTest.class);
        class RMTxStoreTestSetup extends TestSetup {
            public RMTxStoreTestSetup(Test test) {
                    super(test);
            }
            
            protected void setUp() {
                   
                deleteExistingDatabase(true);
                
                store = new RMTxStore();
                Map<String, String> params = new HashMap<String, String>();
                params.put(RMTxStore.DRIVER_CLASS_NAME_PROPERTY, "org.apache.derby.jdbc.EmbeddedDriver");
                params.put(RMTxStore.CONNECTION_URL_PROPERTY, "jdbc:derby:rmdb;create=true");
                store.init(params); 
            }
            
            
            protected void tearDown() {
                store = null;
                deleteExistingDatabase(false);
            }
        }
        return new RMTxStoreTestSetup(suite);  
    }
     
    public void testCreateTables() throws SQLException {
        // tables should  have been created during initialisation
        // but verify the operation is idempotent
        store.createTables();     
    }

    public void testCreateDeleteSrcSequences() {
        IMocksControl control = EasyMock.createNiceControl();
        RMSourceSequence seq = control.createMock(RMSourceSequence.class);
        Identifier sid1 = RMUtils.getWSRMFactory().createIdentifier();
        sid1.setValue("sequence1");
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);
        EasyMock.expect(seq.getExpiry()).andReturn(null);
        EasyMock.expect(seq.getOfferingSequenceIdentifier()).andReturn(null);
        EasyMock.expect(seq.getEndpointIdentifier()).andReturn(CLIENT_ENDPOINT_ID);
        
        control.replay();
        store.createSourceSequence(seq);   
        control.verify();
        
        control.reset();
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);
        EasyMock.expect(seq.getExpiry()).andReturn(null);
        EasyMock.expect(seq.getOfferingSequenceIdentifier()).andReturn(null);
        EasyMock.expect(seq.getEndpointIdentifier()).andReturn(CLIENT_ENDPOINT_ID);
        
        control.replay();
        try {
            store.createSourceSequence(seq);  
            fail("Expected RMStoreException was not thrown.");
        } catch (RMStoreException ex) {
            SQLException se = (SQLException)ex.getCause();
            // duplicate key value 
            assertEquals("23505", se.getSQLState());
        }
        control.verify();
        
        control.reset();
        Identifier sid2 = RMUtils.getWSRMFactory().createIdentifier();
        sid2.setValue("sequence2");
        EasyMock.expect(seq.getIdentifier()).andReturn(sid2);  
        EasyMock.expect(seq.getExpiry()).andReturn(new Date());
        Identifier sid3 = RMUtils.getWSRMFactory().createIdentifier();
        sid3.setValue("offeringSequence3");
        EasyMock.expect(seq.getOfferingSequenceIdentifier()).andReturn(sid3);
        EasyMock.expect(seq.getEndpointIdentifier()).andReturn(SERVER_ENDPOINT_ID);
        
         
        control.replay();
        store.createSourceSequence(seq);   
        control.verify();
            
        store.removeSourceSequence(sid1);
        store.removeSourceSequence(sid2);
        
        // deleting once again is a no-op
        store.removeSourceSequence(sid2);
       
    }
    
    public void testCreateDeleteDestSequences() {
        IMocksControl control = EasyMock.createNiceControl();
        RMDestinationSequence seq = control.createMock(RMDestinationSequence.class);
        Identifier sid1 = RMUtils.getWSRMFactory().createIdentifier();
        sid1.setValue("sequence1");
        EndpointReferenceType epr = RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS);
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);
        EasyMock.expect(seq.getAcksTo()).andReturn(epr);        
        EasyMock.expect(seq.getEndpointIdentifier()).andReturn(SERVER_ENDPOINT_ID);
        
        control.replay();
        store.createDestinationSequence(seq);   
        control.verify();
        
        control.reset();
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);
        EasyMock.expect(seq.getAcksTo()).andReturn(epr);        
        EasyMock.expect(seq.getEndpointIdentifier()).andReturn(SERVER_ENDPOINT_ID);
        
        control.replay();
        try {
            store.createDestinationSequence(seq);  
            fail("Expected RMStoreException was not thrown.");
        } catch (RMStoreException ex) {
            SQLException se = (SQLException)ex.getCause();
            // duplicate key value 
            assertEquals("23505", se.getSQLState());
        }
        control.verify();
        
        control.reset();
        Identifier sid2 = RMUtils.getWSRMFactory().createIdentifier();
        sid2.setValue("sequence2");
        EasyMock.expect(seq.getIdentifier()).andReturn(sid2); 
        epr = RMUtils.createReference(NON_ANON_ACKS_TO);
        EasyMock.expect(seq.getAcksTo()).andReturn(epr);
        EasyMock.expect(seq.getEndpointIdentifier()).andReturn(CLIENT_ENDPOINT_ID);
        
        control.replay();
        store.createDestinationSequence(seq);   
        control.verify();
            
        store.removeDestinationSequence(sid1);
        store.removeDestinationSequence(sid2);
        
        // deleting once again is a no-op
        store.removeDestinationSequence(sid2);
       
    }
    
    public void testCreateDeleteMessages() throws IOException, SQLException  {
        IMocksControl control = EasyMock.createNiceControl();
        RMMessage msg = control.createMock(RMMessage.class);
        Identifier sid1 = RMUtils.getWSRMFactory().createIdentifier();
        sid1.setValue("sequence1");
        EasyMock.expect(msg.getMessageNr()).andReturn(BigInteger.ONE); 
        InputStream is = new ByteArrayInputStream(new byte[89]);
        EasyMock.expect(msg.getContextAsStream()).andReturn(is);
        
        control.replay();
        store.beginTransaction();
        store.storeMessage(sid1, msg);
        store.commit();
        control.verify();
        
        control.reset();
        EasyMock.expect(msg.getMessageNr()).andReturn(BigInteger.ONE); 
        EasyMock.expect(msg.getContextAsStream()).andReturn(is);
        
        control.replay();
        store.beginTransaction();
        try {
            store.storeMessage(sid1, msg);
        } catch (SQLException ex) {
            assertEquals("23505", ex.getSQLState());
        }
        store.abort();
        control.verify();
        
        control.reset();
        EasyMock.expect(msg.getMessageNr()).andReturn(BigInteger.TEN); 
        EasyMock.expect(msg.getContextAsStream()).andReturn(is); 
        
        control.replay();
        store.beginTransaction();
        store.storeMessage(sid1, msg);
        store.commit();
        control.verify();
        
        Collection<BigInteger> messageNrs = new ArrayList<BigInteger>();
        messageNrs.add(BigInteger.ZERO);
        messageNrs.add(BigInteger.TEN);
        messageNrs.add(BigInteger.ONE);
        messageNrs.add(BigInteger.TEN);
        
        store.removeMessages(sid1, messageNrs);
        
        Identifier sid2 = RMUtils.getWSRMFactory().createIdentifier();
        sid1.setValue("sequence2");
        store.removeMessages(sid2, messageNrs);
    }
    
    public static void testUpdateDestinationSequence() throws SQLException, IOException {
        IMocksControl control = EasyMock.createNiceControl();
        RMDestinationSequence seq = control.createMock(RMDestinationSequence.class);
        Identifier sid1 = RMUtils.getWSRMFactory().createIdentifier();
        sid1.setValue("sequence1");
        EndpointReferenceType epr = RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS);
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);
        EasyMock.expect(seq.getAcksTo()).andReturn(epr);        
        EasyMock.expect(seq.getEndpointIdentifier()).andReturn(SERVER_ENDPOINT_ID);
        
        control.replay();
        store.createDestinationSequence(seq);   
        control.verify();
        
        control.reset();
        EasyMock.expect(seq.getLastMessageNr()).andReturn(null);
        InputStream is = new ByteArrayInputStream(new byte[32]);
        EasyMock.expect(seq.getAcknowledgmentAsStream()).andReturn(is);        
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);
        
        control.replay();
        store.beginTransaction();
        store.updateDestinationSequence(seq);
        store.abort();
        
        control.reset();
        EasyMock.expect(seq.getLastMessageNr()).andReturn(BigInteger.TEN);
        EasyMock.expect(seq.getAcknowledgmentAsStream()).andReturn(is);        
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);
        
        control.replay();
        store.beginTransaction();
        store.updateDestinationSequence(seq);
        store.abort();
        
        store.removeDestinationSequence(sid1);
    }
    
    public void testUpdateSourceSequence() throws SQLException {
        IMocksControl control = EasyMock.createNiceControl();
        RMSourceSequence seq = control.createMock(RMSourceSequence.class);
        Identifier sid1 = RMUtils.getWSRMFactory().createIdentifier();
        sid1.setValue("sequence1");
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);
        EasyMock.expect(seq.getExpiry()).andReturn(null);
        EasyMock.expect(seq.getOfferingSequenceIdentifier()).andReturn(null);
        EasyMock.expect(seq.getEndpointIdentifier()).andReturn(CLIENT_ENDPOINT_ID);
        
        control.replay();
        store.createSourceSequence(seq);   
        control.verify();        
        
        control.reset();
        EasyMock.expect(seq.getCurrentMessageNr()).andReturn(BigInteger.ONE);
        EasyMock.expect(seq.getLastMessage()).andReturn(false);
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);   
        
        control.replay();
        store.beginTransaction();
        store.updateSourceSequence(seq);
        store.abort();
        
        control.reset();
        EasyMock.expect(seq.getCurrentMessageNr()).andReturn(BigInteger.TEN);
        EasyMock.expect(seq.getLastMessage()).andReturn(true);  
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);
        
        control.replay();
        store.beginTransaction();
        store.updateSourceSequence(seq);
        store.abort();
        
        store.removeSourceSequence(sid1);
        
    }
    
    
    private static void deleteExistingDatabase(boolean now) {
        if (root.exists()) {
            recursiveDelete(root, now);
        }
        File log = new File("derby.log");
        if (log.exists()) {
            if (now) {
                log.delete();
            } else {
                log.deleteOnExit();
            }
        }       
    }
    
    private static void recursiveDelete(File dir, boolean now) {
        for  (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                recursiveDelete(f, now);
            } else {
                if (now) {
                    f.delete();
                } else {
                    f.deleteOnExit();
                }
            }
        }
        if (now) {
            dir.delete();
        } else {
            dir.deleteOnExit();
        }
    }
}
