package org.apache.cxf.service.factory;

import org.apache.cxf.Bus;
import org.apache.cxf.databinding.DataReaderFactory;
import org.apache.cxf.databinding.DataWriterFactory;
import org.apache.cxf.interceptors.MessageSenderInterceptor;
import org.apache.cxf.interceptors.OutgoingChainInterceptor;
import org.apache.cxf.interceptors.OutgoingChainSetupInterceptor;
import org.apache.cxf.interceptors.ServiceInvokerInterceptor;
import org.apache.cxf.service.Service;

public abstract class AbstractServiceFactoryBean {
    private Bus bus;
    private DataReaderFactory dataReaderFactory;
    private DataWriterFactory dataWriterFactory;
    private Service service;
    
    public abstract Service create();

    protected void initializeDefaultInterceptors() {
        service.getInInterceptors().add(new ServiceInvokerInterceptor());
        service.getInInterceptors().add(new OutgoingChainSetupInterceptor());
        service.getInInterceptors().add(new OutgoingChainInterceptor());
        service.getOutInterceptors().add(new MessageSenderInterceptor());
    }
    
    protected void initializeDataBindings() {
        service.setDataReaderFactory(dataReaderFactory);
        service.setDataWriterFactory(dataWriterFactory);
    }
    
    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
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

    public Service getService() {
        return service;
    }

    protected void setService(Service service) {
        this.service = service;
    }
 
}
