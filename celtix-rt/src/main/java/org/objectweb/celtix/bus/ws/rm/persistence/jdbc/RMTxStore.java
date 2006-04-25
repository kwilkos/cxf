package org.objectweb.celtix.bus.ws.rm.persistence.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.bus.ws.rm.DestinationSequence;
import org.objectweb.celtix.bus.ws.rm.RMMessageImpl;
import org.objectweb.celtix.bus.ws.rm.RMUtils;
import org.objectweb.celtix.bus.ws.rm.SourceSequence;
import org.objectweb.celtix.bus.ws.rm.persistence.RMStoreException;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;
import org.objectweb.celtix.ws.rm.persistence.RMStore;


public class RMTxStore implements RMStore {
   
    public static final String DRIVER_CLASS_NAME_PROPERTY = 
        "org.objectweb.celtix.rm.persistence.jdbc.driver";
    public static final String CONNECTION_URL_PROPERTY =
        "org.objectweb.celtix.rm.persistence.jdbc.url";    
    public static final String USER_NAME_PROPERTY =
        "org.objectweb.celtix.rm.persistence.jdbc.user";
    public static final String PASSWORD_PROPERTY =
        "org.objectweb.celtix.rm.persistence.jdbc.password";
    
    private static final String CREATE_DEST_SEQUENCES_TABLE_STMT =
        "CREATE TABLE CELTIX_RM_DEST_SEQUENCES " 
        + "(SEQ_ID VARCHAR(256) NOT NULL, "
        + "ACKS_TO VARCHAR(1024) NOT NULL, "
        + "LAST_MSG_NO DECIMAL(31, 0), "
        + "ENDPOINT_ID VARCHAR(1024), "
        + "ACKNOWLEDGED BLOB, "
        + "PRIMARY KEY (SEQ_ID))";
    private static final String CREATE_SRC_SEQUENCES_TABLE_STMT =
        "CREATE TABLE CELTIX_RM_SRC_SEQUENCES " 
        + "(SEQ_ID VARCHAR(256) NOT NULL, "
        + "CUR_MSG_NO DECIMAL(31, 0) NOT NULL DEFAULT 1, "
        + "LAST_MSG CHAR(1), "
        + "EXPIRY BIGINT, " 
        + "OFFERING_SEQ_ID VARCHAR(256), "
        + "ENDPOINT_ID VARCHAR(1024), "            
        + "PRIMARY KEY (SEQ_ID))";
    private static final String CREATE_MESSAGES_TABLE_STMT =
        "CREATE TABLE {0} " 
        + "(SEQ_ID VARCHAR(256) NOT NULL, "
        + "MSG_NO DECIMAL(31, 0) NOT NULL, "
        + "CONTEXT BLOB, "
        + "PRIMARY KEY (SEQ_ID, MSG_NO))";
    private static final String INBOUND_MSGS_TABLE_NAME = "CELTIX_RM_INBOUND_MESSAGES";
    private static final String OUTBOUND_MSGS_TABLE_NAME = "CELTIX_RM_OUTBOUND_MESSAGES";    
    
    
    private static final String CREATE_DEST_SEQUENCE_STMT_STR 
        = "INSERT INTO CELTIX_RM_DEST_SEQUENCES (SEQ_ID, ACKS_TO, ENDPOINT_ID) VALUES(?, ?, ?)";
    private static final String CREATE_SRC_SEQUENCE_STMT_STR
        = "INSERT INTO CELTIX_RM_SRC_SEQUENCES VALUES(?, 1, '0', ?, ?, ?)";
    private static final String DELETE_DEST_SEQUENCE_STMT_STR =
        "DELETE FROM CELTIX_RM_DEST_SEQUENCES WHERE SEQ_ID = ?";
    private static final String DELETE_SRC_SEQUENCE_STMT_STR =
        "DELETE FROM CELTIX_RM_SRC_SEQUENCES WHERE SEQ_ID = ?";
    private static final String UPDATE_DEST_SEQUENCE_STMT_STR =
        "UPDATE CELTIX_RM_DEST_SEQUENCES SET LAST_MSG_NO = ?, ACKNOWLEDGED = ? WHERE SEQ_ID = ?";
    private static final String UPDATE_SRC_SEQUENCE_STMT_STR =
        "UPDATE CELTIX_RM_SRC_SEQUENCES SET CUR_MSG_NO = ?, LAST_MSG = ? WHERE SEQ_ID = ?";
    private static final String CREATE_MESSAGE_STMT_STR 
        = "INSERT INTO {0} VALUES(?, ?, ?)";
    private static final String DELETE_MESSAGE_STMT_STR =
        "DELETE FROM {0} WHERE SEQ_ID = ? AND MSG_NO = ?";
    
    private static final String SELECT_DEST_SEQUENCES_STMT_STR =
        "SELECT SEQ_ID, ACKS_TO, LAST_MSG_NO, ACKNOWLEDGED FROM CELTIX_RM_DEST_SEQUENCES "
        + "WHERE ENDPOINT_ID = ?";
    private static final String SELECT_SRC_SEQUENCES_STMT_STR =
        "SELECT SEQ_ID, CUR_MSG_NO, LAST_MSG, EXPIRY, OFFERING_SEQ_ID FROM CELTIX_RM_SRC_SEQUENCES "
        + "WHERE ENDPOINT_ID = ?";
    private static final String SELECT_MESSAGES_STMT_STR =
        "SELECT MSG_NO, CONTEXT FROM {0} WHERE SEQ_ID = ?";
   
    private static final Logger LOG = LogUtils.getL7dLogger(RMTxStore.class);
    
    private static Map<String, Connection> connectionMap;
    private Connection connection;
    private PreparedStatement createDestSequenceStmt;
    private PreparedStatement createSrcSequenceStmt;
    private PreparedStatement deleteDestSequenceStmt;
    private PreparedStatement deleteSrcSequenceStmt;
    private PreparedStatement updateDestSequenceStmt;
    private PreparedStatement updateSrcSequenceStmt;
    private PreparedStatement selectDestSequencesStmt;
    private PreparedStatement selectSrcSequencesStmt;
    
    private PreparedStatement createInboundMessageStmt;
    private PreparedStatement createOutboundMessageStmt;
    private PreparedStatement deleteInboundMessageStmt;
    private PreparedStatement deleteOutboundMessageStmt;
    private PreparedStatement selectInboundMessagesStmt;
    private PreparedStatement selectOutboundMessagesStmt;
    
    // RMStore interface 
    
    public void init(Map<String, String> params) {
        connect(params);
    }
    
    public void createSourceSequence(RMSourceSequence seq) {
        try {
            beginTransaction();
            
            if (null == createSrcSequenceStmt) {
                createSrcSequenceStmt = connection.prepareStatement(CREATE_SRC_SEQUENCE_STMT_STR);
            }
            assert null != createSrcSequenceStmt;
            createSrcSequenceStmt.setString(1, seq.getIdentifier().getValue());
            Date expiry = seq.getExpiry();
            createSrcSequenceStmt.setLong(2, expiry == null ? 0 : expiry.getTime());
            Identifier osid = seq.getOfferingSequenceIdentifier();
            createSrcSequenceStmt.setString(3, osid == null ? null : osid.getValue());
            createSrcSequenceStmt.setString(4, seq.getEndpointIdentifier());
            createSrcSequenceStmt.execute();    
            
            commit();
            
        } catch (SQLException ex) {
            abort();
            throw new RMStoreException(ex);
        }
    }
    
    public void createDestinationSequence(RMDestinationSequence seq) {
        try {
            beginTransaction();
            
            if (null == createDestSequenceStmt) {
                createDestSequenceStmt = connection.prepareStatement(CREATE_DEST_SEQUENCE_STMT_STR);
            }
            createDestSequenceStmt.setString(1, seq.getIdentifier().getValue());
            createDestSequenceStmt.setString(2, seq.getAcksTo().getAddress().getValue());
            createDestSequenceStmt.setString(3, seq.getEndpointIdentifier());
            
            createDestSequenceStmt.execute();
            
            commit();
            
        } catch (SQLException ex) {
            abort();
            throw new RMStoreException(ex);
        }
    }  
    
    public void removeDestinationSequence(Identifier sid) {
        try {
            beginTransaction();
            
            if (null == deleteDestSequenceStmt) {
                deleteDestSequenceStmt = connection.prepareStatement(DELETE_DEST_SEQUENCE_STMT_STR);
            }
            deleteDestSequenceStmt.setString(1, sid.getValue());
            deleteDestSequenceStmt.execute();
            
            commit();
            
        } catch (SQLException ex) {
            abort();
            throw new RMStoreException(ex);
        }        
    }


    public void removeSourceSequence(Identifier sid) {
        try {
            beginTransaction();
            
            if (null == deleteSrcSequenceStmt) {
                deleteSrcSequenceStmt = connection.prepareStatement(DELETE_SRC_SEQUENCE_STMT_STR);
            }
            deleteSrcSequenceStmt.setString(1, sid.getValue());
            deleteSrcSequenceStmt.execute();
            
            commit();
            
        } catch (SQLException ex) {
            abort();
            throw new RMStoreException(ex);
        }        
    }


    public Collection<RMDestinationSequence> getDestinationSequences(String endpointIdentifier) {
        Collection<RMDestinationSequence> seqs = new ArrayList<RMDestinationSequence>();
        try {
            if (null == selectDestSequencesStmt) {
                selectDestSequencesStmt = 
                    connection.prepareStatement(SELECT_DEST_SEQUENCES_STMT_STR);               
            }
            selectDestSequencesStmt.setString(1, endpointIdentifier);
            
            ResultSet res = selectDestSequencesStmt.executeQuery(); 
            while (res.next()) {
                // do something
                Identifier sid = RMUtils.getWSRMFactory().createIdentifier();                
                sid.setValue(res.getString(1));
                EndpointReferenceType acksTo = RMUtils.createReference(res.getString(2));  
                BigDecimal lm = res.getBigDecimal(3);
                InputStream is = res.getBinaryStream(4);                
                SequenceAcknowledgement ack = RMUtils.getPersistenceUtils()
                    .getSequenceAcknowledgment(is);                
                DestinationSequence seq = new DestinationSequence(sid, acksTo, 
                                                                  lm == null ? null : lm.toBigInteger(), ack);
                seqs.add(seq);                                                 
            }
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, new Message("SELECT_DEST_SEQ_FAILED_MSG", LOG).toString(), ex);
        }
        return seqs;
    }

    public Collection<RMSourceSequence> getSourceSequences(String endpointIdentifier) {
        Collection<RMSourceSequence> seqs = new ArrayList<RMSourceSequence>();
        try {
            if (null == selectSrcSequencesStmt) {
                selectSrcSequencesStmt = 
                    connection.prepareStatement(SELECT_SRC_SEQUENCES_STMT_STR);     
            }
            selectSrcSequencesStmt.setString(1, endpointIdentifier);
            ResultSet res = selectSrcSequencesStmt.executeQuery();
            
            while (res.next()) {
                Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
                sid.setValue(res.getString(1));
                BigInteger cmn = res.getBigDecimal(2).toBigInteger();
                boolean lm = res.getBoolean(3);
                long lval = res.getLong(4);
                Date expiry = 0 == lval ? null : new Date(lval);
                String oidValue = res.getString(5);
                Identifier oi = null;
                if (null != oidValue) {
                    oi = RMUtils.getWSRMFactory().createIdentifier();
                    oi.setValue(oidValue);
                }                            
                SourceSequence seq = new SourceSequence(sid, expiry, oi, cmn, lm);
                seqs.add(seq);                          
            }
        } catch (SQLException ex) {
            // ignore
            LOG.log(Level.WARNING, new Message("SELECT_SRC_SEQ_FAILED_MSG", LOG).toString(), ex);
        }
        return seqs;
    }
        

    public Collection<RMMessage> getMessages(Identifier sid, boolean outbound) {
        Collection<RMMessage> msgs = new ArrayList<RMMessage>();
        try {
            PreparedStatement stmt = outbound ? selectOutboundMessagesStmt : selectInboundMessagesStmt;
            if (null == stmt) {
                stmt = connection.prepareStatement(MessageFormat.format(SELECT_MESSAGES_STMT_STR,
                    outbound ? OUTBOUND_MSGS_TABLE_NAME : INBOUND_MSGS_TABLE_NAME));
                if (outbound) {
                    selectOutboundMessagesStmt = stmt;                    
                } else {
                    selectInboundMessagesStmt = stmt;
                }
            }

            stmt.setString(1, sid.getValue());
            ResultSet res = stmt.executeQuery();
            while (res.next()) {
                BigInteger mn = res.getBigDecimal(1).toBigInteger();
                InputStream is = res.getBinaryStream(2);
                RMMessageImpl msg = new RMMessageImpl(mn, is);
                msgs.add(msg);                
            }            
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, new Message(outbound ? "SELECT_OUTBOUND_MSGS_FAILED_MSG"
                : "SELECT_INBOUND_MSGS_FAILED_MSG", LOG).toString(), ex);
        }        
        return msgs;
    }


    public void persistIncoming(RMDestinationSequence seq, RMMessage msg) {
        try {
            beginTransaction();
            
            updateDestinationSequence(seq);
            
            storeMessage(seq.getIdentifier(), msg, false);
            
            commit();
            
        } catch (SQLException ex) {
            abort();
            throw new RMStoreException(ex);
        } catch (IOException ex) {
            abort();
            throw new RMStoreException(ex);        
        }        
    }

    public void persistOutgoing(RMSourceSequence seq, RMMessage msg) {
        try {
            beginTransaction();
            
            updateSourceSequence(seq);
            
            storeMessage(seq.getIdentifier(), msg, true);
            
            commit();
            
        } catch (SQLException ex) {
            abort();
            throw new RMStoreException(ex);
        } catch (IOException ex) {
            abort();
            throw new RMStoreException(ex);        
        }        
        
    }

    public void removeMessages(Identifier sid, Collection<BigInteger> messageNrs, boolean outbound) {
        try {
            beginTransaction();
            PreparedStatement stmt = outbound ? deleteOutboundMessageStmt : deleteInboundMessageStmt;
            if (null == stmt) {
                stmt = connection.prepareStatement(MessageFormat.format(DELETE_MESSAGE_STMT_STR,
                    outbound ? OUTBOUND_MSGS_TABLE_NAME : INBOUND_MSGS_TABLE_NAME));
                if (outbound) {
                    deleteOutboundMessageStmt = stmt;                    
                } else {
                    deleteInboundMessageStmt = stmt;
                }
            }

            stmt.setString(1, sid.getValue());
                        
            for (BigInteger messageNr : messageNrs) {
                stmt.setBigDecimal(2, new BigDecimal(messageNr));
                stmt.execute();
            }
            
            commit();
            
        } catch (SQLException ex) {
            abort();
            throw new RMStoreException(ex);
        }        
    }   

    // transaction demarcation
    
    protected void beginTransaction() {
        // no-op 
    }
    
    protected void commit() throws SQLException {
        connection.commit();
    }
    
    protected void abort() {
        try {
            connection.rollback(); 
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, new Message("ABORT_FAILED_MSG", LOG).toString(), ex);
        }     
    }
    
    // helpers
    
    protected void storeMessage(Identifier sid, RMMessage msg, boolean outbound) 
        throws IOException, SQLException {
        PreparedStatement stmt = outbound ? createOutboundMessageStmt : createInboundMessageStmt;
        if (null == stmt) {
            stmt = connection.prepareStatement(MessageFormat.format(CREATE_MESSAGE_STMT_STR,
                outbound ? OUTBOUND_MSGS_TABLE_NAME : INBOUND_MSGS_TABLE_NAME));
            if (outbound) {
                createOutboundMessageStmt = stmt;                    
            } else {
                createInboundMessageStmt = stmt;
            }
        }
        
        
       
        int i = 1;
        stmt.setString(i++, sid.getValue());
        stmt.setBigDecimal(i++, new BigDecimal(msg.getMessageNr())); 
        InputStream is = msg.getContextAsStream();
        stmt.setBinaryStream(i++, is, is.available()); 
        stmt.execute();
    }
    
    protected void updateSourceSequence(RMSourceSequence seq) 
        throws SQLException {
        if (null == updateSrcSequenceStmt) {
            updateSrcSequenceStmt = connection.prepareStatement(UPDATE_SRC_SEQUENCE_STMT_STR);
        }
        updateSrcSequenceStmt.setBigDecimal(1, new BigDecimal(seq.getCurrentMessageNr())); 
        updateSrcSequenceStmt.setBoolean(2, seq.isLastMessage()); 
        updateSrcSequenceStmt.setString(3, seq.getIdentifier().getValue());
        updateSrcSequenceStmt.execute();
    }
    
    protected void updateDestinationSequence(RMDestinationSequence seq) 
        throws SQLException, IOException {
        if (null == updateDestSequenceStmt) {
            updateDestSequenceStmt = connection.prepareStatement(UPDATE_DEST_SEQUENCE_STMT_STR);
        }
        BigInteger lastMessageNr = seq.getLastMessageNr();
        updateDestSequenceStmt.setBigDecimal(1, lastMessageNr == null ? null
            : new BigDecimal(lastMessageNr)); 
        InputStream is = seq.getAcknowledgmentAsStream();
        updateDestSequenceStmt.setBinaryStream(2, is, is.available()); 
        updateDestSequenceStmt.setString(3, seq.getIdentifier() .getValue());
        updateDestSequenceStmt.execute();
    }
    
    protected void createTables() throws SQLException {
        
        Statement stmt = null;
        
        stmt = connection.createStatement();
        try {
            stmt.executeUpdate(CREATE_SRC_SEQUENCES_TABLE_STMT);
        } catch (SQLException ex) {
            if (!"X0Y32".equals(ex.getSQLState())) {
                throw ex;
            } else {
                LOG.fine("Table CELTIX_RM_SRC_SEQUENCES already exists.");
            }
        }
        stmt.close();
        
        stmt = connection.createStatement();
        try {
            stmt.executeUpdate(CREATE_DEST_SEQUENCES_TABLE_STMT);
        } catch (SQLException ex) {
            if (!"X0Y32".equals(ex.getSQLState())) {
                throw ex;
            } else {
                LOG.fine("Table CELTIX_RM_DEST_SEQUENCES already exists.");
            }
        }
        stmt.close();
        
        for (String tableName : new String[] {OUTBOUND_MSGS_TABLE_NAME, INBOUND_MSGS_TABLE_NAME}) {
            stmt = connection.createStatement();
            try {
                stmt.executeUpdate(MessageFormat.format(CREATE_MESSAGES_TABLE_STMT, tableName));
            } catch (SQLException ex) {
                if (!"X0Y32".equals(ex.getSQLState())) {
                    throw ex;
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Table " + tableName + " already exists.");
                    }
                }
            }
            stmt.close();
        }
    }
    
    synchronized void connect(Map<String, String> params) {
        
        if (null == connectionMap) {
            connectionMap = new HashMap<String, Connection>();
        }
        String url = params.get(CONNECTION_URL_PROPERTY);
        assert null != url;
        connection = connectionMap.get(url);
        if (null != connection) {
            return;
        }
        
        String driverClassName = params.get(DRIVER_CLASS_NAME_PROPERTY);
        assert null != driverClassName;
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new RMStoreException(ex);
        }
             
        assert null != params.get(USER_NAME_PROPERTY);
        
        try {
            connection = DriverManager.getConnection(url, 
                params.get(USER_NAME_PROPERTY), params.get(PASSWORD_PROPERTY));
            connection.setAutoCommit(false);
            createTables();
            
        } catch (SQLException ex) {
            throw new RMStoreException(ex);
        }   
        
        connectionMap.put(url, connection);
        assert connection == connectionMap.get(url);
    }   
    
    /**
     * Accessor for connection - used in tests only.
     * @return the connection
     */
    Connection getConnection() {
        return connection;
    }
}
