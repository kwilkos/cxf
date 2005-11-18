package org.objectweb.celtix.tools.processors.wsdl2;

import org.objectweb.celtix.tools.common.Processor;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;

public class WSDLToProcessor implements Processor {

    public void run() {
        System.out.println("WSDLToProcessor Running...");
    }
    
    public void setEnvironment(ProcessorEnvironment env) {
        System.out.println("WSDLToProcessor setting...");
    }

}
