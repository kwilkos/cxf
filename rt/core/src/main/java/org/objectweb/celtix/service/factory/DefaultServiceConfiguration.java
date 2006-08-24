package org.objectweb.celtix.service.factory;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.objectweb.celtix.service.model.InterfaceInfo;

public class DefaultServiceConfiguration extends AbstractServiceConfiguration {

    @Override
    public QName getOperationName(InterfaceInfo service, Method method) {
        return new QName(service.getName().getNamespaceURI(), method.getName());
    }

}
