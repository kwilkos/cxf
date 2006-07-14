package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.soap2.ServiceModelUtil;
import org.objectweb.celtix.bindings.soap2.SoapMessage;
import org.objectweb.celtix.bindings.soap2.SoapVersion;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.servicemodel.BindingOperationInfo;
import org.objectweb.celtix.servicemodel.MessageInfo;
import org.objectweb.celtix.servicemodel.MessagePartInfo;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxStreamFilter;
import org.objectweb.celtix.staxutils.StaxUtils;
        
public class RPCInterceptor extends AbstractPhaseInterceptor {
        
    private static final String RPC_INTERCEPTOR_EXCEPTION = "rpc.interceptor.exception";
    private static final String INBOUND_MESSAGE = "message.inbound";
    
    private SoapMessage soapMessage;

    private DepthXMLStreamReader xmlReader;

    private void init(Message message) {
        if (!(message instanceof SoapMessage)) {
            return;
        }
        this.soapMessage = (SoapMessage) message;
        
        this.xmlReader = getXMLStreamReader();
    }

    private String getOperationName() {
        if (!StaxUtils.toNextElement(this.xmlReader)) {
            this.soapMessage.put(RPC_INTERCEPTOR_EXCEPTION,
                        new RuntimeException("There must be a method name element."));
        }

        String opName = xmlReader.getLocalName();
        if (!isInboundMessage() && opName.endsWith("Response")) {
            opName = opName.substring(0, opName.length() - 8);
        }
        return opName;
    }

    private BindingOperationInfo getOperation(String opName) {
        BindingOperationInfo operation = ServiceModelUtil.getOperation(this.soapMessage, opName);
        if (operation == null) {
            this.soapMessage.put(RPC_INTERCEPTOR_EXCEPTION,
                        new RuntimeException("Could not find operation:" + opName));
        }
        return operation;
    }

    public void handleMessage(Message message) {
        init(message);

        BindingOperationInfo operation = getOperation(getOperationName());
        
        storeOperation(operation);

        MessageInfo msg;
        if (isInboundMessage()) {
            msg = operation.getInput().getMessageInfo();
        } else {
            msg = operation.getOutput().getMessageInfo();
            if (msg.getMessageParts().size() > 0) {
                StaxUtils.nextEvent(this.xmlReader);
                StaxUtils.toNextElement(this.xmlReader);
                Object retVal = getDataReader().read(msg.getMessageParts().get(0).getName(),
                                                     -1,
                                                     this.xmlReader);
                message.put("RETURN", retVal);
            }
        }
        
        List<Object> parameters = new ArrayList<Object>();
        //int noArg = operation.getInput().size();

        StaxUtils.nextEvent(this.xmlReader);
        while (StaxUtils.toNextElement(this.xmlReader)) {
            MessagePartInfo p = msg.getMessageParts().get(parameters.size());
            
            if (p == null) {
                throw new RuntimeException("Parameter " + this.xmlReader.getName() + " does not exist!");
            }
            
            QName name = xmlReader.getName();
            if (!p.getName().equals(name)) {
                throw new RuntimeException("Parameter " + name + " does not exist!");
            }
            try {
                parameters.add(readParameter(this.xmlReader, p, parameters.size()));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Read parameter failed");
            }
        }
        message.put("PARAMETERS", parameters);
        
        message.getInterceptorChain().doIntercept(message);
    }

    protected boolean isInboundMessage() {
        return this.soapMessage.containsKey(INBOUND_MESSAGE);
    }

    protected DataReader<XMLStreamReader> getDataReader() {
        DataBindingCallback callback = (DataBindingCallback) this.soapMessage.get("JAXB_CALLBACK");

        DataReader<XMLStreamReader> dataReader = null;
        for (Class<?> cls : callback.getSupportedFormats()) {
            if (cls == XMLStreamReader.class) {
                dataReader = callback.createReader(XMLStreamReader.class);
                break;
            }
        }

        if (dataReader == null) {
            throw new RuntimeException("Could not figure out how to unmarshal data");
        }
        return dataReader;
    }
    
    protected Object readParameter(XMLStreamReader reader, MessagePartInfo p, int idx) throws Exception {
        return getDataReader().read(p.getName(), idx, reader);
    }


    protected void storeOperation(BindingOperationInfo operation) {
        this.soapMessage.put(MessageContext.WSDL_OPERATION, operation.getName());
    }
    
    private DepthXMLStreamReader getXMLStreamReader() {
        SoapVersion version = this.soapMessage.getVersion();
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(this.soapMessage.getSource(InputStream.class));
        StaxStreamFilter filter = new StaxStreamFilter(new QName[]{version.getEnvelope(),
                                                                   version.getBody()});
        xr = StaxUtils.createFilteredReader(xr, filter);
        return new DepthXMLStreamReader(xr);
    }
}
