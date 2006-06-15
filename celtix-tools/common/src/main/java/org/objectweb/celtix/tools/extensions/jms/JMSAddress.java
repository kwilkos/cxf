package org.objectweb.celtix.tools.extensions.jms;

import java.io.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import org.w3c.dom.*;

public class JMSAddress implements ExtensibilityElement, Serializable {
    // private String address;
    private String address;
    private Element element;
    private boolean required;
    private QName elementType;
    private String documentBaseURI;

    // attributes used by jms:address element
    private String destinationStyle = "queue";
    private String initialContextFactory = "org.activemq.jndi.ActiveMQInitialContextFactory";
    private String jndiConnectionFactoryName = "ConnectionFactory";
    private String messageType = "text";

    private String jndiProviderURL;
    private String jndiDestinationName;
    private String durableSubscriberName;
    private boolean useMessageIDAsCorrelationID = true;

    public void setDocumentBaseURI(String baseURI) {
        this.documentBaseURI = baseURI;
    }

    public String getDocumentBaseURI() {
        return this.documentBaseURI;
    }

    public void setElement(Element elem) {
        this.element = elem;
    }

    public Element getElement() {
        return element;
    }

    public void setRequired(Boolean r) {
        this.required = r;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setElementType(QName elemType) {
        this.elementType = elemType;
    }

    public QName getElementType() {
        return elementType;
    }

    public void setJndiProviderURL(String url) {
        this.jndiProviderURL = url;
    }

    public String getJndiProviderURL() {
        return this.jndiProviderURL;
    }

    public String getDestinationStyle() {
        return destinationStyle;
    }

    public String getDurableSubscriberName() {
        return durableSubscriberName;
    }

    public void setDurableSubscriberName(String newDurableSubscriberName) {
        durableSubscriberName = newDurableSubscriberName;
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public void setInitialContextFactory(String newInitContextFactory) {
        initialContextFactory = newInitContextFactory;
    }

    public String getJndiConnectionFactoryName() {
        return jndiConnectionFactoryName;
    }

    public void setJndiConnectionFactoryName(String newJndiConnectionFactoryName) {
        jndiConnectionFactoryName = newJndiConnectionFactoryName;
    }

    public String getJndiDestinationName() {
        return jndiDestinationName;
    }

    public void setJndiDestinationName(String newJndiDestinationName) {
        jndiDestinationName = newJndiDestinationName;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String newMessageType) {
        messageType = newMessageType;
    }

    public boolean isUseMessageIDAsCorrelationID() {
        return useMessageIDAsCorrelationID;
    }

    public void setUseMessageIDAsCorrelationID(boolean newUseMessageIDAsCorrelationID) {
        useMessageIDAsCorrelationID = newUseMessageIDAsCorrelationID;
    }

    public void setDestinationStyle(String newDestinationStyle) {
        destinationStyle = newDestinationStyle;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String newAddress) {
        address = newAddress;
    }

    public String getAttrXMLString() {
        StringBuffer sb = new StringBuffer(300);
        if (destinationStyle != null) {
            sb.append("destinationStyle=\"" + destinationStyle + "\" ");
        }
        if (initialContextFactory != null) {
            sb.append("initialContextFactory=\"" + initialContextFactory + "\" ");
        }
        if (jndiConnectionFactoryName != null) {
            sb.append("jndiConnectionFactoryName=\"" + jndiConnectionFactoryName + "\" ");
        }
        if (messageType != null) {
            sb.append("messageType=\"" + messageType + "\" ");
        }
        if (jndiProviderURL != null) {
            sb.append("jndiProviderURL=\"" + jndiProviderURL + "\" ");
        }
        if (jndiDestinationName != null) {
            sb.append("jndiDestinationName=\"" + jndiDestinationName + "\" ");
        }
        if (durableSubscriberName != null) {
            sb.append("durableSubscriberName=\"" + durableSubscriberName + "\" ");
        }
        sb.append("useMessageIDAsCorrelationID=\"" + String.valueOf(useMessageIDAsCorrelationID) + "\" ");

        return sb.toString();
    }
}
