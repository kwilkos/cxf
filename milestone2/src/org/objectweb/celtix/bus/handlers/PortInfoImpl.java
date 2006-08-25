package org.objectweb.celtix.bus.handlers;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.PortInfo;

public class PortInfoImpl implements PortInfo {

    private final QName portName;
    private final QName serviceName;
    private final String bindingId; 
    
    public PortInfoImpl(QName sname, QName pname, String bid) {
        checkNullArgument(sname, "service name");
        checkNullArgument(pname, "port name");
        portName = pname;
        serviceName = sname;
        bindingId = bid;
    }
    
    
    public QName getServiceName() {
        return serviceName;
    }

    public QName getPortName() {
        return portName; 
    }

    public String getBindingID() {
        return bindingId;
    }

    public boolean equals(Object o) { 
        if (o == this) {
            return true;
        }
        
        if (o == null || !(o instanceof PortInfoImpl)) {
            return false;            
        }        
        PortInfoImpl rhs = (PortInfoImpl)o;
        
        boolean bindingIdsEqual = false; 
        if (bindingId == null && rhs.getBindingID() == null) {
            bindingIdsEqual = true;
        } else if (bindingId == null && rhs.getBindingID() != null) {
            bindingIdsEqual = false;
        } else { 
            bindingIdsEqual = bindingId.equals(rhs.getBindingID());
        }
        return serviceName.equals(rhs.getServiceName()) && portName.equals(rhs.getPortName())
                                  && bindingIdsEqual;
    }
    
    public int hashCode() {
        
        int ret = 17;
        ret = 37 * ret + serviceName.hashCode();
        ret = 37 * ret + portName.hashCode();
        if (bindingId != null) {
            ret = 37 * ret + bindingId.hashCode();
        }
        return ret;
    }
    
    private void checkNullArgument(Object arg, String name) { 
        if (arg == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }
    
}
