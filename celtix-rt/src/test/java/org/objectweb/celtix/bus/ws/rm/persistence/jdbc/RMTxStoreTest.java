package org.objectweb.celtix.bus.ws.rm.persistence.jdbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Node;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bus.bindings.soap.SOAPBindingImpl;
import org.objectweb.celtix.bus.ws.rm.DestinationSequence;
import org.objectweb.celtix.bus.ws.rm.Names;
import org.objectweb.celtix.bus.ws.rm.RMHandlerTest;
import org.objectweb.celtix.bus.ws.rm.RMUtils;
import org.objectweb.celtix.bus.ws.rm.SourceSequence;
import org.objectweb.celtix.bus.ws.rm.persistence.RMStoreException;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;

public class RMTxStoreTest extends TestCase {
      
   
    private static final String CLIENT_ENDPOINT_ID = 
        "celtix.{http://celtix.objectweb.org/greeter_control}GreeterService/GreeterPort";
    private static final String SERVER_ENDPOINT_ID = 
        "celtix.{http://celtix.objectweb.org/greeter_control}GreeterService";
    private static final String NON_ANON_ACKS_TO = 
        "http://localhost:9999/decoupled_endpoint";
    private static final String SOAP_MSG_KEY = "org.objectweb.celtix.bindings.soap.message";
    
    private  RMTxStore store;
    
    public static Test suite() throws Exception {
        
        TestSuite suite = new TestSuite(RMTxStoreTest.class);
        class RMTxStoreTestSetup extends TestSetup {
            public RMTxStoreTestSetup(Test test) {
                    super(test);
            }
            
            protected void setUp() {                  
                RMTxStore.deleteDatabaseFiles("rmdb", true);
            }
            
            
            protected void tearDown() {
                RMTxStore st = new RMTxStore();
                Map<String, String> params = new HashMap<String, String>();
                params.put(RMTxStore.CONNECTION_URL_PROPERTY, "jdbc:derby:rmdb;create=true");
                st.init(params);
                Connection c = st.getConnection();
                try {
                    c.close();
                } catch (SQLException ex) {
                    // ignore
                }
                RMTxStore.deleteDatabaseFiles("rmdb", false);
            }
        }
        return new RMTxStoreTestSetup(suite);  
    }
    
    public void setUp() {
        store = new RMTxStore();
        Map<String, String> params = new HashMap<String, String>();
        params.put(RMTxStore.DRIVER_CLASS_NAME_PROPERTY, "org.apache.derby.jdbc.EmbeddedDriver");
        params.put(RMTxStore.CONNECTION_URL_PROPERTY, "jdbc:derby:rmdb;create=true");
        store.init(params);
        
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
        EasyMock.expect(msg.getMessageNr()).andReturn(BigInteger.ONE).times(2); 
        InputStream is = new ByteArrayInputStream(new byte[89]);
        EasyMock.expect(msg.getContextAsStream()).andReturn(is).times(2);
        
        control.replay();
        store.beginTransaction();
        store.storeMessage(sid1, msg, true);
        store.storeMessage(sid1, msg, false);
        store.commit();
        control.verify();
        
        control.reset();
        EasyMock.expect(msg.getMessageNr()).andReturn(BigInteger.ONE); 
        EasyMock.expect(msg.getContextAsStream()).andReturn(is);
        
        control.replay();
        store.beginTransaction();
        try {
            store.storeMessage(sid1, msg, true);
        } catch (SQLException ex) {
            assertEquals("23505", ex.getSQLState());
        }
        store.abort();
        control.verify();
        
        control.reset();
        EasyMock.expect(msg.getMessageNr()).andReturn(BigInteger.TEN).times(2); 
        EasyMock.expect(msg.getContextAsStream()).andReturn(is).times(2); 
        
        control.replay();
        store.beginTransaction();
        store.storeMessage(sid1, msg, true);
        store.storeMessage(sid1, msg, false);
        store.commit();
        control.verify();
        
        Collection<BigInteger> messageNrs = new ArrayList<BigInteger>();
        messageNrs.add(BigInteger.ZERO);
        messageNrs.add(BigInteger.TEN);
        messageNrs.add(BigInteger.ONE);
        messageNrs.add(BigInteger.TEN);
        
        store.removeMessages(sid1, messageNrs, true);
        store.removeMessages(sid1, messageNrs, false);
        
        Identifier sid2 = RMUtils.getWSRMFactory().createIdentifier();
        sid1.setValue("sequence2");
        store.removeMessages(sid2, messageNrs, true);
    }
    
    public void testUpdateDestinationSequence() throws SQLException, IOException {
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
        EasyMock.expect(seq.isLastMessage()).andReturn(false);
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);   
        
        control.replay();
        store.beginTransaction();
        store.updateSourceSequence(seq);
        store.abort();
        
        control.reset();
        EasyMock.expect(seq.getCurrentMessageNr()).andReturn(BigInteger.TEN);
        EasyMock.expect(seq.isLastMessage()).andReturn(true);  
        EasyMock.expect(seq.getIdentifier()).andReturn(sid1);
        
        control.replay();
        store.beginTransaction();
        store.updateSourceSequence(seq);
        store.abort();
        
        store.removeSourceSequence(sid1);
        
    }
    
    public void testGetDestinationSequences() throws SQLException, IOException {
        
        Identifier sid1 = null;
        Identifier sid2 = null;
        
        Collection<RMDestinationSequence> seqs = store.getDestinationSequences("unknown");
        assertEquals(0, seqs.size());
        
        try {
            sid1 = setupDestinationSequence("sequence1");

            seqs = store.getDestinationSequences(SERVER_ENDPOINT_ID);
            assertEquals(1, seqs.size());
            checkRecoveredDestinationSequences(seqs);

            sid2 = setupDestinationSequence("sequence2");
            seqs = store.getDestinationSequences(SERVER_ENDPOINT_ID);
            assertEquals(2, seqs.size());
            checkRecoveredDestinationSequences(seqs);
        } finally {
            if (null != sid1) {
                store.removeDestinationSequence(sid1);
            }
            if (null != sid2) {
                store.removeDestinationSequence(sid2);
            }
        }
    }
    
    public void testGetSourceSequences() throws SQLException, IOException {
        
        Identifier sid1 = null;
        Identifier sid2 = null;
        
        Collection<RMSourceSequence> seqs = store.getSourceSequences("unknown");
        assertEquals(0, seqs.size());
        
        try {
            sid1 = setupSourceSequence("sequence1");

            seqs = store.getSourceSequences(CLIENT_ENDPOINT_ID);
            assertEquals(1, seqs.size());
            checkRecoveredSourceSequences(seqs);

            sid2 = setupSourceSequence("sequence2");
            seqs = store.getSourceSequences(CLIENT_ENDPOINT_ID);
            assertEquals(2, seqs.size());
            checkRecoveredSourceSequences(seqs);
        } finally {
            if (null != sid1) {
                store.removeSourceSequence(sid1);
            }
            if (null != sid2) {
                store.removeSourceSequence(sid2);
            }
        }
    }
    
    public void testGetMessages() throws SQLException, IOException, SOAPException {
        
        Identifier sid1 = RMUtils.getWSRMFactory().createIdentifier();
        sid1.setValue("sequence1");
        Identifier sid2 = RMUtils.getWSRMFactory().createIdentifier();
        sid2.setValue("sequence2");
        
        Collection<RMMessage> out = store.getMessages(sid1, true);
        assertEquals(0, out.size());
        Collection<RMMessage> in = store.getMessages(sid1, false);
        assertEquals(0, out.size());
        
        try {
            setupMessage(sid1, BigInteger.ONE, true);
            setupMessage(sid1, BigInteger.ONE, false);

            out = store.getMessages(sid1, true);
            assertEquals(1, out.size());
            checkRecoveredMessages(out);
            
            in = store.getMessages(sid1, false);
            assertEquals(1, in.size());
            checkRecoveredMessages(in);
            
            setupMessage(sid1, BigInteger.TEN, true);
            setupMessage(sid1, BigInteger.TEN, false);
            
            out = store.getMessages(sid1, true);
            assertEquals(2, out.size());
            checkRecoveredMessages(out);
            
            in = store.getMessages(sid1, false);
            assertEquals(2, in.size());
            checkRecoveredMessages(in);
        } finally {
            Collection<BigInteger> msgNrs = new ArrayList<BigInteger>();
            msgNrs.add(BigInteger.ONE);
            msgNrs.add(BigInteger.TEN);
         
            store.removeMessages(sid1, msgNrs, true);
            store.removeMessages(sid1, msgNrs, false);
        }
    }
    
    private Identifier setupDestinationSequence(String s) throws IOException, SQLException {
        IMocksControl control = EasyMock.createNiceControl();
        DestinationSequence seq = control.createMock(DestinationSequence.class);
        
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue(s);
        EndpointReferenceType epr = RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS);
        
            
        SequenceAcknowledgement ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        AcknowledgementRange range = 
            RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();
        range.setLower(BigInteger.ONE);
        range.setUpper(BigInteger.ONE);
        ack.getAcknowledgementRange().add(range); 
        BigInteger lmn = null;
        
        if ("sequence2".equals(s)) {
            range = 
                RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();
            range.setLower(new BigInteger("3"));
            range.setUpper(BigInteger.TEN);
            ack.getAcknowledgementRange().add(range); 
            lmn = BigInteger.TEN;
        } 
        
        EasyMock.expect(seq.getIdentifier()).andReturn(sid);
        EasyMock.expect(seq.getAcksTo()).andReturn(epr);
        EasyMock.expect(seq.getEndpointIdentifier()).andReturn(SERVER_ENDPOINT_ID);
        EasyMock.expect(seq.getLastMessageNr()).andReturn(lmn);
        InputStream is = RMUtils.getPersistenceUtils().getAcknowledgementAsInputStream(ack);
        EasyMock.expect(seq.getAcknowledgmentAsStream()).andReturn(is);
        EasyMock.expect(seq.getIdentifier()).andReturn(sid);
        
        control.replay();
        store.createDestinationSequence(seq);           
        store.beginTransaction();
        store.updateDestinationSequence(seq);
        
        store.commit();
        
        return sid;
    }
    
    private Identifier setupSourceSequence(String s) throws IOException, SQLException {
        IMocksControl control = EasyMock.createNiceControl();
        SourceSequence seq = control.createMock(SourceSequence.class);
        
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue(s);      
            
        Date expiry = null;
        Identifier osid = null;
        BigInteger cmn = BigInteger.ONE;
        boolean lm = false;
        
        if ("sequence2".equals(s)) {
            expiry = new Date(System.currentTimeMillis() + 3600 * 1000);
            osid = RMUtils.getWSRMFactory().createIdentifier();
            osid.setValue("offeringSequence");
            cmn = BigInteger.TEN;
            lm = true;            
        } 
        
        EasyMock.expect(seq.getIdentifier()).andReturn(sid);
        EasyMock.expect(seq.getExpiry()).andReturn(expiry);
        EasyMock.expect(seq.getOfferingSequenceIdentifier()).andReturn(osid);
        EasyMock.expect(seq.getEndpointIdentifier()).andReturn(CLIENT_ENDPOINT_ID);
        EasyMock.expect(seq.getCurrentMessageNr()).andReturn(cmn);
        EasyMock.expect(seq.isLastMessage()).andReturn(lm);
        EasyMock.expect(seq.getIdentifier()).andReturn(sid);
        
        control.replay();
        store.createSourceSequence(seq);           
        store.beginTransaction();
        store.updateSourceSequence(seq);        
        store.commit();
        
        return sid;
    }
    
    public void setupMessage(Identifier sid, BigInteger mn, boolean outbound) 
        throws IOException, SQLException, SOAPException  {
        IMocksControl control = EasyMock.createNiceControl();
        RMMessage msg = control.createMock(RMMessage.class);
        EasyMock.expect(msg.getMessageNr()).andReturn(mn);
              
        MessageContext ctx = new GenericMessageContext();
        ctx.put("a", "astring");
        ctx.put("b", Boolean.TRUE);
        ctx.put("c", new Integer(Integer.MIN_VALUE));
        ctx.put("d", mn);
        ctx.put("e", this);
        InputStream mis = RMHandlerTest.class.getResourceAsStream("resources/GreetMeDocLiteralRequest.xml");
        SOAPBindingImpl binding = new SOAPBindingImpl(false);
        SOAPMessage smsg = binding.getMessageFactory().createMessage(null, mis);
        ctx.put(SOAP_MSG_KEY, smsg);
        InputStream cis = RMUtils.getPersistenceUtils().getContextAsInputStream(ctx);
        EasyMock.expect(msg.getContextAsStream()).andReturn(cis);
        
        control.replay();
        store.beginTransaction();
        store.storeMessage(sid, msg, outbound);        
        store.commit();
    }
    
    private void checkRecoveredDestinationSequences(Collection<RMDestinationSequence> seqs) {
        
        for (RMDestinationSequence recovered : seqs) {
            assertTrue("sequence1".equals(recovered.getIdentifier().getValue())
                                          || "sequence2".equals(recovered.getIdentifier().getValue()));
            assertEquals(Names.WSA_ANONYMOUS_ADDRESS, recovered.getAcksTo().getAddress().getValue());
            assertNull(recovered.getEndpointIdentifier());
            if ("sequence1".equals(recovered.getIdentifier().getValue())) {                      
                assertNull(recovered.getLastMessageNr());                
                assertEquals(1, recovered.getAcknowledgment().getAcknowledgementRange().size());
                AcknowledgementRange r = recovered.getAcknowledgment().getAcknowledgementRange().get(0);
                assertEquals(BigInteger.ONE, r.getLower());
                assertEquals(BigInteger.ONE, r.getUpper());
            } else {
                assertEquals(BigInteger.TEN, recovered.getLastMessageNr());
                assertEquals(2, recovered.getAcknowledgment().getAcknowledgementRange().size());
                AcknowledgementRange r = recovered.getAcknowledgment().getAcknowledgementRange().get(0);
                assertEquals(BigInteger.ONE, r.getLower());
                assertEquals(BigInteger.ONE, r.getUpper());
                r = recovered.getAcknowledgment().getAcknowledgementRange().get(1);
                assertEquals(new BigInteger("3"), r.getLower());
                assertEquals(BigInteger.TEN, r.getUpper());                
            }
        }
    }
    
    private void checkRecoveredSourceSequences(Collection<RMSourceSequence> seqs) {
        
        for (RMSourceSequence recovered : seqs) {
            assertTrue("sequence1".equals(recovered.getIdentifier().getValue())
                                          || "sequence2".equals(recovered.getIdentifier().getValue()));
            assertNull(recovered.getEndpointIdentifier());
            if ("sequence1".equals(recovered.getIdentifier().getValue())) {                      
                assertFalse(recovered.isLastMessage());
                assertEquals(BigInteger.ONE, recovered.getCurrentMessageNr());  
                assertNull(recovered.getExpiry());
                assertNull(recovered.getOfferingSequenceIdentifier());
            } else {
                assertTrue(recovered.isLastMessage());
                assertEquals(BigInteger.TEN, recovered.getCurrentMessageNr()); 
                assertNotNull(recovered.getExpiry());
                assertEquals("offeringSequence", recovered.getOfferingSequenceIdentifier().getValue());
            }
        }
    }
    
    public void checkRecoveredMessages(Collection<RMMessage> msgs) throws SOAPException { 
        for (RMMessage msg : msgs) {
            BigInteger mn = msg.getMessageNr();            
            assertTrue(BigInteger.ONE.equals(mn) 
                       || BigInteger.TEN.equals(mn));
            MessageContext mc = msg.getContext();
            assertEquals("astring", mc.get("a"));
            assertTrue((Boolean)mc.get("b"));
            assertEquals(Integer.MIN_VALUE, ((Integer)mc.get("c")).intValue());
            assertEquals(mn, (BigInteger)mc.get("d"));
            assertNull(mc.get("e"));
            SOAPMessage smsg = (SOAPMessage)mc.get(SOAP_MSG_KEY);
            for (int i = 0; i < smsg.getSOAPBody().getChildNodes().getLength(); i++) {
                Node node = smsg.getSOAPBody().getChildNodes().item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    assertEquals("greetMeRequest", node.getLocalName());
                }
            }
        }
    }
}
