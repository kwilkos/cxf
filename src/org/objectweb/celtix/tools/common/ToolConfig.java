package org.objectweb.celtix.tools.common;


import org.objectweb.celtix.configuration.AbstractCommandLineConfiguration;


public class ToolConfig extends AbstractCommandLineConfiguration {

    private String[] originalArgs; 
    
    public ToolConfig() {       
    }
    
    public ToolConfig(String[] args) {
        parseCommandLine(args, true);
    }

    @Override
    protected void parseCommandLine(String[] args, boolean consume) {

        originalArgs = new String[args.length];
        System.arraycopy(args, 0, originalArgs, 0, args.length);
        super.parseCommandLine(args, consume);
    }

    public String[] getOriginalArgs() {
        return originalArgs;
    }

}

