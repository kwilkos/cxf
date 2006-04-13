package org.objectweb.celtix.bus.ws.rm.persistence.jdbc;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.objectweb.celtix.bus.ws.rm.persistence.RMStoreException;
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
    
    protected Connection connection;
    
    // RMStore interface 
    
    public void init(Map<String, String> params) {
        String driverClassName = params.get(DRIVER_CLASS_NAME_PROPERTY);
        assert null != driverClassName;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
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
    

    public void createSequence(RMSourceSequence seq) {
        // TODO Auto-generated method stub
        
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

    public void persistIncoming(Identifier destSequenceId, boolean lastMessage, RMMessage msg) {
        // TODO Auto-generated method stub
        
    }

    public void persistOutgoing(Identifier srcSequenceId, boolean lastMessage, RMMessage msg) {
        // TODO Auto-generated method stub
        
    }

    public void removeMessage(Identifier sid, Collection<BigInteger> messageNr) {
        // TODO Auto-generated method stub
        
    }

    public void removeSequence(Identifier seq) {
        // TODO Auto-generated method stub
        
    }
    

    // transaction demarcation
    
    protected void beginTransaction() {
        // no-op 
    }
    
    protected void commit() throws SQLException {
        connection.commit();
    }
    
    protected void abort() throws SQLException {
        connection.rollback();
    }
    
    // helpers
    
    protected void updateSourceSequence(Identifier sid, BigInteger currentMesssageNr, boolean lastMessage) {
        
    }
    
    protected void updateDestinationSequence(Identifier sid, BigInteger lastMessage) {
    }
    
    protected void createTables() {
        
    }
    
    
}
