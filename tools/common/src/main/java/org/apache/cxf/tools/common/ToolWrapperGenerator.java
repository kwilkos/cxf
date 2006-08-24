package org.apache.cxf.tools.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.cxf.configuration.CommandlineConfiguration;

/**
 * A generator which wraps external tools.  
 * 
 * @author codea
 *
 */
public class ToolWrapperGenerator implements Generator {

    protected final String toolClassName;
    private final ClassLoader classLoader; 
    private CommandlineConfiguration config;

    /**
     * construct a generator which delegates to the tool specified by 
     * the class <code>theToolClassName<code>
     *
     * @param theToolClassName class name of the tool to delegate to
     */
    protected ToolWrapperGenerator(String theToolClassName) {
        this(theToolClassName, ToolWrapperGenerator.class.getClassLoader());
    }

    /**
     * construct a generator which delegates to the tool specified by 
     * the class <code>theToolClassName<code> and the tool class is
     * loaded via the specified classloader
     *
     * @param theToolClassName class name of the tool to delegate to
     * @param theClassLoader the classloader to load the tool class 
     */
    protected ToolWrapperGenerator(String theToolClassName, ClassLoader theClassLoader) {
        classLoader = theClassLoader;
        toolClassName = theToolClassName;
    }
    
    /** invoked main method of tool class, passing in required arguments
     * 
     */
    public void generate() {
        try {
            Class<?> toolClass = Class.forName(toolClassName, true, classLoader);
            Method mainMethod = toolClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object)getToolArguments());
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() != null) { 
                ex.getTargetException().printStackTrace();
            }
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

    public void setConfiguration(CommandlineConfiguration newConfig) {
        this.config = newConfig;        
    }

}

