package org.objectweb.celtix.jca.celtix;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;

public interface CeltixManagedConnection extends ManagedConnection {

    void close(Object handle) throws ResourceException; 
    CeltixManagedConnectionFactory getManagedConnectionFactory(); 
    CeltixTransaction getCeltixTransaction(); 
}
