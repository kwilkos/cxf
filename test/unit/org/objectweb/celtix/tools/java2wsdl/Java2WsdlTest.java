package org.objectweb.celtix.tools.java2wsdl;

import org.objectweb.celtix.tools.common.DelegatingToolTestBase;
import org.objectweb.celtix.tools.common.Generator;
import org.objectweb.celtix.tools.common.ToolBase;

public class Java2WsdlTest extends DelegatingToolTestBase {
    
    @Override
    protected ToolBase createTool(String[] args, Generator theGenerator) {
        return new Java2Wsdl(args, theGenerator);
    }

}
