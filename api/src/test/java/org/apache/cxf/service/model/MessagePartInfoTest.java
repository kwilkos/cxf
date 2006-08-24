package org.apache.cxf.service.model;


import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class MessagePartInfoTest extends TestCase {
    
        
    private MessagePartInfo messagePartInfo;
        
    public void setUp() throws Exception {
        
        messagePartInfo = new MessagePartInfo(new QName(
            "http://apache.org/hello_world_soap_http", "testMessagePart"), null);
        messagePartInfo.setIsElement(true);
        messagePartInfo.setElementQName(new QName(
            "http://apache.org/hello_world_soap_http/types", "testElement"));
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testName() throws Exception {
        assertEquals(messagePartInfo.getName().getLocalPart(), "testMessagePart");
        assertEquals(messagePartInfo.getName().getNamespaceURI()
                     , "http://apache.org/hello_world_soap_http");
        messagePartInfo.setName(new QName(
            "http://apache.org/hello_world_soap_http1", "testMessagePart1"));
        assertEquals(messagePartInfo.getName().getLocalPart(), "testMessagePart1");
        assertEquals(messagePartInfo.getName().getNamespaceURI()
                     , "http://apache.org/hello_world_soap_http1");
        
    }

    public void testElement() {
        assertTrue(messagePartInfo.isElement());
        assertEquals(messagePartInfo.getElementQName().getLocalPart(), "testElement");
        assertEquals(messagePartInfo.getElementQName().getNamespaceURI(),
                     "http://apache.org/hello_world_soap_http/types");
        assertNull(messagePartInfo.getTypeQName());
    }
    
    public void testType() {
        messagePartInfo.setTypeQName(new QName(
            "http://apache.org/hello_world_soap_http/types", "testType"));
        assertNull(messagePartInfo.getElementQName());
        assertFalse(messagePartInfo.isElement());
        assertEquals(messagePartInfo.getTypeQName().getLocalPart(), "testType");
        assertEquals(messagePartInfo.getTypeQName().getNamespaceURI(),
                     "http://apache.org/hello_world_soap_http/types");
    }
}
