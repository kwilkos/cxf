package org.objectweb.celtix.tools.wsdl2java;


import org.objectweb.celtix.tools.common.DelegatingToolTestBase;
import org.objectweb.celtix.tools.common.Generator;
import org.objectweb.celtix.tools.common.ToolBase;


public class Wsdl2JavaTest extends DelegatingToolTestBase {
    
    
    public void setUp() { 
        
        super.setUp();
        
    }
    
    public void tearDown() { 
        
        super.tearDown(); 
    }
    
    public void testNoWsdlPrintsError() { 
        
        Wsdl2Java.main(new String[] {});
        assertTrue("error message must be displayed", 
                   getStdOut().contains(Wsdl2JavaMessages.WSDL_NOT_SPECIFIED));
    }
    

    protected ToolBase createTool(String[] args, Generator generator) {
        return new Wsdl2Java(args, generator);
    }
 
       
  
}


