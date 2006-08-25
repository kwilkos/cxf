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
        name = optionName;
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
        return value;
    }
    
    public boolean exists() {
        return false;
    }
    
    public void initialize(String v) {
        value = v;
    }
    
    public void initialize(String[]args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].compareTo(name) == 0 && i < args.length - 1) {
                    value = args[i + 1];
                    break;
                }
            }
        }
    }
}
