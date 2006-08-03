package org.objectweb.celtix.service;

import java.util.Map;

import javax.xml.namespace.QName;

import org.objectweb.celtix.databinding.DataReaderFactory;
import org.objectweb.celtix.databinding.DataWriterFactory;
import org.objectweb.celtix.interceptors.InterceptorProvider;
import org.objectweb.celtix.service.model.ServiceInfo;

public interface Service extends Map<String, Object>, InterceptorProvider {
    
    QName getName();
    
    ServiceInfo getServiceInfo();

    DataReaderFactory getDataReaderFactory();
    
    DataWriterFactory getDataWriterFactory();

    void setDataReaderFactory(DataReaderFactory dataReaderFactory);

    void setDataWriterFactory(DataWriterFactory dataWriterFactory);
    
}
