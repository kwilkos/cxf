package org.apache.cxf.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class WrappedInInterceptor extends AbstractInDatabindingInterceptor {
    public static final String SINGLE_WRAPPED_PART = "single.wrapped.in.part";
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(WrappedInInterceptor.class);
    
    public WrappedInInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
    }

    public void handleMessage(Message message) {
        DepthXMLStreamReader xmlReader = getXMLStreamReader(message);

        // Trying to find the operation name from the XML.
        if (xmlReader.getEventType() != XMLStreamConstants.START_ELEMENT
            && !StaxUtils.toNextElement(xmlReader)) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_OPERATION_ELEMENT", BUNDLE));
        }
        BindingOperationInfo operation = message.getExchange().get(BindingOperationInfo.class);
        boolean requestor = isRequestor(message);
        
        if (operation == null) {
            String opName = xmlReader.getLocalName();
            if (requestor && opName.endsWith("Response")) {
                opName = opName.substring(0, opName.length() - 8);
            }
    
            // TODO: Allow overridden methods.
            operation = ServiceModelUtil.getOperation(message.getExchange(), opName);
            if (operation == null) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("NO_OPERATION", BUNDLE, opName));
            }
            message.getExchange().put(BindingOperationInfo.class, operation);
            message.getExchange().put(OperationInfo.class, operation.getOperationInfo());
        }
        if (requestor) {
            message.put(MessageInfo.class, operation.getOperationInfo().getOutput());
            message.put(BindingMessageInfo.class, operation.getOutput());            
        } else {
            message.put(MessageInfo.class, operation.getOperationInfo().getInput());
            message.put(BindingMessageInfo.class, operation.getInput());
        }
        
        DataReader<XMLStreamReader> dr = getDataReader(message);
        List<Object> objects;
        
        // Determine if there is a wrapper class
        if (Boolean.TRUE.equals(operation.getOperationInfo().getProperty(SINGLE_WRAPPED_PART))) {
            objects = new ArrayList<Object>();
            Object wrappedObject = dr.read(xmlReader);
            objects.add(wrappedObject);
        } else {
            // Unwrap each part individually if we don't have a wrapper
            objects = new ArrayList<Object>();
            int depth = xmlReader.getDepth();
            
            try {
                while (xmlReader.nextTag() == XMLStreamReader.START_ELEMENT && xmlReader.getDepth() > depth) {
                    objects.add(dr.read(xmlReader));
                }
            } catch (XMLStreamException e) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("STAX_READ_EXC", BUNDLE), e);
            }
        }
        
        message.setContent(List.class, objects);
    }

}

