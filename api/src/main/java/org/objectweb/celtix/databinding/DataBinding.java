package org.objectweb.celtix.databinding;

import java.util.Map;

import org.objectweb.celtix.service.model.SchemaInfo;
import org.objectweb.celtix.service.model.ServiceInfo;

public interface DataBinding {
    
    DataReaderFactory getDataReaderFactory();
    
    DataWriterFactory getDataWriterFactory();
    
    Map<String, SchemaInfo> getSchemas(ServiceInfo serviceInfo); 
}
