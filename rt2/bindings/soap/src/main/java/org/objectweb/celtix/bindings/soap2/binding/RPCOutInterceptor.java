package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bindings.soap2.SoapMessage;
import org.objectweb.celtix.helpers.NSStack;
import org.objectweb.celtix.message.AbstractWrappedMessage;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.servicemodel.MessagePartInfo;
import org.objectweb.celtix.servicemodel.OperationInfo;
import org.objectweb.celtix.servicemodel.Service;

public class RPCOutInterceptor extends AbstractPhaseInterceptor {
    
    private static final String SERVICE_MODEL = "service.model";
    private static final String INBOUND_MESSAGE = "message.inbound";
    
    private SoapMessage soapMessage;
    private Service service;
    private NSStack nsStack;
    private XMLStreamWriter xmlWriter;
    private DataWriter<XMLStreamWriter> dataWriter;
    
    private void init(Message message) {
        if (!(message instanceof SoapMessage)) {
            return;
        }
        this.soapMessage = (SoapMessage) message;
        
        this.xmlWriter = getXMLStreamWriter(message);
        this.service = (Service) message.get(SERVICE_MODEL);
        nsStack = new NSStack();
        nsStack.push();
    }

    private void finish() throws XMLStreamException {
        this.xmlWriter.writeEndElement();
        this.xmlWriter.flush();
        this.xmlWriter.close();
    }

    public void intercept(Message message) {
        try {
            init(message);
            
            addOperationNode();

            OperationInfo operation = this.service.getOperation(getOperationName());
            
            if (isOutboundMessage()) {
                addReturnPart(operation);
            } else {
                addParts(operation);
            }

            finish();
        } catch (Exception e) {
            soapMessage.put(AbstractWrappedMessage.OUTBOUND_EXCEPTION, e);
        }
        
        message.getInterceptorChain().doIntercept(message);
    }

    private void addReturnPart(OperationInfo operation) {
        MessagePartInfo ret = operation.getOutput().getMessageParts().get(0);
        if (ret != null && !ret.isHeader()) {
            getDataWriter().write(this.soapMessage.get("RETURN"), ret.getName(), this.xmlWriter);
        }
    }

    private void addParts(OperationInfo operation) {
        int noArgs = operation.getInput().size();

        List<?> parts = (List<?>)this.soapMessage.get("PARAMETERS");
        Object[] args = parts.toArray();
        
        for (int idx = 0; idx < noArgs; idx++) {
            MessagePartInfo part = operation.getInput().getMessageParts().get(idx);
            if (!part.isHeader()) {
                Object partValue = args[idx];
                if (part.isInOut()) {
                    partValue = ((Holder)args[idx]).value;
                }
                
                getDataWriter().write(partValue,
                                      part.getName(),
                                      this.xmlWriter);
            }
        }
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

    private String getOperationName() {
        return (String) this.soapMessage.get(MessageContext.WSDL_OPERATION);
    }

    public void writeStartElement(XMLStreamWriter writer, String prefix, String name, String namespace)
        throws XMLStreamException {
        if (prefix == null) {
            prefix = "";
        }

        if (namespace.length() > 0) {
            writer.writeStartElement(prefix, name, namespace);
            writer.writeNamespace(prefix, namespace);
        } else {
            writer.writeStartElement(name);
            writer.writeDefaultNamespace("");
        }
    }

    protected void addOperationNode() throws XMLStreamException {
        String responseSuffix = isOutboundMessage() ? "Response" : "";
        String namespaceURI = this.service.getTargetNamespace();
        nsStack.add(namespaceURI);
        String prefix = nsStack.getPrefix(namespaceURI);

        String operationName = getOperationName() + responseSuffix;

        // TODO. use the writeStartElement
        writeStartElement(this.xmlWriter, prefix, operationName, namespaceURI);
    }

    private XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getResult(XMLStreamWriter.class);
    }
}
