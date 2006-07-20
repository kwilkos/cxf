package org.objectweb.celtix.service.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;


public abstract class AbstractMessageContainer extends AbstractPropertiesHolder {
    private OperationInfo operation;
    private QName mName;
    private Map<QName, MessagePartInfo> messageParts 
        = new LinkedHashMap<QName, MessagePartInfo>(4);
    
    /**
     * Initializes a new instance of the <code>MessagePartContainer</code>.
     * @param operation the operation.
     * @param nm 
     */
    AbstractMessageContainer(OperationInfo op, QName nm) {
        operation = op;
        mName = nm;
    }

    public QName getName() {
        return mName;
    }
    
    
    /**
     * Returns the operation of this container.
     *
     * @return the operation.
     */
    public OperationInfo getOperation() {
        return operation;
    }

    /**
     * Adds an message part to this conainer.
     *
     * @param name  the qualified name of the message part.
     * @param clazz the type of the message part.
     */
    public MessagePartInfo addMessagePart(QName name) {
        if (name == null) {
            throw new IllegalArgumentException("Invalid name [" + name + "]");
        }

        MessagePartInfo part = new MessagePartInfo(name, this);
        addMessagePart(part);
        return part;
    }
    public MessagePartInfo addMessagePart(String name) {
        return addMessagePart(new QName(this.getOperation().getInterface().getService().getTargetNamespace(),
                                        name));
    }    
    /**
     * Adds an message part to this container.
     *
     * @param part the message part.
     */
    public void addMessagePart(MessagePartInfo part) {
        messageParts.put(part.getName(), part);
    }

    public int getMessagePartIndex(MessagePartInfo part) {
        int idx = 0;
        for (MessagePartInfo p : messageParts.values()) {
            if (part == p) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    /**
     * Removes an message part from this container.
     *
     * @param name the qualified message part name.
     */
    public void removeMessagePart(QName name) {
        MessagePartInfo messagePart = getMessagePart(name);
        if (messagePart != null) {
            messageParts.remove(name);
        }
    }
    
    /**
     * Returns the message part with the given name, if found.
     *
     * @param name the qualified name.
     * @return the message part; or <code>null</code> if not found.
     */
    public MessagePartInfo getMessagePart(QName name) {
        return messageParts.get(name);
    }
    
    /**
     * Returns all message parts for this message.
     *
     * @return all message parts.
     */
    public List<MessagePartInfo> getMessageParts() {
        return Collections.unmodifiableList(new ArrayList<MessagePartInfo>(messageParts.values()));
    }
    
    public int size() {
        return messageParts.size();
    }
    

}
