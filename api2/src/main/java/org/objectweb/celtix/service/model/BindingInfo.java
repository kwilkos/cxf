package org.objectweb.celtix.service.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;

public class BindingInfo extends AbstractPropertiesHolder {
    
    private static final Logger LOG = LogUtils.getL7dLogger(BindingInfo.class);
    
    QName name;
    ServiceInfo service;
    final String namespaceURI;
    
    Map<QName, BindingOperationInfo> operations = new ConcurrentHashMap<QName, BindingOperationInfo>(4);
    
    public BindingInfo(ServiceInfo serv, String n) {
        service = serv;
        namespaceURI = n;
    }
    
    public InterfaceInfo getInterface() {
        return service.getInterface();
    }

    public ServiceInfo getService() {
        return service;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    public void setName(QName n) {
        name = n;
    }
    public QName getName() {
        return name;
    }
    
    private boolean nameEquals(String a, String b) {
        if (a == null) {
            // in case of input/output itself is empty
            return true;
        } else {
            return "".equals(a) ? "".equals(b) : a.equals(b);
        }
    }
    public BindingOperationInfo buildOperation(QName opName, String inName, String outName) {
        for (OperationInfo op : getInterface().getOperations()) {
            if (opName.equals(op.getName())
                && nameEquals(inName, op.getInputName())
                && nameEquals(outName, op.getOutputName())) {
                
                return new BindingOperationInfo(this, op);
            }
        }
        return null;
    }

    /**
     * Adds an operation to this service.
     *
     * @param operation the operation.
     */
    public void addOperation(BindingOperationInfo operation) {
        if (operation.getName() == null) {
            throw new NullPointerException(
                new Message("BINDING.OPERATION.NAME.NOT.NULL", LOG).toString());
        } 
        if (operations.containsKey(operation.getName())) {
            throw new IllegalArgumentException(
                new Message("DUPLICATED.OPERATION.NAME", LOG, new Object[]{operation.getName()}).toString());
        }
        
        operations.put(operation.getName(), operation);
    }

    /**
     * Returns the operation info with the given name, if found.
     *
     * @param name the name.
     * @return the operation; or <code>null</code> if not found.
     */
    public BindingOperationInfo getOperation(QName oname) {
        return operations.get(oname);
    }

    /**
     * Returns all operations for this service.
     *
     * @return all operations.
     */
    public Collection<BindingOperationInfo> getOperations() {
        return Collections.unmodifiableCollection(operations.values());
    }
}


