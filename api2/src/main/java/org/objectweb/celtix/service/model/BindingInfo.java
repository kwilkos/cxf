package org.objectweb.celtix.service.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.objectweb.celtix.databinding.DataReaderFactory;
import org.objectweb.celtix.databinding.DataWriterFactory;

public class BindingInfo extends AbstractPropertiesHolder {
    QName name;
    ServiceInfo service;
    
    Map<String, BindingOperationInfo> operations = new ConcurrentHashMap<String, BindingOperationInfo>(4);
    
    Invoker invoker;
    DataReaderFactory readerFactory;
    DataWriterFactory writerFactory;
    
    
    public BindingInfo(ServiceInfo serv) {
        service = serv;
    }
    
    public InterfaceInfo getInterface() {
        return service.getInterface();
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
    
    
    private boolean nameEquals(String a, String b) {
        return "".equals(a) ? "".equals(b) : a.equals(b);
    }
    public BindingOperationInfo buildOperation(String opName, String inName, String outName) {
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
        if ((operation.getName() == null) || (operation.getName().length() == 0)) {
            throw new IllegalArgumentException("Invalid name [" + operation.getName() + "]");
        }
        if (operations.containsKey(operation.getName())) {
            throw new IllegalArgumentException("An operation with name [" + operation.getName()
                                               + "] already exists in this service");
        }
        operations.put(operation.getName(), operation);
    }

    /**
     * Returns the operation info with the given name, if found.
     *
     * @param name the name.
     * @return the operation; or <code>null</code> if not found.
     */
    public BindingOperationInfo getOperation(String oname) {
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
    
    
    

    public Invoker getDefaultInvoker() {
        return invoker;
    }
    
    public void setDefaultInvoker(Invoker i) {
        invoker = i;
    }
    
    
    public DataReaderFactory getDefaultReaderFactory() {
        return readerFactory;
    }
    public void setDefaultReaderFactory(DataReaderFactory rf) {
        readerFactory = rf;
    }
    public DataWriterFactory getDefaultWriterFactory() {
        return writerFactory;
    }
    public void setDefaultWriterFactory(DataWriterFactory wf) {
        writerFactory = wf;
    }
    
}


