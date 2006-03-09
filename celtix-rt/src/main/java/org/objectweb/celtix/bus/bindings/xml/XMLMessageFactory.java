package org.objectweb.celtix.bus.bindings.xml;

import java.io.*;
import org.w3c.dom.*;

import org.objectweb.celtix.helpers.XMLUtils;

public final class XMLMessageFactory {

    private XMLUtils xmlUtils = new XMLUtils();
    
    private XMLMessageFactory() {
    }
    
    public XMLMessage createMessage() throws XMLBindingException {
        return new XMLMessage();
    }

    public XMLMessage createMessage(InputStream in) throws XMLBindingException {
        XMLMessage message = new XMLMessage();
        try {
            Document doc = xmlUtils.parse(in);
            if (doc != null) {
                message.setRoot(doc);
            }
        } catch (Exception exp) {
            throw new XMLBindingException("Create XML binding message exception:", exp);
        }
        return message;
    }

    public static XMLMessageFactory newInstance() {
        return new XMLMessageFactory();
    }
}
