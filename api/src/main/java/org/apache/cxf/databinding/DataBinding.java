package org.apache.cxf.databinding;

import java.util.Map;

import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;

public interface DataBinding {
    
    DataReaderFactory getDataReaderFactory();
    
    DataWriterFactory getDataWriterFactory();
    
    Map<String, SchemaInfo> getSchemas(ServiceInfo serviceInfo); 
}
