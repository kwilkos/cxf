package org.objectweb.celtix.jca.celtix;


import javax.security.auth.Subject;

import org.objectweb.celtix.Bus;

public interface CeltixInvocationHandlerData  {

    // need to rewrite
    void setTarget(Object t); 
    Object getTarget(); 
    
    void setBus(Bus bus); 
    Bus getBus(); 
    
    void setManagedConnection(CeltixManagedConnection managedConnection); 
    CeltixManagedConnection getManagedConnection(); 

    void setSubject(Subject subject);
    Subject getSubject(); 
}
