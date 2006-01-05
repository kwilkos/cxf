package org.objectweb.celtix.tools.common;

public class ToolException extends RuntimeException {

    
    private static final long serialVersionUID = -4418907917249006910L;

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

