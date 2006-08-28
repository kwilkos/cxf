/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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

