package org.objectweb.celtix.service.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;

public class InterfaceInfo extends AbstractPropertiesHolder {
    private static final Logger LOG = LogUtils.getL7dLogger(InterfaceInfo.class);
    
    QName name;
    ServiceInfo service;
    
    Map<QName, OperationInfo> operations = new ConcurrentHashMap<QName, OperationInfo>(4);
    
    
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
    public OperationInfo addOperation(QName oname) {
        if (oname == null) {
            throw new NullPointerException(
                new Message("OPERATION.NAME.NOT.NULL", LOG).toString());
        } 
        if (operations.containsKey(oname)) {
            throw new IllegalArgumentException(
                new Message("DUPLICATED.OPERATION.NAME", LOG, new Object[]{oname}).toString());
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
    public OperationInfo getOperation(QName oname) {
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
