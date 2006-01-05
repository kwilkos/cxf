package org.objectweb.celtix.tools.common.generators;


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
        this(JAXWSCodeGenerator.class.getClassLoader());
    }
    public JAXWSCodeGenerator(ClassLoader loader) {
        this(DEFAULT_TOOL_NAME, loader);
    }
    
    public JAXWSCodeGenerator(String theToolClassName, ClassLoader loader) {
        super(theToolClassName, loader);
    }
}

