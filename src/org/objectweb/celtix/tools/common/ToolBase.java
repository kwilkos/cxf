package org.objectweb.celtix.tools.common;

import org.objectweb.celtix.configuration.Configuration;

/**
 * Base class for Celtix tools
 * 
 * @author codea
 *
 */
public abstract class ToolBase {

    private final ToolConfig toolConfig; 
    
    protected ToolBase(String[] args) {
        toolConfig = new ToolConfig(args);
    }
    
    public Configuration getConfiguration() {    
        return toolConfig;
    }
    
    public abstract void run(); 

    public static void reportError(final String msg) {
        System.out.println(msg);
    }

}
