package org.objectweb.celtix.interceptors;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.jaxb.WrapperHelper;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessageInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceModelUtil;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxUtils;

public class WrappedInInterceptor extends AbstractInDatabindingInterceptor {
    public static final String SINGLE_WRAPPED_PART = "single.wrapped.part";
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
            throw new Fault(new org.objectweb.celtix.common.i18n.Message("NO_OPERATION_ELEMENT", BUNDLE));
        }
    
        boolean requestor = isRequestor(message);
        String opName = xmlReader.getLocalName();
        if (requestor && opName.endsWith("Response")) {
            opName = opName.substring(0, opName.length() - 8);
        }

        // TODO: Allow overridden methods.
        BindingOperationInfo operation = ServiceModelUtil.getOperation(message.getExchange(), opName);
        if (operation == null) {
            throw new Fault(new org.objectweb.celtix.common.i18n.Message("NO_OPERATION", BUNDLE, opName));
        }
        message.getExchange().put(BindingOperationInfo.class.getName(), operation);
        
        DataReader<XMLStreamReader> dr = getDataReader(message);
        List<Object> objects;
        
        // Determine if there is a wrapper class
        if (Boolean.TRUE.equals(operation.getOperationInfo().getProperty(SINGLE_WRAPPED_PART))) {
            // Find the appropriate message that we're processing
            OperationInfo unwrappedOp = operation.getOperationInfo().getUnwrappedOperation();
            MessageInfo messageInfo;
            if (!requestor) {
                messageInfo = unwrappedOp.getInput();
            } else {
                messageInfo = unwrappedOp.getOutput();
            }
            
            if (messageInfo.getMessageParts().size() > 0) {
                Object wrappedObject = dr.read(xmlReader);
                
                objects = getWrappedParts(wrappedObject, messageInfo);
            } else {
                objects = new ArrayList<Object>();
            }
        } else {
            // Unwrap each part individually if we don't have a wrapper
            objects = new ArrayList<Object>();
            int depth = xmlReader.getDepth();
            
            try {
                while (xmlReader.nextTag() == XMLStreamReader.START_ELEMENT && xmlReader.getDepth() > depth) {
                    objects.add(dr.read(xmlReader));
                }
            } catch (XMLStreamException e) {
                throw new Fault(new org.objectweb.celtix.common.i18n.Message("STAX_READ_EXC", BUNDLE), e);
            }
        }
        
        message.setContent(Object.class, objects);
    }

    private List<Object> getWrappedParts(Object wrappedObject, MessageInfo message) {
        List<Object> objects = new ArrayList<Object>();
        
        try {
            for (MessagePartInfo part : message.getMessageParts()) {
                objects.add(WrapperHelper.getWrappedPart(part.getName().getLocalPart(), wrappedObject));
            }
        } catch (Exception e) {
            throw new Fault(new org.objectweb.celtix.common.i18n.Message("COULD_NOT_UNRWAP", BUNDLE), e);
        }

        return objects;
    }
}

