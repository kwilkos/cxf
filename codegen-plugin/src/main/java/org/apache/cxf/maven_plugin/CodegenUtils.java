package org.apache.cxf.maven_plugin;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.cxf.tools.wsdl2java.WSDLToJava;


public final class CodegenUtils {
    
    static long timestamp;
    
    private CodegenUtils() {
        //not consructed
    }
    
    public static long getCodegenTimestamp() {
        if (timestamp != 0) {
            return timestamp;
        }
        
        getClassTime(CodegenUtils.class);
        getClassTime(WSDLToJava.class);

        
        return timestamp;
    }

    private static void getClassTime(Class class1) {
        String str = "/" + class1.getName().replace('.', '/') + ".class";
        URL url = class1.getResource(str);
        if (url != null) {
            while ("jar".equals(url.getProtocol())) {
                str = url.getPath();
                if (str.lastIndexOf("!") != -1) {
                    str = str.substring(0, str.lastIndexOf("!"));
                }
                try {
                    url = new URL(str);
                } catch (MalformedURLException e) {
                    return;
                }
            }
            JarFile jar;
            try {
                jar = new JarFile(url.getPath());
                Enumeration entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = (JarEntry)entries.nextElement();
                    if (!entry.isDirectory()
                        && !entry.getName().startsWith("META")
                        && entry.getTime() > timestamp) {
                        
                        timestamp = entry.getTime();
                    }                    
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
