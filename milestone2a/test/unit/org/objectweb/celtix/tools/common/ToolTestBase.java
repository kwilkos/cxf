package org.objectweb.celtix.tools.common;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;

import junit.framework.TestCase;

public abstract class ToolTestBase extends TestCase {

    protected PrintStream oldStdErr; 
    protected PrintStream oldStdOut; 
    protected URL wsdlLocation; 
    
    protected ByteArrayOutputStream errOut = new ByteArrayOutputStream(); 
    protected ByteArrayOutputStream stdOut = new ByteArrayOutputStream(); 

    public void setUp() { 
        
        oldStdErr = System.err; 
        oldStdOut = System.out;
        
        System.setErr(new PrintStream(errOut));
        System.setOut(new PrintStream(stdOut));
        
        wsdlLocation = ToolTestBase.class.getResource("/wsdl/hello_world.wsdl");
    }
    
    public void tearDown() { 
        
        System.setErr(oldStdErr);
        System.setOut(oldStdOut);
    }
    
    protected String getStdOut() {
        return new String(stdOut.toByteArray());
    }
    

}

