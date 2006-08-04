package org.objectweb.celtix.bindings.soap2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.wsdl.extensions.soap.SOAPHeader;
import javax.xml.namespace.QName;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingMessageInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.SchemaInfo;
import org.objectweb.celtix.service.model.ServiceInfo;



public final class ServiceModelUtil {
    private static final String HEADERS_PROPERTY = ServiceModelUtil.class.getName() + ".HEADERS";

    private ServiceModelUtil() {
    }

    public static BindingOperationInfo getOperation(SoapMessage soapMessage, QName opName) {
        BindingInfo service = (BindingInfo)soapMessage.get(Message.BINDING_INFO);
        return service.getOperation(opName);
    }

    @SuppressWarnings("unchecked")
    public static Set<QName> getHeaderParts(BindingMessageInfo bmi) {
        Object obj = bmi.getProperty(HEADERS_PROPERTY);        
        if (obj == null) {
            Set<QName> set = new HashSet<QName>();
            List<MessagePartInfo> mps = bmi.getMessageInfo().getMessageParts();
            for (SOAPHeader head : bmi.getExtensors(SOAPHeader.class)) {
                String pn = head.getPart();   
                for (MessagePartInfo mpi : mps) {
                    if (pn.equals(mpi.getName().getLocalPart())) {
                        if (mpi.isElement()) {
                            set.add(mpi.getElementQName());
                        } else {
                            set.add(mpi.getTypeQName());
                        }
                        break;
                    }
                }
            }
            bmi.setProperty(HEADERS_PROPERTY, set);
            return set;
        }
        return (Set<QName>)obj;
    }

    public static Set<QName> getHeaderQNameInOperationParam(SoapMessage soapMessage) {
        Set<QName> headers = new HashSet<QName>();
        BindingInfo binding = (BindingInfo)soapMessage.get(Message.BINDING_INFO);
        if (binding != null) {
            String operationName = (String)soapMessage.get(Message.OPERATION_INFO);
            if (operationName != null) {
                for (BindingOperationInfo opi : binding.getOperations()) {
                    if (opi.getName().getLocalPart().equals(operationName)) {
                        headers.addAll(getHeaderParts(opi.getInput()));
                        headers.addAll(getHeaderParts(opi.getOutput()));
                        break;
                    }
                }
            } 
        }
        return headers;
    }
    
    public static SchemaInfo getSchema(ServiceInfo serviceInfo, MessagePartInfo messagePartInfo) {
        SchemaInfo schemaInfo = null;
        String tns = null;
        if (messagePartInfo.isElement()) {
            tns = messagePartInfo.getElementQName().getNamespaceURI();
        } else {
            tns = messagePartInfo.getTypeQName().getNamespaceURI();
        }
        for (SchemaInfo schema : serviceInfo.getTypeInfo().getSchemas()) {
            if (tns.equals(schema.getNamespaceURI())) {
                schemaInfo = schema;
            }
        }
        return schemaInfo;
    }
}
