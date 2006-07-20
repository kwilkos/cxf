package org.objectweb.celtix.bindings.soap2.binding;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bindings.soap2.AbstractSoapInterceptor;
import org.objectweb.celtix.bindings.soap2.SoapMessage;
import org.objectweb.celtix.helpers.NSStack;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.staxutils.StaxUtils;

public class RPCOutInterceptor extends AbstractSoapInterceptor {
    
    private static final String SERVICE_MODEL_BINDING = "service.model.binding";
    private static final String INBOUND_MESSAGE = "message.inbound";
    
    private SoapMessage soapMessage;
    private BindingInfo bindingInfo;
    private NSStack nsStack;
    private XMLStreamWriter xmlWriter;
    private DataWriter<XMLStreamWriter> dataWriter;
    
    private void init(SoapMessage message) {
        this.soapMessage = message;
        
        this.xmlWriter = getXMLStreamWriter(message);
        this.bindingInfo = (BindingInfo) message.get(SERVICE_MODEL_BINDING);
        nsStack = new NSStack();
        nsStack.push();
    }

    private void finish() throws XMLStreamException {
        this.xmlWriter.writeEndElement();
        this.xmlWriter.flush();
        this.xmlWriter.close();
    }

    public void handleMessage(SoapMessage message) {
        try {
            init(message);
            
            addOperationNode();

            BindingOperationInfo operation = this.bindingInfo.getOperation(getOperationName());
            
            if (isOutboundMessage()) {
                addReturnPart(operation);
            } else {
                addParts(operation);
            }

            finish();
        } catch (Exception e) {
            soapMessage.setResult(Exception.class, e);
        }
        
        message.getInterceptorChain().doIntercept(message);
    }

    private void addReturnPart(BindingOperationInfo operation) {
        MessagePartInfo ret = operation.getOutput().getMessageInfo().getMessageParts().get(0);
        if (ret != null) {
            getDataWriter().write(this.soapMessage.get("RETURN"), ret.getName(), this.xmlWriter);
        }
    }

    private void addParts(BindingOperationInfo operation) {
        int noArgs = operation.getInput().getMessageInfo().size();

        List<?> parts = (List<?>)this.soapMessage.get("PARAMETERS");
        Object[] args = parts.toArray();
        
        for (int idx = 0; idx < noArgs; idx++) {
            MessagePartInfo part = operation.getInput().getMessageInfo().getMessageParts().get(idx);
            Object partValue = args[idx];
            /*
            if (part.isInOut()) {
                partValue = ((Holder)args[idx]).value;
            }
            */
            
            getDataWriter().write(partValue,
                                  part.getName(),
                                  this.xmlWriter);
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

    protected void addOperationNode() throws XMLStreamException {
        String responseSuffix = isOutboundMessage() ? "Response" : "";
        String namespaceURI = this.bindingInfo.getService().getTargetNamespace();
        nsStack.add(namespaceURI);
        String prefix = nsStack.getPrefix(namespaceURI);

        String operationName = getOperationName() + responseSuffix;

        StaxUtils.writeStartElement(this.xmlWriter, prefix, operationName, namespaceURI);
    }

    private XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getResult(XMLStreamWriter.class);
    }
}
