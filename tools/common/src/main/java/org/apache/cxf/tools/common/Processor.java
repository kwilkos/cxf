package org.apache.cxf.tools.common;


public interface Processor {
    void process() throws ToolException;
    void setEnvironment(ProcessorEnvironment env);
}
