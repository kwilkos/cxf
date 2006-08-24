package org.objectweb.celtix.service.model;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class BindingMessageInfoTest extends TestCase {

    private BindingMessageInfo bindingMessageInfo;
    
    public void setUp() throws Exception {
        MessageInfo messageInfo = new MessageInfo(null, new QName(
              "http://objectweb.org/hello_world_soap_http", "testMessage"));
        bindingMessageInfo = new BindingMessageInfo(messageInfo, null);
    }
    
    public void testMessage() {
        assertNotNull(bindingMessageInfo.getMessageInfo());
        assertEquals(bindingMessageInfo.getMessageInfo().getName().getLocalPart(), "testMessage");
        assertEquals(bindingMessageInfo.getMessageInfo().getName().getNamespaceURI(),
              "http://objectweb.org/hello_world_soap_http");
        assertNull(bindingMessageInfo.getBindingOperation());
    }
}
