package org.objectweb.celtix.servicemodel;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

/**
 * 
 */
public final class OperationInfo extends AbstractPropertiesHolder {
    
    final BindingInfo service;
    String opName;
    
    MessageInfo inputMessage;
    MessageInfo outputMessage;
    Map<String, FaultInfo> faults;
    
    boolean rpc;
    String action = "";

    DataReaderFactory readerFactory;
    DataWriterFactory writerFactory;
    Invoker invoker;
    
    OperationInfo(BindingInfo serv, String n) { 
        service = serv;
        setName(n);
    }
    
    /**
     * Returns the name of the Operation.
     * @return the name of the Operation
     */
    public String getName() {
        return opName;
    }
    /**
     * Sets the name of the operation.
     * @param name the new name of the operation
     */
    public void setName(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Invalid name [" + name + "]");
        }        
        opName = name;
    }
    

    public boolean isRPC() {
        return rpc;
    }
    public void setRPC(boolean b) {
        rpc = b;
    }
    

    public String getSOAPAction() {
        return action;
    }
    public void setSOAPAction(String s) {
        action = s;
    }
    
    
    public MessageInfo createMessage(QName nm) {
        return new MessageInfo(this, nm);
    }

    public MessageInfo getOutput() {
        return outputMessage;
    }
    public void setOutput(MessageInfo out) {
        outputMessage = out;
    }    
    public boolean hasOutput() {
        return outputMessage != null;
    }

    public MessageInfo getInput() {
        return inputMessage;
    }
    public void setInput(MessageInfo in) {
        inputMessage = in;
    }
    public boolean hasInput() {
        return inputMessage != null;
    }
    
    public boolean isOneWay() {
        return inputMessage != null && outputMessage == null;
    }
    
    
    /**
     * Adds an fault to this operation.
     *
     * @param name the fault name.
     */
    public FaultInfo addFault(String name, QName message) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Invalid name [" + name + "]");
        }
        if (faults != null && faults.containsKey(name)) {
            throw new IllegalArgumentException("A fault with name [" + name
                                               + "] already exists in this operation");
        }
        FaultInfo fault = new FaultInfo(name, message, this);
        addFault(fault);
        return fault;
    }

    /**
     * Adds a fault to this operation.
     *
     * @param fault the fault.
     */
    synchronized void addFault(FaultInfo fault) {
        if (faults == null) { 
            faults = new ConcurrentHashMap<String, FaultInfo>(4);
        }
        faults.put(fault.getFaultName(), fault);
    }

    /**
     * Removes a fault from this operation.
     *
     * @param name the qualified fault name.
     */
    public void removeFault(String name) {
        if (faults != null) {
            faults.remove(name);
        }
    }

    /**
     * Returns the fault with the given name, if found.
     *
     * @param name the name.
     * @return the fault; or <code>null</code> if not found.
     */
    public FaultInfo getFault(String name) {
        if (faults != null) {
            return faults.get(name);
        }
        return null;
    }

    /**
     * Returns all faults for this operation.
     *
     * @return all faults.
     */
    public Collection<FaultInfo> getFaults() {
        if (faults == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(faults.values());
    }
    
    
    public Invoker getInvoker() {
        if (invoker == null) {
            return service.getDefaultInvoker();
        }
        return invoker;
    }
    public void setInvoker(Invoker i) {
        invoker = i;
    }
    
    public DataReaderFactory getReaderFactory() {
        if (readerFactory == null) {
            return service.getDefaultReaderFactory();
        }
        return readerFactory;
    }
    public void setReaderFactory(DataReaderFactory rf) {
        readerFactory = rf;
    }
    public DataWriterFactory getWriterFactory() {
        if (writerFactory == null) {
            return service.getDefaultWriterFactory();
        }
        return writerFactory;
    }
    public void setWriterFactory(DataWriterFactory wf) {
        writerFactory = wf;
    }

}
