package org.apache.cxf.service.model;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.Service;

public final class ServiceModelUtil {

    private ServiceModelUtil() {
    }

    public static Service getService(Exchange exchange) {
        return exchange.get(Service.class);
    }
    
    public static String getTargetNamespace(Exchange exchange) {
        return getService(exchange).getServiceInfo().getTargetNamespace();
    }
    
    public static BindingOperationInfo getOperation(Exchange exchange, String opName) {
        return getOperation(exchange, new QName(getTargetNamespace(exchange), opName));
    }

    public static BindingOperationInfo getOperation(Exchange exchange, QName opName) {
        Endpoint ep = exchange.get(Endpoint.class);
        BindingInfo service = ep.getEndpointInfo().getBinding();
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

    public static QName getPartName(MessagePartInfo part) {
        QName name = part.getElementQName();
        if (name == null) {
            name = part.getTypeQName();
        }
        return name;
    }

    public static QName getRPCPartName(MessagePartInfo part) {
        QName name = getPartName(part);
        return new QName(name.getNamespaceURI(), part.getName().getLocalPart());
    }
}
