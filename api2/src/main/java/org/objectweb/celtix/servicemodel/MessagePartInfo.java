package org.objectweb.celtix.servicemodel;

import javax.xml.namespace.QName;

public final class MessagePartInfo extends AbstractPropertiesHolder {
    private QName name;
    private String pname;
    private AbstractMessageContainer mInfo;
    private boolean header;
    private boolean inOut;

    MessagePartInfo(QName n, AbstractMessageContainer info) {
        mInfo = info;
        name = n;
    }

    /**
     * @return Returns the name.
     */
    public QName getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(QName n) {
        name = n;
    }
    
    public String getPartName() {
        return pname;
    }
    public void setPartName(String n) {
        pname = n;
    }

    public AbstractMessageContainer getMessageInfo() {
        return mInfo;
    }
    
    public boolean isHeader() {
        return header;
    }
    public void setHeader(boolean b) {
        header = b;
    }

    public boolean isInOut() {
        return inOut;
    }
    
    public void setInOut(boolean b) {
        inOut = b;
    }
}
