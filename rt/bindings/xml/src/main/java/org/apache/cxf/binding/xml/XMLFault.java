package org.apache.cxf.binding.xml;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptor.Fault;

public class XMLFault extends Fault {
    
    static final long serialVersionUID = 100000;
    
    public XMLFault(Message message, Throwable throwable) {
        super(message, throwable);
    }
    
    public XMLFault(Message message) {
        super(message);
    }

    public XMLFault(Throwable t) {
        super(t);
    }

    @SuppressWarnings("deprecation")
    protected XMLFault(String message) {
        super(message);
    }


}
