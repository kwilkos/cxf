package org.objectweb.celtix.tools.wsdl2java.generators;

import org.objectweb.celtix.tools.common.ToolWrapperGenerator;

/** generates a wsdl definition from an annotated java class
 * Currently this simply delegate to the JAX-WS RI WsGen tool
 * 
 * @author codea
 *
 */
public class JAXWSWsdlGenerator extends ToolWrapperGenerator {

    static final String DEFAULT_TOOL_NAME = "com.sun.tools.ws.WsImport";
    
    public JAXWSWsdlGenerator() {
        this(DEFAULT_TOOL_NAME);
    }
    
    
    public JAXWSWsdlGenerator(String theToolClassName) {
        super(theToolClassName);
    }

}

