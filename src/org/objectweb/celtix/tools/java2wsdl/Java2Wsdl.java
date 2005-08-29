package org.objectweb.celtix.tools.java2wsdl;

import org.objectweb.celtix.tools.common.Generator;
import org.objectweb.celtix.tools.common.ToolBase;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.wsdl2java.generators.JAXWSWsdlGenerator;


public final class Java2Wsdl extends ToolBase {
    

    private final Generator generator;

    Java2Wsdl(String[] args) {
        this(args, new JAXWSWsdlGenerator());
    }

    Java2Wsdl(String[] args, Generator gen) {
        super(args);
        generator = gen;
    }
    
    @Override
    public void run() {
        generator.setConfiguration(getConfiguration());
        generator.generate();
    }

    /** validate and parse command line arguments and then delegate to the appropriate 
     * generator(s)
     * 
     * @param args
     */
    public static void main(String[] args) {
        
        try { 
            Java2Wsdl tool = new Java2Wsdl(args, null);
            tool.run();
        } catch (ToolException ex) {
            ToolBase.reportError(ex.getMessage());
        }       
    }
}

