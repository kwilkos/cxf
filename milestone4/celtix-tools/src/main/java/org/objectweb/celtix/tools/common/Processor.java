package org.objectweb.celtix.tools.common;

import org.objectweb.celtix.tools.common.toolspec.ToolException;

public interface Processor {
    void process() throws ToolException;
    void setEnvironment(ProcessorEnvironment env);
}
