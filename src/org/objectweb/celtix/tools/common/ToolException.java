package org.objectweb.celtix.tools.common;

public class ToolException extends RuntimeException {

    public ToolException() {
        super();
    }

    public ToolException(String msg) {
        super(msg);
    }

    public ToolException(String msg, Throwable t) {
        super(msg, t);
    }

    public ToolException(Throwable t) {
        super(t);
    }

}

