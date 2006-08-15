package org.objectweb.celtix.interceptors;

import java.util.Arrays;

import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.databinding.DataReaderFactory;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceModelUtil;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxUtils;

public class WrappedInInterceptor extends AbstractPhaseInterceptor<Message> {
        
    public void handleMessage(Message message) {
        try {
            DepthXMLStreamReader xmlReader = getXMLStreamReader(message);

            // Trying to find the operation name.
            // If it's empty, then, we should resolve it from the message.
            String opName = (String) message.get(Message.INVOCATION_OPERATION);
            if (opName == null) {
                if (!StaxUtils.toNextElement(xmlReader)) {
                    message.setContent(Exception.class,
                                       new RuntimeException("There must be a method name element."));
                }
            
                opName = xmlReader.getLocalName();
                if (!isInboundMessage(message) && opName.endsWith("Response")) {
                    opName = opName.substring(0, opName.length() - 8);
                }
            }
        
            // Store the operation name.
            message.put(Message.INVOCATION_OPERATION, opName);

            BindingOperationInfo operation = ServiceModelUtil.getOperation(message, opName);
            if (operation == null) {
                message.setContent(Exception.class,
                                   new RuntimeException("Could not find operation:"
                                                        + opName
                                                        + " from the service model!"));
            }
        
            DataReader<XMLStreamReader> dr = getDataReader(message, operation.getOperationInfo());

            Object wrappedObject = dr.read(xmlReader);
            
            message.put(Message.INVOCATION_OBJECTS, Arrays.asList(wrappedObject));
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Read parameter failed");
        }  
    }

    protected boolean isInboundMessage(Message message) {
        return message.containsKey(Message.INBOUND_MESSAGE);
    }

    protected DataReader<XMLStreamReader> getDataReader(Message message, OperationInfo oi) {
        String key = (String) message.getExchange().get(Message.DATAREADER_FACTORY_KEY);
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
    
    private DepthXMLStreamReader getXMLStreamReader(Message message) {
        XMLStreamReader xr = message.getContent(XMLStreamReader.class);
        return new DepthXMLStreamReader(xr);
    }
}

