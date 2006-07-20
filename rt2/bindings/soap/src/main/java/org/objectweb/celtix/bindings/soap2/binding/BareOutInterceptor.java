package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.Holder;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bindings.soap2.AbstractSoapInterceptor;
import org.objectweb.celtix.bindings.soap2.SoapMessage;
import org.objectweb.celtix.message.AbstractWrappedMessage;
import org.objectweb.celtix.message.Message;

public class BareOutInterceptor extends AbstractSoapInterceptor {
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
            
            if (isOutboundMessage()) {
                addReturnPart();
            }

            addParts();

            finish();
        } catch (Exception e) {
            soapMessage.put(AbstractWrappedMessage.OUTBOUND_EXCEPTION, e);
        }
        message.getInterceptorChain().doIntercept(message);
    }

    protected DataWriter<XMLStreamWriter> getDataWriter() {
        if (dataWriter != null) {
            return dataWriter;
        }

        DataBindingCallback callback = getCallback();
            
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

    private DataBindingCallback getCallback() {
        return (DataBindingCallback) this.soapMessage.get("JAXB_CALLBACK");
    }

    private void addReturnPart() {
        getDataWriter().write(this.soapMessage.get("RETURN"),
                              getCallback().getWebResultQName(),
                              this.xmlWriter);
    }

    private void addParts() {
        int noArgs = getCallback().getParamsLength();

        List<?> parts = (List<?>)this.soapMessage.get("PARAMETERS");
        Object[] args = parts.toArray();
        
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = getCallback().getWebParam(idx);
            Object partValue = args[idx];

            if (param.mode() != WebParam.Mode.IN) {
                partValue = ((Holder)args[idx]).value;
            }

            QName elName = new QName(param.targetNamespace(), param.name());

            getDataWriter().write(partValue,
                                  elName,
                                  this.xmlWriter);
        }
    }

    protected boolean isOutboundMessage() {
        return this.soapMessage.containsKey(INBOUND_MESSAGE);
    }

    private XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getResult(XMLStreamWriter.class);
    }
}
