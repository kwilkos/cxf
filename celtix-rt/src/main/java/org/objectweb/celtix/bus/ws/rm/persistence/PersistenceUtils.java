package org.objectweb.celtix.bus.ws.rm.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;

public class PersistenceUtils {
    private static final Logger LOG = LogUtils.getL7dLogger(PersistenceUtils.class);
    
    private static final String SOAP_MSG_KEY = "org.objectweb.celtix.bindings.soap.message";
    private JAXBContext context;
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;
    private MessageFactory msgFactory;
    
    

    public InputStream getContextAsInputStream(MessageContext ctx) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            int nKeys = 0;
            for (String key : ctx.keySet()) {
                Object value = ctx.get(key); 
                if (value instanceof Serializable) {
                    nKeys++;
                } else if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Skipping key: " + key + " (value of type " 
                        + value.getClass().getName() + " is not serializable)");
                }
            }
            
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            
            oos.writeInt(nKeys);
            
            for (String key : ctx.keySet()) {
                Object value = ctx.get(key); 
                if (value instanceof Serializable) {
                    oos.writeObject(key); 
                    oos.writeObject(value);
                }                
            }

            SOAPMessage msg = (SOAPMessage)ctx.get(SOAP_MSG_KEY);
            msg.writeTo(bos);
            
            bos.close();

        } catch (Exception ex) {
            throw new RMStoreException(ex);    
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Context as stream: " + new String(bos.toByteArray()));
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }
    
    public MessageContext getContext(InputStream is) {
        MessageContext ctx = new GenericMessageContext();
        try {
            ObjectInput oi = new ObjectInputStream(is);
            int nKeys = oi.readInt();
            
            for (int i = 0; i < nKeys; i++) {
                String key = (String)oi.readObject();
                Object value = oi.readObject();
                ctx.put(key, value);
            }
            
            // construct SOAPMessage from input stream
            
            SOAPMessage msg = getMessageFactory().createMessage(null, is);
            ctx.put(SOAP_MSG_KEY, msg);
            
        } catch (Exception ex) {
            throw new RMStoreException(ex);  
        }
        return ctx;
    }

    public InputStream getAcknowledgementAsInputStream(SequenceAcknowledgement ack) {
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
        try {
            getMarshaller().marshal(ack, bos);
        } catch (JAXBException ex) {
            throw new RMStoreException(ex);
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }
    
    public SequenceAcknowledgement getSequenceAcknowledgment(InputStream is) {
        Object obj = null;
        try {
            obj = getUnmarshaller().unmarshal(is);
            if (obj instanceof JAXBElement<?>) {
                JAXBElement<?> el = (JAXBElement<?>)obj;
                obj = el.getValue();
            }
        } catch (JAXBException ex) {
            throw new RMStoreException(ex);
        }
        return (SequenceAcknowledgement)obj;
    }
    
    private JAXBContext getContext() throws JAXBException {
        if (null == context) {
            context = JAXBContext.newInstance(SequenceAcknowledgement.class.getPackage().getName(),
                                              getClass().getClassLoader()); 
        }
        return context;
    }
    
    
    private Unmarshaller getUnmarshaller() throws JAXBException {
        if (null == unmarshaller) {
            unmarshaller = getContext().createUnmarshaller();
        }
        return unmarshaller;
    }
    
    private Marshaller getMarshaller() throws JAXBException {
        if (null == marshaller) {
            marshaller = getContext().createMarshaller();
        }
        return marshaller;
    }
    
    private MessageFactory getMessageFactory() throws SOAPException {
        if (null == msgFactory) {
            msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        }
        return msgFactory;        
    }
   
}
