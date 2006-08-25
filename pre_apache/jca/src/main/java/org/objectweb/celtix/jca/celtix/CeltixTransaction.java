package org.objectweb.celtix.jca.celtix;

import javax.resource.ResourceException;

public interface CeltixTransaction {

    boolean isActive();
    void clearThreadAssociation() throws ResourceException;
    void makeThreadAssociation() throws ResourceException;
}
