package org.objectweb.celtix.tools.common;

public interface Processor {
    void run();
    void setEnvironment(ProcessorEnvironment env);
}
