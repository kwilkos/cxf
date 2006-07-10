package org.objectweb.celtix.servicemodel;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Service extends AbstractPropertiesHolder {
    
    Invoker invoker;
    Map<String, OperationInfo> operations = new ConcurrentHashMap<String, OperationInfo>(4);
    String targetNamespace;
    boolean rpc;
    DataReaderFactory readerFactory;
    DataWriterFactory writerFactory;
    
    
    public String getTargetNamespace() {
        return targetNamespace;
    }
    public void setTargetNamespace(String ns) {
        targetNamespace = ns;
    }

    public boolean isDefaultRPC() {
        return rpc;
    }
    public void setDefaultRPC(boolean b) {
        rpc = b;
    }
    
    
    /**
     * Adds an operation to this service.
     *
     * @param name the qualified name of the operation.
     * @return the operation.
     */
    public OperationInfo addOperation(String name) {
        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException("Invalid name [" + name + "]");
        }
        if (operations.containsKey(name)) {
            throw new IllegalArgumentException("An operation with name [" + name
                                               + "] already exists in this service");
        }

        OperationInfo operation = new OperationInfo(this, name);
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
    public OperationInfo getOperation(String name) {
        return operations.get(name);
    }

    /**
     * Returns all operations for this service.
     *
     * @return all operations.
     */
    public Collection<OperationInfo> getOperations() {
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


