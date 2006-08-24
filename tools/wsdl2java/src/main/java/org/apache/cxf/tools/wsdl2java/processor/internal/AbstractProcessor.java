package org.apache.cxf.tools.wsdl2java.processor.internal;

import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.util.ClassCollector;

public abstract class AbstractProcessor {
    protected static final Logger LOG = LogUtils.getL7dLogger(AbstractProcessor .class);
    protected ProcessorEnvironment env;
    
    protected ClassCollector  collector;
    
    public AbstractProcessor(ProcessorEnvironment penv) {
        this.env = penv;
        this.collector = (ClassCollector)env.get(ToolConstants.GENERATED_CLASS_COLLECTOR);
    }

}
