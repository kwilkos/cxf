package org.objectweb.celtix.tools.common.toolspec;

import java.io.*;

public class ToolException extends Exception {
    public static final long serialVersionUID = 1L;
    
    private final Throwable cause;

    public ToolException(String s) {
        super(s);
        cause = null;
    }

    public ToolException(String s, Throwable c) {
        super(s);
        this.cause = c;
    }

    public void printStackTrace() {
        super.printStackTrace();
        if (cause != null) {
            System.err.println("Caused by: ");
            cause.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream out) {
        super.printStackTrace(out);
        if (cause != null) {
            out.println("Caused by: ");
            cause.printStackTrace(out);
        }
    }

    public void printStackTrace(PrintWriter out) {
        super.printStackTrace(out);
        if (cause != null) {
            out.println("Caused by: ");
            cause.printStackTrace(out);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(super.getMessage());

        if (cause != null) {
            sb.append("\nCaused by:\n");
            sb.append(cause.toString());
        }
        return sb.toString();
    }

    public Throwable getCause() {
        return cause;
    }
}
