package org.objectweb.celtix.bus.jaxws;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Node;

/**
 * JAXBEncoderDecoder
 * @author apaibir
 */
public final class JAXBEncoderDecoder {
    private JAXBEncoderDecoder() {        
    }
    
    public static void marshall(Object elValue, QName elNname,  Node destNode) {
        
        try {
            JAXBContext context = JAXBContext.newInstance(elValue.getClass());
            
            Object mObj = elValue;
            Marshaller u = context.createMarshaller();
            u.setProperty(Marshaller.JAXB_ENCODING , "UTF-8");
            u.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            u.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            if (elValue.getClass().isAnnotationPresent(XmlRootElement.class)) {
                String packageName = elValue.getClass().getPackage().getName();
                Class<?> objectFactory = Class.forName(packageName + ".ObjectFactory");
                Method methods[] = objectFactory.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getParameterTypes().length == 1
                        && method.getParameterTypes()[0].equals(elValue.getClass())) {

                        XmlElementDecl elementDecl = method.getAnnotation(XmlElementDecl.class);
                        if (null != elementDecl) {
                            QName elementType = new QName(elementDecl.namespace(), elementDecl.name());
                            if (elementType.equals(elNname)) {
                                mObj = method.invoke(objectFactory.newInstance(),
                                                    elValue);                        
                            }
                        }
                    } 
                }
            } else {
                mObj = JAXBElement.class.getConstructor(new Class[] {QName.class, Class.class, Object.class})
                    .newInstance(elNname, mObj.getClass(), mObj);
            }

            u.marshal(mObj, destNode);
        } catch (Exception ex) {
            throw new WebServiceException("Marshalling Error", ex);
        }
    }
    
    public static Object unmarshall(Node srcNode, QName elName, Class<?> clazz) {
        Object obj = null;
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller u = context.createUnmarshaller();

            obj = (clazz != null) ? u.unmarshal(srcNode, clazz) : u.unmarshal(srcNode);
            
            if (obj instanceof JAXBElement<?>) {
                JAXBElement<?> el = (JAXBElement<?>)obj;
                if (el.getName().equals(elName)) {
                    obj = el.getValue();
                }
            }
        } catch (Exception ex) {
            throw new WebServiceException("Unmarshalling error", ex);
        }
        return obj;
    }

    public static Object unmarshall(JAXBContext context, Node srcNode, QName elName) {
        Object obj = null;
        try {
            Unmarshaller u = context.createUnmarshaller();

            obj = u.unmarshal(srcNode);
            
            if (obj instanceof JAXBElement<?>) {
                JAXBElement<?> el = (JAXBElement<?>)obj;
                if (el.getName().equals(elName)) {
                    obj = el.getValue();
                }
            }
        } catch (Exception ex) {
            throw new WebServiceException("Unmarshalling error", ex);
        }
        return obj;
    }
    
    public static Class getClassFromType(Type t) {
        if (t instanceof Class) {
            return (Class)t;
        } else if (t instanceof GenericArrayType) {
            GenericArrayType g = (GenericArrayType)t;
            return Array.newInstance(getClassFromType(g.getGenericComponentType()), 0).getClass();
        } else if (t instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType)t;
            return getClassFromType(p.getRawType());
        }
        //TypeVariable and WildCardType are not handled as it is unlikely such Types will 
        // JAXB Code Generated.
        assert false;
        throw new IllegalArgumentException("Cannot get Class object from unknown Type");
    }

}
