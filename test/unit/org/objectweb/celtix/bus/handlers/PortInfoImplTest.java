package org.objectweb.celtix.bus.handlers;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class PortInfoImplTest extends TestCase {

    private PortInfoImpl p1 = new PortInfoImpl(new QName("http://foo.bar.com/test", "Port"),
                                       new QName("http://foo.bar.com/test", "Service"),
                                       "bindingid");
                                       
    private PortInfoImpl p2 = new PortInfoImpl(new QName("http://foo.bar.com/test", "Port"),
                                       new QName("http://foo.bar.com/test", "Service"),
                                       "bindingid");
    private PortInfoImpl p3 = new PortInfoImpl(new QName("http://foo.bar.com/test", "Port"),
                                       new QName("http://foo.bar.com/test", "Service"),
                                       "bindingid");
    private PortInfoImpl p4 = new PortInfoImpl(new QName("http://foo.bar.com/test", "Port1"),
                                       new QName("http://foo.bar.com/test", "Service"),
                                       "bindingid");

    public void testConstructor() {
        
        try {
            new PortInfoImpl(null, new QName("", ""), "");
            fail("did not get expected exception");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
        try {
            new PortInfoImpl(new QName("", ""), null, "");
            fail("did not get expected exception");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
        // this should not throw exception 
        //
        new PortInfoImpl(new QName("", ""), new QName("", ""), null);
        
    }
    
    public void testAttributes() {

        QName portName = new QName("http://foo.bar.com/test", "Port"); 
        final QName serviceName = new QName("http://foo.bar.com/test", "Service"); 
        final String bindingId = "bindingid"; 
        
        PortInfoImpl pii = new PortInfoImpl(serviceName, portName, bindingId);
        
        assertEquals(bindingId, pii.getBindingID());
        assertEquals(portName, pii.getPortName());
        assertEquals(serviceName, pii.getServiceName());        
    }
    
    public void testEquals() { 
        

        assertEquals(p1, p1);
        assertEquals(p1, p2);
        assertEquals(p2, p3);
        assertEquals(p1, p3);
        assertTrue(!p1.equals(p4));
        assertTrue(!p1.equals(null));
        assertTrue(!p1.equals("foobar"));
    }
    
    public void testHashCode() { 
        
        assertEquals(p1.hashCode(), p2.hashCode());
        assertTrue(p1.hashCode() != p4.hashCode());
    }
}
