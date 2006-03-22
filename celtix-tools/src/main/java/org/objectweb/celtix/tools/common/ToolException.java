package org.objectweb.celtix.tools.common;
import org.objectweb.celtix.common.i18n.Message;
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
    

    public String toString() {
        StringBuffer sb = new StringBuffer(super.getMessage());

        return sb.toString();
    }

   

}

