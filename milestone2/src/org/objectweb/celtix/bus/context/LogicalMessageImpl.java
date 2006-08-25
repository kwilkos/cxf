package org.objectweb.celtix.bus.context;





import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.Source;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.bindings.ObjectMessageContextImpl;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.helpers.WrapperHelper;

public class LogicalMessageImpl implements LogicalMessage {

    private static final Logger LOG = LogUtils.getL7dLogger(LogicalMessageImpl.class); 

    private final LogicalMessageContextImpl msgContext;
    
    public LogicalMessageImpl(LogicalMessageContextImpl lmctx) {
        msgContext = lmctx;
    }

    public Source getPayload() {
        throw new UnsupportedOperationException("getPayload");
    }

    public void setPayload(Source arg0) {
        throw new UnsupportedOperationException("setPayload");
    }

    public Object getPayload(JAXBContext jaxbCtx) {

        if (msgContext.get(ObjectMessageContext.MESSAGE_PAYLOAD) == null) { 
            buildMessagePayload();
        }

        return msgContext.get(ObjectMessageContext.MESSAGE_PAYLOAD);
    }

    public void setPayload(Object payload, JAXBContext ctx) {
        msgContext.put(ObjectMessageContext.MESSAGE_PAYLOAD, payload);
        writePayloadToContext(payload);
    }


    private void buildMessagePayload() {

        Object payload = null; 

        if (isRequest()) { 
            Object[] args = (Object[])msgContext.get(ObjectMessageContextImpl.METHOD_PARAMETERS);
            if (args == null || args.length == 0) {
                
                // no arguments expected in message, so leave payload as
                // null and return
                assert msgContext.get(ObjectMessageContext.MESSAGE_PAYLOAD) == null;
                return;
            } 
            payload = buildPayloadFromRequest(args);
        } else {
            payload = buildPayloadFromResponse();
        }
        msgContext.put(ObjectMessageContext.MESSAGE_PAYLOAD, payload);
    }


    private Object buildPayloadFromResponse() { 
        Method m = (Method)msgContext.get(ObjectMessageContextImpl.METHOD_OBJ);
        // TODO -- add support for 'out' params
        //
        if (!Void.TYPE.equals(m.getReturnType())) {
            ResponseWrapper ann = m.getAnnotation(ResponseWrapper.class); 
            assert ann != null : "ResponseWrapper is null";
            WebResult wr = m.getAnnotation(WebResult.class);
            assert wr != null : "WebResult is null for method " + m; 

            Object returnVal = msgContext.get(ObjectMessageContextImpl.METHOD_RETURN);
            
            // if a handler has aborted the processing sequence, the
            // return type may be null 
            if (returnVal != null) {
                Object wrapper = createWrapperInstance(ann.className()); 
                setWrapperValue(wrapper, wr.name(), returnVal);
                return wrapper;
            }
        }
        return null;
    } 


    private Object buildPayloadFromRequest(Object[] args) { 

        RequestWrapper ann = getMethod().getAnnotation(RequestWrapper.class);
        assert ann != null : "failed to get request wrapper annotation"; 
            
        Object wrapper = createWrapperInstance(ann.className());
        int argIndex = 0;

        Collection<WebParam> annotations = getWebParamAnnotations(getMethod()); 

        for (WebParam wp : annotations) {
            setWrapperValue(wrapper, wp.name(), args[argIndex++]);
        }

        return wrapper;
    }


    private Object createWrapperInstance(String className) { 
        try {
            Class<?> wrapperClass = Class.forName(className, true, 
                                                  LogicalMessageContextImpl.class.getClassLoader());
            return wrapperClass.newInstance();
        } catch (IllegalAccessException ex) {
            LOG.log(Level.SEVERE, "WRAPPER_MISSING_DEFAULT_CTOR_MSG", ex); 
        } catch (InstantiationException ex) {
            LOG.log(Level.SEVERE, "WRAPPER_INSTANTIATION_FAILURE_MSG", ex); 
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "WRAPPER_LOAD_FAILURE_MSG", ex); 
        }
        // should never get here, I think
        assert false : "unable to create wrappper " + className;
        return null;
    } 


    private void writeRequestToContext(Object payload) { 
        Method method = getMethod();
        Object[] args = new Object[method.getParameterTypes().length]; 
        Collection<WebParam> annotations = getWebParamAnnotations(method);
            
        int i = 0;
        for (WebParam wp : annotations) {
            args[i++] = getAttributeFromWrapper(payload, wp.name());
        }

        msgContext.put(ObjectMessageContextImpl.METHOD_PARAMETERS, args);
    }

    private void writeResponseToContext(Object payload) { 
    
        WebResult wr = getMethod().getAnnotation(WebResult.class);
        assert wr != null : "WebResult is null for method " + getMethod(); 

        Object retVal = getAttributeFromWrapper(payload, wr.name());
        msgContext.put(ObjectMessageContextImpl.METHOD_RETURN, retVal);
    }

    private void writePayloadToContext(Object payload) { 

        if (isRequestPayload(payload)) {
            writeRequestToContext(payload);
        } else {
            writeResponseToContext(payload);
        }
    }

    private Object getAttributeFromWrapper(Object wrapper, String name) {
        assert wrapper != null; 
        assert name != null; 

        Object ret = null; 
        try {
            String getterName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            Method getter = wrapper.getClass().getMethod(getterName); 
            ret = getter.invoke(wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert ret != null :  "unable to get attribute: " + name + " from " + wrapper; 
        return ret;
    }


    private boolean isRequestPayload(Object payload) { 
        
        Method m = getMethod();

        RequestWrapper reqWrapper = m.getAnnotation(RequestWrapper.class);
        ResponseWrapper respWrapper = m.getAnnotation(ResponseWrapper.class);
        
        if (reqWrapper != null) {
            return payload.getClass().getName().equals(reqWrapper.className());
        } else if (respWrapper != null) {
            return !payload.getClass().getName().equals(respWrapper.className());
        }
        return true;
    } 

    
    private boolean isRequest() { 

        Boolean isInputMsg = (Boolean)msgContext.get(ObjectMessageContext.MESSAGE_INPUT); 
        Boolean isOutbound = (Boolean)msgContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY); 

        assert isOutbound != null : "MessageContext.MESSAGE_OUTBOUND must be set in context";
        assert isInputMsg != null : "ObjectMessageContext.MESSAGE_INPUT must be set in context " + msgContext;

        return (isOutbound && !isInputMsg) || (!isOutbound && !isInputMsg);
    } 

    private void setWrapperValue(Object wrapper, String name, Object value) { 

        try {
            WrapperHelper.setWrappedPart(name, wrapper, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Collection<WebParam> getWebParamAnnotations(Method m) { 
        
        Collection<WebParam> ret = new ArrayList<WebParam>();  

        for (Annotation[] anns : m.getParameterAnnotations()) {
            for (Annotation a : anns) {
                if (a instanceof WebParam) {
                    ret.add((WebParam)a);
                }
            }
        }
        return ret;
    }

    private Method getMethod() { 
        Method m = (Method)msgContext.get(ObjectMessageContextImpl.METHOD_OBJ);
        assert m != null : "failed to get method from ObjectMessageContext";
        return m;
    }
}
