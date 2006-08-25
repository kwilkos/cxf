package org.objectweb.celtix.service.model;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class MessageInfoTest extends TestCase {
    
    private MessageInfo messageInfo;
    
    public void setUp() throws Exception {
        messageInfo = new MessageInfo(null, new QName(
            "http://objectweb.org/hello_world_soap_http", "testMessage"));
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testName() throws Exception {
        assertEquals(messageInfo.getName().getLocalPart(), "testMessage");
        assertEquals(messageInfo.getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
    }
    
    public void testMessagePartInfo() throws Exception {
        QName qname = new QName(
                                "http://objectweb.org/hello_world_soap_http", "testMessagePart");
        
        messageInfo.addMessagePart(qname);
        assertEquals(messageInfo.getMessageParts().size(), 1);
        MessagePartInfo messagePartInfo = messageInfo.getMessagePart(qname);
        assertEquals(messagePartInfo.getName().getLocalPart(), "testMessagePart");
        assertEquals(messagePartInfo.getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
        assertEquals(messagePartInfo.getMessageInfo(), messageInfo);
        messagePartInfo = new MessagePartInfo(new QName(
             "http://objectweb.org/hello_world_soap_http", "testMessagePart"), messageInfo);
        messageInfo.addMessagePart(messagePartInfo);
        //add two same part, so size is still 1
        assertEquals(messageInfo.getMessageParts().size(), 1);
        messagePartInfo = new MessagePartInfo(new QName(
            "http://objectweb.org/hello_world_soap_http", "testMessagePart2"), messageInfo);
        messageInfo.addMessagePart(messagePartInfo);
        assertEquals(messageInfo.getMessageParts().size(), 2);
    }

}
