package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Method;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Node;

/**
 * JAXBEncoderDecoder
 * @author apaibir
 */
public class JAXBEncoderDecoder {
    final JAXBContext context;
    final String contextPath;
    
    public JAXBEncoderDecoder(String packageName) throws SOAPException {
        contextPath = packageName;
        try {
            context = JAXBContext.newInstance(packageName);
        } catch (Exception ex) {
            throw new SOAPException("Could not create JAXB Context", ex);
        }
    }
    
    public void marshall(Object elValue, QName elNname,  Node destNode) throws SOAPException {
        try {
            Marshaller u = context.createMarshaller();
            u.setProperty(Marshaller.JAXB_ENCODING , "UTF-8");
            u.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            u.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            Object mObj = elValue;

            Class<?> objectFactory = Class.forName(contextPath + ".ObjectFactory");
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
            u.marshal(mObj, destNode);
        } catch (Exception ex) {
            throw new SOAPException("Marshalling Error", ex);
        }
    }
        
    public Object unmarshall(Node srcNode, QName elName) throws SOAPException {
        try {
            Unmarshaller u = context.createUnmarshaller();
        
            Object o = u.unmarshal(srcNode);
            if (o instanceof JAXBElement<?>) {
                JAXBElement<?> el = (JAXBElement<?>)o;
                if (el.getName().equals(elName)) {
                    return el.getValue();
                }
            }
        } catch (Exception ex) {
            throw new SOAPException("Unmarshalling error", ex);
        }
        return null;
    }
}
