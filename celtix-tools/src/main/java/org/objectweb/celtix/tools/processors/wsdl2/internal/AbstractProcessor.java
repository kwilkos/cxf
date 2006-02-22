package org.objectweb.celtix.tools.processors.wsdl2.internal;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;

public abstract class AbstractProcessor {

    protected ProcessorEnvironment env;
    
    protected ClassCollector  collector;
    
    public AbstractProcessor(ProcessorEnvironment penv) {
        this.env = penv;
        this.collector = (ClassCollector)env.get(ToolConstants.GENERATED_CLASS_COLLECTOR);
    }

}
