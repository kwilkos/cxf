package org.objectweb.celtix.tools;

import org.objectweb.celtix.tools.common.Generator;
import org.objectweb.celtix.tools.common.ToolBase;
import org.objectweb.celtix.tools.common.generators.JAXWSWsdlGenerator;
import org.objectweb.celtix.tools.jaxws.JAXWSToolHelper;


public final class Java2Wsdl extends ToolBase {
    
    public Java2Wsdl(String[] args) {
        this(args, new JAXWSWsdlGenerator(JAXWSToolHelper.getToolClassLoader()));
    }

    public Java2Wsdl(String[] args, Generator gen) {
        super(args, gen);
    }
    

    /** validate and parse command line arguments and then delegate to the appropriate 
     * generator(s)
     * 
     * @param args
     */
    public static void main(String[] args) {
     
        // create and invoke the tool
        JAXWSToolHelper.executeTool(Java2Wsdl.class, args);
    }
}


