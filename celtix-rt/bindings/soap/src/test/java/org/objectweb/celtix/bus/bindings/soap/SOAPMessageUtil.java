package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

public final class SOAPMessageUtil {

    private SOAPMessageUtil() {
        // Utility class - never constructed
    }
    
    public static Method getMethod(Class<?> clazz, String methodName) throws Exception {
        Method[] declMethods = clazz.getDeclaredMethods();
        for (Method method : declMethods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    /* The method creates a object array based on the number of method parameters
     * also creates holder objects for in-out and out params.
     */
    public static Object[] getMessageObjects(Method method) throws Exception {
        int idx = 0;
        Object[] methodArgs = (Object[])
            Array.newInstance(Object.class, method.getParameterTypes().length);
        for (Class<?> cls : method.getParameterTypes()) {
            if (cls.isAssignableFrom(Holder.class)) {
                methodArgs[idx] = cls.newInstance();
            } 
            idx++;
        }

        return methodArgs;
    }

    /* The method wraps inout, out method parameters into javax.xml.ws.Holder<T>
     * and returns a array of objects.
     */
    
    public static Object[] getMessageObjects(Method method, Object... args) throws Exception {
        assert args.length == method.getParameterTypes().length;
        int idx = 0;
        Object[] methodArgs = (Object[])
            Array.newInstance(Object.class, method.getParameterTypes().length);
        for (Class<?> cls : method.getParameterTypes()) {
            if (cls.isAssignableFrom(Holder.class)) {
                methodArgs[idx] = cls.newInstance();
                Holder.class.getField("value").set(methodArgs[idx], args[idx]);
            } else {
                methodArgs[idx] = args[idx];
            }
            idx++;
        }

        return methodArgs;
    }
    
    public static String createWrapDocLitSOAPMessage(QName wrapName, QName elName, String data) {
        StringBuffer str = new StringBuffer();
        
        str.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        str.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        str.append("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" ");
        str.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        str.append("<SOAP-ENV:Body>");

        str.append("<ns4:" + wrapName.getLocalPart() + " xmlns:ns4=\"" + wrapName.getNamespaceURI() + "\">");
        if (elName != null) {
            str.append("<ns4:" + elName.getLocalPart() + ">");
            str.append(data);
            str.append("</ns4:" + elName.getLocalPart() + ">");
        }
        str.append("</ns4:" + wrapName.getLocalPart() + ">");
        
        str.append("</SOAP-ENV:Body>");
        str.append("</SOAP-ENV:Envelope>");
        
        return str.toString();
    }
    
    public static String createRPCLitSOAPMessage(QName  operation, QName elName, String data) {
        StringBuffer str = new StringBuffer();
        
        str.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        str.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        str.append("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" ");
        str.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        str.append("<SOAP-ENV:Body>");

        str.append("<ns4:" + operation.getLocalPart());
        str.append(" xmlns:ns4=\"" + operation.getNamespaceURI() + "\">");
        if (elName != null) {
            str.append("<" + elName.getLocalPart() + ">");
            str.append(data);
            str.append("</" + elName.getLocalPart() + ">");
        }
        str.append("</ns4:" + operation.getLocalPart() + ">");
        
        str.append("</SOAP-ENV:Body>");
        str.append("</SOAP-ENV:Envelope>");
        
        return str.toString();
    }
    
    public static String createBareDocLitSOAPMessage(QName elName, String data) {
        StringBuffer str = new StringBuffer();
        
        str.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        str.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        str.append("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" ");
        str.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        str.append("<SOAP-ENV:Header/>");
        str.append("<SOAP-ENV:Body>");

        str.append("<ns4:" + elName.getLocalPart() + " xmlns:ns4=\"" + elName.getNamespaceURI() + "\">");
        str.append(data);
        str.append("</ns4:" + elName.getLocalPart() + ">");
        
        str.append("</SOAP-ENV:Body>");
        str.append("</SOAP-ENV:Envelope>");
        
        return str.toString();
    }
}
