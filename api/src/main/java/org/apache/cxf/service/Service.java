package org.apache.cxf.service;

import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.namespace.QName;

import org.apache.cxf.databinding.DataReaderFactory;
import org.apache.cxf.databinding.DataWriterFactory;
import org.apache.cxf.interceptors.InterceptorProvider;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.ServiceInfo;

public interface Service extends Map<String, Object>, InterceptorProvider {
    
    QName getName();
    
    ServiceInfo getServiceInfo();

    DataReaderFactory getDataReaderFactory();
    
    DataWriterFactory getDataWriterFactory();

    void setDataReaderFactory(DataReaderFactory dataReaderFactory);

    void setDataWriterFactory(DataWriterFactory dataWriterFactory);
    
    Executor getExecutor();

    void setExecutor(Executor executor);
    
    Invoker getInvoker();
    
    void setInvoker(Invoker invoker);
}
