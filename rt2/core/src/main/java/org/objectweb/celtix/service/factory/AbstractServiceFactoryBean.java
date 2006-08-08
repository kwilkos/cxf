package org.objectweb.celtix.service.factory;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.databinding.DataReaderFactory;
import org.objectweb.celtix.databinding.DataWriterFactory;
import org.objectweb.celtix.interceptors.ServiceInvokerInterceptor;
import org.objectweb.celtix.service.Service;

public abstract class AbstractServiceFactoryBean {
    private Bus bus;
    private DataReaderFactory dataReaderFactory;
    private DataWriterFactory dataWriterFactory;
    private Service service;
    
    public abstract Service create();

    protected void initializeDefaultInterceptors() {
        service.getInInterceptors().add(new ServiceInvokerInterceptor());
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
