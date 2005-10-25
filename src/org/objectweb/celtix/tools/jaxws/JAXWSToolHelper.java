package org.objectweb.celtix.tools.jaxws;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.celtix.tools.Wsdl2Java;
import org.objectweb.celtix.tools.common.ToolBase;
import org.objectweb.celtix.tools.common.ToolException;


public final class JAXWSToolHelper {

    public static final String JAXWS_HOME_PROP = "jaxws.home";
    public static final String NO_JAXWS_HOME_MSG = "Please set the jaxws.home system property";

    private static final Logger LOG = Logger.getLogger(JAXWSToolHelper.class.getName());
    
    private static ClassLoader toolClassLoader; 
    

    static { 
        setSystemProperties();
    }

    

    private JAXWSToolHelper() {
        // complete 
    }
    
    public static void setSystemProperties() {
        
        // set properties to ensure that the RI's StAX implementation is used
        //
        System.setProperty("javax.xml.stream.XMLEventFactory", 
                           "com.sun.xml.stream.events.ZephyrEventFactory");
        System.setProperty("javax.xml.stream.XMLInputFactory",
                           "com.sun.xml.stream.ZephyrParserFactory");
        System.setProperty("javax.xml.stream.XMLOutputFactory",
                           "com.sun.xml.stream.ZephyrWriterFactory");
    }
    
    
    public static void executeTool(Class<? extends ToolBase> toolClass, String[] args) {
        
        ClassLoader origCtxLoader = Thread.currentThread().getContextClassLoader(); 
        ClassLoader jaxwsHomeClassLoader = null; 
        
        try {
            // create a classloader for the tool that can load all of the JAXWS libraries
            jaxwsHomeClassLoader = getToolClassLoader();
            Thread.currentThread().setContextClassLoader(jaxwsHomeClassLoader);

            // build the classpath for the tool
            setSystemClassPath();

            Constructor<? extends ToolBase> ctor = toolClass.getConstructor(String[].class);
            ToolBase tool = ctor.newInstance((Object)args);
            tool.run();            
        } catch (ToolException ex) {
            ToolBase.reportError(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally { 
            Thread.currentThread().setContextClassLoader(origCtxLoader);
        }
    }      
    
    
 

    public static synchronized ClassLoader getToolClassLoader() {
    
        if (toolClassLoader == null) {
            toolClassLoader = new URLClassLoader(getJAXWSClassPath(), 
                                                 Wsdl2Java.class.getClassLoader());
        }
        return toolClassLoader;
    }
    
    
    static String getURLsAsPath(URL[] classpathURLs) {

        if (classpathURLs == null) {
            return "";
        }
        
        
        StringBuilder builder = new StringBuilder(); 
        
        for (int i = 0; i < classpathURLs.length; i++) {
            builder.append(classpathURLs[i].getPath());
            if (i < classpathURLs.length - 1) {
                builder.append(File.pathSeparatorChar);
            }
        }        
        return builder.toString();
    }
    

    /** add the jars from JAXWS_HOME/lib to the classpath.  This allows the 
     *  underlying tool to compile generated code correctly. 
     */
    static void setSystemClassPath() throws MalformedURLException {
        setSystemClassPath(getJAXWSClassPath());
    }
    
    static void setSystemClassPath(URL[] path) throws MalformedURLException { 
        
        String cp = getURLsAsPath(path);
        String origCP = System.getProperty("java.class.path");
        if (origCP != null) {
            cp = cp + File.pathSeparator + origCP;
        }
        LOG.finest("setting system classpath: " + cp);
        System.setProperty("java.class.path", cp);
    }
    
    
    
    /**
     *  get the libraries from $JAXWS_HOME/lib 
     *  
     * @return classloader which can load from the JAXWS home lib directory
     * @throws TooException if jaxws.home system property is not set
     */
    static URL[] getJAXWSClassPath() { 
        
        String jaxwsHome = System.getProperty(JAXWS_HOME_PROP, "");
        
        if ("".equals(jaxwsHome)) { 
            throw new ToolException(NO_JAXWS_HOME_MSG);
        }
        
        File jaxwsLib = new File(jaxwsHome, "lib/");
        
        List<URL> jars = new ArrayList<URL>();
        if (jaxwsLib.exists() && jaxwsLib.isDirectory()) {
            File[] files = jaxwsLib.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(".jar")) {
                    try {
                        jars.add(files[i].toURL());
                    } catch (MalformedURLException ex) {
                        // this exception should not be throws, but just 
                        // in case:
                        ex.printStackTrace();
                    }
                }
            }
        }        
        return jars.toArray(new URL[jars.size()]);
    }
}
