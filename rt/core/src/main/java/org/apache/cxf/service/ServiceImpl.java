package org.apache.cxf.service;

import java.util.concurrent.Executor;

import javax.xml.namespace.QName;

import org.apache.cxf.databinding.DataReaderFactory;
import org.apache.cxf.databinding.DataWriterFactory;
import org.apache.cxf.interceptors.AbstractAttributedInterceptorProvider;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.ServiceInfo;

public class ServiceImpl extends AbstractAttributedInterceptorProvider implements Service {

    private ServiceInfo serviceInfo;
    private DataReaderFactory dataReaderFactory;
    private DataWriterFactory dataWriterFactory;
    private Executor executor;
    private Invoker invoker;
    
    public ServiceImpl(ServiceInfo si) {
        serviceInfo = si;
    }

    public QName getName() {
        return serviceInfo.getName();
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public DataReaderFactory getDataReaderFactory() {
        return dataReaderFactory;
    }

    public void setDataReaderFactory(DataReaderFactory dataReaderFactory) {
        this.dataReaderFactory = dataReaderFactory;
    }

    public DataWriterFactory getDataWriterFactory() {
        return dataWriterFactory;
    }

    public void setDataWriterFactory(DataWriterFactory dataWriterFactory) {
        this.dataWriterFactory = dataWriterFactory;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }
}
