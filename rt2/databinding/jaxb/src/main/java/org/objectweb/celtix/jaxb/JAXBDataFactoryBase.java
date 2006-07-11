package org.objectweb.celtix.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.validation.Schema;

public abstract class JAXBDataFactoryBase {
    protected JAXBContext context; 
    protected Schema schema;
    
    public Schema getSchema() {
        return schema;
    }
    public JAXBContext getJAXBContext() {
        return context;
    }
}
