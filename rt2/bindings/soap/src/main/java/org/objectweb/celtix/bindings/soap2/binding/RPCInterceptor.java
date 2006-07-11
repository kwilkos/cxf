package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.soap2.SoapMessage;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.servicemodel.BindingInfo;
import org.objectweb.celtix.servicemodel.MessageInfo;
import org.objectweb.celtix.servicemodel.MessagePartInfo;
import org.objectweb.celtix.servicemodel.OperationInfo;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxStreamFilter;
import org.objectweb.celtix.staxutils.StaxUtils;
        
import static org.objectweb.celtix.datamodel.soap.SOAPConstants.SOAP_BODY;
import static org.objectweb.celtix.datamodel.soap.SOAPConstants.SOAP_ENV;

public class RPCInterceptor extends AbstractPhaseInterceptor {
    
    private static final String SERVICE_MODEL_BINDING = "service.model.binding";
    private static final String RPC_INTERCEPTOR_EXCEPTION = "rpc.interceptor.exception";
    private static final String INBOUND_MESSAGE = "message.inbound";
    
    private SoapMessage soapMessage;

    private DepthXMLStreamReader xmlReader;

    public void intercept(Message message) {
        if (!(message instanceof SoapMessage)) {
            return;
        }
        this.soapMessage = (SoapMessage) message;
        
        this.xmlReader = getXMLStreamReader(message);

        if (!StaxUtils.toNextElement(this.xmlReader)) {
            message.put(RPC_INTERCEPTOR_EXCEPTION,
                        new RuntimeException("There must be a method name element."));
        }

        String opName = xmlReader.getLocalName();
        if (!isInboundMessage() && opName.endsWith("Response")) {
            opName = opName.substring(0, opName.length() - 8);
        }

        BindingInfo service = (BindingInfo)message.get(SERVICE_MODEL_BINDING);
        OperationInfo operation = service.getOperation(opName);

        if (operation == null) {
            message.put(RPC_INTERCEPTOR_EXCEPTION,
                        new RuntimeException("Could not find operation:" + opName));
        }
        
        storeOperation(operation);

        MessageInfo msg;
        if (isInboundMessage()) {
            msg = operation.getInput();
        } else {
            msg = operation.getOutput();
            if (msg.getMessageParts().size() > 0 && !msg.getMessageParts().get(0).isHeader()) {
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


    protected void storeOperation(OperationInfo operation) {
        this.soapMessage.put(MessageContext.WSDL_OPERATION, operation.getName());
    }
    
    private DepthXMLStreamReader getXMLStreamReader(Message message) {
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(message.getSource(InputStream.class));
        StaxStreamFilter filter = new StaxStreamFilter(new QName[]{SOAP_ENV, SOAP_BODY});
        xr = StaxUtils.createFilteredReader(xr, filter);
        return new DepthXMLStreamReader(xr);
    }
}
