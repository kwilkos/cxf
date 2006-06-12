package org.objectweb.celtix.bus.ws.rm.persistence;

import java.io.InputStream;
import java.lang.reflect.Method;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bus.bindings.soap.SOAPBindingImpl;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.hello_world_soap_http.Greeter;

public class PersistenceUtilsTest extends TestCase {

    public void testContext() throws Exception {
        
        SOAPBindingImpl binding = new SOAPBindingImpl(false);
        ObjectMessageContextImpl objContext = new ObjectMessageContextImpl();
        objContext.setMethod(getMethod(Greeter.class, "greetMe"));
        SOAPMessageContext soapContext = (SOAPMessageContext)binding.createBindingMessageContext(objContext);
        soapContext.put(ObjectMessageContext.MESSAGE_INPUT, false);
        String arg0 = new String("TestSOAPInputPMessage");
        objContext.setMessageObjects(arg0);

        binding.marshal(objContext,
                         soapContext,
                         new JAXBDataBindingCallback(objContext.getMethod(),
                                                     DataBindingCallback.Mode.PARTS,
                                                     null));
        SOAPMessage msg = soapContext.getMessage();
        
        PersistenceUtils pu = new PersistenceUtils();
        InputStream is = pu.getContextAsInputStream(soapContext);
        assert null != is;        
        MessageContext restored = pu.getContext(is); 
        assertEquals(3, restored.keySet().size());
        assertEquals(soapContext.get(ObjectMessageContext.MESSAGE_INPUT), 
                     restored.get(ObjectMessageContext.MESSAGE_INPUT));
        Object[] params = (Object[])soapContext.get(ObjectMessageContext.METHOD_PARAMETERS);
        Object[] restoredParams = (Object[])restored.get(ObjectMessageContext.METHOD_PARAMETERS);
        assertEquals(params.length, restoredParams.length);
        assertEquals(params[0], restoredParams[0]);
        SOAPMessage restoredMsg = ((SOAPMessageContext)binding.
            createBindingMessageContext(objContext)).getMessage();
        assertEquals(msg.getSOAPBody().getChildNodes().getLength(), 
                     restoredMsg.getSOAPBody().getChildNodes().getLength());
        assertNull(msg.getSOAPHeader());
        assertNull(restoredMsg.getSOAPHeader());
        
        InputStream is2 = pu.getContextAsInputStream(restored);
        is.reset();
        is2.reset();        
        assertEquals(is.available(), is2.available());
    }

    private Method getMethod(Class<?> clazz, String methodName) throws Exception {
        Method[] declMethods = clazz.getDeclaredMethods();
        for (Method method : declMethods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
        
}
