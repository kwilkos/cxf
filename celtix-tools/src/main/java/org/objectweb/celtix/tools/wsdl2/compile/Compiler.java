package org.objectweb.celtix.tools.wsdl2.compile;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import org.objectweb.celtix.tools.common.toolspec.ToolException;

public class Compiler {

    private OutputStream out;

    public Compiler(OutputStream o) {
        this.out = o;
    }

    public boolean internalCompile(String[] args) throws ToolException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     
        Class javacMainClass = null;
        Class[] compileMethodSignature;
        compileMethodSignature = new Class[2];
        compileMethodSignature[0] = (new String[0]).getClass();
        compileMethodSignature[1] = PrintWriter.class;
        try {
            javacMainClass = classLoader.loadClass("com.sun.tools.javac.Main");
            try {

                Method compileMethod = javacMainClass.getMethod("compile", compileMethodSignature);
                try {
                    Object result = compileMethod.invoke(null, new Object[] {args, new PrintWriter(out)});
                    if (!(result instanceof Integer)) {
                        return false;
                    }
                    return ((Integer)result).intValue() == 0;
                } catch (Exception e1) {
		    e1.printStackTrace();
		    throw new ToolException("Compile exception", e1);
                }
            } catch (NoSuchMethodException e2) {
              	    e2.printStackTrace();
		    throw new ToolException(e2.getMessage(), e2);
            }
        } catch (ClassNotFoundException e3) {
	    e3.printStackTrace();
            throw new ToolException(e3.getMessage(), e3);
            
        } catch (SecurityException e4) {
	    e4.printStackTrace();
            throw new ToolException(e4.getMessage() , e4);
        }
    }
}
