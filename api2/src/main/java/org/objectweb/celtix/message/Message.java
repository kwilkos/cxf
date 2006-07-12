package org.objectweb.celtix.message;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.objectweb.celtix.channels.Channel;
import org.objectweb.celtix.interceptors.InterceptorChain;

public interface Message extends Map<String, Object> {
    
    String INBOUND_EXCEPTION = "INBOUND_EXCEPTION";
    String OUTBOUND_EXCEPTION = "OUTBOUND_EXCEPTION";    
    String SERVICE_MODEL_BINDING = "service.model.binding";
    
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
