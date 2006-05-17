package org.objectweb.celtix.bus.handlers;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.HandlerChain;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.handlers.HandlerChainBuilder;

public class AnnotationHandlerChainBuilder extends HandlerChainBuilder {

    static final Logger LOG = LogUtils.getL7dLogger(AnnotationHandlerChainBuilder.class);
    
    public AnnotationHandlerChainBuilder() {
        // emtpy
    }
    
    public AnnotationHandlerChainBuilder(Bus bus) {
        super(bus);
    }
    
   
    public List<Handler> buildHandlerChainFor(Class<?> clz, List<Handler> existingHandlers) { 

        LOG.fine("building handler chain");
        HandlerChainAnnotation hcAnn = findHandlerChainAnnotation(clz); 
        List<Handler> chain = null;
        if (hcAnn == null) { 
            LOG.fine("no HandlerChain annotation on " + clz); 
            chain = new ArrayList<Handler>();
        } else {
            hcAnn.validate(); 
            
            HandlerChainDocument doc = getHandlerChainDocument(hcAnn);
            HandlerChainType hc = doc.getChain(hcAnn.getChainName());
            
            if (null == hc) {
                throw new WebServiceException(new Message("CHAIN_NOT_SPECIFIED_EXC", LOG).toString());
            } 

            chain =  buildHandlerChain(hc, clz.getClassLoader()); 
        }
        assert chain != null;
        if (existingHandlers != null) { 
            chain.addAll(existingHandlers);
        } 
        return sortHandlers(chain);
    } 

    public List<Handler> buildHandlerChainFor(Class<?> clz) { 
        return buildHandlerChainFor(clz, null);
    } 

    private HandlerChainAnnotation findHandlerChainAnnotation(Class<?> clz) { 

        HandlerChain ann = clz.getAnnotation(HandlerChain.class); 
        Class<?> declaringClass = clz; 

        if (ann == null) { 
            for (Class<?> iface : clz.getInterfaces()) { 
                if (LOG.isLoggable(Level.FINE)) { 
                    LOG.fine("checking for HandlerChain annotation on " + iface.getName());
                }
                ann = iface.getAnnotation(HandlerChain.class);
                if (ann != null) { 
                    declaringClass = iface;
                    break;
                }
            }
        } 
        if (ann != null) { 
            return new HandlerChainAnnotation(ann, declaringClass);
        } else { 
            return null;
        }
    } 

    private static class HandlerChainAnnotation { 
        private final Class<?> declaringClass; 
        private final HandlerChain ann; 
        
        HandlerChainAnnotation(HandlerChain hc, Class<?> clz) { 
            ann = hc; 
            declaringClass = clz;
        } 

        public Class<?> getDeclaringClass() { 
            return declaringClass; 
        } 

        public String getFileName() { 
            return ann.file(); 
        } 

        public String getChainName() { 
            return ann.name(); 
        }
        
        public void validate() { 
            if (null == ann.file() || "".equals(ann.file())) {
                throw new WebServiceException(new Message("ANNOTATION_WITHOUT_URL_EXC", LOG).toString());   
            } 
            if (null == ann.name() || "".equals(ann.name())) {
                LOG.fine("no handler name specified, defaulting to first declared");
            } 
        } 

        public String toString() { 
            return "[" + declaringClass + "," + ann + "]";
        }
    } 
     
    private HandlerChainDocument getHandlerChainDocument(HandlerChainAnnotation hcAnn) {
        InputStream in = hcAnn.getDeclaringClass().getResourceAsStream(hcAnn.getFileName());

        if (null == in) {
            throw new WebServiceException(new Message("HANDLER_CFG_FILE_NOT_FOUND_EXC", LOG, 
                                                      hcAnn.getFileName()).toString()); 
        }

        LOG.log(Level.INFO, "reading handler chain configuration from " + hcAnn.getFileName());
        return new HandlerChainDocument(in, true);
    }
}
