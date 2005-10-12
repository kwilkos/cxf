package org.objectweb.celtix.bus.context;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.MessageContextWrapper;

public class MessageContextWrapperTest extends AbstractMessageContextTestBase {

    private GenericMessageContext wrapped;
    private MessageContextWrapper wrapper; 
    
    @Override
    protected MessageContext getMessageContext() {
        wrapped = new GenericMessageContext();
        wrapper = new MessageContextWrapper(wrapped);
        return wrapper;
    }

    
    public void setUp() { 
        super.setUp();               
    }
    
    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.size()'
     */
    public void testSize() {

        assertEquals(wrapped.size(), wrapper.size());
        wrapper.put("foo", "bar");
        assertEquals(wrapped.size(), wrapper.size());
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.isEmpty()'
     */
    public void testIsEmpty() {

        assertEquals(wrapped.isEmpty(), wrapper.isEmpty());
        wrapper.put("foo", "bar");
        assertEquals(wrapped.isEmpty(), wrapper.isEmpty());
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.containsKey(Object)'
     */
    public void testContainsKey() {

        assertEquals(wrapped.containsKey("foo"), wrapper.containsKey("foo"));
        wrapper.put("foo", "bar");
        assertEquals(wrapped.containsKey("foo"), wrapper.containsKey("foo"));
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.containsValue(Object)'
     */
    public void testContainsValue() {

        assertEquals(wrapped.containsValue("bar"), wrapper.containsValue("bar"));
        wrapper.put("foo", "bar");
        assertEquals(wrapped.containsValue("bar"), wrapper.containsValue("bar"));
        
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.get(Object)'
     */
    public void testGetPut() {
        assertEquals(wrapped.get("foo"), wrapper.get("foo"));
        wrapper.put("foo", "bar");
        assertEquals(wrapped.get("foo"), wrapper.get("foo"));
    }


    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.remove(Object)'
     */
    public void testRemove() {

        wrapper.put("foo", "bar");
        wrapper.remove("foo");
        assertEquals(wrapped.containsKey("foo"), wrapper.containsKey("foo"));
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.
     * putAll(Map<? extends String, ? extends Object>)'
     */
    public void testPutAll() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");
        wrapper.putAll(map);
        
        assertTrue(wrapper.containsKey("foo"));
        assertTrue(wrapper.containsValue("bar"));
        assertTrue(wrapped.containsKey("foo"));
        assertTrue(wrapped.containsValue("bar"));
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.clear()'
     */
    public void testClear() {

        wrapper.put("foo", "bar");
        wrapper.clear();
        assertTrue(wrapper.isEmpty());
        assertTrue(wrapped.isEmpty());
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.keySet()'
     */
    public void testKeySet() {
        wrapper.put("foo", "bar");
        assertEquals(1, wrapper.keySet().size());
        assertTrue(wrapper.keySet().contains("foo"));

        assertEquals(1, wrapped.keySet().size());
        assertTrue(wrapped.keySet().contains("foo"));
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.values()'
     */
    public void testValues() {
        wrapper.put("foo", "bar");
        assertEquals(1, wrapper.values().size());
        assertTrue(wrapper.values().contains("bar"));

        assertEquals(1, wrapped.values().size());
        assertTrue(wrapped.values().contains("bar"));

    }

    /*
     * Test method for 'org.objectweb.celtix.bus.context.MessageContextWrapper.entrySet()'
     */
    public void testEntrySet() {
        
        wrapper.put("foo", "bar");
        assertEquals(wrapped.entrySet(), wrapper.entrySet());
    }

}
