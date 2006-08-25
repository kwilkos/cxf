package org.objectweb.celtix.tools.common.generators;


import junit.framework.TestCase;

import org.objectweb.celtix.tools.common.ToolWrapperGenerator;

public class JAXWSGeneratorTest extends TestCase {

    public void testJAXWSWrapsTool() { 
        JAXWSCodeGenerator gen = new JAXWSCodeGenerator(); 
        assertEquals(JAXWSCodeGenerator.DEFAULT_TOOL_NAME, ((ToolWrapperGenerator)gen).getToolClassName());
    }
}

