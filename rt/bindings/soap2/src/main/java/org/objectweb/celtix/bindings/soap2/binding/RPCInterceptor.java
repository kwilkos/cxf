package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.bindings.soap2.utils.DepthXMLStreamReader;
import org.objectweb.celtix.bindings.soap2.utils.StaxUtils;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.rio.Message;
import org.objectweb.celtix.rio.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.rio.soap.SoapMessage;
import org.objectweb.celtix.servicemodel.MessageInfo;
import org.objectweb.celtix.servicemodel.MessagePartInfo;
import org.objectweb.celtix.servicemodel.OperationInfo;
import org.objectweb.celtix.servicemodel.Service;

public class RPCInterceptor extends AbstractPhaseInterceptor {
    
    private static final String SERVICE_MODEL = "service.model";
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

        StaxUtils.toNextElement(this.xmlReader);
        
        StaxUtils.nextEvent(this.xmlReader);
        StaxUtils.toNextElement(this.xmlReader);

        StaxUtils.nextEvent(this.xmlReader);
        if (!StaxUtils.toNextElement(this.xmlReader)) {
            message.put(RPC_INTERCEPTOR_EXCEPTION,
                        new RuntimeException("There must be a method name element."));
        }

        String opName = xmlReader.getLocalName();

        Service service = (Service) message.get(SERVICE_MODEL);
        OperationInfo operation = service.getOperation(opName);

        if (operation == null) {
            message.put(RPC_INTERCEPTOR_EXCEPTION,
                        new RuntimeException("Could not find operation:" + opName));
        }
        
        storeOperation(operation);

        MessageInfo msg;
        if (message.containsKey(INBOUND_MESSAGE)) {
            msg = operation.getInput();
        } else {
            msg = operation.getOutput();
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
                parameters.add(readParameter(this.xmlReader, p));
            } catch (Exception e) {
                throw new RuntimeException("Read parameter failed");
            }
        }
        message.put("PARAMETERS", parameters);
        
        // message.getInterceptorChain().doIntercept(message);
    }
    
    protected Object readParameter(XMLStreamReader reader, MessagePartInfo p) throws Exception {
        JAXBContext context = (JAXBContext) this.soapMessage.get("JAXB_CONTEXT");
        // TODO. currently don't know where to get the type class of the parameter.
        //       for unit test convenient, just assume that the class type info can get from the message.
        return JAXBEncoderDecoder.unmarshall(context,
                                             null,
                                             reader,
                                             p.getName(),
                                             (Class) this.soapMessage.get("test.parameter"));
        // return JAXBEncoderDecoder.unmarshall(context, null, reader, p.getName(), p.getTypeClass());
    }


    protected void storeOperation(OperationInfo operation) {
        this.soapMessage.put(MessageContext.WSDL_OPERATION, operation.getName());
    }
    
    private DepthXMLStreamReader getXMLStreamReader(Message message) {
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(message.getSource(InputStream.class));
        return new DepthXMLStreamReader(xr);
    }
}
