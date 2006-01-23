package org.objectweb.celtix.tools.common.model;

import com.sun.xml.bind.api.TypeReference;

public class WSDLException {

    private final Class exceptionClass;
 
    private final TypeReference typedetail;
    public WSDLException(Class exceptionClazz, TypeReference detail) {
        this.typedetail = detail;
        this.exceptionClass = exceptionClazz;    
    }

    public Class getExcpetionClass() {
        return exceptionClass;
    }

    public Class getDetailType() {
        return (Class)typedetail.type;
    }

    public TypeReference getDetailTypeReference() {
        return typedetail;
    }
    
   

}
