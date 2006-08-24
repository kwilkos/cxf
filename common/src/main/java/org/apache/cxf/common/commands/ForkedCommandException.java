package org.apache.cxf.common.commands;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.i18n.UncheckedException;

public class ForkedCommandException extends UncheckedException {

    private static final long serialVersionUID = 1L;
        
    public ForkedCommandException(Message msg) {
        super(msg);
    }
    public ForkedCommandException(Message msg, Throwable cause) {
        super(msg, cause);
    }
    public ForkedCommandException(Throwable cause) {
        super(cause);
    }
}
