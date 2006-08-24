package org.apache.cxf.tools.common;
import org.apache.cxf.common.i18n.Message;
public class ToolException extends RuntimeException {

    
    private static final long serialVersionUID = -4418907917249006910L;
    public ToolException() {
        super();
    }
    public ToolException(Message msg) {
        super(msg.toString());
    }
    public ToolException(String msg) {
        super(msg);
    }

    public ToolException(Message msg, Throwable t) {
        super(msg.toString(), t);
    }
    
    public ToolException(String msg, Throwable t) {
        super(msg, t);
    }

    public ToolException(Throwable t) {
        super(t);
    }
    

}

