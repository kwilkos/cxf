package org.objectweb.celtix.bindings.soap2.binding;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bindings.soap2.AbstractSoapInterceptor;
import org.objectweb.celtix.bindings.soap2.SoapMessage;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.message.AbstractWrappedMessage;
import org.objectweb.celtix.message.Message;

public class WrapperOutInterceptor extends AbstractSoapInterceptor {
    
    private static final String INBOUND_MESSAGE = "message.inbound";
    
    private SoapMessage soapMessage;
    private XMLStreamWriter xmlWriter;
    private DataWriter<XMLStreamWriter> dataWriter;
    
    private void init(SoapMessage message) {
        this.soapMessage = message;
        
        this.xmlWriter = getXMLStreamWriter(message);
    }

    private void finish() throws XMLStreamException {
        this.xmlWriter.flush();
        this.xmlWriter.close();
    }

    public void handleMessage(SoapMessage message) {
        try {
            init(message);
            
            addParts(isOutboundMessage());

            finish();
        } catch (Exception e) {
            soapMessage.put(AbstractWrappedMessage.OUTBOUND_EXCEPTION, e);
        }
        
        message.getInterceptorChain().doIntercept(message);
    }

    private void addParts(boolean isOutbound) {
        getDataWriter().writeWrapper((ObjectMessageContext)this.soapMessage.get("OBJECTCONTEXT"),
                                     isOutbound,
                                     this.xmlWriter);
    }

    protected boolean isOutboundMessage() {
        return this.soapMessage.containsKey(INBOUND_MESSAGE);
    }

    protected DataWriter<XMLStreamWriter> getDataWriter() {
        if (dataWriter != null) {
            return dataWriter;
        }

        DataBindingCallback callback = (DataBindingCallback) this.soapMessage.get("JAXB_CALLBACK");
            
        for (Class<?> cls : callback.getSupportedFormats()) {
            if (cls == XMLStreamWriter.class) {
                dataWriter = callback.createWriter(XMLStreamWriter.class);
                break;
            }
        }

        if (dataWriter == null) {
            throw new RuntimeException("Could not figure out how to marshal data");
        }
        return dataWriter;
    }

    private XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getResult(XMLStreamWriter.class);
    }
}
