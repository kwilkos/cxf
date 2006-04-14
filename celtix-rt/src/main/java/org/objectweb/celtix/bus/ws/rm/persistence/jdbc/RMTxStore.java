package org.objectweb.celtix.bus.ws.rm.persistence.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.bus.ws.rm.persistence.RMStoreException;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.rm.Identifier;
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
        = "INSERT INTO CELTIX_RM_MESSAGES VALUES(?, ?, ?)";
    private static final String DELETE_MESSAGE_STMT_STR =
        "DELETE FROM CELTIX_RM_MESSAGES WHERE SEQ_ID = ? AND MSG_NO = ?";
    
    private static final Logger LOG = LogUtils.getL7dLogger(RMTxStore.class);
    
    private Connection connection;
    private PreparedStatement createDestSequenceStmt;
    private PreparedStatement createSrcSequenceStmt;
    private PreparedStatement deleteDestSequenceStmt;
    private PreparedStatement deleteSrcSequenceStmt;
    private PreparedStatement updateDestSequenceStmt;
    private PreparedStatement updateSrcSequenceStmt;
    
    private PreparedStatement createMessageStmt;
    private PreparedStatement deleteMessageStmt;
    
    // RMStore interface 
    
    public void init(Map<String, String> params) {
        String driverClassName = params.get(DRIVER_CLASS_NAME_PROPERTY);
        assert null != driverClassName;
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new RMStoreException(ex);
        }
        
        String url = params.get(CONNECTION_URL_PROPERTY);
        assert null != url;
        assert null != params.get(USER_NAME_PROPERTY);
        
        try {
            connection = DriverManager.getConnection(url,
                                    params.get(USER_NAME_PROPERTY), params.get(PASSWORD_PROPERTY));
            connection.setAutoCommit(false);
            
            createTables();
            
        } catch (SQLException ex) {
            throw new RMStoreException(ex);
        }      
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
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<RMMessage> getInboundMessages(Identifier sid) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<RMMessage> getOutboundMessages(Identifier sid) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<RMSourceSequence> getSourceSequences(String endpointIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

    public void persistIncoming(RMDestinationSequence seq, RMMessage msg) {
        try {
            beginTransaction();
            
            updateDestinationSequence(seq);
            
            storeMessage(seq.getIdentifier(), msg);
            
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
            
            storeMessage(seq.getIdentifier(), msg);
            
            commit();
            
        } catch (SQLException ex) {
            abort();
            throw new RMStoreException(ex);
        } catch (IOException ex) {
            abort();
            throw new RMStoreException(ex);        
        }        
        
    }

    public void removeMessages(Identifier sid, Collection<BigInteger> messageNrs) {
        try {
            beginTransaction();
            
            if (null == deleteMessageStmt) {
                deleteMessageStmt = connection.prepareStatement(DELETE_MESSAGE_STMT_STR);
            }
            
            deleteMessageStmt.setString(1, sid.getValue());
            
            
            for (BigInteger messageNr : messageNrs) {
                deleteMessageStmt.setBigDecimal(2, new BigDecimal(messageNr));
                deleteMessageStmt.execute();
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
    
    protected void storeMessage(Identifier sid, RMMessage msg) throws IOException, SQLException {
        if (null == createMessageStmt) {
            createMessageStmt = connection.prepareStatement(CREATE_MESSAGE_STMT_STR);
        }
        createMessageStmt.setString(1, sid.getValue());
        createMessageStmt.setBigDecimal(2, new BigDecimal(msg.getMessageNr())); 
        InputStream is = msg.getContextAsStream();
        createMessageStmt.setBinaryStream(3, is, is.available()); 
        createMessageStmt.execute();
    }
    
    protected void updateSourceSequence(RMSourceSequence seq) 
        throws SQLException {
        if (null == updateSrcSequenceStmt) {
            updateSrcSequenceStmt = connection.prepareStatement(UPDATE_SRC_SEQUENCE_STMT_STR);
        }
        updateSrcSequenceStmt.setBigDecimal(1, new BigDecimal(seq.getCurrentMessageNr())); 
        updateSrcSequenceStmt.setBoolean(2, seq.getLastMessage()); 
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
        String createStr = null;
        
        createStr = "CREATE TABLE CELTIX_RM_SRC_SEQUENCES " 
            + "(SEQ_ID VARCHAR(256) NOT NULL, "
            + "CUR_MSG_NO DECIMAL(31, 0) NOT NULL DEFAULT 1, "
            + "LAST_MSG CHAR(1), "
            + "EXPIRY BIGINT, " 
            + "OFFERING_SEQ_ID VARCHAR(256), "
            + "ENDPOINT_ID VARCHAR(1024), "            
            + "PRIMARY KEY (SEQ_ID))";
        
        stmt = connection.createStatement();
        try {
            stmt.executeUpdate(createStr);
        } catch (SQLException ex) {
            if (!"X0Y32".equals(ex.getSQLState())) {
                throw ex;
            } else {
                LOG.fine("Table CELTIX_RM_SRC_SEQUENCES already exists.");
            }
        }
        stmt.close();
        
        createStr = "CREATE TABLE CELTIX_RM_DEST_SEQUENCES " 
            + "(SEQ_ID VARCHAR(256) NOT NULL, "
            + "ACKS_TO VARCHAR(1024) NOT NULL, "
            + "LAST_MSG_NO DECIMAL(31, 0), "
            + "ENDPOINT_ID VARCHAR(1024), "
            + "ACKNOWLEDGED BLOB, "
            + "PRIMARY KEY (SEQ_ID))";
        
        stmt = connection.createStatement();
        try {
            stmt.executeUpdate(createStr);
        } catch (SQLException ex) {
            if (!"X0Y32".equals(ex.getSQLState())) {
                throw ex;
            } else {
                LOG.fine("Table CELTIX_RM_DEST_SEQUENCES already exists.");
            }
        }
        stmt.close();
        
        createStr = "CREATE TABLE CELTIX_RM_MESSAGES " 
            + "(SEQ_ID VARCHAR(256) NOT NULL, "
            + "MSG_NO DECIMAL(31, 0) NOT NULL, "
            + "CONTEXT BLOB, "
            + "PRIMARY KEY (SEQ_ID, MSG_NO))";
        
        stmt = connection.createStatement();
        try {
            stmt.executeUpdate(createStr);
        } catch (SQLException ex) {
            if (!"X0Y32".equals(ex.getSQLState())) {
                throw ex;
            } else {
                LOG.fine("Table CELTIX_RM_MESSAGES already exists.");
            }
        }
        stmt.close();
    }

    
}
