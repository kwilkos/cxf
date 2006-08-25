package org.objectweb.celtix.bus.jaxws;

import java.util.Map;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;

public class BindingProviderImplTest extends TestCase {
    
    private ObjectMessageContext objectCtx;
    private BindingProviderImpl bindingProviderImpl;
    private Binding binding;
    private IMocksControl control;

    public void setUp() throws Exception {
        objectCtx = new ObjectMessageContextImpl();
        bindingProviderImpl = new BindingProviderImpl();
        control = EasyMock.createNiceControl();
        binding = control.createMock(Binding.class);
    }
    
    public void testResponseContext() {
        objectCtx.put("foo", new String("fooObject"));
        objectCtx.setScope("foo", MessageContext.Scope.APPLICATION);
        bindingProviderImpl.populateResponseContext(objectCtx);
        Map<String, Object> map = bindingProviderImpl.getResponseContext();
        String str = (String)map.get("foo");
        assertTrue(str.equals("fooObject"));     
    }
    
    public void testBinding() {
        bindingProviderImpl.setBinding(binding);
        Binding thebinding = bindingProviderImpl.getBinding();
        assertEquals("Objects should be equal", binding, thebinding);
    }
    
    public void testRequestContext() {
        Map<String, Object> map = bindingProviderImpl.getRequestContext();
        assertNotNull(map);        
    }
    
    

}
