package org.objectweb.celtix.tools;


import org.objectweb.celtix.tools.common.DelegatingToolTestBase;
import org.objectweb.celtix.tools.common.Generator;
import org.objectweb.celtix.tools.common.ToolBase;


public class Wsdl2JavaTest extends DelegatingToolTestBase {
    
    
    protected ToolBase createTool(String[] args, Generator generator) {
        return new Wsdl2Java(args, generator);
    }
 
       
  
}

