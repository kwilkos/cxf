package org.objectweb.celtix.helpers;

import junit.framework.TestCase;

public class NameSpaceTest extends TestCase {
    
    private final String myURL1 = "http://test.objectweb.com/testurl1";
    private final String myURL2 = "http://test.objectweb.com/testurl2";
    private final String myCustomURL = "http://test.objectweb.com/custom-prefix-url";
    private final String myOwnPrefix = "myown-prefix";
    

    public NameSpaceTest(String arg0) {
        super(arg0);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(NameSpaceTest.class);
    }

    public void testNSStackOperations() throws Exception {
        NSStack  nsStackObj = new NSStack();
        
        nsStackObj.push();
        
        nsStackObj.add(myURL1);
        nsStackObj.add(myOwnPrefix, myCustomURL);
        nsStackObj.add(myURL2);
        
        assertEquals(myURL1, nsStackObj.getURI("ns1"));
        assertEquals(myCustomURL, nsStackObj.getURI(myOwnPrefix));
        assertEquals(myURL2, nsStackObj.getURI("ns2"));
        assertNull(nsStackObj.getURI("non-existent-prefix"));
        
        assertEquals("ns2", nsStackObj.getPrefix(myURL2));
        assertEquals(myOwnPrefix, nsStackObj.getPrefix(myCustomURL));
        assertEquals("ns1", nsStackObj.getPrefix(myURL1));
        assertNull(nsStackObj.getPrefix("non-existent-prefix"));
        
        nsStackObj.pop();
        assertNull(nsStackObj.getPrefix("non-existent-prefix"));
        assertNull(nsStackObj.getPrefix(myCustomURL));
    }
    
    public void testNSDeclOperaions() throws Exception {
        NSDecl nsDecl1 = new NSDecl(myOwnPrefix, myCustomURL);
        NSDecl nsDecl2 = new NSDecl("ns2", myURL2);
        NSDecl nsDecl3 = new NSDecl(myOwnPrefix, myCustomURL);
        
        assertFalse(nsDecl2.equals(nsDecl1));
        assertTrue(nsDecl3.equals(nsDecl1));
        
    }
}
