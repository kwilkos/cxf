package org.objectweb.celtix.tools.wsdl2java.generators;


import org.objectweb.celtix.tools.common.ToolWrapperGenerator;

/**
 * generate JAX-WS 2.0 code from a wsdl.  Currently this class is 
 * a wrapper for WSImport in the JAX WS reference implementation
 * 
 * @author codea
 *
 */
public class JAXWSCodeGenerator extends ToolWrapperGenerator {

    static final String DEFAULT_TOOL_NAME = "com.sun.tools.ws.WsImport";
    
    public JAXWSCodeGenerator() {
        this(DEFAULT_TOOL_NAME);
    }
    
    public JAXWSCodeGenerator(String theToolClassName) {
        super(theToolClassName);
    }
}
