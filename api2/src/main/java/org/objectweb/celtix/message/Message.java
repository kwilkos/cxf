package org.objectweb.celtix.message;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.objectweb.celtix.channels.Channel;
import org.objectweb.celtix.interceptors.InterceptorChain;

public interface Message extends Map<String, Object> {
    
    String BINDING_INFO = "org.objectweb.celtix.service.model.binding";
    String SERVICE_INFO = "org.objectweb.celtix.service.model.service";
    String INTERFACE_INFO = "org.objectweb.celtix.service.model.interface";
    String OPERATION_INFO = "org.objectweb.celtix.service.model.operation";
    String BINDING = "org.objectweb.celtix.binding";
    String TRANSPORT = "org.objectweb.celtix.transport";
    String REQUESTOR_ROLE = "org.objectweb.celtix.client";
    
    
    String BUS = "org.objectweb.celtix.bus";
    
    String getId();
    
    InterceptorChain getInterceptorChain();
    
    Channel getChannel();
    
    Exchange getExchange();
    
    Collection<Attachment> getAttachments();
    
    
    <T> T getSource(Class<T> format);
    
    <T> void setSource(Class<T> format, Object content);
    
    Set<Class> getSourceFormats();
    
    
    <T> T getResult(Class<T> format);
    
    <T> void setResult(Class<T> format, Object content);
    
    Set<Class> getResultFormats();
}
