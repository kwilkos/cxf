package org.objectweb.celtix.service.model;

import javax.xml.namespace.QName;

import org.objectweb.celtix.message.Message;

public final class ServiceModelUtil {

    private ServiceModelUtil() {
    }

    public static String getTargetNamespace(Message message) {
        return ((BindingInfo) message.get(Message.BINDING_INFO)).getService().getTargetNamespace();
    }
    
    public static BindingOperationInfo getOperation(Message message, String opName) {
        BindingInfo service = (BindingInfo)message.get(Message.BINDING_INFO);
        return service.getOperation(new QName(getTargetNamespace(message), opName));
    }

    public static BindingOperationInfo getOperation(Message message, QName opName) {
        BindingInfo service = (BindingInfo)message.get(Message.BINDING_INFO);
        return service.getOperation(opName);
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
