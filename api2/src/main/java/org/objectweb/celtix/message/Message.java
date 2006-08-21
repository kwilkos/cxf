package org.objectweb.celtix.message;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.objectweb.celtix.interceptors.InterceptorChain;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.Destination;

public interface Message extends Map<String, Object> {
    
    String TRANSPORT = "org.objectweb.celtix.transport";    
    String REQUESTOR_ROLE = "org.objectweb.celtix.client";
    String ONEWAY_MESSAGE = "org.objectweb.celtix.transport.isOneWayMessage";

    String ATTACHMENT_DESERIALIZER = "org.objectweb.celtix.databinding.attachments.AttachmentDeserializer";
    String ATTACHMENT_SERIALIZER = "org.objectweb.celtix.databinding.attachments.AttachmentSerializer";

    String INBOUND_MESSAGE = "org.objectweb.celtix.message.inbound";
    String INVOCATION_OBJECTS = "org.objectweb.celtix.invocation.objects";
    
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
