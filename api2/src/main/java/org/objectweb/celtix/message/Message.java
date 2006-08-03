package org.objectweb.celtix.message;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.objectweb.celtix.interceptors.InterceptorChain;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.Destination;

public interface Message extends Map<String, Object> {
    
    String BINDING_INFO = "org.objectweb.celtix.service.model.binding";
    String SERVICE_INFO = "org.objectweb.celtix.service.model.service";
    String INTERFACE_INFO = "org.objectweb.celtix.service.model.interface";
    String OPERATION_INFO = "org.objectweb.celtix.service.model.operation";
    String BINDING = "org.objectweb.celtix.binding";
    String TRANSPORT = "org.objectweb.celtix.transport";    
    String BUS = "org.objectweb.celtix.bus";
    String REQUESTOR_ROLE = "org.objectweb.celtix.client";
    String DATAREADER_FACTORY_KEY = "org.objectweb.celtix.databinding.reader.factory.key";
    String DATAWRITER_FACTORY_KEY = "org.objectweb.celtix.databinding.writer.factory.key";
    
    
    String getId();
    
    InterceptorChain getInterceptorChain();
    
    /**
     * @return the associated Conduit if message is outbound, null otherwise
     */
    Conduit getConduit();

    /**
     * @return the associated Destination if message is inbound, null otherwise
     */
    Destination getDestination();
    
    Exchange getExchange();
    
    Collection<Attachment> getAttachments();

    /**
     * @return the mime type string  
     */
    String getAttachmentMimeType();

    /**
     * Retreive the encapsulated content as a particular type (a result type
     * if message is outbound, a source type if message is inbound)
     * 
     * @param format the expected content format 
     * @return the encapsulated content
     */    
    <T> T getContent(Class<T> format);

    /**
     * Provide the encapsulated content as a particular type (a result type
     * if message is outbound, a source type if message is inbound)
     * 
     * @param format the provided content format 
     * @param content the content to be encapsulated
     */    
    <T> void setContent(Class<T> format, Object content);
    
    /**
     * @return the set of currently encapsulated content formats
     */
    Set<Class> getContentFormats();
        
}
