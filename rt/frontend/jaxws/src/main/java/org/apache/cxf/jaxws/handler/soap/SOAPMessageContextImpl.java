package org.apache.cxf.jaxws.handler.soap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;

public class SOAPMessageContextImpl extends WrappedMessageContext implements SOAPMessageContext {

    SOAPMessageContextImpl(Message m) {
        super(m);
    }

    public void setMessage(SOAPMessage message) {
        getWrappedMessage().setContent(SOAPMessage.class, message);
    }

    public SOAPMessage getMessage() {
        SOAPMessage message = getWrappedMessage().getContent(SOAPMessage.class);
        if (null == message) {

            try {
                MessageFactory factory = MessageFactory.newInstance();
                // getMimeHeaders from message
                MimeHeaders mhs = null;
                InputStream is = getWrappedMessage().getContent(InputStream.class);
                message = factory.createMessage(mhs, is);
                getWrappedMessage().setContent(SOAPMessage.class, message);
            } catch (SOAPException ex) {
                // do something
            } catch (IOException ex) {
                // do something
            }
        }
        return message;
    }

    // TODO: handle the boolean parameter
    public Object[] getHeaders(QName name, JAXBContext context, boolean allRoles) {
        Element headerElements = getWrappedSoapMessage().getHeaders(Element.class);
        if (headerElements == null) {
            return null;
        }
        Collection<Object> objects = new ArrayList<Object>();
        for (int i = 0; i < headerElements.getChildNodes().getLength(); i++) {
            if (headerElements.getChildNodes().item(i) instanceof Element) {
                Element e = (Element)headerElements.getChildNodes().item(i);
                if (name.equals(e.getNamespaceURI())) {
                    try {
                        objects.add(context.createUnmarshaller().unmarshal(e));
                    } catch (JAXBException ex) {
                        // do something
                    }
                }
            }
        }
        Object[] headerObjects = new Object[objects.size()];
        return objects.toArray(headerObjects);
    }

    public Set<String> getRoles() {
        // TODO Auto-generated method stub
        return null;
    }

    private SoapMessage getWrappedSoapMessage() {
        return (SoapMessage)getWrappedMessage();
    }
}
