package org.apache.cxf.service.factory;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.apache.cxf.service.model.InterfaceInfo;

public class DefaultServiceConfiguration extends AbstractServiceConfiguration {

    @Override
    public QName getOperationName(InterfaceInfo service, Method method) {
        return new QName(service.getName().getNamespaceURI(), method.getName());
    }

}
