package org.objectweb.celtix.configuration;

/**
 * Represents a command line option, similar to <code>ConfigurationItem</code>.
 * 
 * @author asmyth
 *
 */
public class CommandLineOption {
    
    private String name;
    private Object value;
        
    public CommandLineOption(String optionName) {
    }
    
    public String toString() {
        return name;        
    }
    
    
    public String getName() {
        return name;
    }
    
    public String getShortcut() {
        return null;
    }
    
    public Object getValue() {
        return null;
    }
    
    public boolean exists() {
        return false;
    }
    
    public void initialize(String v) {
        value = v;
    }
    
    public void initialize(String[]args) {
    }
}
