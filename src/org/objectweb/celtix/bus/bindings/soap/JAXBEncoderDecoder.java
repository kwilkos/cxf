package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Method;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Node;

/**
 * JAXBEncoderDecoder
 * @author apaibir
 */
public final class JAXBEncoderDecoder {
    private JAXBEncoderDecoder() {        
    }
    
    public static void marshall(Object elValue, QName elNname,  Node destNode) throws SOAPException {
        
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
            throw new SOAPException("Marshalling Error", ex);
        }
    }
    
    public static Object unmarshall(Node srcNode, QName elName, Class<?> clazz) throws SOAPException {
        Object obj = null;
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller u = context.createUnmarshaller();
            JAXBElement<?> el = u.unmarshal(srcNode, clazz);
            if (el.getName().equals(elName)) {
                obj = el.getValue();
            }
        } catch (Exception ex) {
            throw new SOAPException("Unmarshalling error", ex);
        }
        return obj;
    }    
}
