package org.objectweb.celtix.common.commands;

import java.io.File;

public final class JavaHelper {

    private JavaHelper() {
        //complete
    }

    /** Get the command to launch a JVM.  Find the java command
     * relative to the java.home property rather than what is on the
     * path.  It is possible that the java version being used it not
     * on the path
     *
     */
    public static String getJavaCommand() { 
        String javaHome = System.getProperty("java.home");
        if (null != javaHome) { 
            return javaHome + File.separator + "bin"  
                + File.separator  + "java" + ForkedCommand.EXE_SUFFIX; 
        } else { 
            return "java" + ForkedCommand.EXE_SUFFIX;
        } 
    } 
}
