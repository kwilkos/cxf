package org.objectweb.celtix.tools.common;

public interface Processor {
    void process() throws Exception;
    void setEnvironment(ProcessorEnvironment env);
}
