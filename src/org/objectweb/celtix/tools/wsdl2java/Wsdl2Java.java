package org.objectweb.celtix.tools.wsdl2java;

import org.objectweb.celtix.tools.common.Generator;
import org.objectweb.celtix.tools.common.ToolBase;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.wsdl2java.generators.JAXWSCodeGenerator;

public final class Wsdl2Java extends ToolBase {
    
    private Generator defaultGenerator = new JAXWSCodeGenerator();    

    Wsdl2Java(String[] args) {
        super(args);
    }

    /**
     * constructor for use by unit tests
     * 
     * @param args command line arguments
     * @param generator default generator to use
     */
    Wsdl2Java(String[] args, Generator generator) {
        this(args);
        defaultGenerator = generator;
    }

 

    /** call the code generators
     * 
     */
    @Override
    public void run() {
        
        // there is a single generator now but this will be expanded 
        // to includ, for example, as JAX-RPC generator
        //
        defaultGenerator.setConfiguration(getConfiguration());    
        defaultGenerator.generate();        
    }

    
    /** validate and parse command line arguments and then delegate to the appropriate 
     * generator(s)
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {

        try { 
            Wsdl2Java tool = new Wsdl2Java(args);
            tool.run();
        } catch (ToolException ex) {
            ToolBase.reportError(ex.getMessage());
        }
    }
}

