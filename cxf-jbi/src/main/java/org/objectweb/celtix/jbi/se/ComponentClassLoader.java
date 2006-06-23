package org.objectweb.celtix.jbi.se;

import java.net.URL;
import java.net.URLClassLoader;

public class ComponentClassLoader extends URLClassLoader { 
    
    public ComponentClassLoader(URL[] urls, ClassLoader parent) { 
        super(urls, parent);
    } 
    
    public void addResource(URL url) { 
        addURL(url);
    } 
} 