package org.objectweb.celtix.bindings.soap2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.databinding.DataReaderFactory;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessageInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceModelUtil;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxStreamFilter;
import org.objectweb.celtix.staxutils.StaxUtils;

public class RPCInInterceptor extends AbstractSoapInterceptor {
        

    public RPCInInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
    }

    private BindingOperationInfo getOperation(SoapMessage message, DepthXMLStreamReader xmlReader) {
        if (!StaxUtils.toNextElement(xmlReader)) {
            message.setContent(Exception.class,
                               new RuntimeException("There must be a method name element."));
        }

        String opName = xmlReader.getLocalName();
        if (!isInboundMessage(message) && opName.endsWith("Response")) {
            opName = opName.substring(0, opName.length() - 8);
        }

        BindingOperationInfo operation = ServiceModelUtil.getOperation(message,
                                                                       new QName(xmlReader.getNamespaceURI(),
                                                                                 opName));
        if (operation == null) {
            message.setContent(Exception.class,
                               new RuntimeException("Could not find operation:" + opName));
        }
        return operation;
    }

    public void handleMessage(SoapMessage message) {
        DepthXMLStreamReader xmlReader = getXMLStreamReader(message);
        
        BindingOperationInfo operation = null;
        if (!isOperationResolved(message)) {
            operation = getOperation(message, xmlReader);
            // Store operation into the message.
            message.put(Message.INVOCATION_OPERATION, operation.getName().getLocalPart());
        }

        MessageInfo msg;
        DataReader<XMLStreamReader> dr = getDataReader(message,
                                                       operation.getOperationInfo());
         
        if (isInboundMessage(message)) {
            msg = operation.getInput().getMessageInfo();
        } else {
            msg = operation.getOutput().getMessageInfo();
        }

        List<Object> parameters = new ArrayList<Object>();

        StaxUtils.nextEvent(xmlReader);
        while (StaxUtils.toNextElement(xmlReader)) {
            int idx = parameters.size();
            MessagePartInfo p = msg.getMessageParts().get(idx);
            if (p == null) {
                message.setContent(Exception.class,
                                   new RuntimeException("Parameter "
                                                        + xmlReader.getName()
                                                        + " does not exist!"));
            }
            QName name = xmlReader.getName();
            QName elName = ServiceModelUtil.getRPCPartName(p);

            if (!elName.getLocalPart().equals(name.getLocalPart())) {
                String expMessage = "Parameter "
                    + name
                    + " does not equal to the name in the servicemodel!";
                message.setContent(Exception.class, new RuntimeException(expMessage));
            }
            parameters.add(dr.read(elName,
                                   xmlReader,
                                   getParameterTypeClass(message, idx)));
        }
        
        message.put(Message.INVOCATION_OBJECTS, parameters);
    }

    protected Class<?> getParameterTypeClass(SoapMessage message, int idx) {
        // For the RPC style, if we use jaxb to do the unmarshall, AFAIK it requires the class info.
        // Don't know from which part we can get the implementor class,
        // We just assume that this cls can be retrieved from the message.
        // Refactoring this piece of code
        // after we figure out how the interceptors interact with the frontend(s).
        Method m = (Method) message.get("IMPLEMENTOR_METHOD");
        if (isInboundMessage(message)) {
            return m.getParameterTypes()[idx];
        } else {
            return m.getReturnType();
        }
    }

    protected boolean isInboundMessage(SoapMessage message) {
        return message.containsKey(Message.INBOUND_MESSAGE);
    }

    protected DataReader<XMLStreamReader> getDataReader(SoapMessage message, OperationInfo oi) {
        String key = (String) message.getExchange().get(SoapMessage.DATAREADER_FACTORY_KEY);
        DataReaderFactory factory = (DataReaderFactory) oi.getProperty(key);

        DataReader<XMLStreamReader> dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == XMLStreamReader.class) {
                dataReader = factory.createReader(XMLStreamReader.class);
                break;
            }
        }
        if (dataReader == null) {
            message.setContent(Exception.class,
                               new RuntimeException("Could not figure out how to unmarshal data"));
        }        
        return dataReader;
    }
    
    protected boolean isOperationResolved(SoapMessage message) {
        return message.get(Message.INVOCATION_OPERATION) != null;
    }
    
    private DepthXMLStreamReader getXMLStreamReader(SoapMessage message) {
        SoapVersion version = message.getVersion();
        XMLStreamReader xr = message.getContent(XMLStreamReader.class);
        StaxStreamFilter filter = new StaxStreamFilter(new QName[]{version.getEnvelope(),
                                                                   version.getBody()});
        xr = StaxUtils.createFilteredReader(xr, filter);
        return new DepthXMLStreamReader(xr);
    }
}

