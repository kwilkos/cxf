package org.apache.cxf.service.model;

import javax.xml.namespace.QName;

public final class MessagePartInfo extends AbstractPropertiesHolder {
    private QName pname;
    private AbstractMessageContainer mInfo;
    
    private boolean isElement;
    private QName typeName;

    MessagePartInfo(QName n, AbstractMessageContainer info) {
        mInfo = info;
        pname = n;
    }

    /**
     * @return Returns the name.
     */
    public QName getName() {
        return pname;
    }
    /**
     * @param name The name to set.
     */
    public void setName(QName n) {
        pname = n;
    }
    
    public boolean isElement() { 
        return isElement;
    }
    public void setIsElement(boolean b) {
        isElement = b;
    }
    
    public QName getElementQName() {
        if (isElement) {
            return typeName; 
        }
        return null;
    }
    public QName getTypeQName() {
        if (!isElement) {
            return typeName; 
        }
        return null;
    }
    public void setTypeQName(QName qn) {
        isElement = false;
        typeName = qn;
    }
    public void setElementQName(QName qn) {
        isElement = true;
        typeName = qn;
    }
    
    public AbstractMessageContainer getMessageInfo() {
        return mInfo;
    }

}
