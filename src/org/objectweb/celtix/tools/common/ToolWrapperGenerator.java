package org.objectweb.celtix.tools.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.celtix.configuration.Configuration;

/**
 * A generator which wraps external tools.  
 * 
 * @author codea
 *
 */
public class ToolWrapperGenerator implements Generator {

    protected final String toolClassName;
    private Configuration config;

    /**
     * construct a generator which delegates to the tool specified by 
     * the class <code>theToolClassName<code>
     * @param theToolClassName class name of the tool to delegate to
     */
    protected ToolWrapperGenerator(String theToolClassName) {
        super();
        this.toolClassName = theToolClassName;
    }

    /** invoked main method of tool class, passing in required arguments
     * 
     */
    public void generate() {
        try {
            Class<?> toolClass = getClass().getClassLoader().loadClass(toolClassName);
            Method mainMethod = toolClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object)getToolArguments());
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public String getToolClassName() { 
        return toolClassName;
    }
    
    
    private String[] getToolArguments() {
        // build up array of arguments based on the command line 
        // that have been given.  For now, just grab the arguments 
        // that were passed in on the command line
        if (config != null) {
            return ((ToolConfig)config).getOriginalArgs();
        } else {
            return new String[0];
        }
    }

    public void setConfiguration(Configuration newConfig) {
        this.config = newConfig;        
    }

}

