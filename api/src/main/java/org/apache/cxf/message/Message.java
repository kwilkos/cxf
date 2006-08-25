package org.apache.cxf.message;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;

public interface Message extends Map<String, Object> {
    
    String TRANSPORT = "org.apache.cxf.transport";    
    String REQUESTOR_ROLE = "org.apache.cxf.client";
    String ONEWAY_MESSAGE = "org.apache.cxf.transport.isOneWayMessage";

    String ATTACHMENT_DESERIALIZER = "org.apache.cxf.databinding.attachments.AttachmentDeserializer";
    String ATTACHMENT_SERIALIZER = "org.apache.cxf.databinding.attachments.AttachmentSerializer";

    String INBOUND_MESSAGE = "org.apache.cxf.message.inbound";
    String INVOCATION_OBJECTS = "org.apache.cxf.invocation.objects";
    
    String getId();
    
    InterceptorChain getInterceptorChain();
    void setInterceptorChain(InterceptorChain chain);
    
    /**
     * @return the associated Conduit if message is outbound, null otherwise
     */
    Conduit getConduit();

    /**
     * @return the associated Destination if message is inbound, null otherwise
     */
    Destination getDestination();
    
    Exchange getExchange();

    void setExchange(Exchange exchange);
    
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
    Set<Class<?>> getContentFormats();   
    
    
    
    /**
     * Convienience method for storing/retrieving typed objects from the map.
     * equivilent to:  (T)get(key.getName());
     * @param <T> key
     * @return
     */
    <T> T get(Class<T> key);
    /**
     * Convienience method for storing/retrieving typed objects from the map.
     * equivilent to:  put(key.getName(), value);
     * @param <T> key
     * @return
     */
    <T> void put(Class<T> key, T value);    
}
