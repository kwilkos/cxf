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
    private Generator defaultGenerator; 
    
    protected ToolBase(String[] args, Generator generator) {
        this(new ToolConfig(args), generator);
    }

    protected ToolBase(ToolConfig config, Generator generator) {
        toolConfig = config;
        defaultGenerator = generator;
    }
    
    
    protected ToolBase(String[] args) {
        toolConfig = new ToolConfig(args);
    }
    
    public Configuration getConfiguration() {    
        return toolConfig;
    }
    
    public void run() {
        
        if (defaultGenerator != null) {
            defaultGenerator.setConfiguration(toolConfig);
            defaultGenerator.generate();
        }
    }

    public static void reportError(final String msg) {
        System.out.println("error: " + msg);
    }
}

