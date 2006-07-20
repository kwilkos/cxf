package org.objectweb.celtix.databinding;


//REVISIT - need to move the Reader/Writer stuff, also probably 
//need the MessageInfo/OperationInfo as a param 
public interface DataWriterFactory {
    
    Class<?>[] getSupportedFormats();
    
    <T> DataWriter<T> createWriter(Class<T> cls);
}

