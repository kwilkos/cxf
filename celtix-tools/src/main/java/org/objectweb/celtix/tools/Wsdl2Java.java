package org.objectweb.celtix.tools;


import org.objectweb.celtix.tools.common.Generator;
import org.objectweb.celtix.tools.common.ToolBase;
import org.objectweb.celtix.tools.common.generators.JAXWSCodeGenerator;
import org.objectweb.celtix.tools.jaxws.JAXWSToolHelper;

public final class Wsdl2Java extends ToolBase {
    

    public Wsdl2Java(String[] args) {
        this(args, new JAXWSCodeGenerator(JAXWSToolHelper.getToolClassLoader()));
    }
    
    /**
     * constructor for use by unit tests
     * 
     * @param args command line arguments
     * @param generator default generator to use
     */
    public Wsdl2Java(String[] args, Generator generator) {
        super(args, generator);
    }
    
    
    /** validate and parse command line arguments and then delegate to the appropriate 
     * generator(s)
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
                
        // create and invoke the tool
        JAXWSToolHelper.executeTool(Wsdl2Java.class, args);
    }
}
