package org.objectweb.celtix.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.validation.Schema;

public abstract class JAXBDataFactoryBase {
    protected JAXBContext context; 
    protected Schema schema;

    public void setSchema(Schema s) {
        this.schema = s;
    }

    public void setJAXBContext(JAXBContext jc) {
        this.context = jc;
    }
    
    public Schema getSchema() {
        return schema;
    }
    public JAXBContext getJAXBContext() {
        return context;
    }
}
