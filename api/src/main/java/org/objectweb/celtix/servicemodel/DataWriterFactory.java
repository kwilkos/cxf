package org.objectweb.celtix.servicemodel;

import org.objectweb.celtix.bindings.DataWriter;

//REVISIT - need to move the Reader/Writer stuff, also probably 
//need the MessageInfo/OperationInfo as a param 
public interface DataWriterFactory {
    
    Class<?>[] getSupportedFormats();
    
    <T> DataWriter<T> createWriter(Class<T> cls);
}

