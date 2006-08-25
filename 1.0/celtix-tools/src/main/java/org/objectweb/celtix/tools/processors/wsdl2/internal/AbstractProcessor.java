package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.logging.Logger;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.java2.JavaToWSDLProcessor;

public abstract class AbstractProcessor {
    protected static final Logger LOG = LogUtils.getL7dLogger(JavaToWSDLProcessor.class);
    protected ProcessorEnvironment env;
    
    protected ClassCollector  collector;
    
    public AbstractProcessor(ProcessorEnvironment penv) {
        this.env = penv;
        this.collector = (ClassCollector)env.get(ToolConstants.GENERATED_CLASS_COLLECTOR);
    }

}
