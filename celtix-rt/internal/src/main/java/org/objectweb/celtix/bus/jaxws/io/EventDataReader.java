package org.objectweb.celtix.bus.jaxws.io;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Future;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bus.jaxws.ClassHelper;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.jaxb.JAXBUtils;

public class EventDataReader<T> implements DataReader<T> {
    final JAXBDataBindingCallback callback;

    public EventDataReader(JAXBDataBindingCallback cb) {
        callback = cb;
    }

    public Object read(int idx, T input) {
        return read(null, idx, input);
    }

    public Object read(QName name, int idx, T input) {
        Class<?> cls;
        if (idx == -1) {
            cls = callback.getMethod().getReturnType();
        } else {
            cls = callback.getMethod().getParameterTypes()[idx];
            if (cls.isAssignableFrom(Holder.class)) {
                //INOUT and OUT Params are mapped to Holder<T>.
                Type[] genericParameterTypes = callback.getMethod().getGenericParameterTypes();
                //ParameterizedType represents Holder<?>
                ParameterizedType paramType = (ParameterizedType)genericParameterTypes[idx];
                cls = JAXBEncoderDecoder.getClassFromType(
                                         paramType.getActualTypeArguments()[0]);
            }
        }
        
        XMLEventReader reader = (XMLEventReader)input;

        return JAXBEncoderDecoder.unmarshall(callback.getJAXBContext(),
            callback.getSchema(), reader, name, cls);
    }

    public void readWrapper(ObjectMessageContext objCtx, boolean isOutBound, T input) {
        XMLEventReader reader = (XMLEventReader)input;
        String wrapperType = isOutBound ? callback.getResponseWrapperType()
                                        : callback.getRequestWrapperType();

        QName elName = isOutBound ? callback.getResponseWrapperQName()
                                  : callback.getRequestWrapperQName();

        try {
            XMLEvent evnt = reader.peek();            
            while (null != evnt 
                   && evnt.getEventType() != XMLEvent.START_ELEMENT) {
                evnt = reader.nextEvent();
            }
            
            if (null == evnt) {
                throw new WebServiceException("START_ELEMENT XMLEvent not present on the stream.");    
            }
        } catch (XMLStreamException xse) {
            throw new WebServiceException("START_ELEMENT XMLEvent not present on the stream.", xse);
        }

        Object obj = null;
        try {
            obj = JAXBEncoderDecoder.unmarshall(callback.getJAXBContext(),
                                                callback.getSchema(), reader,
                                                elName, ClassHelper.forName(wrapperType));
        } catch (ClassNotFoundException e) {
            throw new WebServiceException("Could not unmarshall wrapped type (" + wrapperType + ") ", e);
        }

        if (isOutBound && callback.getWebResult() != null) {
            Method method = callback.getMethod();
            if (JAXBUtils.isAsync(method)) {
                Method syncMethod = callback.getSyncMethod();
                Type gtype = method.getGenericReturnType();

                if (Future.class.equals(method.getReturnType())) {
                    Type types[] = method.getGenericParameterTypes();
                    if (types.length > 0 && types[types.length - 1] instanceof ParameterizedType) {
                        gtype = types[types.length - 1];
                    }
                }
                if (gtype instanceof ParameterizedType
                    && ((ParameterizedType)gtype).getActualTypeArguments().length == 1
                    && ((ParameterizedType)gtype).getActualTypeArguments()[0] instanceof Class) {
                    Class cls = (Class)((ParameterizedType)gtype).getActualTypeArguments()[0];
                    if (cls.getName().equals(wrapperType)) {
                        syncMethod = null;
                    }
                }
                method = syncMethod;
            }
            if (method != null) {
                Object retVal = callback.getWrappedPart(callback.getWebResultQName().getLocalPart(),
                                                        obj,
                                                        method.getReturnType());
                objCtx.setReturn(retVal);
            } else {
                objCtx.setReturn(obj);
            }
        }

        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = callback.getMethod().getParameterTypes().length;
        Object[] methodArgs = objCtx.getMessageObjects();
        try {
            for (int idx = 0; idx < noArgs; idx++) {
                WebParam param = callback.getWebParam(idx);
                if ((param.mode() != ignoreParamMode) && !param.header()) {
                    Class<?> cls = callback.getMethod().getParameterTypes()[idx];
                    if (param.mode() != WebParam.Mode.IN) {
                        //INOUT and OUT Params are mapped to Holder<T>.
                        Type[] genericParameterTypes = callback.getMethod().getGenericParameterTypes();
                        //ParameterizedType represents Holder<?>
                        ParameterizedType paramType = (ParameterizedType)genericParameterTypes[idx];
                        Class<?> c =
                            JAXBEncoderDecoder.getClassFromType(paramType.getActualTypeArguments()[0]);
                        Object partValue = callback.getWrappedPart(param.name(), obj, c);
                        //TO avoid type safety warning the Holder
                        //needs tobe set as below.
                        cls.getField("value").set(methodArgs[idx], partValue);
                    } else {
                        methodArgs[idx] = callback.getWrappedPart(param.name(), obj, cls);
                    }
                }
            }
        } catch (IllegalAccessException iae) {
            throw new WebServiceException("Could not unwrap the parts.", iae);
        } catch (NoSuchFieldException nsfe) {
            throw new WebServiceException("Could not unwrap the parts.", nsfe);
        }
    }
}
