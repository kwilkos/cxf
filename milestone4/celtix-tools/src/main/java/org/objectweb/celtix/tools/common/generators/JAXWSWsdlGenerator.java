package org.objectweb.celtix.tools.common.generators;

import org.objectweb.celtix.tools.common.ToolWrapperGenerator;

/** generates a wsdl definition from an annotated java class
 * Currently this simply delegate to the JAX-WS RI WsGen tool
 * 
 * @author codea
 *
 */
public class JAXWSWsdlGenerator extends ToolWrapperGenerator {

    static final String DEFAULT_TOOL_NAME = "com.sun.tools.ws.WsGen";
    
    public JAXWSWsdlGenerator(ClassLoader loader) {
        this(DEFAULT_TOOL_NAME, loader);
    }
    
    
    public JAXWSWsdlGenerator(String theToolClassName, ClassLoader loader) {
        super(theToolClassName, loader);
    }

}

