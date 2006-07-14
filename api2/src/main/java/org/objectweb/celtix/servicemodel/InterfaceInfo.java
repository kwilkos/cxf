package org.objectweb.celtix.servicemodel;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

public class InterfaceInfo extends AbstractPropertiesHolder {
    QName name;
    ServiceInfo service;
    
    Map<String, OperationInfo> operations = new ConcurrentHashMap<String, OperationInfo>(4);
    
    
    public InterfaceInfo(ServiceInfo info, QName q) {
        name = q;
        service = info;
    }
    
    public ServiceInfo getService() {
        return service;
    }
    
    public void setName(QName n) {
        name = n;
    }
    public QName getName() {
        return name;
    }
    
    
    /**
     * Adds an operation to this service.
     *
     * @param name the qualified name of the operation.
     * @return the operation.
     */
    public OperationInfo addOperation(String oname) {
        if ((oname == null) || (oname.length() == 0)) {
            throw new IllegalArgumentException("Invalid name [" + oname + "]");
        }
        if (operations.containsKey(oname)) {
            throw new IllegalArgumentException("An operation with name [" + oname
                                               + "] already exists in this service");
        }

        OperationInfo operation = new OperationInfo(this, oname);
        addOperation(operation);
        return operation;
    }

    /**
     * Adds an operation to this service.
     *
     * @param operation the operation.
     */
    void addOperation(OperationInfo operation) {
        operations.put(operation.getName(), operation);
    }

    /**
     * Returns the operation info with the given name, if found.
     *
     * @param name the name.
     * @return the operation; or <code>null</code> if not found.
     */
    public OperationInfo getOperation(String oname) {
        return operations.get(oname);
    }

    /**
     * Returns all operations for this service.
     *
     * @return all operations.
     */
    public Collection<OperationInfo> getOperations() {
        return Collections.unmodifiableCollection(operations.values());
    }   

}
